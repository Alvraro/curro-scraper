package es.chachimente.curros.scraper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import es.chachimente.curroscraper.application.CurryScraperApplication;
import es.chachimente.curroscraper.model.CompanyInfo;
import es.chachimente.curroscraper.model.GlassdoorCompanyInfo;
import es.chachimente.curroscraper.scraper.GlassdoorScraper;
import es.chachimente.curroscraper.scraper.Scraper;

@SpringBootTest(classes = CurryScraperApplication.class, properties = "spring.batch.job.enabled=false")
class GlassdoorScraperTest {

	@Test
	void testFullData() {
		final String companyName = "knowmad mood";
		final String glassdoorURL = "https://www.glassdoor.es/Resumen/Trabajar-en-knowmad-mood-EI_IE297765.12,24.htm";
		final Float globalScore = 4.0f;
		final Float nationalScore = 3.9f;
		final Float localScore = 3.9f;
		final LocalDate lastGlobalUpdate = LocalDate.of(2026, 5, 29);
		final LocalDate lastNationalUpdate = LocalDate.of(2026, 5, 29);
		final LocalDate lastLocalUpdate = LocalDate.of(2026, 5, 28);
		
		CompanyInfo companyInfo = new CompanyInfo(companyName, null, null);
		GlassdoorScraper scraper = new GlassdoorScraper();
		
		CompanyInfo result = scraper.process(companyInfo);
		assertEquals(result.name(), companyName);
		
		GlassdoorCompanyInfo glassdoorInfo = result.glassdoorInfo();
		assertEquals(companyName, glassdoorInfo.company());
		assertEquals(glassdoorURL, glassdoorInfo.URL());
		assertEquals(globalScore, glassdoorInfo.globalScore());
		assertEquals(nationalScore, glassdoorInfo.nationalScore());
		assertEquals(localScore, glassdoorInfo.localScore());
		assertEquals(lastGlobalUpdate, glassdoorInfo.lastGlobalUpdate());
		assertEquals(lastNationalUpdate, glassdoorInfo.lastNationalUpdate());
		assertEquals(lastLocalUpdate, glassdoorInfo.lastLocalUpdate());
	}

	@Test
	void testCompanyNotFound() {
		final String fakeCompanyName = "nonexistent company";
		
		CompanyInfo companyInfo = new CompanyInfo(fakeCompanyName, null, null);
		GlassdoorScraper scraper = new GlassdoorScraper();
		
		CompanyInfo result = scraper.process(companyInfo);
		assertEquals(fakeCompanyName, result.name());
		
		GlassdoorCompanyInfo glassdoorInfo = result.glassdoorInfo();
		assertEquals(Scraper.COMPANY_NOT_FOUND, glassdoorInfo.company());
		assertEquals(Scraper.COMPANY_NOT_FOUND, glassdoorInfo.URL());
		assertNull(glassdoorInfo.globalScore());
		assertNull(glassdoorInfo.nationalScore());
		assertNull(glassdoorInfo.localScore());
		assertNull(glassdoorInfo.lastGlobalUpdate());
		assertNull(glassdoorInfo.lastNationalUpdate());
		assertNull(glassdoorInfo.lastLocalUpdate());
	}
}
