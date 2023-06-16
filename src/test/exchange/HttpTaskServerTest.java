package test.exchange;

import exchange.CustomGson;
import exchange.HttpTaskServer;
import exchange.KVServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.Statuses;
import task.SubTask;
import task.Task;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static test.exchange.HttpTaskClientForTests.Method.*;

/**
 * Класс переработан для наибольшего соответствия критериям FIRST (fast, independent,
 * repeatable, self-validating, timely) для модульных тестов по рекомендациям из книги Р. Мартина
 * "Чистый код".
 * P.S. про ордеринг прочитал, спасибо, однако ордеринг создает временную зависимость между тестами,
 * соответственно они перестают быть независимыми.
 */
public class HttpTaskServerTest {
    HttpTaskClientForTests client = new HttpTaskClientForTests();
    private KVServer kvs;
    private HttpTaskServer htServer;
    CustomGson testGson;

    @BeforeEach
    void runServers() throws IOException {
        kvs = new KVServer();
        kvs.start();
        htServer = new HttpTaskServer();
        htServer.start();
        testGson = new CustomGson(htServer.getTaskMan());
    }

    @AfterEach
    void stopServers() {
        htServer.stop();
        kvs.stop();
    }

    @Test
    void wrongPath() {
        String resp = client.sendRequest(DELETE, "", "/wrong");
        assertTrue(resp.startsWith("404"));
    }

    @Test
    void tasksGETWhenEmpty() {
        String resp = client.sendRequest(GET, "", "/tasks");
        System.out.println(resp);
        assertTrue(resp.startsWith("200"));
        assertTrue(resp.contains("\"SUBTASK:\\nТаких заданий нет.\\n\""));
        assertTrue(resp.contains("EPICTASK:\\nТаких заданий нет.\\n\""));
        assertTrue(resp.contains("TASK:\\nТаких заданий нет.\\n"));
    }

    @Test
    void tasksDELETE() {
        String resp = client.sendRequest(DELETE, "", "/tasks");
        assertEquals("202 Полностью очищено.", resp);
    }

    @Test
    void tasksPOST() {
        String resp = client.sendRequest(POST, "", "/tasks");
        assertEquals("405 Неверный запрос. Проверьте правильность метода в запросе.", resp);
    }

    @Test
    void tasksTaskGETWhenEmpty() {
        String resp = client.sendRequest(GET, "", "/tasks", "/task");
        assertEquals("200 []", resp);
    }

    @Test
    void tasksSubTaskGETWhenEmpty() {
        String resp = client.sendRequest(GET, "", "/tasks", "/subtask");
        assertEquals("200 []", resp);
    }

    @Test
    void tasksEpicGETWhenEmpty() {
        String resp = client.sendRequest(GET, "", "/tasks", "/epic");
        assertEquals("200 []", resp);
    }

    @Test
    void tasksTaskWrongPOST() {
        String resp = client.sendRequest(POST, "Круши-ломай!", "/tasks", "/task");
        assertTrue(resp.startsWith("422 Ошибка при обработке запроса:"));
    }

    @Test
    void tasksTaskPOST() {
        Task task = new Task(1, "first", Statuses.IN_PROGRESS, "of all...",
                LocalDateTime.of(2023, 4, 26, 10, 40), 10,
                LocalDateTime.of(0, 1, 1, 0, 0));
        String reqBody = testGson.getGsonForHttpManager().toJson(task);
        String resp = client.sendRequest(POST,
                reqBody,
                "/tasks", "/task");
        Task responseTask = testGson.getGsonForHttpManager().fromJson
                (client.sendRequest(GET, null, "/tasks", "/task", "?id=", "100000")
                                .substring(4), task.Task.class);
        assertTrue(resp.startsWith("201 Новая задача типа"));
        assertTrue(resp.endsWith("была создана."));
        assertNotEquals(task, responseTask);
        assertNotEquals(task.getId(), responseTask.getId());//id присваивается на стороне сервера
        assertEquals(task.getName(), responseTask.getName());
        assertEquals(task.getStatus(), responseTask.getStatus());
        assertEquals(task.getDescription(), responseTask.getDescription());
        assertEquals(task.getStartTime(), responseTask.getStartTime());
        assertEquals(task.getDuration(), responseTask.getDuration());
        assertNotEquals(task.getEndTime(), responseTask.getEndTime());
        assertEquals(task.getStartTime().plusMinutes(task.getDuration()), responseTask.getEndTime());

        String resp2 = client.sendRequest(POST,
                "{}",
                "/tasks", "/task");
        System.out.println(resp2);
        System.out.println(client.sendRequest(GET, "", "/tasks", "/task", "?id=", "100001"));
        assertTrue(resp2.startsWith("201 Новая задача типа"));
        assertTrue(resp2.endsWith("была создана."));
    }

