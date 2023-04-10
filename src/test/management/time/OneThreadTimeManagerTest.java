package test.management.time;

import management.time.OneThreadTimeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.SubTask;
import task.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static management.time.OneThreadTimeManager.DATE_TIME_FORMATTER;
import static org.junit.jupiter.api.Assertions.*;

class OneThreadTimeManagerTest {
    static Task task;
    static EpicTask epic;
    static SubTask sub1;
    static SubTask sub2;
    OneThreadTimeManager tm = new OneThreadTimeManager();

    @BeforeEach
    void generateTasksAndSetId() {
        task = new Task();
        epic = new EpicTask();
        sub1 = new SubTask(epic);
        sub2 = new SubTask(epic);
        task.setId(101);
        epic.setId(202);
        sub1.setId(303);
        sub2.setId(304);
    }

    @BeforeEach
    void clearBeforeEach() {
        tm.clear();
    }

    @Test
    void setTime() {
        tm.setTime("01.01.1991 00:00", 10, task);
        assertTrue(tm.isTimeSet(task));
        assertEquals(LocalDateTime.parse("01.01.1991 00:00", DATE_TIME_FORMATTER), task.getStartTime());
        assertEquals(10, task.getDuration());
        assertEquals(LocalDateTime.parse("01.01.1991 00:00", DATE_TIME_FORMATTER).plusMinutes(10),
                task.getEndTime());
        tm.setTime("01.01.1991 00:00", 10, sub1);
        assertFalse(tm.isTimeSet(sub1));
        tm.setTime("01.01.1991 00:09", 15, sub1);
        assertFalse(tm.isTimeSet(sub1));
        tm.setTime("31.12.1990 23:01", 65, sub2);
        assertFalse(tm.isTimeSet(sub2));
        tm.setTime("01.01.1991 00:09", 15, task);
        assertEquals(LocalDateTime.parse("01.01.1991 00:09", DATE_TIME_FORMATTER),
                task.getStartTime());
    }

    @Test
    void getPrioritizedTasks() {
        sub1.getMyEpic().getMySubTaskMap().put(sub1.getId(), sub1);
        sub1.getMyEpic().getMySubTaskMap().put(sub2.getId(), sub1);
        tm.setTime("01.01.1991 00:00", 10, sub1);
        tm.setTime("01.01.1991 00:01", 15, task);
        tm.setTime("31.12.1990 23:01", 65, task);
        epic.setTime();
        tm.addToRanged(sub2);
        tm.addToRanged(task);
        tm.addToRanged(epic);
        tm.addToRanged(sub1);
        ArrayList<Task> manuallyAranged = new ArrayList<>();
        manuallyAranged.add(epic);
        manuallyAranged.add(sub1);
        manuallyAranged.add(task);
        manuallyAranged.add(sub2);
        assertEquals(manuallyAranged, tm.getPrioritizedTasks());
    }

    @Test
    void isTimeSet() {
        tm.isTimeSet(task);
        assertFalse(tm.isTimeSet(task));
        task.setStartTime(LocalDateTime.now());
        assertFalse(tm.isTimeSet(task));
        task.setDuration(15);
        assertFalse(tm.isTimeSet(task));
        task.setEndTime(LocalDateTime.now());
        assertTrue(tm.isTimeSet(task));
    }

    @Test
    void addToRanged() {
        assertEquals(0, tm.getPrioritizedTasks().size());
        tm.addToRanged(epic);
        assertEquals(1, tm.getPrioritizedTasks().size());
    }

    @Test
    void removeFromRanged() {
        assertEquals(0, tm.getPrioritizedTasks().size());
        tm.addToRanged(epic);
        assertEquals(1, tm.getPrioritizedTasks().size());
        tm.removeFromRanged(epic);
        assertEquals(0, tm.getPrioritizedTasks().size());
    }

    @Test
    void removeFromValidation() {
        tm.setTime("01.01.1991 00:00", 10, sub1);
        tm.setTime("01.01.1991 00:01", 15, task);
        tm.removeFromValidation(sub1);
        tm.setTime("31.12.1990 23:01", 65, task);
        assertTrue(tm.isTimeSet(task));
    }

    @Test
    void clear() {
        tm.addToRanged(task);
        assertFalse(tm.getPrioritizedTasks().isEmpty());
        tm.setTime("01.01.1991 00:00", 10, sub1);
        tm.setTime("01.01.1991 00:00", 10, sub2);
        assertFalse(tm.isTimeSet(sub2));
        tm.clear();
        assertTrue(tm.getPrioritizedTasks().isEmpty());
        tm.setTime("01.01.1991 00:00", 10, sub2);
        assertTrue(tm.isTimeSet(sub2));
    }
}