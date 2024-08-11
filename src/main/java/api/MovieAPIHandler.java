package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;

import java.io.IOException;

public class MovieAPIHandler implements HttpHandler {

    private final MovieAPI movieAPI;
    public MovieAPIHandler(Driver driver) throws IOException {
        this.movieAPI = new MovieAPI(driver);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Get type of request as a String
        String requestMethod = exchange.getRequestMethod();
        // Get request body as a String
        String requestBody = movieAPI.readRequestBody(exchange.getRequestBody());
        // Get the url endpoint to call the correct db method
        String requestURI = movieAPI.returnRequestURI(exchange);

        try {
            // Parse the request body and put it into a JSON object
            JSONObject json = new JSONObject(requestBody);
            System.out.println("Converted to JSON: " + json);
            if (requestMethod.equals("GET")) {
                // Check which fields need to be parsed and which method called
                switch (requestURI) {
                    case "/api/v1/getActor": {
                        String actorsId = json.getString("actorId");
                        movieAPI.getActor(actorsId, exchange);
                        break;
                    }
                    case "/api/v1/getMovie": {
                        String moviesId = json.getString("movieId");
                        movieAPI.getMovie(moviesId, exchange);
                        break;
                    }
                    case "/api/v1/hasRelationship": {
                        String actorsId = json.getString("actorId");
                        String moviesId = json.getString("movieId");
                        movieAPI.hasRelationship(actorsId, moviesId, exchange);
                        break;
                    }
                    case "/api/v1/computeBaconNumber": {
                        String actorsId = json.getString("actorId");
                        movieAPI.computeBaconNumber(actorsId, exchange);
                        break;
                    }
                    case "/api/v1/computeBaconPath": {
                        String actorsId = json.getString("actorId");
                        movieAPI.computeBaconPath(actorsId, exchange);
                        break;
                    }
                    default:
                        break;
                }
            }
            else if (requestMethod.equals("PUT")) {
                switch (requestURI) {
                    case "/api/v1/addActor": {
                        String actorsName = json.getString("name");
                        String actorsId = json.getString("actorId");
                        movieAPI.addActor(actorsName, actorsId, exchange);
                        break;
                    }
                    case "/api/v1/addMovie": {
                        String moviesName = json.getString("name");
                        String moviesId = json.getString("movieId");
                        movieAPI.addMovie(moviesName, moviesId, exchange);
                        break;
                    }
                    case "/api/v1/addRelationship": {
                        String actorsId = json.getString("actorId");
                        String moviesId = json.getString("movieId");
                        movieAPI.addRelationship(actorsId, moviesId, exchange);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        catch (JSONException e) {
            // 400 response: body improperly formatted
            throw new RuntimeException(e);
        }

    }

}
