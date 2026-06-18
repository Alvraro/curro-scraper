package es.chachimente.curros.scraper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import es.chachimente.curroscraper.application.curroimporter.Application;
import es.chachimente.curroscraper.model.CompanyInfo;
import es.chachimente.curroscraper.model.MalditasConsultorasCompanyInfo;
import es.chachimente.curroscraper.scraper.MalditasConsultorasScraper;
import es.chachimente.curroscraper.scraper.Scraper;

@SpringBootTest(classes = Application.class, properties = "spring.batch.job.enabled=false")
class MalditasConsultorasScraperTest {

	@Test
	void testFullData() {
		testCompany("knowmad mood", "knowmad mood", "knowmad mood", "https://www.linkedin.com/company/knowmad-mood", "https://malditasconsultoras.com/opiniones-de-knowmad-mood/", "www.knowmadmood.com", 54.35f, LocalDate.of(2024, 6, 13));
	}
	
	@Test
	void testCompanyNotInFirstPage() {
		testCompany("Keepler Data Tech", "Keepler Data Tech", "Keepler", "https:/www.linkedin.com/company/keepler", "https://malditasconsultoras.com/opiniones-de-keepler/", "unknown", 28.96f, LocalDate.of(2025, 1, 7));
	}

	@Test
	void testCompanyNotFound() {
		testCompany("nonexistent company", Scraper.COMPANY_NOT_FOUND, Scraper.COMPANY_NOT_FOUND, Scraper.COMPANY_NOT_FOUND, Scraper.COMPANY_NOT_FOUND, Scraper.COMPANY_NOT_FOUND, null, null);
	}
	
	@Test
	void testCompanyNotFirstResultPlexus() {
		testCompany("Plexus", "Plexus Tech", "Plexus", "https:/www.linkedin.com/company/plexus-tech", "https://malditasconsultoras.com/opiniones-de-plexus/", "unknown", 45.65f, LocalDate.of(2025, 1, 6));
	}

	@Test
	void testCompanyNotFirstResultTecdata() {
		testCompany("TECDATA", "Grupo TECDATA Engineering", "Grupo TECDATA Engineering", "https://www.linkedin.com/company/tecdata-engineering", "https://malditasconsultoras.com/opiniones-de-tecdata/", "https://www.tecdata.es", 54.42f, LocalDate.of(2024, 7, 8));
	}
	
	// Common test function
	void testCompany(String companyName, String expectedFullName, String expectedShortName, String expectedLinkedInURL, String expectedCompanyURL, String expectedExternalURL, Float expectedRotacionHistorica, LocalDate expectedLastUpdate) {
		CompanyInfo companyInfo = new CompanyInfo(companyName, null, null);
		MalditasConsultorasScraper scraper = new MalditasConsultorasScraper();
		
		CompanyInfo result = scraper.process(companyInfo);
		assertEquals(companyName, result.name());
		
		MalditasConsultorasCompanyInfo mcInfo = result.malditasConsultorasInfo();
		assertEquals(expectedFullName, mcInfo.fullName());
		assertEquals(expectedShortName, mcInfo.shortName());
		assertEquals(expectedLinkedInURL, mcInfo.linkedInURL());
		assertEquals(expectedCompanyURL, mcInfo.malditasConsultorasURL());
		assertEquals(expectedExternalURL, mcInfo.externalURL());
		assertEquals(expectedRotacionHistorica, mcInfo.rotacionHistorica());
		assertEquals(expectedLastUpdate, mcInfo.lastUpdate());
	}
}
