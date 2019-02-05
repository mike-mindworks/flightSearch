package com.northland.flightSearch.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/")
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
public class FlightSearchController {

	// Amadeus constants
	static final String AMADEUS_OAUTH_URL = "https://test.api.amadeus.com/v1/security/oauth2/token/form";
	static final String AMADEUS_FLIGHT_DESTINATIONS_URL = "https://test.api.amadeus.com/v1/shopping/flight-destinations?origin=";
	static final String AMADEUS_API_KEY = "7nNFOqhxyqppgPq9eYgDvmhNjnkl9qW6";
	static final String AMADEUS_API_SECRET = "bd5MZ2tNEAVyQYJA";
	
	@Autowired
	RestTemplateBuilder builder;
	
    @GetMapping("")
    public String index() {
        return "Greetings from Spring Boot!";
    }
    
    @GetMapping("/getCheapestThreeFlights")
    public String getCheapestThreeFlights(@RequestParam("originCode") Optional<String> originCode) {
    	String origin = "MAD";
    	if (originCode.isPresent()) {
    		origin = originCode.get();
    	}
    	
    	List<String> cheapestThreeFlights = getCheapestThreeFlightsFromAmadeus(origin);
    	return createCheapestThreeFlightsResponse(cheapestThreeFlights, origin);
    }
    
    @SuppressWarnings("rawtypes")
    /**
     * This method retrieves the three lowest prices returned from the Amadeus
     * test API web service for a given airport origin code.
     * 
     * @param origin - String value of the airport code the flight is coming from
     * @return List<String> - A list of the prices of the 3 cheapest flights as Strings
     */
	private List<String> getCheapestThreeFlightsFromAmadeus(final String origin) {
    	List<String> cheapestThreeFlightsList = new ArrayList<String>();
    	
    	// The easy way... using the Amadeus SDK
    	/*
    	Amadeus amadeus = Amadeus
                .builder(AMADEUS_API_KEY, AMADEUS_API_SECRET)
                .build();
    	try {
			FlightDestination[] flightDestinations = amadeus.shopping.flightDestinations.get(Params
					  .with("origin", origin));
			
		} catch (ResponseException e) {
			System.out.println("Exception caught: " + e);
			e.printStackTrace();
		}
		*/
    	
    	// The more challenging way, use a RestTemplate to retrieve results
    	// Build the HTTP Request with the OAuth token in the header
    	HttpEntity request = buildAuthenticatedHttpRequest();
    	try {
    		// Submit the request to the Amadeus test API web service
        	RestTemplate restTemplate = builder.build();
        	ResponseEntity<String> jsonResponse = restTemplate.exchange(AMADEUS_FLIGHT_DESTINATIONS_URL + origin, HttpMethod.GET, request, String.class);

        	// Read the Json data from the response
        	JsonNode root = new ObjectMapper().readTree(jsonResponse.getBody());
	    	JsonNode dataNode = root.path("data");
	    	if (dataNode == null) {
	    		// No data in the response
	    		return cheapestThreeFlightsList;
	    	}
	    	
	    	// We only want the first 3 cheapest flights
	    	Iterator<JsonNode> itr = dataNode.iterator();
	    	int count = 0;
	    	while (itr.hasNext() && (count < 3)) {
	    		JsonNode flightNode = itr.next();
	    		if (flightNode == null) continue;
	    		JsonNode priceNode = flightNode.path("price");
	    		if (priceNode == null) continue;
	    		JsonNode totalNode = priceNode.path("total");
	    		if (totalNode == null) continue;
	    		// Optional - You didn't ask for other flight details 
	    		// but it looks stupid without the destination and departure date at least
	    		JsonNode destinationNode = flightNode.path("destination");
	    		JsonNode departureDateNote = flightNode.path("departureDate");

	    		cheapestThreeFlightsList.add(buildPriceString(totalNode, destinationNode, departureDateNote));
	    		count++; 
	    	}
    	}
    	catch (HttpClientErrorException hcee) {
    		System.out.println("HttpClientErrorException caught: " + hcee);
    		hcee.printStackTrace();
    	}
    	catch (Exception e) {
    		System.out.println("Exception caught: " + e);
    		e.printStackTrace();
    	}
    	return cheapestThreeFlightsList;
    }
    
