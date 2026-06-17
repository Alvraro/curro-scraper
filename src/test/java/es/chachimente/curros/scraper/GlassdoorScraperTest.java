package es.chachimente.curros.scraper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import es.chachimente.curroscraper.application.curroimporter.Application;
import es.chachimente.curroscraper.model.CompanyInfo;
import es.chachimente.curroscraper.model.GlassdoorCompanyInfo;
import es.chachimente.curroscraper.scraper.GlassdoorScraper;
import es.chachimente.curroscraper.scraper.Scraper;

@SpringBootTest(classes = Application.class, properties = "spring.batch.job.enabled=false")
class GlassdoorScraperTest {

	@Test
	void testFullData() {
		testCompany("knowmad mood", "https://www.glassdoor.es/Resumen/Trabajar-en-knowmad-mood-EI_IE297765.12,24.htm", 4.0f, 3.9f, 3.9f, LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 10), LocalDate.of(2026, 5, 28), 366, 251, 141);
	}

	@Test
	void testFullDataNoReviewScrolling() {
		testCompany("Civica", "https://www.glassdoor.es/Resumen/Trabajar-en-Civica-EI_IE35357.12,18.htm", 3.0f, 4.2f, null, LocalDate.of(2026, 1, 30), LocalDate.of(2026, 1, 30), null, 5, 4, 0);
	}
	
	@Test
	void testFullDataMoreThan1kReviews() {
		testCompany("Amazon", "https://www.glassdoor.es/Resumen/Trabajar-en-Amazon-EI_IE6036.12,18.htm", 3.5f, 3.6f, 3.8f, LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 7), 4065, 1492, 521);
	}
	
	@Test
	void testFullDataMoreThan10kReviews() {		
		testCompany("Accenture", "https://www.glassdoor.es/Resumen/Trabajar-en-Accenture-EI_IE4138.12,21.htm", 3.7f, 3.7f, 3.7f, LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 15), 11329, 1889, 1106);
	}
	
	@Test
	void testCompanyNotFound() {
		testCompany(Scraper.COMPANY_NOT_FOUND, Scraper.COMPANY_NOT_FOUND, null, null, null, null, null, null, null, null, null);
	}
	
	// Common test function
	private void testCompany(String companyName, String glassdoorURL, Float globalScore, Float nationalScore,
			Float localScore, LocalDate lastGlobalUpdate, LocalDate lastNationalUpdate, LocalDate lastLocalUpdate,
			Integer globalNumberOfReviews, Integer nationalNumberOfReviews, Integer localNumberOfReviews) {
		CompanyInfo companyInfo = new CompanyInfo(companyName, null, null);
		GlassdoorScraper scraper = new GlassdoorScraper();
		
		CompanyInfo result = scraper.process(companyInfo);
		assertEquals(result.name(), companyName);
		
		GlassdoorCompanyInfo glassdoorInfo = result.glassdoorInfo();
		assertEquals(companyName, glassdoorInfo.company());
		assertEquals(glassdoorURL, glassdoorInfo.URL());
		assertEquals(globalScore, glassdoorInfo.globalScore());
		assertEquals(lastGlobalUpdate, glassdoorInfo.lastGlobalUpdate());
		assertEquals(globalNumberOfReviews, glassdoorInfo.globalNumberOfReviews());
		assertEquals(nationalScore, glassdoorInfo.nationalScore());
		assertEquals(lastNationalUpdate, glassdoorInfo.lastNationalUpdate());
		assertEquals(nationalNumberOfReviews, glassdoorInfo.nationalNumberOfReviews());
		assertEquals(localScore, glassdoorInfo.localScore());
		assertEquals(lastLocalUpdate, glassdoorInfo.lastLocalUpdate());
		assertEquals(localNumberOfReviews, glassdoorInfo.localNumberOfReviews());
	}
}
