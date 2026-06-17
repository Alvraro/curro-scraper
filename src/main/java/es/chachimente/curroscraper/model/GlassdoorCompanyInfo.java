package es.chachimente.curroscraper.model;

import java.time.LocalDate;

public record GlassdoorCompanyInfo(String company, String URL, 
		Float globalScore, Float nationalScore, Float localScore, 
		LocalDate lastGlobalUpdate, LocalDate lastNationalUpdate, LocalDate lastLocalUpdate,
		Integer globalNumberOfReviews, Integer nationalNumberOfReviews, Integer localNumberOfReviews) {
}
