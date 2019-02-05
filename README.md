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

Given more time I'd create a front end in Angular as well that makes the call to the
web service and uses binding to display the responses.
