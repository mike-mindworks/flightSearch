package com.northland.flightSearch.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.northland.flightSearch.domain.FlightInfo;
import com.northland.flightSearch.service.FlightSearchService;

@RestController
@RequestMapping("/refactored")
/**
 * This Controller class contains the logic for retrieving the prices of the 
 * three least expensive flights leaving from a given origin airport code.
 * 
 * NOTE: The Amadeus URL returns the cheapest flights first, hence there is no
 *       sorting done on the returned collection.
 *       
 * @author Mike Allan
 *
 */
public class RefactoredFlightSearchController {

	// Amadeus constants
	static final String AMADEUS_OAUTH_URL = "https://test.api.amadeus.com/v1/security/oauth2/token/form";
	static final String AMADEUS_FLIGHT_DESTINATIONS_URL = "https://test.api.amadeus.com/v1/shopping/flight-destinations?origin=";
	static final String AMADEUS_API_KEY = "7nNFOqhxyqppgPq9eYgDvmhNjnkl9qW6";
	static final String AMADEUS_API_SECRET = "bd5MZ2tNEAVyQYJA";
	
	@Autowired
	@Qualifier("amadeusFlightSearchService")
	FlightSearchService flightSearchService;
	
    @GetMapping("")
    public String index() {
        return "Greetings from the Refactored Flight Search Controller!";
    }
    
    @GetMapping("/getCheapestThreeFlights")
    /**
     * This method retrieves the three cheapest flights that depart from the
     * specified originCode or from the origin code "MAD" if no originCode
     * parameter is specified in the request string.
     * 
     * @param originCode - String value of the airport code the flight leaves from
     * @return String - Formatted HTML displaying the cheapest 3 flights
     */
    public String getCheapestThreeFlights(@RequestParam("originCode") Optional<String> originCode) {
    	String origin = "MAD";
    	if (originCode.isPresent()) {
    		origin = originCode.get();
    	}
    	
    	List<FlightInfo> flightList = flightSearchService.getFlightsSortedByIncreasingPrice(origin);
    	return createCheapestThreeFlightsResponse(flightList, origin);
    }
    
    /**
     * This method builds HTML output of the three cheapest flights for an
     * origin code based on the results we got back from the FlightSearchService
     * 
     * @param cheapestThreeFlights - A List of FlightInfo objects
     * @return String - Formatted HTML output for a response
     */
    private String createCheapestThreeFlightsResponse(List<FlightInfo> flightList, String origin) {
    	
    	if ((flightList == null) || (flightList.size() == 0)) {
    		return "There are no flights that use the origin code \"" + origin + "\"";
    	}
	    	
    	StringBuffer returnString = new StringBuffer();
    	returnString.append("The prices of the cheapest 3 flights from origin code \"");
    	returnString.append(origin);
    	returnString.append("\" are:");
    	returnString.append("<ul>");
    	
    	Iterable<FlightInfo> flightInfoItr = () -> flightList.iterator();
    	String flightListHtml = StreamSupport.stream(flightInfoItr.spliterator(), false).
    			limit(3).
    			map(flightInfo -> "<li>" + buildPriceString(flightInfo) + "</li>").
    			collect(Collectors.joining());
    	returnString.append(flightListHtml);
    	
		returnString.append("</ul>");
		
        return returnString.toString();
    }
    
    /**
     * This builds a price String that includes the destination and departure
     * date from a FlightInfo object.
     * 
     * @param flightInfo - A FlightInfo object to build the price string from
     * @return String - Contains price, destination and departure date
     */
    private String buildPriceString(FlightInfo flightInfo) {
    	StringBuffer priceString = new StringBuffer();
    	priceString.append("$");
    	priceString.append(flightInfo.getPrice().getTotal());
    	
    	if (flightInfo.getDestination() != null) {
    		priceString.append(" - Flying to ");
    		priceString.append(flightInfo.getDestination());
    	}
 
    	if (flightInfo.getDepartureDate() != null) {
	    	if (flightInfo.getDestination() != null) {
	    		priceString.append(" on ");
	    	}
	    	else {
	    		priceString.append(" - Departing on ");
	    	}
    		priceString.append(flightInfo.getDepartureDate());
    	}
    	
    	return priceString.toString();
    }
    
}
