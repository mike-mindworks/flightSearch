package com.northland.flightSearch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class FlightSearchController {
	
	@Autowired
	RestTemplateBuilder builder;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
    
    @RequestMapping("/getFlights")
    public String getFlights() {
    	RestTemplate restTemplate = builder.build();
    	//restTemplate.exchange("https://test.api.amadeus.com/v1/shopping/flight-destinations?origin=MAD", responseType)
    	
        return "getFlights() invoked";
    }

}
