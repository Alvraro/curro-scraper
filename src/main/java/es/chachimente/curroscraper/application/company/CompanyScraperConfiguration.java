package es.chachimente.curroscraper.application.company;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.chachimente.curroscraper.application.SharedJobConfiguration;

@Configuration
public class CompanyScraperConfiguration {
	// Input
	private static final String COMPANIES_INPUT_FILE = "data/company-scraper/input/company-names.csv";
	
	// Output
	private static final String COMPANY_INFO_FILE = "data/company-scraper/generated/company-info.csv";
	
	// CompanyImporter Job
	@Bean
	public Job companyImporterJob(JobRepository jobRepository) {
		return new JobBuilder(jobRepository)
				.start(SharedJobConfiguration.companyScraperStep(jobRepository, COMPANIES_INPUT_FILE, COMPANY_INFO_FILE))
				.build();
	}
}