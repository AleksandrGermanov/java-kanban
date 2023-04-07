package task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(Statuses.NEW, testTask.getStatus());
        testTask.setStatus(Statuses.IN_PROGRESS);
        assertEquals(Statuses.IN_PROGRESS, testTask.getStatus());
    }

    @Test
    void testStartTimeOptSetterGetter() {
        assertTrue(testTask.getStartTimeOpt().isEmpty());
        LocalDateTime testLDT = LocalDateTime.now();
        testTask.setStartTimeOpt(Optional.of(testLDT));
        assertEquals(testLDT, testTask.getStartTimeOpt().get());
    }

    @Test
    void testDurationOptSetterGetter() {
        assertTrue(testTask.getDurationOpt().isEmpty());
        testTask.setDurationOpt(Optional.of(15));
        assertEquals(15, testTask.getDurationOpt().get());
    }

    @Test
    void testEndTimeOptSetterGetter() {
        assertTrue(testTask.getEndTimeOpt().isEmpty());
        LocalDateTime testLDT = LocalDateTime.now();
        testTask.setEndTimeOpt(Optional.of(testLDT));
        assertEquals(testLDT, testTask.getEndTimeOpt().get());
    }
}