    /**
     * This method creates an HttpEntity with an authentication token from the
     * Amadeus test API web service using my client ID and client secret
     * 
     * @return HttpEntity - An empty HTTP request with populated header
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private HttpEntity buildAuthenticatedHttpRequest() {
    	// Get the OAuth2 token from the Amadeus API
    	String token = getAmadeusOAuthToken();
    	
    	// Now insert the token in the HttpHeader for the request
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        
    	return new HttpEntity(headers);
    }
    
    /**
     * This builds a price String that includes the destination and departure date
     * 
     * @param totalNode - JsonNode from response containing the price
     * @param destinationNode - JsonNode from response containing the destination
     * @param departureDateNote - JsonNode from response containing the departure date
     * @return String - Contains price, destination and departure date
     */
    private String buildPriceString(JsonNode totalNode, JsonNode destinationNode, JsonNode departureDateNote) {
    	StringBuffer priceString = new StringBuffer();
    	priceString.append("$");
    	priceString.append(totalNode.asText());
    	
    	if (destinationNode != null) {
    		priceString.append(" - Flying to ");
    		priceString.append(destinationNode.asText());
    	}
 
    	if (departureDateNote != null) {
	    	if (destinationNode != null) {
	    		priceString.append(" on ");
	    	}
	    	else {
	    		priceString.append(" - Departing on ");
	    	}
    		priceString.append(departureDateNote.asText());
    	}
    	
    	return priceString.toString();
    }
    
    /**
     * This method builds HTML output of the three cheapest flights for an
     * origin code based on the results we got back from the Amadeus test
     * web service
     * 
     * @param cheapestThreeFlights - A List of price Strings
     * @return String - Formatted HTML output for a response
     */
    private String createCheapestThreeFlightsResponse(List<String> cheapestThreeFlightsList, String origin) {
    	
    	if ((cheapestThreeFlightsList == null) || (cheapestThreeFlightsList.size() == 0)) {
    		return "There are no flights that use the origin code \"" + origin + "\"";
    	}
	    	
    	StringBuffer returnString = new StringBuffer();
    	returnString.append("The prices of the cheapest 3 flights from origin code \"");
    	returnString.append(origin);
    	returnString.append("\" are:");
    	returnString.append("<ul>");
    	
    	Iterator<String> itr = cheapestThreeFlightsList.iterator();
    	int count = 0;
    	while (itr.hasNext() && (count < 3)) {
    		count++;
    		String priceString = itr.next();

    		returnString.append("<li>");
    		returnString.append(priceString);
    		returnString.append("</li>");
    	}
		returnString.append("</ul>");
		
        return returnString.toString();
    }
    
    /**
     * This method retrieves an OAuth authentication token from the Amadeus
     * test web service using my client ID and client secret
     * 
     * @return String - The authentication token to use when submitting requests
     */
    private String getAmadeusOAuthToken( ) { 
    	RestTemplate restTemplate = builder.build();
    	
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	
    	MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
    	map.add("grant_type", "client_credentials");
    	map.add("client_id", AMADEUS_API_KEY);
    	map.add("client_secret", AMADEUS_API_SECRET);
    	
    	HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
    	ResponseEntity<String> jsonResponse = restTemplate.postForEntity(AMADEUS_OAUTH_URL, request , String.class);
   	
    	ObjectMapper mapper = new ObjectMapper();
    	String token = "";
    	try {
	    	JsonNode root = mapper.readTree(jsonResponse.getBody());
	    	JsonNode tokenNode = root.path("access_token");
	    	token = tokenNode.asText();
    	}
    	catch (Exception e) {
    		System.out.println("Exception caught: " + e);
    	}
    	
        return token;
    }

}
