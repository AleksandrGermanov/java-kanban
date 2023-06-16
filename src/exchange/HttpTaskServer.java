package exchange;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import management.Managers;
import management.task.HttpTaskManager;
import management.task.TaskFamily;
import management.task.TaskManager;
import myExceptions.ManagerIOException;
import myExceptions.NoMatchesFoundException;
import task.EpicTask;
import task.SubTask;
import task.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static management.task.TaskFamily.TASK;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskMan;
    private final CustomGson customGson;

    public HttpTaskServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", this::handleTasks);
        taskMan = Managers.getDefault();
        customGson = new CustomGson(taskMan);
    }

    public TaskManager getTaskMan() {
        return taskMan;
    }

    public void start() {
        System.out.println("Сервер стартовал на порту " + PORT);
        server.start();
    }

    public void stop() {
        server.stop(1);
        System.out.println("Останавливаем сервер.");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] messageBytes = message.getBytes(UTF_8);
        exchange.sendResponseHeaders(statusCode, messageBytes.length);
        exchange.getResponseBody().write(messageBytes);
    }

    private void handleTasks(HttpExchange exchange) throws IOException {
        try {
            String key = exchange.getRequestURI().getPath().substring("/tasks".length());
            if (key.isBlank() || key.equals("/")) {
                handleBlankKey(exchange);
            } else if (key.startsWith("/task")) {
                handleTaskFamilyKey(exchange, TASK);
            } else if (key.startsWith("/subtask")) {
                handleTaskFamilyKey(exchange, TaskFamily.SUBTASK);
            } else if (key.startsWith("/epic")) {
                if (key.startsWith("/epic/subtasks") && "GET".equals(exchange.getRequestMethod())) {
                    handleEpicSubtasksGET(exchange);
                }else {
                    handleStandardSubKey(exchange, TaskFamily.EPICTASK);
                }
            } else if (key.matches("/history/?")) {
                handleHistoryKey(exchange);
            } else if (key.matches("/prioritized/?")) {
                handlePrioritizedKey(exchange);
            } else {
                sendResponse(exchange, 400, "Неверный запрос. Проверьте правильность URL.");
            }
        } catch (ManagerIOException e) {
            sendResponse(exchange, 500, "Ошибка загрузки/сохранения данных на сервере "
                    + "для сохранения данных.");
            e.printStackTrace();
        } catch (IOException e) {
            sendResponse(exchange, 500, "Ошибка загрузки/сохранения данных на операционном сервере.");
            e.printStackTrace();
        } catch (NoMatchesFoundException e) {
            sendResponse(exchange, 422, "Ошибка при обработке запроса: "
                    + "несоответствие сохраненных и переданных/запрошенных данных.");
            e.printStackTrace();
        } catch (JsonParseException e) {
            sendResponse(exchange, 422, "Ошибка при обработке запроса: "
                    + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
        sendResponse(exchange, 500, "Непредвиденная ошибка сервера.");
        e.printStackTrace();
    }finally {
            exchange.close();
        }

    }

    private void handleBlankKey(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                Type listOfStrings = new TypeToken<List<String>>() {
                }.getType();
                String gson = CustomGson.getSimplePrettyGson().toJson(taskMan.getTaskList(), listOfStrings);
                exchange.getResponseHeaders().add("Context-type", "application/json");
                sendResponse(exchange, 200, gson);
                break;
            case "DELETE":
                taskMan.removeAllTasks();
                sendResponse(exchange, 202, "Полностью очищено.");
                break;
            default:
                sendResponse(exchange, 405,
                        "Неверный запрос. Проверьте правильность метода в запросе.");
        }
    }

    private void handleTaskFamilyKey(HttpExchange exchange, TaskFamily TF) throws IOException {
        String subKey = exchange
                .getRequestURI()
                .getPath()
                .substring((String.format("/tasks/%s", TF.name())).length());
        String regex = String.format("/?(\\?id=%d+[0-9]*)?", TF.ordinal() + 1);

        if (!subKey.matches(regex)) {
            sendResponse(exchange, 400, "Неверный запрос. Проверьте правильность URL.");
            return;
        }
        handleStandardSubKey(exchange, TF);
    }

    private void handleStandardSubKey(HttpExchange exchange, TaskFamily TF) throws IOException {
        String query = exchange.getRequestURI().getRawQuery();
        String method = exchange.getRequestMethod();

        if (query == null) {
            handleEmptyQuery(exchange, TF, method);
        } else {
            handleQuery(exchange, TF, query, method);
        }
    }

    private void handleQuery(HttpExchange exchange, TaskFamily TF, String query, String method) throws IOException {
        try {
            int id = Integer.parseInt(query.substring(
                    query.indexOf(String.valueOf(TF.ordinal() + 1))));
            if (taskMan.getTask(id) == null) {
                sendResponse(exchange, 404, "Проверьте правильность ID.");
                return;
            }
            switch (method) {
                case "GET":
                    handleQueryGET(exchange, TF, id);
                    break;
                case "POST":
                    handleQueryPOST(exchange, TF);
                    break;
                case "DELETE":
                    handleQueryDELETE(exchange, id);
                    break;
                default:
                    sendResponse(exchange, 405,
                            "Неверный запрос. Проверьте правильность метода в запросе.");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400,
                    "Неверный запрос. Невозможно прочитать ID");
        }
    }

    private void handleQueryDELETE(HttpExchange exchange, int id) throws IOException {
        taskMan.removeTask(id);
        sendResponse(exchange, 202, "Удалено.");
    }

    private void handleQueryPOST(HttpExchange exchange, TaskFamily TF) throws IOException {
        String gsonIn = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
        switch (TF) {
            case TASK:
                Task task = customGson.getGsonForHttpManager().fromJson(gsonIn, Task.class);
                taskMan.renewTask(task);
                break;
            case SUBTASK:
                SubTask sub = customGson.getGsonForHttpManager().fromJson(gsonIn, SubTask.class);
                taskMan.renewTask(sub);
                break;
            case EPICTASK:
                EpicTask epic = customGson.getGsonForHttpManager().fromJson(gsonIn, EpicTask.class);
                taskMan.renewTask(epic);
                break;
        }
        sendResponse(exchange, 202, "Обновлено.");
    }

    private void handleQueryGET(HttpExchange exchange, TaskFamily TF, int id) throws IOException {
        Type taskType = Task.class;
        switch (TF) {
            case SUBTASK:
                taskType = SubTask.class;
                break;
            case EPICTASK:
                taskType = EpicTask.class;
                break;
        }
        String gsonTaskOut = customGson.getGsonForHttpManager()
                .toJson(taskMan.getTask(id), taskType);
        exchange.getResponseHeaders().add("Context-type", "application/json");
        sendResponse(exchange, 200, gsonTaskOut);
    }

    private void handleEmptyQuery(HttpExchange exchange, TaskFamily TF, String method) throws IOException {
        switch (method) {
            case "GET":
                handleEmptyQueryGET(exchange, TF);
                break;
            case "POST":
                handleEmptyQueryPOST(exchange, TF);
                break;
            case "DELETE":
                handleEmptyQueryDELETE(exchange, TF);
                break;
            default:
                sendResponse(exchange, 405,
                        "Неверный запрос. Проверьте правильность метода в запросе.");
        }
    }

    private void handleEmptyQueryDELETE(HttpExchange exchange, TaskFamily TF) throws IOException {
        taskMan.removeAllTasks(TF);
        sendResponse(exchange, 202, "Очищено.");
    }

    private void handleEmptyQueryGET(HttpExchange exchange, TaskFamily TF) throws IOException {
        Type listOfStrings = new TypeToken<List<String>>() {
        }.getType();
        String gsonOut = CustomGson.getSimplePrettyGson().toJson(taskMan.getTaskList(TF), listOfStrings);
        exchange.getResponseHeaders().add("Context-type", "application/json");
        sendResponse(exchange, 200, gsonOut);
    }

    private void handleEmptyQueryPOST(HttpExchange exchange, TaskFamily TF) throws IOException {
        int newId = 0;
        String gsonIn = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
        switch (TF) {
            case TASK:
                Task task = customGson.getGsonForHttpManager().fromJson(gsonIn, Task.class);
                ((HttpTaskManager) taskMan).setTime(task.getStartTime(), task.getDuration(), task);
                taskMan.createTask(task);
                newId = task.getId();
                break;
            case SUBTASK:
                SubTask sub = customGson.getGsonForHttpManager().fromJson(gsonIn, SubTask.class);
                taskMan.createTask(sub);
                ((HttpTaskManager) taskMan).setTime(sub.getStartTime(), sub.getDuration(), sub);
                newId = sub.getId();
                break;
            case EPICTASK:
                EpicTask epic = customGson.getGsonForHttpManager().fromJson(gsonIn, EpicTask.class);
                taskMan.createTask(epic);
                epic.setTime();
                newId = epic.getId();
                break;
        }
        sendResponse(exchange, 201, "Новая задача типа "
                + TF + " с id " + newId + " была создана.");
    }

    private void handleEpicSubtasksGET(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        int id = Integer.parseInt(query.substring(
                query.indexOf(String.valueOf(TaskFamily.EPICTASK.ordinal() + 1))));
        Type listOfStrings = new TypeToken<List<String>>() {
        }.getType();
        String gson = CustomGson.getSimplePrettyGson().toJson(taskMan.getEpicSubsList(taskMan.getTask(id))
                , listOfStrings);
        sendResponse(exchange, 200, gson);
    }

    private void handleHistoryKey(HttpExchange exchange) throws IOException {
        if(!"GET".equals(exchange.getRequestMethod())){
            sendResponse(exchange, 405,
                    "Неверный запрос. Проверьте правильность метода в запросе.");
            return;
        }
        Type listOfStrings = new TypeToken<List<String>>() {
        }.getType();
        String gsonOut = customGson.getGsonForHttpManager().toJson(taskMan.getHistory(), listOfStrings);
        exchange.getResponseHeaders().add("Context-type", "application/json");
        sendResponse(exchange, 200, gsonOut);
    }

    private void handlePrioritizedKey(HttpExchange exchange) throws IOException {
        if(!"GET".equals(exchange.getRequestMethod())){
            sendResponse(exchange, 405,
                    "Неверный запрос. Проверьте правильность метода в запросе.");
            return;
        }
        Type listOfStrings = new TypeToken<List<String>>() {
        }.getType();
        String gsonOut = customGson.getGsonForHttpManager().toJson(taskMan.getPrioritizedTasks(), listOfStrings);
        exchange.getResponseHeaders().add("Context-type", "application/json");
        sendResponse(exchange, 200, gsonOut);
    }
}



