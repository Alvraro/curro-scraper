package es.chachimente.curroscraper.model;

import java.time.LocalDate;

public record GlassdoorCompanyInfo(String company, String glassdoorURL, String externalURL,
		Float globalScore, Float nationalScore, Float localScore, 
		LocalDate lastGlobalUpdate, LocalDate lastNationalUpdate, LocalDate lastLocalUpdate,
		Integer globalNumberOfReviews, Integer nationalNumberOfReviews, Integer localNumberOfReviews) {
}
