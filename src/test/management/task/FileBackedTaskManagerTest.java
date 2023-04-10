package test.management.task;

import management.task.FileBackedTaskManager;
import management.task.ManualTestTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    static Path testPath = Paths.get(System.getProperty("user.dir"), "/src/test/test.csv");

    @BeforeEach
    void cleanUpAndCreateTaskMan() {
        try {
            Files.deleteIfExists(testPath);
        } catch (IOException e) {
        }
        taskMan = new FileBackedTaskManager(testPath);
    }

    @Test
    void testDownLoadAndBackUp() {
        taskMan = new FileBackedTaskManager(testPath);
        taskMan.removeAllTasks();
        ArrayList<String> emptyTaskList = taskMan.getTaskList();
        List<Task> emptyHistory = taskMan.getHistory();
        //воспроизводим пустой лист из истории
        taskMan = new FileBackedTaskManager(testPath);
        assertEquals(emptyTaskList, taskMan.getTaskList());
        assertEquals(emptyHistory, taskMan.getHistory());

        ManualTestTaskManager<FileBackedTaskManager> mttm = new ManualTestTaskManager<>(taskMan);
        mttm.createRandomTasks(20);
        ArrayList<String> oldTaskList = taskMan.getTaskList();
        //сравниваем старый и новый TaskList и проверяем, что история не поменялась
        taskMan = new FileBackedTaskManager(testPath);
        assertEquals(oldTaskList, taskMan.getTaskList());
        assertNotEquals(emptyTaskList, taskMan.getTaskList());
        assertEquals(emptyHistory, taskMan.getHistory());
        mttm = new ManualTestTaskManager<>(taskMan);
        mttm.getRandomTasks(12);
        //убеждаемся, что у нас есть история и сохраняем ее
        assertNotEquals(emptyHistory, taskMan.getHistory());
        List<Task> oldHistory = taskMan.getHistory();
        //создаем новый менеджер и проверяем историю
        taskMan = new FileBackedTaskManager(testPath);
        mttm = new ManualTestTaskManager<>(taskMan);
        assertEquals(oldTaskList, taskMan.getTaskList());
        assertEquals(oldHistory, taskMan.getHistory());
        //удаляем задания и проверяем тасклист на неравенство; историю не сравниваем, т.к. теоретически есть
        //возможность что ни одно задание из истории не будет удалено.
        mttm.removeRandomTasks(40);
        assertNotEquals(oldTaskList, taskMan.getTaskList());
        oldTaskList = taskMan.getTaskList();
        oldHistory = taskMan.getHistory();
        taskMan = new FileBackedTaskManager(testPath);
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
        taskMan = new FileBackedTaskManager(testPath);
        assertEquals(oldTaskList, taskMan.getTaskList());
        assertEquals(oldHistory, taskMan.getHistory());
    }

    @Test
    void multiplyDownLoadAndBackUpTest() {
        for (int i = 0; i < 100; i++) {
            testDownLoadAndBackUp();
        }
    }
}