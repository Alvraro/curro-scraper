package es.chachimente.curroscraper.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.transform.FieldExtractor;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import es.chachimente.curroscraper.model.CompanyInfo;
import es.chachimente.curroscraper.model.CompanyName;
import es.chachimente.curroscraper.model.CurroInfo;
import es.chachimente.curroscraper.model.CurroURL;
import es.chachimente.curroscraper.scraper.GlassdoorScraper;
import es.chachimente.curroscraper.scraper.LinkedInCurroScraper;
import es.chachimente.curroscraper.scraper.MalditasConsultorasScraper;

@Configuration
public class CurryScraperConfiguration {
	
	/*
	@Bean
	public JobOperatorFactoryBean jobOperator(JobRepository jobRepository) {
		JobOperatorFactoryBean jobOperatorFactoryBean = new JobOperatorFactoryBean();
		jobOperatorFactoryBean.setJobRepository;
		jobOperatorFactoryBean.setTaskExecutor(new SimpleAsyncTaskExecutor());
		return jobOperatorFactoryBean;
	}
	*/

	// CurroImporter Job
	@Bean
	public Job curroImporterJob(JobRepository jobRepository) {
		return new JobBuilder(jobRepository)
				.start(curroScraperStep(jobRepository))
				.next(curro2CompanyStep(jobRepository))
				.next(companyScraperStep(jobRepository))
				.build();
	}

	// curroScraper Step: Scrape LinkedIn for job info and write to output file
	@Bean
	public Step curroScraperStep(JobRepository jobRepository) {
		return new StepBuilder(jobRepository)
				.<CurroURL, CurroInfo> chunk(10)
				.reader(curroURLReader())
				.processor(curroScraper())
				.writer(curroWriter())
				.build();
	}

	@Bean
	public FlatFileItemReader<CurroURL> curroURLReader() {
		return new FlatFileItemReaderBuilder<CurroURL>()
				.name("curroURLReader")
				.resource(new FileSystemResource("data/input/curros.csv"))
				.delimited()
				.names("URL")
				.targetType(CurroURL.class)
				.build();
	}

	@Bean
	public LinkedInCurroScraper curroScraper() {
		return new LinkedInCurroScraper();
	}
	
	@Bean
	public FlatFileItemWriter<CurroInfo> curroWriter() {
		return new FlatFileItemWriterBuilder<CurroInfo>()
				.name("curroWriter")
				.resource(new FileSystemResource("data/generated/curros-info.csv"))
				.delimited()
				.quoteCharacter("\"")
				.names("title", "company", "location", "description", "URL")
				.build();
	}

	// curro2Company Step: Extract company names from jobs info
	@Bean
	public Step curro2CompanyStep(JobRepository jobRepository) {
		return new StepBuilder(jobRepository)
				.<CurroInfo, CompanyName> chunk(10)
				.reader(curroInfoReader())
				.processor(curro2CompanyProcessor())
				.writer(companyNameWriter())
				.build();
	}
	
	@Bean
	public ItemReader<CurroInfo> curroInfoReader() {
		return new FlatFileItemReaderBuilder<CurroInfo>()
				.name("curroInfoReader")
				.resource(new FileSystemResource("data/generated/curros-info.csv"))
				.delimited()
				.names("title", "company", "location", "description", "URL")
				.targetType(CurroInfo.class)
				.build();
	}

	@Bean
	public ItemProcessor<CurroInfo, CompanyName> curro2CompanyProcessor() {
		return new ItemProcessor<CurroInfo, CompanyName>() {
			@Override
			public CompanyName process(CurroInfo curroInfo) throws Exception {
				return new CompanyName(curroInfo.company());
			}
		};
	}
	
	@Bean
	public ItemWriter<CompanyName> companyNameWriter() {
		return new FlatFileItemWriterBuilder<CompanyName>()
				.name("companyNameWriter")
				.resource(new FileSystemResource("data/generated/company-names.csv"))
				.delimited()
				.names("name")
				.build();
	}
	
