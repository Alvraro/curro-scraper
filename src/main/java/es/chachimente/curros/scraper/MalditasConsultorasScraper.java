package es.chachimente.curros.scraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class MalditasConsultorasScraper implements ItemProcessor<LinkedInCurro, MalditasConsultorasCompanyInfo>{

	private static final Logger log = LoggerFactory.getLogger(MalditasConsultorasScraper.class);
	
	@Override
	public MalditasConsultorasCompanyInfo process(LinkedInCurro item) throws Exception {
		log.info("Processing curro: " + item);
		return null;
	}

}
