package ca.yorku.eecs;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.AuthTokens;
import apiservice.GetHandler;
import apiservice.PutHandler;
import apiservice.MovieAPI;

public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

        Config config = Config.builder().withoutEncryption().build();
        Driver driver = GraphDatabase.driver(
                "bolt://localhost:7687",
                AuthTokens.basic("neo4j", "12345678"),
                config);
        // instantiate API singleton wrapper class with helper methods (contains one instance of db driver)
        MovieAPI movieAPI = new MovieAPI(driver);
        // create the put http contexts with endpoints and handlers
        server.createContext("/api/v1/addActor", new PutHandler(movieAPI.driver));
        server.createContext("/api/v1/addMovie", new PutHandler(movieAPI.driver));
        server.createContext("/api/v1/addRelationship", new PutHandler(movieAPI.driver));
        // create the get http contexts with endpoints and handlers
        server.createContext("/api/v1/getActor", new GetHandler(movieAPI.driver));
        server.createContext("/api/v1/getMovie", new GetHandler(movieAPI.driver));
        server.createContext("/api/v1/hasRelationship", new GetHandler(movieAPI.driver));

        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
        driver.session();
    }
}
