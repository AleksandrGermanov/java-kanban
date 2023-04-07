package management.task;

import myExceptions.NoMatchesFoundException;
import org.junit.jupiter.api.Test;
import task.*;

import static org.junit.jupiter.api.Assertions.*;

class TaskFamilyTest {

    @Test
    void getEnumFromClass() {
        try {
            assertEquals(TaskFamily.TASK, TaskFamily.getEnumFromClass(Task.class));
            assertEquals(TaskFamily.SUBTASK, TaskFamily.getEnumFromClass(SubTask.class));
            assertEquals(TaskFamily.EPICTASK, TaskFamily.getEnumFromClass(EpicTask.class));
            assertThrows(NoMatchesFoundException.class, () -> TaskFamily.getEnumFromClass(TaskFamily.class));
        }
        catch (NoMatchesFoundException e){}
    }
}