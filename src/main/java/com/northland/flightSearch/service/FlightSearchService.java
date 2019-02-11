package com.northland.flightSearch.service;

import java.util.List;

import com.northland.flightSearch.domain.FlightInfo;

public interface FlightSearchService {

	public List<FlightInfo> getFlightsSortedByIncreasingPrice(final String origin);
	
}
