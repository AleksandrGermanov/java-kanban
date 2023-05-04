package test.exchange;

import exchange.HttpTaskServer;
import exchange.KVServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static test.exchange.HttpTaskClientForTests.Method.*;

public class HttpTaskServerTest {
    HttpTaskClientForTests client = new HttpTaskClientForTests();

    static KVServer kvs;

    static HttpTaskServer htServer;

    @BeforeAll
    static void runServers() throws IOException {
        kvs = new KVServer();
        kvs.start();
        htServer = new HttpTaskServer();
        htServer.start();
    }

    @AfterAll
    static void stopServers() {
        htServer.stop();
        kvs.stop();
    }

    @Test
    void accumulate() { //тесты собраны под одной аннотацией, т.к. важен порядок их выполнения.
        wrongPath();
        tasksGETWhenEmpty();
        tasksDELETEWhenEmpty();
        tasksPOST();
        tasksTaskGETWhenEmpty();
        tasksSubTaskGETWhenEmpty();
        tasksEpicGETWhenEmpty();
        tasksTaskPOST();
        tasksEpicTaskPOST();
        tasksSubTaskPOST();
        tasksGETWhenFull();
        tasksTaskGETWhenFull();
        tasksSubTaskGETWhenFull();
        tasksEpicGETWhenFull();
        tasksPrioritizedGET();
        tasksPrioritizedPOST();
        tasksHistoryGET();
        tasksHistoryPOST();
        tasksTaskPOSTWithQuery();
        tasksEpicPOSTWithQuery();
        tasksSubTaskPOSTWithQuery();
        tasksTaskGETWithQuery();
        tasksEpicGETWithQuery();
        tasksEpicSubtasksGETWithQuery();
        tasksSubTaskGETWithQuery();
        tasksTaskDELETEWithQuery();
        tasksEpicDELETEWithQuery();
        tasksSubTaskDELETEWithQuery();
        tasksTaskDELETE();
        tasksEpicDELETE();
        tasksSubTaskDELETE();
        tasksDELETE();
    }

    void wrongPath() {
        String resp = client.sendRequest(DELETE, "", "/wrong");
        assertTrue(resp.startsWith("404"));
    }


    void tasksGETWhenEmpty() {
        String resp = client.sendRequest(GET, "", "/tasks");
        System.out.println(resp);
        assertTrue(resp.startsWith("200"));
        assertTrue(resp.contains("\"SUBTASK:\\nТаких заданий нет.\\n\""));
        assertTrue(resp.contains("EPICTASK:\\nТаких заданий нет.\\n\""));
        assertTrue(resp.contains("TASK:\\nТаких заданий нет.\\n"));
    }


    void tasksDELETEWhenEmpty() {
        String resp = client.sendRequest(DELETE, "", "/tasks");
        assertEquals("202 Полностью очищено.", resp);
    }


    void tasksPOST() {
        String resp = client.sendRequest(POST, "", "/tasks");
        assertEquals("405 Неверный запрос. Проверьте правильность метода в запросе.", resp);
    }


    void tasksTaskGETWhenEmpty() {
        String resp = client.sendRequest(GET, "", "/tasks", "/task");
        assertEquals("200 []", resp);
    }


    void tasksSubTaskGETWhenEmpty() {
        String resp = client.sendRequest(GET, "", "/tasks", "/subtask");
        assertEquals("200 []", resp);
    }


    void tasksEpicGETWhenEmpty() {
        String resp = client.sendRequest(GET, "", "/tasks", "/epic");
        assertEquals("200 []", resp);
    }


    void tasksTaskPOST() {
        String resp1 = client.sendRequest(POST, "Круши-ломай!", "/tasks", "/task");
        assertTrue(resp1.startsWith("422 Ошибка при обработке запроса:"));
        String resp2 = client.sendRequest(POST,
                "{\n"
                        + "  \"id\": 100000,\n"
                        + "  \"name\": \"first\",\n"
                        + "  \"status\": \"IN_PROGRESS\",\n"
                        + "  \"description\": \"of all...\",\n"
                        + "  \"startTime\": \"26.04.2023 10:40\",\n"
                        + "  \"duration\": 10,\n"
                        + "  \"endTime\": \"01.10.1000 00:01\"\n"
                        + "}",
                "/tasks", "/task");
        System.out.println(resp2);
        System.out.println(client.sendRequest(GET, null, "/tasks", "/task", "?id=", "100000"));
        assertTrue(resp2.startsWith("201 Новая задача типа"));
        assertTrue(resp2.endsWith("была создана."));
        String resp3 = client.sendRequest(POST,
                "{}",
                "/tasks", "/task");
        System.out.println(resp3);
        System.out.println(client.sendRequest(GET, null, "/tasks", "/task", "?id=", "100001"));
        assertTrue(resp3.startsWith("201 Новая задача типа"));
        assertTrue(resp3.endsWith("была создана."));
    }