    @Test
    void tasksEpicTaskPOST() {
        String resp1 = client.sendRequest(POST, "Круши-ломай!", "/tasks", "/epic");
        assertTrue(resp1.startsWith("422 Ошибка при обработке запроса:"));

        EpicTask epic = new EpicTask(1, "first", Statuses.IN_PROGRESS, "of all...",
                LocalDateTime.of(2023, 4, 26, 10, 40), 10,
                LocalDateTime.of(0, 1, 1, 0, 0));
        String reqBody = testGson.getGsonForHttpManager().toJson(epic);
        String resp = client.sendRequest(POST,
                reqBody,
                "/tasks", "/epic");
        Task responseTask = testGson.getGsonForHttpManager().fromJson
                (client.sendRequest(GET, null, "/tasks", "/epic", "?id=", "200000")
                        .substring(4), task.EpicTask.class);
        assertTrue(resp.startsWith("201 Новая задача типа"));
        assertTrue(resp.endsWith("была создана."));
        assertNotEquals(epic, responseTask);
        assertNotEquals(epic.getId(), responseTask.getId());
        assertEquals(epic.getName(), responseTask.getName());
        assertNotEquals(epic.getStatus(), responseTask.getStatus());
        assertEquals(Statuses.NEW, responseTask.getStatus());
        assertEquals(epic.getDescription(), responseTask.getDescription());
        assertNotEquals(epic.getStartTime(), responseTask.getStartTime());
        assertNull(responseTask.getStartTime());
        assertNotEquals(epic.getDuration(), responseTask.getDuration());
        assertNull(responseTask.getDuration());
        assertNotEquals(epic.getEndTime(), responseTask.getEndTime());
        assertNull(responseTask.getEndTime());
    }

    @Test
    void tasksSubTaskPOST() {
        String resp1 = client.sendRequest(POST,
                "{}",
                "/tasks", "/epic");
        assertTrue(resp1.startsWith("201 Новая задача типа"));
        assertTrue(resp1.endsWith("была создана."));
        EpicTask responseEpic = testGson.getGsonForHttpManager().fromJson(
                client.sendRequest(GET, null, "/tasks", "/epic", "?id=", "200000")
                        .substring(4), task.EpicTask.class);
        SubTask task = new SubTask(0, "sub", Statuses.IN_PROGRESS, "subsub",
                LocalDateTime.of(2023, 4, 26, 10, 40), 10,
                LocalDateTime.of(0, 1, 1, 0, 0), responseEpic);
        String reqBody = testGson.getGsonForHttpManager().toJson(task);
        String resp = client.sendRequest(POST,
                reqBody,
                "/tasks", "/subtask");
        SubTask responseTask = testGson.getGsonForHttpManager().fromJson
                (client.sendRequest(GET, null, "/tasks", "/subtask", "?id=", "300001")
                        .substring(4), task.SubTask.class);
        assertTrue(resp.startsWith("201 Новая задача типа"));
        assertTrue(resp.endsWith("была создана."));
        assertNotEquals(task, responseTask);
        assertNotEquals(task.getId(), responseTask.getId());
        assertEquals(task.getName(), responseTask.getName());
        assertEquals(task.getStatus(), responseTask.getStatus());
        assertEquals(task.getDescription(), responseTask.getDescription());
        assertEquals(task.getStartTime(), responseTask.getStartTime());
        assertEquals(task.getDuration(), responseTask.getDuration());
        assertNotEquals(task.getEndTime(), responseTask.getEndTime());
        assertEquals(task.getStartTime().plusMinutes(task.getDuration()), responseTask.getEndTime());
        assertEquals(task.getMyEpicId(), responseTask.getMyEpicId());
    }


