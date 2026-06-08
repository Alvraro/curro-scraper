package es.chachimente.curroscraper.scraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import es.chachimente.curroscraper.model.CurroInfo;
import es.chachimente.curroscraper.model.CurroURL;

public class LinkedInCurroScraper extends Scraper implements ItemProcessor<CurroURL, CurroInfo> {

	private static final Logger log = LoggerFactory.getLogger(LinkedInCurroScraper.class);
	
	@Override
	public CurroInfo process(CurroURL linkedInURL) throws Exception {
		log.info("Processing URL: " + linkedInURL);
		
		Connection connection = Jsoup.connect(linkedInURL.URL())
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36");		
		Document document = connection.get();

		String jobTitle, companyName, description, location;
		
		try {
			jobTitle = document.getElementsByClass("top-card-layout__title font-sans text-lg papabear:text-xl font-bold leading-open text-color-text mb-0 topcard__title").first().text();			
		} catch (Exception e) {
			log.error("Could not extract job title for '" + linkedInURL + "': " + e);
			jobTitle = EXTRACTION_ERROR;
		}
		
		try {
			companyName = document.getElementsByClass("topcard__org-name-link topcard__flavor--black-link").first().text();
		} catch (Exception e) {
			log.error("Could not extract company name for '" + linkedInURL + "': " + e);
			companyName = EXTRACTION_ERROR;
		}
		
		try {
			description = document.getElementsByClass("show-more-less-html__markup relative overflow-hidden").first().text();
		} catch (Exception e) {
			log.error("Could not extract job description for '" + linkedInURL + "': " + e);
			description = EXTRACTION_ERROR;
		}
		
		try {
			location = document.getElementsByClass("topcard__flavor topcard__flavor--bullet").first().text();
		}
		catch (Exception e) {
			log.error("Could not extract job location for '" + linkedInURL + "': " + e);
			location = EXTRACTION_ERROR;
		}
		
		return new CurroInfo(jobTitle, companyName, location, description, linkedInURL.URL());
	}

}
