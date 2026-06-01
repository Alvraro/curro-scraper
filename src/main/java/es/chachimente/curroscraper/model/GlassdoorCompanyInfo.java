package es.chachimente.curroscraper.model;

import java.util.Date;

public record GlassdoorCompanyInfo(String company, String URL, Float globalScore, Float localScore, Date lastUpdate) {

}
