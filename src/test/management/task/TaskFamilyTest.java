package test.management.task;

import management.task.TaskFamily;
import myExceptions.NoMatchesFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.SubTask;
import task.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskFamilyTest {

    @Test
    void getEnumFromClass() {
        try {
            Assertions.assertEquals(TaskFamily.TASK, TaskFamily.getEnumFromClass(Task.class));
            assertEquals(TaskFamily.SUBTASK, TaskFamily.getEnumFromClass(SubTask.class));
            assertEquals(TaskFamily.EPICTASK, TaskFamily.getEnumFromClass(EpicTask.class));
            assertThrows(NoMatchesFoundException.class, () -> TaskFamily.getEnumFromClass(TaskFamily.class));
        } catch (NoMatchesFoundException e) {
        }
    }
}