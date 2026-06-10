package es.chachimente.curroscraper.application.companyimporter;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.chachimente.curroscraper.application.SharedJobConfiguration;

@Configuration
public class JobConfiguration {
	@Value("${company-importer.data-folder}")
	private String DATA_FOLDER;
	
	// Input
	@Value("${company-importer.input.companies-file}")
	private String COMPANIES_INPUT_FILE;
	
	// Output
	@Value("${company-importer.output.company-info-file}")
	private String COMPANY_INFO_FILE;
	
	// CompanyImporter Job
	@Bean
	public Job companyImporterJob(JobRepository jobRepository) {
		return new JobBuilder(jobRepository)
				.start(SharedJobConfiguration.companyScraperStep(jobRepository, COMPANIES_INPUT_FILE, COMPANY_INFO_FILE))
				.build();
	}
}