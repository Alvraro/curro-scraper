package es.chachimente.curroscraper.application.curroimporter;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.chachimente.curroscraper.application.SharedJobConfiguration;
import es.chachimente.curroscraper.model.CompanyName;
import es.chachimente.curroscraper.model.CurroInfo;
import es.chachimente.curroscraper.model.CurroURL;

@Configuration
public class JobConfiguration {
	@Value("${curro-importer.data-folder}")
	private static String DATA_FOLDER;
	
	// Input
	@Value("${curro-importer.input.curros-file}")
	private String CURROS_INPUT_FILE;
	
	// Output
	@Value("${curro-importer.output.curro-info-file}")
	private String CURROS_INFO_FILE;
	@Value("${curro-importer.output.company-names-file}")
	private String COMPANY_NAMES_FILE;
	@Value("${curro-importer.output.company-info-file}")
	private String COMPANY_INFO_FILE;

	// CurroImporter Job
	@Bean
	public Job curroImporterJob(JobRepository jobRepository) {
		return new JobBuilder(jobRepository)
				.start(curroScraperStep(jobRepository))
				.next(curro2CompanyStep(jobRepository))
				.next(SharedJobConfiguration.companyScraperStep(jobRepository, COMPANY_NAMES_FILE, COMPANY_INFO_FILE))
				.build();
	}

	// curroScraper Step: Scrape LinkedIn for job info and write to output file
	@Bean
	public Step curroScraperStep(JobRepository jobRepository) {
		return new StepBuilder(jobRepository)
				.<CurroURL, CurroInfo> chunk(10)
				.reader(SharedJobConfiguration.curroURLReader(CURROS_INPUT_FILE))
				.processor(SharedJobConfiguration.curroScraper())
				.writer(SharedJobConfiguration.curroWriter(CURROS_INFO_FILE))
				.build();
	}
	
	// curro2Company Step: Extract company names from jobs info
	@Bean
	public Step curro2CompanyStep(JobRepository jobRepository) {
		return new StepBuilder(jobRepository)
				.<CurroInfo, CompanyName> chunk(10)
				.reader(SharedJobConfiguration.curroInfoReader(CURROS_INFO_FILE))
				.processor(SharedJobConfiguration.curro2CompanyProcessor())
				.writer(SharedJobConfiguration.companyNameWriter(COMPANY_NAMES_FILE))
				.build();
	}
}