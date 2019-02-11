CAVEAT: This solution runs as a RESTful web service that expects the following:
-> You have Java 8 or higher installed in your environment
-> You have Maven 3.5 or higher installed in your environment
-> It assumes Amadeus is supplying prices in dollars
-> It assumes the Amadeus web service is providing a sorted list of prices, from lowest to highest.
-> It assumes you were only interested in the 3 prices, not any of the other data from the flight.

To run this as a web service open a command window, change to the root directory of the project and run:

   mvn spring-boot:run
   
Point your browser to http://localhost:8080/getCheapestThreeFlights?originCode=XXX

-> XXX is the airport code for where the flight departs from
-> If no originCode is supplied it uses a default value of "MAD"

If this was an actual production application I would cast the results being returned
by the Amadeus web service into customized Java objects that match the responses using
an ObjectMapper so more data could be displayed.

Given more time I'd create a front end in Angular as well that prompts you for the origin airport code
and makes the call to the web service that use binding to display the responses.

I should also point out that I'd ordinarily add a lot more unit tests to a production application
but I wanted to at least demonstrate how to test private methods that use autowired Spring classes. 
This makes the test more of a system test than a unit test but I'm trying to demonstrate that I always
write unit tests but didn't want to go to the extent of mocking the Amadeus service.

There should be tests for each of the methods in the FlightSearchController class.

There is also a refactored version of the FlightSearchController called RefactoredFlightSearchController that accesses the Amadeus web service as a separate FlightSearchService rather than a simple RESTful API call. Java 8 enhancements were added as well (converted loops to streams)

To test the refactored version point your browser to this URL:
http://localhost:8081/refactored/getCheapestThreeFlights?originCode=MAD

