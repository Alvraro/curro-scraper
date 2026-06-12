package es.chachimente.curroscraper.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import es.chachimente.curroscraper.model.CompanyInfo;
import es.chachimente.curroscraper.model.CompanyName;
import es.chachimente.curroscraper.model.CurroInfo;
import es.chachimente.curroscraper.model.CurroURL;
import es.chachimente.curroscraper.scraper.GlassdoorScraper;
import es.chachimente.curroscraper.scraper.LinkedInCurroScraper;
import es.chachimente.curroscraper.scraper.MalditasConsultorasScraper;

@Configuration
public class SharedJobConfiguration {
	public static FlatFileItemReader<CurroURL> curroURLReader(String path) {
		return new FlatFileItemReaderBuilder<CurroURL>()
				.name("curroURLReader")
				.resource(new FileSystemResource(path))
				.delimited().delimiter(";")
				.names("URL")
				.targetType(CurroURL.class)
				.build();
	}
	
	public static LinkedInCurroScraper curroScraper() {
		return new LinkedInCurroScraper();
	}
	
	public static FlatFileItemWriter<CurroInfo> curroWriter(String path) {
		return new FlatFileItemWriterBuilder<CurroInfo>()
				.name("curroWriter")
				.resource(new FileSystemResource(path))
				.delimited().delimiter(";")
				.quoteCharacter("\"")
				.names("title", "company", "location", "description", "URL")
				.build();
	}
	
	public static ItemReader<CurroInfo> curroInfoReader(String path) {
		return new FlatFileItemReaderBuilder<CurroInfo>()
				.name("curroInfoReader")
				.resource(new FileSystemResource(path))
				.delimited().delimiter(";")
				.names("title", "company", "location", "description", "URL")
				.targetType(CurroInfo.class)
				.build();
	}

	
	public static ItemProcessor<CurroInfo, CompanyName> curro2CompanyProcessor() {
		return new ItemProcessor<CurroInfo, CompanyName>() {
			@Override
			public CompanyName process(CurroInfo curroInfo) throws Exception {
				return new CompanyName(curroInfo.company());
			}
		};
	}
	
	public static ItemWriter<CompanyName> companyNameWriter(String path) {
		return new FlatFileItemWriterBuilder<CompanyName>()
				.name("companyNameWriter")
				.resource(new FileSystemResource(path))
				.delimited().delimiter(";")
				.names("name")
				.build();
	}

	public static ItemReader<CompanyName> companyNameReader(String path) {
		return new FlatFileItemReaderBuilder<CompanyName>()
				.name("companyNameReader")
				.resource(new FileSystemResource(path))
				.delimited().delimiter(";")
				.names("name")
				.targetType(CompanyName.class)
				.build();
	}

	
	public static ItemProcessor<CompanyName, CompanyInfo> companyScraperProcessor() {		
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

	
	public static MalditasConsultorasScraper malditasConsultorasScraper() {
		return new MalditasConsultorasScraper();
	}
	
	
	public static GlassdoorScraper glassdoorScraper() {
		return new GlassdoorScraper();
	}
	
	public static FlatFileItemWriter<CompanyInfo> companyInfoWriter(String path) {
		return new FlatFileItemWriterBuilder<CompanyInfo>().name("companyInfoWriter")
				.resource(new FileSystemResource(path)).delimited().delimiter(";")
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

	// companyScraper Step: Scrape several sources for company info and write to output file
	public static Step companyScraperStep(JobRepository jobRepository, ConcurrentMode concurrentMode, String companiesInputFile, String companyInfoOutputFile) {
		ChunkOrientedStepBuilder<CompanyName, CompanyInfo> flow = new StepBuilder(jobRepository)
				.<CompanyName, CompanyInfo> chunk(50)
				.reader(SharedJobConfiguration.companyNameReader(companiesInputFile))
				.processor(SharedJobConfiguration.companyScraperProcessor())
				.writer(SharedJobConfiguration.companyInfoWriter(companyInfoOutputFile));

		System.err.println("Configuring companyScraperStep with concurrency mode: " + concurrentMode);

		switch(concurrentMode) {
			case SINGLE_THREADED_STEP:
				// No additional configuration needed
				break;
			case MULTI_THREADED_STEP:
				ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
				taskExecutor.setCorePoolSize(10);
				taskExecutor.setMaxPoolSize(10);
				//taskExecutor.setQueueCapacity(100);
				taskExecutor.setThreadNamePrefix("companyScraper-");
				taskExecutor.initialize();
				flow = flow.taskExecutor(taskExecutor);
				//flow = flow.taskExecutor(new SimpleAsyncTaskExecutor("companyScraper-"));
				break;
		}

		return flow.build();
	}
}