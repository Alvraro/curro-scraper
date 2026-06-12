package es.chachimente.curroscraper.scraper;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import es.chachimente.curroscraper.model.CompanyInfo;
import es.chachimente.curroscraper.model.MalditasConsultorasCompanyInfo;

public class MalditasConsultorasScraper extends Scraper implements ItemProcessor<CompanyInfo, CompanyInfo>{
	private static final Logger log = LoggerFactory.getLogger(MalditasConsultorasScraper.class);
	private static final String MALDITAS_CONSULTORAS_BASE_URL = "https://malditasconsultoras.com";
	private DecimalFormat rotacionHistoricaFormat;
	private int TIMEOUT = 40 * 1000; // e.g. Teknei 22s // TODO profile! maybe reduce timeout and add retries?
	
	public MalditasConsultorasScraper() {
		rotacionHistoricaFormat = new DecimalFormat();
		rotacionHistoricaFormat.getDecimalFormatSymbols().setDecimalSeparator(',');
	}
	
	@Override
	public CompanyInfo process(CompanyInfo companyInfo) {
		try {
			log.info(String.format("Processing company '%s'...", companyInfo.name()));
			MalditasConsultorasCompanyInfo malditasConsultorasInfo = scrapeCompanyInfo(companyInfo.name());
			log.info(String.format("Done company '%s'!", companyInfo.name()));
			companyInfo = new CompanyInfo(companyInfo.name(), malditasConsultorasInfo, companyInfo.glassdoorInfo());
		} catch (ParseException | IOException e) {
			log.error("Error scraping company info for " + companyInfo.name(), e);
		}
		return companyInfo;
	}
	
	private MalditasConsultorasCompanyInfo scrapeCompanyInfo(String companyName) throws ParseException, IOException {
		// Fields to extract
		String companyURL, fullName, shortName, linkedInURL;
		LocalDate lastUpdate;
		Float rotacionHistorica;
		
		// Search for the company name
		String searchURL = String.format("%s/?s=Opiniones de %s", MALDITAS_CONSULTORAS_BASE_URL, companyName);

		Connection connection = Jsoup.connect(searchURL).userAgent(USER_AGENT).timeout(TIMEOUT);
		Document document = connection.get();

		// Open the first search result (if any)
		try {
			companyURL = document.getElementsByClass("entry-title").first().child(0).absUrl("href");
		} catch (Exception e) {
			log.warn(String.format("Company '%s' not found on MalditasConsultoras", companyName));
			return new MalditasConsultorasCompanyInfo(COMPANY_NOT_FOUND, COMPANY_NOT_FOUND, COMPANY_NOT_FOUND, COMPANY_NOT_FOUND, null, null);
		}

		document = connection.url(companyURL).get();
		
		// TODO make xpaths local to the main stats table
		//Element statsTable = document.selectXpath("//*[@class=\"wprt-container\"]/descendant::table[1]").first();

		try {
			fullName = document.selectXpath("//*[@id=\"main\"]//div[2]/div[1]/h3[1]/a").first().text();
		} catch (Exception e) {
			log.error("Could not extract full name for " + companyName + ": " + e);
			fullName = EXTRACTION_ERROR;
		}
		try {
			shortName = document.selectXpath("//*[@id=\"main\"]//div[2]/div[1]/h3[2]/a[1]").first().text();
		} catch (Exception e) {
			log.error("Could not extract short name for " + companyName + ": " + e);
			shortName = EXTRACTION_ERROR;
		}
		try {
			linkedInURL = document.selectXpath("//*[@id=\"main\"]//div[2]/div[1]/h3[2]/a[2]").first().attr("href");
			linkedInURL = linkedInURL.replaceAll("//about/", "");
		} catch (Exception e) {
			log.error("Could not extract LinkedIn URL for " + companyName + ": " + e);
			linkedInURL = EXTRACTION_ERROR;
		}
		
		// Extract date (e.g. 06/01/2025)
		try {
			String lastUpdateStr = document.selectXpath("//*[@class=\"wprt-container\"]/descendant::em[1]").first().text();
			Matcher matcher = Pattern.compile("Se ha aplicado un filtro de España. Ultima actualización en (\\d{2}/\\d{2}/\\d{4})").matcher(lastUpdateStr);
			matcher.matches();
			lastUpdateStr = matcher.group(1);
			lastUpdate = LocalDate.parse(lastUpdateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));			
		} catch (Exception e) {
			log.error("Could not extract last update date for " + companyName + ": " + e);
			lastUpdate = null;
		}

		try {
			String rotacionHistoricaStr = document.selectXpath("//*[@class=\"in-cell-link\"]/../../../following::tr[1]/td[2]").first().text();
			rotacionHistorica = rotacionHistoricaFormat.parse(rotacionHistoricaStr).floatValue();
		} catch (Exception e) {
			log.error("Could not extract historical rotation for " + companyName + ": " + e);
			rotacionHistorica = null;
		}
		
		return new MalditasConsultorasCompanyInfo(fullName, shortName, linkedInURL, companyURL, rotacionHistorica, lastUpdate);
	}
}