	// companyScraper Step: Scrape several sources for company info and write to output file
	@Bean
	public Step companyScraperStep(JobRepository jobRepository) {
		return new StepBuilder(jobRepository)
				.<CompanyName, CompanyInfo> chunk(10)
				.reader(companyNameReader())
				.processor(companyScraperProcessor())
				.writer(companyInfoWriter())
				.build();
	}

	@Bean
	public ItemReader<CompanyName> companyNameReader() {
		return new FlatFileItemReaderBuilder<CompanyName>()
				.name("companyNameReader")
				.resource(new FileSystemResource("data/generated/company-names.csv"))
				.delimited()
				.names("name")
				.targetType(CompanyName.class)
				.build();
	}

	@Bean
	public ItemProcessor<CompanyName, CompanyInfo> companyScraperProcessor() {		
		CompositeItemProcessor<CompanyName, CompanyInfo> processor = new CompositeItemProcessor<>();

		List delegates = new ArrayList<>(3);
		// First processor converts CompanyName to CompanyInfo
		delegates.add(new ItemProcessor<CompanyName, CompanyInfo>() {
			@Override
			public CompanyInfo process(CompanyName companyName) throws Exception {
				return new CompanyInfo(companyName.name(), null, null);
			}
		});
		// Then we have one processor per source to enrich CompanyInfo 
		delegates.add(malditasConsultorasScraper());
		delegates.add(glassdoorScraper());		
		processor.setDelegates(delegates);
		
		return processor;
	}

	@Bean
	public MalditasConsultorasScraper malditasConsultorasScraper() {
		return new MalditasConsultorasScraper();
	}
	
	@Bean
	public GlassdoorScraper glassdoorScraper() {
		return new GlassdoorScraper();
	}
	
	@Bean
	public FlatFileItemWriter<CompanyInfo> companyInfoWriter() {
		return new FlatFileItemWriterBuilder<CompanyInfo>().name("companyInfoWriter")
				.resource(new FileSystemResource("data/generated/company-info.csv")).delimited()
				//.names("name", "fullName", "shortName", "linkedInURL", "companyURL", "rotacionHistorica", "lastUpdate", "company", "URL", "globalScore", "localScore", "lastUpdate")
				.fieldExtractor(new FieldExtractor<CompanyInfo>() {

					@Override
					public Object[] extract(CompanyInfo item) {
						return new Object[] {
								item.name(), 
								item.malditasConsultorasInfo() != null ? item.malditasConsultorasInfo().fullName() : null, 
								item.malditasConsultorasInfo() != null ? item.malditasConsultorasInfo().shortName() : null, 
								item.malditasConsultorasInfo() != null ? item.malditasConsultorasInfo().linkedInURL() : null, 
								item.malditasConsultorasInfo() != null ? item.malditasConsultorasInfo().companyURL() : null, 
								item.malditasConsultorasInfo() != null ? item.malditasConsultorasInfo().rotacionHistorica() : null, 
								item.malditasConsultorasInfo() != null ? item.malditasConsultorasInfo().lastUpdate() : null, 
								item.glassdoorInfo() != null ? item.glassdoorInfo().company() : null, 
								item.glassdoorInfo() != null ? item.glassdoorInfo().URL() : null, 
								item.glassdoorInfo() != null ? item.glassdoorInfo().globalScore() : null,
								item.glassdoorInfo() != null ? item.glassdoorInfo().nationalScore() : null,
								item.glassdoorInfo() != null ? item.glassdoorInfo().localScore() : null, 
								item.glassdoorInfo() != null ? item.glassdoorInfo().lastGlobalUpdate() : null,
								item.glassdoorInfo() != null ? item.glassdoorInfo().lastNationalUpdate() : null,
								item.glassdoorInfo() != null ? item.glassdoorInfo().lastLocalUpdate() : null
						};
					}

				})
				//.names("name", "malditasConsultorasInfo", "glassdoorInfo")
				.build();
	}
}