    void tasksEpicTaskPOST() {
        String resp1 = client.sendRequest(POST, "Круши-ломай!", "/tasks", "/epic");
        assertTrue(resp1.startsWith("422 Ошибка при обработке запроса:"));
        String resp2 = client.sendRequest(POST,
                "{\n"
                        + "  name: \"epic1\",\n"
                        + "  status: \"NEW\",\n"
                        + "  \"description\": \"has 2 subs\"\n"
                        + "}",
                "/tasks", "/epic");
        System.out.println(resp2);
        System.out.println(client.sendRequest(GET, null, "/tasks", "/epic", "?id=", "200002"));
        assertTrue(resp2.startsWith("201 Новая задача типа"));
        assertTrue(resp2.endsWith("была создана."));
        String resp3 = client.sendRequest(POST,
                "{name: epic2}",
                "/tasks", "/epic");
        System.out.println(resp3);
        System.out.println(client.sendRequest(GET, null, "/tasks", "/epic", "?id=", "200003"));
        assertTrue(resp3.startsWith("201 Новая задача типа"));
        assertTrue(resp3.endsWith("была создана."));
    }

    void tasksSubTaskPOST() {
        String resp4 = client.sendRequest(POST, "Круши-ломай!", "/tasks", "/subtask");
        assertTrue(resp4.startsWith("422 Ошибка при обработке запроса:"));
        String resp5 = client.sendRequest(POST,
                "{ name: \"sub1\","
                        + "description: \"1st of 200002\","
                        + "epicId: 200002}",
                "/tasks", "/subtask");
        System.out.println(client.sendRequest(GET, null, "/tasks", "/subtask", "/?id=", "300004"));
        assertTrue(resp5.startsWith("201 Новая задача типа"));
        assertTrue(resp5.endsWith("была создана."));
        String resp6 = client.sendRequest(POST,
                "{\n"
                        + "  \"name\": \"subWithTime\",\n"
                        + "  \"status\": \"IN_PROGRESS\",\n"
                        + "  \"description\": \"descOfSubWithTime\",\n"
                        + "  \"startTime\": \"26.04.2022 10:40\",\n"
                        + "  \"duration\": 10,\n"
                        + "  \"endTime\": \"null\",\n"
                        + "epicId: 200002}",
                "/tasks", "/subtask");
        System.out.println(resp6);
        System.out.println(client.sendRequest(GET, null, "/tasks", "/subtask", "/?id=", "300005"));
        assertTrue(resp6.startsWith("201 Новая задача типа"));
        assertTrue(resp6.endsWith("была создана."));
    }

    void tasksGETWhenFull() {
        String resp = client.sendRequest(GET, "", "/tasks");
        System.out.println(resp);
        assertTrue(resp.startsWith("200"));
        assertFalse(resp.contains("\"SUBTASK:\\nТаких заданий нет.\\n\""));
        assertFalse(resp.contains("EPICTASK:\\nТаких заданий нет.\\n\""));
        assertFalse(resp.contains("TASK:\\nТаких заданий нет.\\n"));
    }

    void tasksTaskGETWhenFull() {
        String resp = client.sendRequest(GET, "", "/tasks", "/task");
        assertTrue(resp.startsWith("200"));
        assertNotEquals("200 []", resp);
    }


    void tasksSubTaskGETWhenFull() {
        String resp = client.sendRequest(GET, "", "/tasks", "/subtask");
        assertTrue(resp.startsWith("200"));
        assertNotEquals("200 []", resp);
    }


    void tasksEpicGETWhenFull() {
        String resp = client.sendRequest(GET, "", "/tasks", "/epic");
        assertTrue(resp.startsWith("200"));
        assertNotEquals("200 []", resp);
    }

    void tasksPrioritizedGET() {
        String resp = client.sendRequest(GET, "", "/tasks", "/prioritized");
        assertTrue(resp.startsWith("200"));
        assertNotEquals("200 []", resp);
    }

    void tasksPrioritizedPOST() {
        String resp = client.sendRequest(POST, "", "/tasks", "/prioritized");
        assertTrue(resp.startsWith("405"));
    }

    void tasksHistoryGET() {
        String resp = client.sendRequest(GET, "", "/tasks", "/history");
        assertTrue(resp.startsWith("200"));
        assertNotEquals("200 []", resp);
    }

    void tasksHistoryPOST() {
        String resp = client.sendRequest(POST, "", "/tasks", "/history");
        assertTrue(resp.startsWith("405"));
    }

