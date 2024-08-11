package api;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.security.ntlm.Server;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.List;


public class MovieAPI {

    private final Driver driver;

    public MovieAPI(Driver driver) throws IOException {
        this.driver = driver;
    }

    // Read all request body io streams into a string that can be parsed with json conversion
    public String readRequestBody(InputStream inputStream) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            requestBody.append(new String(buffer, 0, bytesRead));
        }
        return requestBody.toString();
    }

    // Sends the given response and response code back
    public void sendResponse(HttpExchange exchange, String response, int rCode) throws IOException {
        exchange.sendResponseHeaders(rCode, response.getBytes().length);
        // Set the content type header to json format
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        // Write the response body to the output stream
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    public String returnRequestURI(HttpExchange exchange) {
        return exchange.getRequestURI().toString();
    }

    // Get an actor from the database
    public void getActor(String actorId, HttpExchange ex) throws IOException {
        try (Session session = driver.session()) {
            String query = "MATCH (a:Actor {actorId: $actorId}) OPTIONAL MATCH (a)-[:ACTED_IN]->(m:Movie) RETURN a.actorId AS actorId, a.name AS name, COLLECT(m.movieId) AS movies";
            StatementResult sr = session.writeTransaction(tx -> tx.run(query, Values.parameters("actorId", actorId)));
            session.close();
            if (sr.hasNext()) {
                Record record = sr.next();
                String actorIdRecord = record.get("actorId").asString();
                System.out.println(actorIdRecord);
                String name = record.get("name").asString();
                System.out.println(name);
                List<String> movies = record.get("movies").asList(Value::asString);
                System.out.println(movies);

                String response = String.format("{\"actorId\":\"%s\",\"name\":\"%s\",\"movies\":%s}", actorIdRecord, name, movies.isEmpty() ? "[]" : movies);
                sendResponse(ex, response, 200);
            }
            else {
                sendResponse(ex, "Actor not found with specified actorId in the database.", 404);
            }
        }
        catch (ServerException e) {
            String errorResponse = "Internal server or database error";
            sendResponse(ex, errorResponse, 500);
            throw new ServerException(e.getMessage());
        }

    }

    // Get a movie from the DB
    public void getMovie(String movieId, HttpExchange ex) throws IOException {
        try (Session session = driver.session()){
            String query = "MATCH (m:Movie {movieId: $movieId}) OPTIONAL MATCH (m)<-[:ACTED_IN]-(a:Actor) RETURN m.movieId AS movieId, m.name AS mName, COLLECT(a.actorId) AS actors";;
            StatementResult sr = session.writeTransaction(tx -> tx.run(query, Values.parameters("movieId", movieId)));
            if (sr.hasNext()) {
                Record record = sr.next();
                String movieIdResult = record.get("movieId").asString();
                String mName = record.get("mName").toString();
                List<Object> actors = record.get("actors").asList(Value::asString);
                String response = String.format(
                        "{\"actorId\":\"%s\",\"name\":%s,\"movies\":%s}",
                        movieIdResult, mName, actors.isEmpty() ? "[]" : actors
                );
                sendResponse(ex, response, 200);
            }
            else {
                sendResponse(ex, "Movie not found with specified movieId in the database.", 404);
            }
        }
        catch (ServerException e) {
            String errorResponse = "Internal server or database error";
            sendResponse(ex, errorResponse, 500);
            throw new ServerException(e.getMessage());
        }
    }

    // Check if a relationship exists between an actor and a movie
    public void hasRelationship(String actorId, String movieId, HttpExchange ex) throws IOException {
        try (Session session = driver.session()){
            String query = "MATCH (a:Actor {actorId: $actorId})-[r:ACTED_IN]->(m:Movie {movieId: $movieId}) RETURN a AS aNode, m AS mNode, EXISTS(r) AS relationshipExists";
            StatementResult sr = session.writeTransaction(tx -> tx.run(query, Values.parameters("actorId", actorId, "movieId", movieId)));
            if (sr.hasNext()) {
                boolean hasRelationship = sr.next().get("relationshipExists").asBoolean();
                String response = String.format(
                        "{\"actorId\":\"%s\",\"movieId\":\"%s\",\"hasRelationship\":%b}",
                        actorId, movieId, hasRelationship
                );
                sendResponse(ex, String.valueOf(new JSONObject(response)), 200);
            }
            else {
                sendResponse(ex, "Movie and/or Actor not found with the specified actorId and movieId in the database", 404);
            }
        }
        catch (JSONException e) {
            String errorResponse = "Internal server or database error";
            sendResponse(ex, errorResponse, 500);
            throw new ServerException(e.getMessage());
        }
    }

    //  ALL PUT METHODS

    // Add an actor to the db
    public void addActor(String name, String actorId, HttpExchange ex) throws IOException {
        String response;
        try (Session session1 = driver.session()) {
            String checkActorQuery = "MATCH (a:Actor {actorId: $actorId}) RETURN a";
            StatementResult srCheck = session1.writeTransaction(tx -> tx.run(checkActorQuery, Values.parameters("actorId", actorId)));
            session1.close();
            // Check if a duplicate actor node exists and return appropriate response
            if (srCheck.hasNext()) {
                response = "Actor already exists in the movie database";
                sendResponse(ex, response, 400);
            }
            else {
                String query = "MERGE (a:Actor {actorId: $actorId}) ON CREATE SET a.name = $name RETURN a";
                Session session2 = driver.session();
                session2.writeTransaction(tx -> tx.run(query, Values.parameters("actorId", actorId, "name", name)));
                session2.close();
                response = "Actor successfully added to the movie database";
                sendResponse(ex, response, 200);
            }
        } catch (ServerException e) {
            response = "Internal server or database error";
            sendResponse(ex, response, 500);
            throw new ServerException(e.getMessage());
        }
    }

    // Add a movie to the db
    public void addMovie(String name, String movieId, HttpExchange ex) throws IOException {
        String response;
        try (Session session1 = driver.session()){
            String checkMovieQuery = "MATCH (m:Movie {movieId: $movieId}) RETURN m";
            StatementResult srCheck = session1.writeTransaction(tx -> tx.run(checkMovieQuery, Values.parameters("movieId", movieId)));
            session1.close();
            // Check if a duplicate movie node exists and return appropriate response
            if (srCheck.hasNext()) {
                response = "Movie already exists in the movie database";
                sendResponse(ex, response, 400);
            }
            else {
                String query = "MERGE (m:Movie {movieId: $movieId}) ON CREATE SET m.name = $name RETURN m";
                Session session2 = driver.session();
                session2.writeTransaction(tx -> tx.run(query, Values.parameters("name", name, "movieId", movieId)));
                session2.close();
                response = "Movie successfully added to the movie database";
                sendResponse(ex, response, 200);
            }
        }
        catch (ServerException e) {
            response = "Internal server or database error";
            sendResponse(ex, response, 500);
            throw new ServerException(e.getMessage());
        }
    }

    // Add a relationship between actor and movie to the db
    public void addRelationship(String actorId, String movieId, HttpExchange ex) throws IOException {
        String response;
        try (Session session = driver.session()){
            String query = "MATCH (a:Actor {actorId: $actorId}), (m:Movie {movieId: $movieId}) MERGE (a)-[r:ACTED_IN]->(m) " +
                    "RETURN a AS aNode, m AS mNode, EXISTS((a)-[:ACTED_IN]->(m)) AS relationshipExists";
            StatementResult sr = session.writeTransaction(tx -> tx.run(query, Values.parameters("actorId", actorId, "movieId", movieId)));
            // retrieve the nodes and check for their existence in the database
            boolean relationshipExists = sr.next().get("relationshipExists").asBoolean();
            boolean actorExists = sr.next().get("aNode").asBoolean();
            boolean movieExists = sr.next().get("mNode").asBoolean();
            if (!actorExists || !movieExists) {
                response = "Either the actor or movie does not exist in the database";
                sendResponse(ex, response, 404);
            }
            else {
                if (relationshipExists) {
                    response = "Relationship already exists in the movie database";
                    sendResponse(ex, response, 400);
                }
                else {
                    response = "Relationship successfully added to the movie database";
                    sendResponse(ex, response, 200);
                }
            }
        }
        catch (ServerException e) {
            response = "Internal server or database error";
            sendResponse(ex, response, 500);
            throw new ServerException(e.getMessage());
        }
    }

    public void computeBaconNumber(String actorId, HttpExchange ex) throws IOException {
        String response;
        String query = "MATCH (k:KB {name: 'Kevin Bacon'})-[:ACTED_IN]-(movie:Movie)-[:ACTED_IN]-(a:Actor {name: $target}) " +
                       "WITH k, a, shortestPath((k)-[:ACTED_IN*]-(target)) AS path RETURN CASE WHEN path IS NOT NULL THEN {baconNumber: length(nodes(path)) - 1} ELSE {baconNumber: 0} END AS result";
        try (Session session = driver.session()){
            StatementResult sr = session.writeTransaction(tx -> tx.run(query, Values.parameters("name", actorId)));
            if (sr.hasNext()) {
                response = sr.next().get("result").asString();
                sendResponse(ex, response, 200);
            }
            else {
                response = "No results found";
                sendResponse(ex, response, 404);
            }
        }
        catch (ServerException e) {
            response = "Internal server or database error";
            sendResponse(ex, response, 500);
            throw new ServerException(e.getMessage());
        }

    }

    public void computeBaconPath(String actorId, HttpExchange ex) throws IOException {
        String response;
        String query = "MATCH (k:KB {name: 'Kevin Bacon'})-[:ACTED_IN]-(movie:Movie)-[:ACTED_IN]-(a:Actor {name: $target}) " +
                "WITH k, a, shortestPath((k)-[:ACTED_IN*]-(target)) AS path RETURN path AS result";
        try (Session session = driver.session()){
            StatementResult sr = session.writeTransaction(tx -> tx.run(query, Values.parameters("name", actorId)));
            ArrayList<String> baconPathIds = new ArrayList<String>();
            if (sr.hasNext()) {
                while (sr.hasNext()) {
                    baconPathIds.add(sr.next().get("path").get("actorId").asString());
                }
                response = String.format("{\"baconPath\": %s}", baconPathIds);
                sendResponse(ex, String.valueOf(new JSONObject(response)), 200);
            }
            else {
                response = "No path exists";
                sendResponse(ex, response, 404);
            }
        }
        catch (JSONException e) {
            response = "Internal server or database error";
            sendResponse(ex, response, 500);
            throw new ServerException(e.getMessage());
        }

    }
}
