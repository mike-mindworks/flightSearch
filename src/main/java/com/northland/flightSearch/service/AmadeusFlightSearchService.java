package com.northland.flightSearch.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.northland.flightSearch.domain.FlightInfo;

@Component
@Qualifier("amadeusFlightSearchService")
public class AmadeusFlightSearchService implements FlightSearchService {
	
	Logger log = LoggerFactory.getLogger(AmadeusFlightSearchService.class);
	
	// Amadeus constants
	static final String AMADEUS_OAUTH_URL = "https://test.api.amadeus.com/v1/security/oauth2/token/form";
	static final String AMADEUS_FLIGHT_DESTINATIONS_URL = "https://test.api.amadeus.com/v1/shopping/flight-destinations?origin=";
	static final String AMADEUS_API_KEY = "7nNFOqhxyqppgPq9eYgDvmhNjnkl9qW6";
	static final String AMADEUS_API_SECRET = "bd5MZ2tNEAVyQYJA";
	
	@Autowired
	RestTemplateBuilder builder;
	
	public AmadeusFlightSearchService() {
		super();
	}

	@SuppressWarnings("rawtypes")
	/**
	 * This method submits a request to the Amadeus web service that retrieves
	 * the flights that depart from a given origin airport code. The results
	 * are sorted in increasing price order.
	 */
	public List<FlightInfo> getFlightsSortedByIncreasingPrice(final String origin) {
    	List<FlightInfo> flightList = new ArrayList<FlightInfo>();
    	
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
	    		return flightList;
	    	}
	    	
	    	ObjectMapper mapper = new ObjectMapper();
	    	Iterable<JsonNode> dataNodeItr = () -> dataNode.iterator();
	    	flightList = StreamSupport.stream(dataNodeItr.spliterator(), false)
	    			.map(flightNode -> {
	    				FlightInfo info = null;
						try {
							info = mapper.treeToValue(flightNode, FlightInfo.class);
						} catch (JsonProcessingException e) {
							log.error("Exception caught converting flightNode to FlightInfo object: " + e);
						}
						return info;
					})
	    			.collect(Collectors.toList());
    	}
    	catch (HttpClientErrorException hcee) {
    		log.error("HttpClientErrorException caught: " + hcee);
    		StringWriter sw = new StringWriter();
    		hcee.printStackTrace(new PrintWriter(sw));
    		log.error("Stack Trace: " + sw.toString());
    	}
    	catch (Exception e) {
    		log.error("Exception caught: " + e);
    		StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		log.error("Stack Trace: " + sw.toString());
    	}
    	return flightList;
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
