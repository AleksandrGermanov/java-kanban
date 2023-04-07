package management.task;

import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.SubTask;
import task.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    T taskMan;

    @Test
    void createTask() {
        Task testTask = new Task();
        taskMan.createTask(testTask);
        assertNotEquals(0, testTask.getId());
        assertEquals(1, taskMan.getTaskList(TaskFamily.TASK).size());
        EpicTask testEpic = new EpicTask();
        taskMan.createTask(testEpic);
        assertNotEquals(0, testEpic.getId());
        assertEquals(1, taskMan.getTaskList(TaskFamily.EPICTASK).size());
        SubTask testSub = new SubTask(testEpic);
        taskMan.createTask(testSub);
        assertNotEquals(0, testSub.getId());
        assertEquals(1, taskMan.getTaskList(TaskFamily.SUBTASK).size());
        assertEquals(testSub, testEpic.getMySubTaskMap().get(testSub.getId()));
    }

    @Test
    void renewTask() {
        Task t = new Task("name", "description");
        taskMan.createTask(t);
        int id = t.getId();
        String name = t.getName();
        String descr = t.getDescription();
        Task t2 = new Task("New name", "New description");
        t2.setId(id);
        taskMan.renewTask(t2);
        assertEquals(1, taskMan.getTaskList(TaskFamily.TASK).size());
        assertNotEquals(name, t2.getName());
        assertNotEquals(descr, t2.getDescription());
    }

    @Test
    void getTaskList() {
        ArrayList<String> emptyTaskList = taskMan.getTaskList();
        Task task = new Task();
        taskMan.createTask(task);
        EpicTask epic = new EpicTask();
        taskMan.createTask(epic);
        SubTask sub = new SubTask(epic);
        taskMan.createTask(sub);
        ArrayList<String> filledTaskList = taskMan.getTaskList();
        taskMan.removeAllTasks();
        assertEquals(emptyTaskList, taskMan.getTaskList());
        taskMan.renewTask(task);//createTask() присваивает новый id
        taskMan.renewTask(epic);
        taskMan.renewTask(sub);
        assertEquals(filledTaskList, taskMan.getTaskList());
    }

    @Test
    void removeAllTasks() {
        ArrayList<String> emptyTaskList = taskMan.getTaskList();
        taskMan.createTask(new Task());
        assertEquals(1, taskMan.getTaskList(TaskFamily.TASK).size());
        taskMan.removeAllTasks(TaskFamily.TASK);
        assertTrue(taskMan.getTaskList(TaskFamily.TASK).isEmpty());
        EpicTask epic = new EpicTask();
        taskMan.createTask(epic);
        SubTask sub = new SubTask(epic);
        taskMan.createTask(sub);
        assertEquals(1, taskMan.getTaskList(TaskFamily.SUBTASK).size());
        taskMan.removeAllTasks(TaskFamily.SUBTASK);
        assertTrue(taskMan.getTaskList(TaskFamily.SUBTASK).isEmpty());
        assertEquals(1, taskMan.getTaskList(TaskFamily.EPICTASK).size());
        taskMan.removeAllTasks(TaskFamily.EPICTASK);
        assertTrue(taskMan.getTaskList(TaskFamily.EPICTASK).isEmpty());
        assertEquals(emptyTaskList, taskMan.getTaskList());
        taskMan.createTask(new Task());
        epic = new EpicTask();
        taskMan.createTask(epic);
        sub  = new SubTask(epic);
        taskMan.createTask(sub);
        taskMan.removeAllTasks();
        assertEquals(emptyTaskList, taskMan.getTaskList());
    }

    @Test
    void getAndRemoveTask() {
        Task task = new Task();
        taskMan.createTask(task);
        int id = task.getId();
        assertEquals(task, taskMan.getTask(id));
        taskMan.removeTask(id);
        assertThrows(NullPointerException.class, () -> taskMan.getTask(id));
    }

    @Test
    void getHistory() {
        List<Task> empty = Collections.emptyList();
        assertEquals(empty, taskMan.getHistory());
        Task task = new Task();
        taskMan.createTask(task);
        taskMan.getTask(task.getId());
        assertNotEquals(empty, taskMan.getHistory());
        taskMan.removeAllTasks();
        assertEquals(empty, taskMan.getHistory());
    }
}