package test.management.history;

import management.history.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.SubTask;
import task.Task;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {
    static Task task = new Task();
    static EpicTask epic = new EpicTask();
    static SubTask sub1 = new SubTask(epic);
    static SubTask sub2 = new SubTask(epic);
    InMemoryHistoryManager hm = new InMemoryHistoryManager();

    @BeforeAll
    static void setId() {
        task.setId(101);
        epic.setId(201);
        sub1.setId(301);
        sub2.setId(302);
    }

    @BeforeEach
    void clearHistoryBeforeEach() {
        hm.clearHistory();
    }

    @Test
    void add() {
        assertEquals(0, hm.getHistory().size());
        hm.add(task);
        assertEquals(1, hm.getHistory().size());
        hm.add(task);
        assertEquals(1, hm.getHistory().size());
        assertEquals(task, hm.getHistory().get(0));
        hm.add(sub1);
        hm.add(epic);
        hm.add(sub2);
        assertEquals(4, hm.getHistory().size());
        assertEquals(sub2, hm.getHistory().get(hm.getHistory().size() - 1));
        hm.add(epic);
        assertEquals(4, hm.getHistory().size());
        assertEquals(epic, hm.getHistory().get(hm.getHistory().size() - 1));
    }

    @Test
    void remove() {
        hm.add(sub1);
        hm.add(epic);
        hm.add(sub2);
        hm.add(task);
        assertEquals(4, hm.getHistory().size());
        hm.remove(sub2.getId());
        assertEquals(3, hm.getHistory().size());
        hm.remove(task.getId());
        assertEquals(2, hm.getHistory().size());
        assertEquals(epic, hm.getHistory().get(hm.getHistory().size() - 1));
        hm.remove(0);
        assertEquals(2, hm.getHistory().size());
        hm.remove(sub1.getId());
        assertEquals(1, hm.getHistory().size());
    }

    @Test
    void getHistory() {
        ArrayList<Task> handMade = new ArrayList<>();
        assertEquals(handMade, hm.getHistory());
        handMade.add(task);
        handMade.add(sub2);
        handMade.add(epic);
        hm.add(task);
        hm.add(sub2);
        hm.add(epic);
        assertEquals(handMade, hm.getHistory());
    }

    @Test
    void clearHistory() {
        hm.add(task);
        hm.add(sub2);
        hm.add(epic);
        assertEquals(3, hm.getHistory().size());
        hm.clearHistory();
        assertEquals(0, hm.getHistory().size());
    }
}