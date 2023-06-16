package test.management.task;

import exchange.KVClient;
import exchange.KVServer;
import management.task.HttpTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.SubTask;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {
    KVServer kvs;
    URI KVServerURI = URI.create("http://localhost:8078/");

    @BeforeEach
    void runKVServerAndCreateTaskMan() throws IOException {
        kvs = new KVServer();
        kvs.start();
        taskMan = new HttpTaskManager(KVServerURI);
    }

    @AfterEach
    void cleanUpAndStopKVS() {
        taskMan.removeAllTasks();
        kvs.stop();
    }

    @Test
    void testDownLoadAndBackUp() {
        taskMan.removeAllTasks();
        ArrayList<String> emptyTaskList = taskMan.getTaskList();
        List<Task> emptyHistory = taskMan.getHistory();
        //воспроизводим пустой лист из истории
        taskMan = new HttpTaskManager(KVServerURI);
        assertEquals(emptyTaskList, taskMan.getTaskList());
        assertEquals(emptyHistory, taskMan.getHistory());
        //создаем случайные задания
        ManualTestTaskManager<HttpTaskManager> mttm = new ManualTestTaskManager<>(taskMan);
        mttm.createRandomTasks(20);
        ArrayList<String> oldTaskList = taskMan.getTaskList();
        //сравниваем старый и новый TaskList и проверяем, что история не поменялась
        taskMan = new HttpTaskManager(KVServerURI);
        assertEquals(oldTaskList, taskMan.getTaskList());
        assertNotEquals(emptyTaskList, taskMan.getTaskList());
        assertEquals(emptyHistory, taskMan.getHistory());
        mttm = new ManualTestTaskManager<>(taskMan);
        mttm.getRandomTasks(12);
        //убеждаемся, что у нас есть история и сохраняем ее
        assertNotEquals(emptyHistory, taskMan.getHistory());
        List<Task> oldHistory = taskMan.getHistory();
        //создаем новый менеджер и проверяем историю
        taskMan = new HttpTaskManager(KVServerURI);
        mttm = new ManualTestTaskManager<>(taskMan);
        assertEquals(oldTaskList, taskMan.getTaskList());
        assertEquals(oldHistory, taskMan.getHistory());
        //удаляем задания и проверяем тасклист на неравенство; историю не сравниваем, т.к. теоретически есть
        //возможность что ни одно задание из истории не будет удалено.
        mttm.removeRandomTasks(40);
        assertNotEquals(oldTaskList, taskMan.getTaskList());
        oldTaskList = taskMan.getTaskList();
        oldHistory = taskMan.getHistory();
        taskMan = new HttpTaskManager(KVServerURI);
        assertEquals(oldTaskList, taskMan.getTaskList());
        assertEquals(oldHistory, taskMan.getHistory());
        //назначим время таскам и субтаскам
        LocalDateTime start = LocalDateTime.now();
        int duration = 9;
        List<Integer> tasksAndSubs = mttm.getAllKeysList().stream()
                .filter(id -> !id.toString().startsWith("2"))
                .collect(Collectors.toList());
        for (int id : tasksAndSubs) {
            taskMan.renewTask(start.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), duration,
                    taskMan.getTask(id));
            start = start.plusMinutes(10);
        }
        assertNotEquals(oldTaskList, taskMan.getTaskList());
        oldTaskList = taskMan.getTaskList();
        oldHistory = taskMan.getHistory();
        //проверяем загрузку задач со временем
        taskMan = new HttpTaskManager(KVServerURI);
        assertEquals(oldTaskList, taskMan.getTaskList());
        assertEquals(oldHistory, taskMan.getHistory());
    }

    @Test
    void testWriting() throws IOException, InterruptedException {
            KVClient client = new KVClient(KVServerURI);
            taskMan.removeAllTasks();
            String state1 = client.loadState();
            taskMan.createTask(new Task("task", "task_description"));
            String state2 = client.loadState();
            assertNotEquals(state1, state2);
            EpicTask epic = new EpicTask("epic", "has 2 subs");
            taskMan.createTask(epic);
            state1 = client.loadState();
            assertNotEquals(state1, state2);
            SubTask subWithTime = new SubTask(epic, "sub with time", "epic's 1st");
            subWithTime.setStartTime(LocalDateTime.parse("12.04.2023 16:37",
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            subWithTime.setDuration(15);
            subWithTime.setEndTime(subWithTime.getStartTime().plusMinutes(subWithTime.getDuration()));
            epic.setTime();
            taskMan.createTask(subWithTime);
            state2 = client.loadState();
            assertNotEquals(state1, state2);
            taskMan.createTask(new SubTask(epic, "sub 2", "epic's 2nd"));
            state1 = client.loadState();
            assertNotEquals(state1, state2);
            taskMan.createTask(new Task());
            state2 = client.loadState();
            assertNotEquals(state1, state2);
            taskMan.getTask(300002);
            state1 = client.loadState();
            assertNotEquals(state1, state2);
    }

    @Test
    void testLoading() throws IOException, InterruptedException{
            KVClient client = new KVClient(KVServerURI);
            taskMan.removeAllTasks();
            String emptyState = client.loadState();
            taskMan.createTask(new Task("task", "task_description"));
            EpicTask epic = new EpicTask("epic", "has 2 subs");
            taskMan.createTask(epic);
            SubTask subWithTime = new SubTask(epic, "sub with time", "epic's 1st");
            subWithTime.setStartTime(LocalDateTime.parse("12.04.2023 16:37",
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            subWithTime.setDuration(15);
            subWithTime.setEndTime(subWithTime.getStartTime().plusMinutes(subWithTime.getDuration()));
            epic.setTime();
            taskMan.createTask(subWithTime);
            taskMan.createTask(new SubTask(epic, "sub 2", "epic's 2nd"));
            taskMan.createTask(new Task());
            taskMan.getTask(300002);
            String fullState = client.loadState();
            client.saveState(emptyState);
            HttpTaskManager tm = new HttpTaskManager(KVServerURI);
            assertNotEquals(taskMan.getTasks(), tm.getTasks());
            client.saveState(fullState);
            HttpTaskManager tm2 = new HttpTaskManager(KVServerURI);
            assertEquals(taskMan.getTasks(), tm2.getTasks());
            assertNotEquals(tm.getTasks(), tm2.getTasks());
    }
}
