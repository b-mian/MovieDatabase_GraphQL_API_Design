package ca.yorku.eecs;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.AuthTokens;
import api.MovieAPIHandler;

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

        MovieAPIHandler API = new MovieAPIHandler(driver);
        // create the put http contexts with endpoints and handlers
        server.createContext("/api/v1/addActor", API);
        server.createContext("/api/v1/addMovie", API);
        server.createContext("/api/v1/addRelationship", API);
        // create the get http contexts with endpoints and handlers
        server.createContext("/api/v1/getActor", API);
        server.createContext("/api/v1/getMovie", API);
        server.createContext("/api/v1/hasRelationship", API);
        server.createContext("/api/v1/computeBaconNumber", API);
        server.createContext("/api/v1/computeBaconPath", API);

        server.start();
        System.out.printf("Server started on port %d...\n", PORT);

    }
}
