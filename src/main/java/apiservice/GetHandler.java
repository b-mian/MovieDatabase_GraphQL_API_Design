package apiservice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Values;

import java.io.IOException;

public class GetHandler implements HttpHandler {

    private final Driver driver;

    public GetHandler(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // Get request body as a string
            String requestBody = MovieAPI.readRequestBody(exchange.getRequestBody());
            // Get the url endpoint to call the correct db method
            String requestURI = MovieAPI.returnRequestURI(exchange);

            String response = "";
            // Parse the request body and put it into a JSON object
            try {
                JSONObject json = new JSONObject(requestBody);
                System.out.println("Converted to JSON: " + json);
                // Check which fields need to be parsed and which method called
                if (requestURI.equals("/api/v1/getActor")) {
                    String actorsId = json.getString("actorId");
                    getActor(actorsId);
                    response = "Actor successfully retrieved from the movie database";
                }
                else if (requestURI.equals("/api/v1/getMovie")) {
                    String moviesId = json.getString("movieId");
                    getMovie(moviesId);
                    response = "Movie successfully added to the movie database";
                }
                else if (requestURI.equals("/api/v1/hasRelationship")) {
                    String actorsId = json.getString("actorId");
                    String moviesId = json.getString("movieId");
                    hasRelationship(actorsId, moviesId);
                    response = "Relationship exists in the database";
                }
                // 200 OK success response
                MovieAPI.sendResponse(exchange, response, 200);
            }
            catch (JSONException e) {
                // 400 response: body improperly formatted
                response = "400 BAD REQUEST";
                MovieAPI.sendResponse(exchange, response, 400);
                throw new RuntimeException(e);
            }
        }
        else {
            // 500 response
            String response = "500 INTERNAL SERVER ERROR";
            exchange.sendResponseHeaders(500, response.getBytes().length);
        }
    }

    // ALL GET METHODS

    private void getActor(String actorId) {
        try (Session session = driver.session()) {
            String query = "MATCH (a:actor {actorId: $actorId}) RETURN a";
            session.run(query, Values.parameters("actorId", actorId) );
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMovie(String movieId) {
        try (Session session = driver.session()) {
            String query = "MATCH (m:movie {movieId: $movieId}) RETURN m";
            session.run(query, Values.parameters("movieId", movieId) );
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hasRelationship(String actorsId, String moviesId) {
        try (Session session = driver.session()) {
            String query = "";
        }
    }
}
