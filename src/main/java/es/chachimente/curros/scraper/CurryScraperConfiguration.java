package es.chachimente.curros.scraper;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Configuration

public class CurryScraperConfiguration {
	
	/*
	@Bean
	public JobOperatorFactoryBean jobOperator(JobRepository jobRepository) {
		JobOperatorFactoryBean jobOperatorFactoryBean = new JobOperatorFactoryBean();
		jobOperatorFactoryBean.setJobRepository(jobRepository);
		jobOperatorFactoryBean.setTaskExecutor(new SimpleAsyncTaskExecutor());
		return jobOperatorFactoryBean;
	}
	*/

	@Bean
	public FlatFileItemReader<LinkedInCurro> reader() {
	  return new FlatFileItemReaderBuilder<LinkedInCurro>()
	    .name("curroReader")
	    .resource(new ClassPathResource("jobs.csv"))
	    .delimited()
	    .names("URL")
	    .targetType(LinkedInCurro.class)
	    .build();
	}

	@Bean
	public FlatFileItemWriter<MalditasConsultorasCompanyInfo> writer() {
	  return new FlatFileItemWriterBuilder<MalditasConsultorasCompanyInfo>()
	    .name("curroWriter")
	    .resource(new FileSystemResource("output.csv"))
	    .delimited()
	    .names("company", "URL", "rotacionHistorica", "lastUpdate")
	    .build();
	}
	
	@Bean
	public MalditasConsultorasScraper processor() {
	  return new MalditasConsultorasScraper();
	}
	
	@Bean
	public Job importUserJob(JobRepository jobRepository, Step malditasConsultorasStep) {
	  return new JobBuilder(jobRepository)
	    .start(malditasConsultorasStep)
	    .build();
	}
	
	@Bean
	public Step malditasConsultorasStep(JobRepository jobRepository, FlatFileItemReader<LinkedInCurro> reader, MalditasConsultorasScraper processor, FlatFileItemWriter<MalditasConsultorasCompanyInfo> writer) {
	  return new StepBuilder("malditasConsultorasStep", jobRepository)
	    .<LinkedInCurro, MalditasConsultorasCompanyInfo> chunk(10)
	    .reader(reader)
	    .processor(processor)
	    .writer(writer)
	    .build();
	}
}
