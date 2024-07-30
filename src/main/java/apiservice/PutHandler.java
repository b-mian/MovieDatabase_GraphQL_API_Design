package apiservice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Values;

import java.io.IOException;

public class PutHandler implements HttpHandler {

    private final Driver driver;

    public PutHandler(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("PUT".equals(exchange.getRequestMethod())) {
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
                if (requestURI.equals("/api/v1/addActor")) {
                    String actorsName = json.getString("name");
                    String actorsId = json.getString("actorId");
                    addActor(actorsName, actorsId);
                    response = "Actor successfully added to the movie database";
                }
                else if (requestURI.equals("/api/v1/addMovie")) {
                    String moviesName = json.getString("name");
                    String moviesId = json.getString("movieId");
                    addMovie(moviesName, moviesId);
                    response = "Movie successfully added to the movie database";
                }
                else if (requestURI.equals("/api/v1/addRelationship")) {
                    String actorsId = json.getString("actorId");
                    String moviesId = json.getString("movieId");
                    addRelationship(actorsId, moviesId);
                    response = "Relationship successfully added to the movie database";
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

    //  ALL PUT METHODS
    // Add an actor to the db
    private void addActor(String name, String actorId) {
        try (Session session = driver.session()) {
            String query = "CREATE (a:actor {name: $name, actorId: $actorId})";
            session.writeTransaction(tx -> tx.run(query, Values.parameters("name", name, "actorId", actorId)));
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Add a movie to the db
    private void addMovie(String name, String movieId) {
        try (Session session = driver.session()) {
            String query = "CREATE (m:movie {name: $name, movieId: $movieId})";
            session.run(query, Values.parameters("name", name, "actorId", movieId) );
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Add a relationship between actor and movie to the db
    private void addRelationship(String actorId, String movieId) {
        try (Session session = driver.session()) {
            String query = "CREATE (a:actor {actorId: $actorId, movieId: $movieId})";
            session.run(query, Values.parameters("actorId", actorId, "movieId", movieId) );
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