    @Test
    void tasksGETWhenFull() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(GET, null, "/tasks");
        System.out.println(resp);
        assertTrue(resp.startsWith("200"));
        assertFalse(resp.contains("\"SUBTASK:\\nТаких заданий нет.\\n\""));
        assertFalse(resp.contains("EPICTASK:\\nТаких заданий нет.\\n\""));
        assertFalse(resp.contains("TASK:\\nТаких заданий нет.\\n"));
    }

    @Test
    void tasksTaskGETWhenFull() {
        String resp1 = client.sendRequest(POST,
                "{}",
                "/tasks", "/task");
        assertTrue(resp1.startsWith("201 Новая задача типа"));
        assertTrue(resp1.endsWith("была создана."));
        String resp2 = client.sendRequest(POST,
                "{}",
                "/tasks", "/task");
        assertTrue(resp2.startsWith("201 Новая задача типа"));
        assertTrue(resp2.endsWith("была создана."));

        String resp = client.sendRequest(GET, "", "/tasks", "/task");
        assertTrue(resp.startsWith("200"));
        assertNotEquals("200 []", resp);
    }

    @Test
    void tasksSubTaskGETWhenFull() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(GET, "", "/tasks", "/subtask");
        assertTrue(resp.startsWith("200"));
        assertNotEquals("200 []", resp);
    }

    @Test
    void tasksEpicGETWhenFull() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(GET, "", "/tasks", "/epic");
        assertTrue(resp.startsWith("200"));
        assertNotEquals("200 []", resp);
    }
    @Test
    void tasksPrioritizedGET() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(GET, "", "/tasks", "/prioritized");
        assertTrue(resp.startsWith("200"));
        assertNotEquals("200 []", resp);
    }

    @Test
    void tasksPrioritizedPOST() {
        String resp = client.sendRequest(POST, "", "/tasks", "/prioritized");
        assertTrue(resp.startsWith("405"));
    }

    @Test
    void tasksHistoryGET() {
        createTaskEpicAndSubTask();
        client.sendRequest(GET, "", "/tasks", "/task", "100000");

        String resp = client.sendRequest(GET, "", "/tasks", "/history");
        assertTrue(resp.startsWith("200"));
        assertNotEquals("200 []", resp);
    }

    @Test
    void tasksHistoryPOST() {
        String resp = client.sendRequest(POST, "", "/tasks", "/history");
        assertTrue(resp.startsWith("405"));
    }

    @Test
    void tasksTaskPOSTWithQuery() {
        createTaskEpicAndSubTask();

        String resp1 = client.sendRequest(POST, "Круши-ломай!", "/tasks", "/task", "?id=", "100000");
        assertTrue(resp1.startsWith("422 Ошибка при обработке запроса:"));
        String resp2 = client.sendRequest(POST,
                "{\n" + "id = 100000,"
                        + "  \"name\": \"new\",\n"
                        + "  \"status\": \"DONE\",\n"
                        + "  \"description\": \"new description\"\n"
                        + "}",
                "/tasks", "/task", "?id=", "100000");
        System.out.println(resp2);
        assertEquals("202 Обновлено.", resp2);
    }

    @Test
    void tasksEpicPOSTWithQuery() {
        createTaskEpicAndSubTask();

        String resp1 = client.sendRequest(POST, "Круши-ломай!", "/tasks", "/epic", "?id=", "200002");
        assertTrue(resp1.startsWith("422 Ошибка при обработке запроса:"));
        String resp2 = client.sendRequest(POST,
                "{\n"
                        + "  \"id\": 200001,\n"
                        + "  name: \" NEW epic1\",\n"
                        + "  status: \"NEW\",\n"
                        + "  \"description\": \"has 1 old sub\",\n"
                        + " mySubsIds: [300002]"
                        + "}",
                "/tasks", "/epic", "?id=", "200001");
        System.out.println(resp2);
        System.out.println(client.sendRequest(GET, null, "/tasks", "/epic", "?id=", "200002"));
        assertEquals("202 Обновлено.", resp2);
    }

    @Test
    void tasksSubTaskPOSTWithQuery() {
        createTaskEpicAndSubTask();

        String resp1 = client.sendRequest(POST, "Круши-ломай!", "/tasks", "/subtask", "?id=", "300002");
        assertTrue(resp1.startsWith("422 Ошибка при обработке запроса:"));
        String resp2 = client.sendRequest(POST,
                "{ name: \"sub1\","
                        + "  \"id\": 300002,\n"
                        + "description: \"only of 200001 renewed\","
                        + "status: \"DONE\","
                        + "epicId: 200001}",
                "/tasks", "/subtask", "?id=", "300002");
        System.out.println(client.sendRequest(GET, null, "/tasks", "/subtask", "?id=", "300002"));
        assertEquals("202 Обновлено.", resp2);
    }

    @Test
    void tasksTaskGETWithQuery() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(GET,
                "",
                "/tasks", "/task", "?id=", "100000");
        assertTrue(resp.startsWith("200"));
    }

    @Test
    void tasksEpicGETWithQuery() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(GET,
                "",
                "/tasks", "/epic", "?id=", "200001");
        System.out.println(resp);
        assertTrue(resp.startsWith("200"));
    }

    @Test
    void tasksEpicSubtasksGETWithQuery() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(GET,
                "",
                "/tasks", "/epic", "/subtasks", "?id=", "200001");
        System.out.println(resp);
        assertTrue(resp.startsWith("200"));
    }

    @Test
    void tasksSubTaskGETWithQuery() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(GET,
                "",
                "/tasks", "/subtask", "?id=", "300002");
        System.out.println(resp);
        assertTrue(resp.startsWith("200"));
    }

    @Test
    void tasksTaskDELETEWithQuery() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(DELETE,
                "",
                "/tasks", "/task", "?id=", "100000");
        assertEquals(resp, "202 Удалено.");
    }

    @Test
    void tasksEpicDELETEWithQuery() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(DELETE,
                "",
                "/tasks", "/epic", "?id=", "200001");
        System.out.println(resp);
        assertEquals(resp, "202 Удалено.");
    }

    @Test
    void tasksSubTaskDELETEWithQuery() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(DELETE,
                "",
                "/tasks", "/subtask", "?id=", "300002");
        System.out.println(resp);
        assertEquals(resp, "202 Удалено.");
    }

    @Test
    void tasksTaskDELETE() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(DELETE,
                "",
                "/tasks", "/task");
        assertEquals(resp, "202 Очищено.");
    }

    @Test
    void tasksEpicDELETE() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(DELETE,
                "",
                "/tasks", "/epic");
        System.out.println(resp);
        assertEquals(resp, "202 Очищено.");
    }

    @Test
    void tasksSubTaskDELETE() {
        createTaskEpicAndSubTask();

        String resp = client.sendRequest(DELETE,
                "",
                "/tasks", "/subtask");
        System.out.println(resp);
        assertEquals(resp, "202 Очищено.");
    }

    void createTaskEpicAndSubTask(){
        client.sendRequest(POST, "{}", "/tasks", "/task");
        client.sendRequest(POST, "{}", "/tasks", "/epic");
        EpicTask responseEpic = testGson.getGsonForHttpManager()
                .fromJson(client.sendRequest(GET, null, "/tasks", "/epic", "?id=", "200001")
                        .substring(4), task.EpicTask.class);
        SubTask task = new SubTask(0, "sub", Statuses.IN_PROGRESS, "subsub",
                LocalDateTime.of(2023, 4, 26, 10, 40), 10,
                LocalDateTime.of(0, 1, 1, 0, 0), responseEpic);
        String reqBody = testGson.getGsonForHttpManager().toJson(task);
        client.sendRequest(POST, reqBody, "/tasks", "/subtask");
    }
}
