package apiservice;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.v1.*;

import java.io.InputStream;

public class MovieAPI {

    public Driver driver;

    public MovieAPI(Driver driver) throws IOException {
        this.driver = driver;
    }

    // Read all request body io streams into a string that can be parsed with json conversion
    static String readRequestBody(InputStream inputStream) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            requestBody.append(new String(buffer, 0, bytesRead));
        }
        return requestBody.toString();
    }

    // Sends the given response and response code back
    static void sendResponse(HttpExchange exchange, String response, int rCode) throws IOException {
        exchange.sendResponseHeaders(rCode, response.length());
    }

    static String returnRequestURI(HttpExchange exchange) throws IOException {
        return exchange.getRequestURI().toString();
    }
}
