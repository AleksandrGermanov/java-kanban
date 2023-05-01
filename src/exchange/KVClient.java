package exchange;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVClient {
    private String apiToken;
    private final HttpClient client = HttpClient.newHttpClient();
    private final URI kvServerURI;

    public KVClient(URI kvServerURI) throws IOException, InterruptedException {
        this.kvServerURI = kvServerURI;
        register(kvServerURI);
    }

    private void register(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "register"))
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        apiToken = response.body();
    }

    public String loadState() throws IOException, InterruptedException {
        String stateURIstring = kvServerURI.toString() + "load/state?API_TOKEN=" + apiToken;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stateURIstring))
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public void saveState(String body) throws IOException, InterruptedException {
        String stateURIstring = kvServerURI.toString() + "save/state?API_TOKEN=" + apiToken;
        HttpRequest.BodyPublisher bp = HttpRequest.BodyPublishers.ofString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stateURIstring))
                .POST(bp).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response);
    }
}
