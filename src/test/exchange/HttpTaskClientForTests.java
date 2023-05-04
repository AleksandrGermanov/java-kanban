package test.exchange;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpTaskClientForTests {
    private final HttpClient client = HttpClient.newHttpClient();
    private final URI httpTaskServerURI = URI.create("http://localhost:8080");

    public String sendRequest(Method METHOD, String body, String... pathParts) {
        String resp = "";
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder();
            String path = String.join("", pathParts);
            builder.uri(URI.create(httpTaskServerURI + path));
            switch (METHOD) {
                case GET:
                    HttpResponse<String> response1 = client.send(builder.GET().build(),
                            HttpResponse.BodyHandlers.ofString());
                    resp = response1.statusCode() + " " + response1.body();
                    break;
                case POST:
                    HttpRequest.BodyPublisher bp = HttpRequest.BodyPublishers.ofString(body);
                    HttpResponse<String> response2 = client.send(builder.POST(bp).build(),
                            HttpResponse.BodyHandlers.ofString());
                    resp = response2.statusCode() + " " + response2.body();
                    break;
                case DELETE:
                    HttpResponse<String> response3 = client.send(builder.DELETE().build(),
                            HttpResponse.BodyHandlers.ofString());
                    resp = response3.statusCode() + " " + response3.body();
                    break;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return resp;
    }

    public enum Method {
        GET,
        POST,
        DELETE
    }
}
