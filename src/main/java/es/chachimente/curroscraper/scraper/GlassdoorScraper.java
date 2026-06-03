package es.chachimente.curroscraper.scraper;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import es.chachimente.curroscraper.model.CompanyInfo;
import es.chachimente.curroscraper.model.GlassdoorCompanyInfo;

public class GlassdoorScraper extends Scraper implements ItemProcessor<CompanyInfo, CompanyInfo>{
	private static final Logger log = LoggerFactory.getLogger(GlassdoorScraper.class);
	private static final String GLASSDOOR_BASE_URL = "https://www.glassdoor.es";
	private static final String COUNTRY = "España";
	private static final String CITY = "Madrid";
	
	private DecimalFormat scoreFormat;
	
	public GlassdoorScraper() {
		scoreFormat = new DecimalFormat();
		scoreFormat.getDecimalFormatSymbols().setDecimalSeparator(',');
	}
	
	@Override
	public CompanyInfo process(CompanyInfo companyInfo) {
		try {
			GlassdoorCompanyInfo glassdoorInfo = scrapeCompanyInfo(companyInfo.name());
			companyInfo = new CompanyInfo(companyInfo.name(), companyInfo.malditasConsultorasInfo(), glassdoorInfo);
			
		} catch (ParseException | IOException e) {
			log.error("Error scraping company info for " + companyInfo.name(), e);
		}
		return companyInfo;
	}
	
	private GlassdoorCompanyInfo scrapeCompanyInfo(String companyName) throws ParseException, IOException {
		log.info(String.format("Processing curro: '%s'", companyName));
		
		// Fields to extract
		String company, companyURL; 
		Float globalScore, nationalScore, localScore; 
		LocalDate lastGlobalUpdate, lastNationalUpdate, lastLocalUpdate;
		
		// Search for the company name
		String searchURL = String.format("%s/Search/results.htm?keyword=%s", GLASSDOOR_BASE_URL, companyName);
		Connection connection = Jsoup.connect(searchURL).userAgent(USER_AGENT);		
		Document document = connection.get();

		try {
			companyURL = document.selectXpath("//h2[text()=\"Empresas\"]/../descendant::a").first().absUrl("href");			
		}
		catch (Exception e) {
			log.warn(String.format("Company '%s' not found on Glassdoor", companyName));
			return new GlassdoorCompanyInfo(Scraper.COMPANY_NOT_FOUND, Scraper.COMPANY_NOT_FOUND, null, null, null, null, null, null);
		}
		
		// Open the first search result (if any)
		document = connection.url(companyURL).userAgent(USER_AGENT).get();
		
		company = document.selectXpath("//h1[contains(@class, 'heading')]").first().text();
		
		// TODO check company matches
		
		// Go to "Reviews"
		//https://www.glassdoor.es/Opiniones/Electronic-Arts-Opiniones-E1628.htm
		String reviewsURL = document.selectXpath("//div[@id='reviews']/descendant::a").first().absUrl("href");
		document = connection.url(reviewsURL).userAgent(USER_AGENT).get();

		// Extract global score and last update date
		globalScore = extractScore(document);
		lastGlobalUpdate = extractLastUpdate(document);
		
		// Extract national and local scores and last update dates
		//https://www.glassdoor.es/Opiniones/Electronic-Arts-Opiniones-E1628.htm?filter.location=Espa%C3%B1a&filter.locationId=219&filter.locationType=N
		document = connection.url(reviewsURL + "?filter.location=" + COUNTRY + "&filter.locationId=219&filter.locationType=N").userAgent(USER_AGENT).get();
		nationalScore = extractScore(document);
		lastNationalUpdate = extractLastUpdate(document);
		
		//https://www.glassdoor.es/Opiniones/Electronic-Arts-Opiniones-E1628.htm?filter.location=Madrid&filter.locationId=10887&filter.locationType=S
		document = connection.url(reviewsURL + "?filter.location=" + CITY + "&filter.locationId=10887&filter.locationType=S").userAgent(USER_AGENT).get();		
		localScore = extractScore(document);
		lastLocalUpdate = extractLastUpdate(document);

		return new GlassdoorCompanyInfo(company, companyURL, globalScore, nationalScore, localScore, lastGlobalUpdate, lastNationalUpdate, lastLocalUpdate);
	}

	private Float extractScore(Element document) throws ParseException {
		String globalScoreStr = document.selectXpath("//p[contains(@class, 'RatingHeadline_rating')]").first().text();
		return scoreFormat.parse(globalScoreStr).floatValue();
	}
	
	private LocalDate extractLastUpdate(Document document) {		
		String lastUpdateStr = document.selectXpath("//span[contains(@class, 'Timestamp_reviewDate')]").first().text();
		// e.g. 29 may 2026
		return LocalDate.parse(lastUpdateStr, DateTimeFormatter.ofPattern("d MMM yyyy"));			
	}
}
