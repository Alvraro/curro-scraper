package es.chachimente.curroscraper.scraper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	
	@Override
	public CompanyInfo process(CompanyInfo companyInfo) {
		try {
			MalditasConsultorasCompanyInfo malditasConsultorasInfo = scrapeCompanyInfo(companyInfo.name());
			companyInfo = new CompanyInfo(companyInfo.name(), malditasConsultorasInfo, companyInfo.glassdoorInfo());
			
		} catch (ParseException | IOException e) {
			log.error("Error scraping company info for " + companyInfo.name(), e);
		}
		return companyInfo;
	}
	
	private MalditasConsultorasCompanyInfo scrapeCompanyInfo(String companyName) throws ParseException, IOException {
		log.info("Processing curro: " + companyName);

		// Fields to extract
		String companyURL, fullName, shortName, linkedInURL;
		Date lastUpdate;
		Float rotacionHistorica;
		
		// Search for the company name
		String searchURL = String.format("%s/?s=%s", MALDITAS_CONSULTORAS_BASE_URL, companyName);
		
		Connection connection = Jsoup.connect(searchURL)
				.userAgent(USER_AGENT);		
		Document document = connection.get();

		// Open the first search result (if any)
		try {
			companyURL = document.getElementsByClass("entry-title").first().child(0).absUrl("href");
		} catch (Exception e) {
			log.info("Could not find search result for " + companyName);
			return new MalditasConsultorasCompanyInfo(COMPANY_NOT_FOUND, COMPANY_NOT_FOUND, COMPANY_NOT_FOUND, COMPANY_NOT_FOUND, (Float)null, (Date)null);
		}
		
		connection.url(companyURL);
		document = connection.get();
		
		try {
			fullName = document.selectXpath("//*[@id=\"main\"]//div[2]/div[1]/h3[1]/a").first().text();
		} catch (Exception e) {
			log.error("Could not extract full name for " + companyName + ": " + e.getMessage());
			fullName = EXTRACTION_ERROR;
		}
		try {
			shortName = document.selectXpath("//*[@id=\"main\"]//div[2]/div[1]/h3[2]/a[1]").first().text();
		} catch (Exception e) {
			log.error("Could not extract short name for " + companyName + ": " + e.getMessage());
			shortName = EXTRACTION_ERROR;
		}
		try {
			linkedInURL = document.selectXpath("//*[@id=\"main\"]//div[2]/div[1]/h3[2]/a[2]").first().attr("href");
		} catch (Exception e) {
			log.error("Could not extract LinkedIn URL for " + companyName + ": " + e.getMessage());
			linkedInURL = EXTRACTION_ERROR;
		}
		
		// Extract date (e.g. 06/01/2025)
		try {
			String lastUpdateStr = document.selectXpath("//*[@id=\"main\"]//div[2]/div[1]/p[4]/em/span").first().text();
			lastUpdateStr = Pattern.compile(".*\\d{2}/\\d{2}/\\d{4}").matcher(lastUpdateStr).toMatchResult().group();
			lastUpdate = new SimpleDateFormat("dd/MM/yyyy").parse(lastUpdateStr);
		} catch (Exception e) {
			log.error("Could not extract last update date for " + companyName + ": " + e.getMessage());
			lastUpdate = null;
		}

		try {
			rotacionHistorica = Float.parseFloat(document.selectXpath("//*[@id=\"post-2091\"]/div[2]/div[1]/div[1]/table/tbody/tr[7]/td[2]/span/strong").first().text());
		} catch (Exception e) {
			log.error("Could not extract historical rotation for " + companyName + ": " + e.getMessage());
			rotacionHistorica = null;
		}
		
		return new MalditasConsultorasCompanyInfo(fullName, shortName, linkedInURL, companyURL, rotacionHistorica, lastUpdate);
	}
}
