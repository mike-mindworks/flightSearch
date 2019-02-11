package com.northland.flightSearch.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Price implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String total;
	
	public Price() { }
	
	public void setTotal(final String total) {
		this.total = total;
	}

	public String getTotal() {
		return this.total;
	}
}
