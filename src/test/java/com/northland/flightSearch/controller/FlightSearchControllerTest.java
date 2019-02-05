package com.northland.flightSearch.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FlightSearchControllerTest {

	@Autowired
	FlightSearchController flightSearchController;
	
	public FlightSearchControllerTest() {
		super();
	}
	
	@Test
	public void getAmadeusOAuthTokenTest() {

		try {
			Method method = FlightSearchController.class.getDeclaredMethod("getAmadeusOAuthToken");
		    method.setAccessible(true);
		    
		    String accessToken = (String)method.invoke(flightSearchController);
		    assertThat(accessToken, is(IsNull.notNullValue()));
		}
		catch (Exception e) {
			System.out.println("Exception caught: " + e);
			e.printStackTrace();
		}
	}
}
