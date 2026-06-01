package es.chachimente.curroscraper.scraper;

import java.io.IOException;
import java.text.ParseException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import es.chachimente.curroscraper.model.CompanyInfo;
import es.chachimente.curroscraper.model.GlassdoorCompanyInfo;

public class GlassdoorScraper extends Scraper implements ItemProcessor<CompanyInfo, CompanyInfo>{
	private static final Logger log = LoggerFactory.getLogger(GlassdoorScraper.class);
	private static final String GLASSDOOR_BASE_URL = "https://www.glassdoor.es";
	
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
		log.info("Processing curro: " + companyName);
		
		// Search for the company name
		String searchURL = String.format("%s/Search/results.htm?keyword=%s", GLASSDOOR_BASE_URL, companyName);
		Connection connection = Jsoup.connect(searchURL)
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36");		
		Document document = connection.get();

		// TODO

		return new GlassdoorCompanyInfo(companyName, (String)null, (Float)null, (Float)null, null);
	}
}
