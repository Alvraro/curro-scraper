package es.chachimente.curroscraper.model;

import java.time.LocalDate;

public record MalditasConsultorasCompanyInfo(String fullName, String shortName, String linkedInURL, String companyURL, Float rotacionHistorica, LocalDate lastUpdate) {

}