    void tasksTaskPOSTWithQuery() {
        String resp1 = client.sendRequest(POST, "Круши-ломай!", "/tasks", "/task", "?id=", "100001");
        assertTrue(resp1.startsWith("422 Ошибка при обработке запроса:"));
        String resp2 = client.sendRequest(POST,
                "{\n" + "id = 100001,"
                        + "  \"name\": \"new\",\n"
                        + "  \"status\": \"DONE\",\n"
                        + "  \"description\": \"new description\"\n"
                        + "}",
                "/tasks", "/task", "?id=", "100001");
        System.out.println(resp2);
        assertEquals("202 Обновлено.", resp2);
    }

    void tasksEpicPOSTWithQuery() {
        String resp1 = client.sendRequest(POST, "Круши-ломай!", "/tasks", "/epic", "?id=", "200002");
        assertTrue(resp1.startsWith("422 Ошибка при обработке запроса:"));
        String resp2 = client.sendRequest(POST,
                "{\n"
                        + "  \"id\": 200002,\n"
                        + "  name: \" NEW epic1\",\n"
                        + "  status: \"NEW\",\n"
                        + "  \"description\": \"has 2 old subs\",\n"
                        + " mySubsIds: [300004, 300005]"
                        + "}",
                "/tasks", "/epic", "?id=", "200002");
        System.out.println(resp2);
        System.out.println(client.sendRequest(GET, null, "/tasks", "/epic", "?id=", "200002"));
        assertEquals("202 Обновлено.", resp2);
    }

    void tasksSubTaskPOSTWithQuery() {
        String resp1 = client.sendRequest(POST, "Круши-ломай!", "/tasks", "/subtask", "?id=", "300004");
        assertTrue(resp1.startsWith("422 Ошибка при обработке запроса:"));
        String resp2 = client.sendRequest(POST,
                "{ name: \"sub1\","
                        + "  \"id\": 300004,\n"
                        + "description: \"1st of 200002 renewed\","
                        + "status: \"DONE\","
                        + "epicId: 200002}",
                "/tasks", "/subtask", "?id=", "300004");
        System.out.println(client.sendRequest(GET, null, "/tasks", "/subtask", "?id=", "300004"));
        assertEquals("202 Обновлено.", resp2);
    }

    void tasksTaskGETWithQuery() {
        String resp = client.sendRequest(GET,
                "",
                "/tasks", "/task", "?id=", "100001");
        assertTrue(resp.startsWith("200"));
    }

    void tasksEpicGETWithQuery() {
        String resp = client.sendRequest(GET,
                "",
                "/tasks", "/epic", "?id=", "200002");
        System.out.println(resp);
        assertTrue(resp.startsWith("200"));
    }

    void tasksEpicSubtasksGETWithQuery() {
        String resp = client.sendRequest(GET,
                "",
                "/tasks", "/epic","/subtasks", "?id=", "200002");
        System.out.println(resp);
        assertTrue(resp.startsWith("200"));
    }

    void tasksSubTaskGETWithQuery() {
        String resp = client.sendRequest(GET,
                "",
                "/tasks", "/subtask", "?id=", "300004");
        System.out.println(resp);
        assertTrue(resp.startsWith("200"));
    }

    void tasksTaskDELETEWithQuery() {
        String resp = client.sendRequest(DELETE,
                "",
                "/tasks", "/task", "?id=", "100001");
        assertEquals(resp, "202 Удалено.");
    }

    void tasksEpicDELETEWithQuery() {
        String resp = client.sendRequest(DELETE,
                "",
                "/tasks", "/epic", "?id=", "200003");
        System.out.println(resp);
        assertEquals(resp, "202 Удалено.");
    }

    void tasksSubTaskDELETEWithQuery() {
        String resp = client.sendRequest(DELETE,
                "",
                "/tasks", "/subtask", "?id=", "300004");
        System.out.println(resp);
        assertEquals(resp, "202 Удалено.");
    }

    void tasksTaskDELETE() {
        String resp = client.sendRequest(DELETE,
                "",
                "/tasks", "/task");
        assertEquals(resp, "202 Очищено.");
    }

    void tasksEpicDELETE() {
        String resp = client.sendRequest(DELETE,
                "",
                "/tasks", "/epic");
        System.out.println(resp);
        assertEquals(resp, "202 Очищено.");
    }

    void tasksSubTaskDELETE() {
        String resp = client.sendRequest(DELETE,
                "",
                "/tasks", "/subtask");
        System.out.println(resp);
        assertEquals(resp, "202 Очищено.");
    }

    void tasksDELETE() {
        String resp = client.sendRequest(DELETE,
                "",
                "/tasks");
        System.out.println(resp);
        assertEquals(resp, "202 Полностью очищено.");
    }
}
