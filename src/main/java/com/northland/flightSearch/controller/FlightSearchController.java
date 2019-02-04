package com.northland.flightSearch.controller;


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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightDestination;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class FlightSearchController {
	
	static final String AMADEUS_API_KEY = "7nNFOqhxyqppgPq9eYgDvmhNjnkl9qW6";
	static final String AMADEUS_API_SECRET = "bd5MZ2tNEAVyQYJA";
	
	@Autowired
	RestTemplateBuilder builder;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
    
    @GetMapping("/getFlights")
    public String getFlights() {
    	// The easy way... using the SDK
    	Amadeus amadeus = Amadeus
                .builder(AMADEUS_API_KEY, AMADEUS_API_SECRET)
                .build();
    	int flightDestinationsCount = 0;
    	
    	try {
			FlightDestination[] flightDestinations = amadeus.shopping.flightDestinations.get(Params
					  .with("origin", "MAD"));
			flightDestinationsCount = flightDestinations.length;
			
		} catch (ResponseException e) {
			System.out.println("Exception caught: " + e);
			e.printStackTrace();
		}
    	
    	// The hard way, using the authentication token
    	String token = getOAuthToken();
    	
    	/*
    	RestTemplate restTemplate = builder.build();
    	ResponseEntity<String> jsonResponse = restTemplate.getForEntity("https://test.api.amadeus.com/v1/shopping/flight-destinations?origin=MAD", String.class);
        return jsonResponse.getBody();
    	*/
    	
        return "Flight Destination count = " + flightDestinationsCount + " - OAUth Token = " + token;
    }
    
    private String getOAuthToken( ) { 
    	final String oauthUrl = "https://test.api.amadeus.com/v1/security/oauth2/token";
    	RestTemplate restTemplate = builder.build();
    	
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	
    	MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
    	map.add("grant_type", "client_credentials");
    	map.add("client_id", AMADEUS_API_KEY);
    	map.add("client_secret", AMADEUS_API_SECRET);
    	
    	HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
    	ResponseEntity<String> jsonResponse = restTemplate.postForEntity(
    			oauthUrl+"/form", request , String.class);
		System.out.println("jsonResponse: " + jsonResponse);
		System.out.println("jsonResponse.body: " + jsonResponse.getBody());
   	
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
    	
		System.out.println("token: " + token);
        return token;
    }

}
