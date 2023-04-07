package management;

import management.history.InMemoryHistoryManager;
import management.task.FileBackedTaskManager;
import management.task.InMemoryTaskManager;
import management.time.OneThreadTimeManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefault() {
        assertInstanceOf(InMemoryTaskManager.class, Managers.getDefault());
    }

    @Test
    void getFileBacked() {
        assertInstanceOf(FileBackedTaskManager.class, Managers.getFileBacked());
    }

    @Test
    void getDefaultHistory() {
        assertInstanceOf(InMemoryHistoryManager.class, Managers.getDefaultHistory());
    }

    @Test
    void getDefaultTime() {
        assertInstanceOf(OneThreadTimeManager.class, Managers.getDefaultTime());
    }
}