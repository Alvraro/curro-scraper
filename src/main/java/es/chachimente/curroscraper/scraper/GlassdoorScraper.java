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
			log.info(String.format("Processing company '%s'...", companyInfo.name()));
			GlassdoorCompanyInfo glassdoorInfo = scrapeCompanyInfo(companyInfo.name());
			log.info(String.format("Done company '%s'!", companyInfo.name()));
			companyInfo = new CompanyInfo(companyInfo.name(), companyInfo.malditasConsultorasInfo(), glassdoorInfo);
			
		} catch (ParseException | IOException e) {
			log.error("Error scraping company info for " + companyInfo.name(), e);
		}
		return companyInfo;
	}
	
	private GlassdoorCompanyInfo scrapeCompanyInfo(String companyName) throws ParseException, IOException {	
		// Fields to extract
		String company = COMPANY_NOT_FOUND, companyURL = COMPANY_NOT_FOUND; 
		Float globalScore = null, nationalScore = null, localScore = null;
		LocalDate lastGlobalUpdate = null, lastNationalUpdate = null, lastLocalUpdate = null;
		Integer globalNumberOfReviews = null, nationalNumberOfReviews = null, localNumberOfReviews = null;
		
		// Search for the company name
		String searchURL = String.format("%s/Search/results.htm?keyword=%s", GLASSDOOR_BASE_URL, companyName);
		Connection connection = Jsoup.connect(searchURL).userAgent(USER_AGENT);		
		Document document = connection.get();

		try {
			companyURL = document.selectXpath("//h2[text()=\"Empresas\"]/../descendant::a").first().absUrl("href");			
		}
		catch (Exception e) {
			log.warn(String.format("Company '%s' not found on Glassdoor", companyName));
			return new GlassdoorCompanyInfo(COMPANY_NOT_FOUND, COMPANY_NOT_FOUND, null, null, null, null, null, null, null, null, null);
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
		try {
			globalScore = extractScore(document);			
		}
		catch (Exception e) {
			log.warn(String.format("Global score not found for company '%s'", companyName));
		}
		try {
			lastGlobalUpdate = extractLastUpdate(document);
		}
		catch (Exception e) {
			log.warn(String.format("Global last update not found for company '%s'", companyName));
		}
		try {
			globalNumberOfReviews = extractNumberOfReviews(document);
		} catch (Exception e) {
			log.warn(String.format("Global number of reviews not found for company '%s'", companyName));
		}
		
		// Extract national and local scores and last update dates
		//https://www.glassdoor.es/Opiniones/Electronic-Arts-Opiniones-E1628.htm?filter.location=Espa%C3%B1a&filter.locationId=219&filter.locationType=N
		//https://www.glassdoor.es/Opiniones/Amazon-Opiniones-E6036.htm?filter.location=Espa%C3%B1a&filter.locationId=219&filter.locationType=N
		document = connection.url(reviewsURL + "?filter.location=" + COUNTRY + "&filter.locationId=219&filter.locationType=N").userAgent(USER_AGENT).get();
		try {
			nationalScore = extractScore(document);			
		}
		catch (Exception e) {
			log.warn(String.format("National score not found for company '%s'", companyName));
		}
		try {
			lastNationalUpdate = extractLastUpdate(document);
		}
		catch (Exception e) {
			log.warn(String.format("National last update not found for company '%s'", companyName));
		}
		try {
			nationalNumberOfReviews = extractNumberOfReviews(document);
		} catch (Exception e) {
			log.warn(String.format("National number of reviews not found for company '%s'", companyName));
		}
		
		// TODO if local city id changes overtime -> extract codes!
		//https://www.glassdoor.es/autocomplete/location?locationTypeFilters=CITY,STATE,COUNTRY&maxLocationsToReturn=10&term=Madrid
		
		//https://www.glassdoor.es/Opiniones/Electronic-Arts-Opiniones-E1628.htm?filter.location=Madrid&filter.locationId=10887&filter.locationType=S
		//(2026-06-17) https://www.glassdoor.es/Opiniones/Electronic-Arts-Opiniones-E1628.htm?filter.location=Madrid&filter.locationId=2664239&filter.locationType=C
		//https://www.glassdoor.es/Opiniones/Amazon-Opiniones-E6036.htm?filter.location=Madrid&filter.locationId=2664239&filter.locationType=C
		document = connection.url(reviewsURL + "?filter.location=" + CITY + "&filter.locationId=2664239&filter.locationType=C").userAgent(USER_AGENT).get();
		try {
			localScore = extractScore(document);			
		}
		catch (Exception e) {
			log.warn(String.format("Local score not found for company '%s'", companyName));
		}
		try {
			lastLocalUpdate = extractLastUpdate(document);
		}
		catch (Exception e) {
			log.warn(String.format("Local last update not found for company '%s'", companyName));
		}
		try {
			localNumberOfReviews = extractNumberOfReviews(document);
		} catch (Exception e) {
			log.warn(String.format("Local number of reviews not found for company '%s'", companyName));
		}

		return new GlassdoorCompanyInfo(company, companyURL, globalScore, nationalScore, localScore, lastGlobalUpdate, lastNationalUpdate, lastLocalUpdate, globalNumberOfReviews, nationalNumberOfReviews, localNumberOfReviews);
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

	private Integer extractNumberOfReviews(Document document) {
		// (> 1 page) Extract number from pagination footer string (e.g. "Viendo 1-5 de 4065 opiniones", "Viendo 1-5 de 18 opiniones", "Viendo 1-5 de 11.329 opiniones")
		Element reviewsElem = document.selectXpath("//div[contains(@class, 'PaginationContainer_paginationFooter')]").first();
		if(reviewsElem != null) {
			String reviewsStr = reviewsElem.text().trim();
			Pattern pattern = Pattern.compile("Viendo 1-\\d de (\\d+(\\.\\d+)?) opiniones");
			Matcher matcher = pattern.matcher(reviewsStr);
			if(matcher.matches()) {
				return Integer.parseInt(matcher.group(1).replace(".", ""));
			}
		}
		
		// (1 page) Extract number from header string (e.g. "5 opiniones")
		String reviewsStr = document.selectXpath("//div[contains(@class, 'SortBar_SearchCount')]").first().text();
		return Integer.parseInt(reviewsStr.replaceAll("opiniones", "").trim());
	}
}
