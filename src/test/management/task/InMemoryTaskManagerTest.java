package test.management.task;

import management.Managers;
import management.history.HistoryManager;
import management.task.InMemoryTaskManager;
import management.time.TimeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.SubTask;
import task.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void createTaskMan() {
        taskMan = new InMemoryTaskManager();
    }

    @Test
    void getTasks() {
        assertInstanceOf(HashMap.class, taskMan.getTasks());
        assertEquals(3, taskMan.getTasks().size());
    }

    @Test
    void getHistMan() {
        assertInstanceOf(HistoryManager.class, taskMan.getHistMan());
    }

    @Test
    void getTimeMan() {
        assertInstanceOf(TimeManager.class, taskMan.getTimeMan());
    }

    @Test
    void testCreateTask() {
        TimeManager timeMan = Managers.getDefaultTime();

        Task task = new Task();
        taskMan.createTask("20.02.2002 20:02", 58, task);
        assertTrue(timeMan.isTimeSet(task));
    }

    @Test
    void testRenewTask() {
        Task task = new Task();
        taskMan.createTask("20.02.2002 20:02", 58, task);
        LocalDateTime oldStart = task.getStartTime();
        int oldDuration = task.getDuration();
        LocalDateTime oldFinish = task.getEndTime();
        taskMan.renewTask("20.02.2002 20:15", 45, task);
        assertNotEquals(oldStart, task.getStartTime());
        assertNotEquals(oldDuration, task.getDuration());
        assertEquals(oldFinish, task.getEndTime());
    }

    @Test
    void getEpicSubsList() {
        EpicTask epic = new EpicTask();
        taskMan.createTask(epic);
        ArrayList<String> empty = new ArrayList<>();
        assertEquals(empty, taskMan.getEpicSubsList(epic));
        taskMan.createTask(new SubTask(epic));
        assertEquals(1, taskMan.getEpicSubsList(epic).size());
    }
}