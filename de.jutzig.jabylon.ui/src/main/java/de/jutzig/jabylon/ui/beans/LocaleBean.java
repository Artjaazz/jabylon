package de.jutzig.jabylon.ui.beans;

import java.util.Locale;

public class LocaleBean {
	private String language = "";
	private String country = "";
	private String variant = ""; 

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getVariant() {
		return variant;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}
	
	public Locale createLocale()
	{
		return new Locale(language, country, variant);
	}
	
	
}
