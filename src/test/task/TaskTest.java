package test.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Statuses;
import task.Task;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TaskTest {
    Task testTask;

    @BeforeEach
    void createTestTask() {
        testTask = new Task("name", "description");
    }

    @Test
    void testNameSetterGetter() {
        assertEquals("name", testTask.getName());
        testTask.setName("newNameWasSet");
        assertEquals("newNameWasSet", testTask.getName());
    }

    @Test
    void testDescriptionSetterGetter() {
        assertEquals("description", testTask.getDescription());
        testTask.setDescription("newDescriptionWasSet");
        assertEquals("newDescriptionWasSet", testTask.getDescription());
    }

    @Test
    void testIdSetterGetter() {
        assertEquals(0, testTask.getId());
        testTask.setId(123456789);
        assertEquals(123456789, testTask.getId());
    }

    @Test
    void testStatusSetterGetter() {
        Assertions.assertEquals(Statuses.NEW, testTask.getStatus());
        testTask.setStatus(Statuses.IN_PROGRESS);
        assertEquals(Statuses.IN_PROGRESS, testTask.getStatus());
    }

    @Test
    void testStartTimeSetterGetter() {
        assertNull(testTask.getStartTime());
        LocalDateTime testLDT = LocalDateTime.now();
        testTask.setStartTime(testLDT);
        assertEquals(testLDT, testTask.getStartTime());
    }

    @Test
    void testDurationSetterGetter() {
        assertNull(testTask.getDuration());
        testTask.setDuration(15);
        assertEquals(15, testTask.getDuration());
    }

    @Test
    void testEndTimeSetterGetter() {
        assertNull(testTask.getEndTime());
        LocalDateTime testLDT = LocalDateTime.now();
        testTask.setEndTime(testLDT);
        assertEquals(testLDT, testTask.getEndTime());
    }
}