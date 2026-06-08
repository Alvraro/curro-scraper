package es.chachimente.curroscraper.application.companyimporter;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.chachimente.curroscraper.application.SharedJobConfiguration;

@Configuration
public class JobConfiguration {
	private static final String DATA_FOLDER = "data/company-importer/";
	
	// Input
	private static final String COMPANIES_INPUT_FILE = DATA_FOLDER + "input/company-names.csv";
	
	// Output
	private static final String COMPANY_INFO_FILE = DATA_FOLDER + "generated/company-info.csv";
	
	// CompanyImporter Job
	@Bean
	public Job companyImporterJob(JobRepository jobRepository) {
		return new JobBuilder(jobRepository)
				.start(SharedJobConfiguration.companyScraperStep(jobRepository, COMPANIES_INPUT_FILE, COMPANY_INFO_FILE))
				.build();
	}
}