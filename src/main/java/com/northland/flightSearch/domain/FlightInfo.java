package com.northland.flightSearch.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class FlightInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String destination;
	private String departureDate;
	private Price price;
	
	public FlightInfo() { }
	
	public String getDestination() {
		return this.destination;
	}
	
	public String getDepartureDate() {
		return this.departureDate;
	}
	
	public Price getPrice() {
		return this.price;
	}
	
	public void setDestination(final String destination) {
		this.destination = destination;
	}
	
	public void setDepartureDate(final String departureDate) {
		this.departureDate = departureDate;
	}
	
	public void setPrice(final Price price) {
		this.price = price;
	}

}
