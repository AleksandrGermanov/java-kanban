package management.task;

import management.Managers;
import management.time.TimeManager;
import task.EpicTask;
import task.SubTask;
import task.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    static InMemoryTaskManager taskMan = new InMemoryTaskManager();

    @BeforeEach
    void createTaskMan() {
        super.taskMan = taskMan;
        taskMan.removeAllTasks();
    }

    @Test
    void createTask() {
        super.createTask();
    }

    @Test
    void testCreateTask() {
        TimeManager timeMan = Managers.getDefaultTime();

        Task task = new Task();
        taskMan.createTask("20.02.2002 20:02", 58, task);
        assertTrue(timeMan.isTimeSet(task));
    }

    @Test
    void renewTask() {
        super.renewTask();
    }

    @Test
    void testRenewTask() {
        Task task = new Task();
        taskMan.createTask("20.02.2002 20:02", 58, task);
        LocalDateTime oldStart = task.getStartTimeOpt().get();
        int oldDuration = task.getDurationOpt().get();
        LocalDateTime oldFinish = task.getEndTimeOpt().get();
        taskMan.renewTask("20.02.2002 20:15", 45, task);
        assertNotEquals(oldStart, task.getStartTimeOpt().get());
        assertNotEquals(oldDuration, task.getDurationOpt().get());
        assertEquals(oldFinish, task.getEndTimeOpt().get());
    }

    @Test
    void getAndRemoveTask() {
        super.getAndRemoveTask();
    }

    @Test
    void getHistory() {
        super.getHistory();
    }

    @Test
    void getTaskList() {
        super.getTaskList();
    }

    @Test
    void removeAllTasks() {
        super.removeAllTasks();
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