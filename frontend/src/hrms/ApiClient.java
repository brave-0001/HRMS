package hrms;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;

public class ApiClient {

    private static final String BASE = "http://127.0.0.1:5000/api";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static String token = "";

    public static void setToken(String t) {
        token = t;
    }

    public static String get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE + path))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    public static String post(String path, String json) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE + path))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(BodyPublishers.ofString(json))
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }
}