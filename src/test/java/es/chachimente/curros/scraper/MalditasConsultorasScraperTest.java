package es.chachimente.curros.scraper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
		final String companyName = "knowmad mood";
		final String fullName = "knowmad mood";
		final String shortName = "knowmad mood";
		final String linkedInURL = "https://www.linkedin.com/company/knowmad-mood";
		final String companyURL = "https://malditasconsultoras.com/opiniones-de-knowmad-mood/";
		final Float rotacionHistorica = 54.35f;
		final LocalDate lastUpdate = LocalDate.of(2024, 6, 13);
		
		CompanyInfo companyInfo = new CompanyInfo(companyName, null, null);
		MalditasConsultorasScraper scraper = new MalditasConsultorasScraper();
		
		CompanyInfo result = scraper.process(companyInfo);
		assertEquals(companyName, result.name());
		
		MalditasConsultorasCompanyInfo mcInfo = result.malditasConsultorasInfo();
		assertEquals(fullName, mcInfo.fullName());
		assertEquals(shortName, mcInfo.shortName());
		assertEquals(linkedInURL, mcInfo.linkedInURL());
		assertEquals(companyURL, mcInfo.companyURL());
		assertEquals(rotacionHistorica, mcInfo.rotacionHistorica());
		assertEquals(lastUpdate, mcInfo.lastUpdate());
	}

	@Test
	void testCompanyNotFound() {
		final String fakeCompanyName = "nonexistent company";
		
		CompanyInfo companyInfo = new CompanyInfo(fakeCompanyName, null, null);
		MalditasConsultorasScraper scraper = new MalditasConsultorasScraper();
		
		CompanyInfo result = scraper.process(companyInfo);
		assertEquals(fakeCompanyName, result.name());
		
		MalditasConsultorasCompanyInfo mcInfo = result.malditasConsultorasInfo();
		assertEquals(Scraper.COMPANY_NOT_FOUND, mcInfo.fullName());
		assertEquals(Scraper.COMPANY_NOT_FOUND, mcInfo.shortName());
		assertEquals(Scraper.COMPANY_NOT_FOUND, mcInfo.linkedInURL());
		assertEquals(Scraper.COMPANY_NOT_FOUND, mcInfo.companyURL());
		assertNull(mcInfo.rotacionHistorica());
		assertNull(mcInfo.lastUpdate());
	}
}
