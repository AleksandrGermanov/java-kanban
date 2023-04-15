package exchange;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import management.Managers;
import management.task.TaskFamily;
import management.task.TaskManager;
import task.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

import static java.nio.charset.StandardCharsets.UTF_8;


public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskMan;
    private final String apiToken;

    public HttpTaskServer(URI kvServerURI) throws IOException {
        //TODO register();
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        register(kvServerURI);
        server.createContext("/tasks", this::handleTasks);
        taskMan = Managers.getDefault();
    }

    private void register(URI uri) {
    }

    private void handleTasks(HttpExchange exchange) throws IOException {
        String key = exchange.getRequestURI().getPath().substring("/tasks/".length());
        if (key.isBlank()) {
            handleBlankKey(exchange);
        } else if (key.startsWith("task")) {
            handleTaskKey(exchange);
        } else if (key.startsWith("subtask")) {
            handleSubtaskKey(exchange);
        } else if (key.startsWith("epic")) {
            handleEpicKey(exchange);
        } else if (key.equals("history")) {
            handleHistoryKey(exchange);
        } else {
            sendResponse(exchange, 400, "Неверный запрос. Проверьте правильность URL.");
            exchange.close();
        }
    }

    private void handleBlankKey(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                taskMan.getTaskList();
                break;
            case "DELETE":
                taskMan.removeAllTasks();
                break;
            default:
                sendResponse(exchange, 400,
                        "Неверный запрос. Проверьте правильность метода в запросе.");
        }
        exchange.close();
    }

    private void handleTaskKey(HttpExchange exchange) throws IOException {
        String subKey = exchange.getRequestURI().getPath().substring("/tasks/task".length());
        if (!subKey.matches("/?(\\?id=[1-3]+[0-9]*)?")) {
            sendResponse(exchange, 400, "Неверный запрос. Проверьте правильность URL.");
            exchange.close();
            return;
        }

        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                taskMan.getTaskList();
                break;
            case "DELETE":
                taskMan.removeAllTasks();
                break;
            default:
                sendResponse(exchange, 400,
                        "Неверный запрос. Проверьте правильность метода в запросе.");
        }
        exchange.close();
    }

    private void handleSubtaskKey(HttpExchange exchange) {
    }

    private void handleEpicKey(HttpExchange exchange) {
    }

    private void handleHistoryKey(HttpExchange exchange) {
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] rBytes = message.getBytes(UTF_8);
        exchange.sendResponseHeaders(statusCode, rBytes.length);
        exchange.getResponseBody().write(rBytes);
    }

    private void handleStandartSubKey(HttpExchange exchange, TaskFamily TF) throws IOException {
        String query = exchange.getRequestURI().getRawQuery();
        String method = exchange.getRequestMethod();

        if (query == null) {
            switch (method) {
                case "GET":
                    taskMan.getTaskList(TF);
                    break;
                case "POST":
                    //TODO ЗАГЛУШКА!!!
                    String reqBody = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                    taskMan.createTask(new Task());//тут должно быть 2 параметра - reqBody и TF
                    break;
                case "DELETE":
                    taskMan.removeAllTasks(TF);
                    break;
                default:
                    sendResponse(exchange, 400,
                            "Неверный запрос. Проверьте правильность метода в запросе.");
            }
        } else {
            int id = Integer.parseInt("0");
        }

        exchange.close();
    }
}

