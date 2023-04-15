package test.management.task;

import management.Managers;
import management.task.FileBackedTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.SubTask;
import task.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    static Path testPath = Paths.get(System.getProperty("user.dir"), "/src/test/test.csv");

    @BeforeEach
    void cleanUpAndCreateTaskMan() {
        try {
            Files.deleteIfExists(testPath);
        } catch (IOException e) {
        }
        taskMan = new FileBackedTaskManager(testPath, Managers.getDefaultHistory(), Managers.getDefaultTime());
    }

    @Test
    void getCSVPath() {
        assertInstanceOf(Path.class, taskMan.getCsvPath());
    }

    @Test
    void testDownLoadAndBackUp() {
        taskMan.removeAllTasks();
        ArrayList<String> emptyTaskList = taskMan.getTaskList();
        List<Task> emptyHistory = taskMan.getHistory();
        //воспроизводим пустой лист из истории
        taskMan = new FileBackedTaskManager(testPath);
        assertEquals(emptyTaskList, taskMan.getTaskList());
        assertEquals(emptyHistory, taskMan.getHistory());
        //создаем случайные задания
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
    void testWriting() {
        try {
            Path manualData = Paths.get(System.getProperty("user.dir"), "/src/test/manual_data.csv");
            if (!Files.exists(manualData)) {
                Files.createFile(manualData);
            }
            taskMan.removeAllTasks();
            Files.writeString(manualData, "id,type,name,status,description,start,duration,end,epic\n"
                    + "idCounter\n"
                    + 0 + "\n"
                    + "History first-to-last\n"
                    + "No history\n", StandardCharsets.UTF_8);
            assertEquals(Files.readAllLines(testPath), Files.readAllLines(manualData));
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
            Files.writeString(manualData, "id,type,name,status,description,start,duration,end,epic\n"
                    + "100000,TASK,task,NEW,task_description,null,null,null\n"
                    + "100004,TASK,Default,NEW,No description,null,null,null\n"
                    + "200001,EPICTASK,epic,NEW,has 2 subs,12.04.2023 16:37,15,12.04.2023 16:52\n"
                    + "300002,SUBTASK,sub with time,NEW,epic's 1st,12.04.2023 16:37,15,12.04.2023 16:52,200001\n"
                    + "300003,SUBTASK,sub 2,NEW,epic's 2nd,null,null,null,200001\n"
                    + "idCounter\n"
                    + "5\n"
                    + "History first-to-last\n"
                    + "300002\n", StandardCharsets.UTF_8);
            assertEquals(Files.readAllLines(testPath), Files.readAllLines(manualData));
            Files.deleteIfExists(manualData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testLoading() {
        try {
            Path manuallyData = Paths.get(System.getProperty("user.dir"), "/src/test/manuallyData.csv");
            if (!Files.exists(manuallyData)) {
                Files.createFile(manuallyData);
            }
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
            Files.writeString(manuallyData, "id,type,name,status,description,start,duration,end,epic\n"
                    + "100000,TASK,task,NEW,task_description,null,null,null\n"
                    + "100004,TASK,Default,NEW,No description,null,null,null\n"
                    + "200001,EPICTASK,epic,NEW,has 2 subs,12.04.2023 16:37,15,12.04.2023 16:52\n"
                    + "300002,SUBTASK,sub with time,NEW,epic's 1st,12.04.2023 16:37,15,12.04.2023 16:52,200001\n"
                    + "300003,SUBTASK,sub 2,NEW,epic's 2nd,null,null,null,200001\n"
                    + "idCounter\n"
                    + "5\n"
                    + "History first-to-last\n"
                    + "300002\n", StandardCharsets.UTF_8);
            FileBackedTaskManager fromManual = new FileBackedTaskManager(manuallyData);
            assertEquals(taskMan.getTaskList(), fromManual.getTaskList());
            assertEquals(taskMan.getHistory(), fromManual.getHistory());
            taskMan.removeAllTasks();
            Files.writeString(manuallyData, "id,type,name,status,description,start,duration,end,epic\n"
                    + "idCounter\n"
                    + "0\n"
                    + "History first-to-last\n"
                    + "No history\n", StandardCharsets.UTF_8);
            taskMan.removeAllTasks();
            fromManual = new FileBackedTaskManager(manuallyData);
            assertEquals(taskMan.getHistory(), fromManual.getHistory());
            Files.deleteIfExists(manuallyData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}