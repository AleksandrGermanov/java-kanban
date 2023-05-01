package test.management;

import exchange.KVServer;
import management.Managers;
import management.history.InMemoryHistoryManager;
import management.task.FileBackedTaskManager;
import management.task.HttpTaskManager;
import management.time.OneThreadTimeManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ManagersTest {

    @Test
    void getDefault() throws IOException {
        KVServer kvs = new KVServer();
        kvs.start();
        assertInstanceOf(HttpTaskManager.class, Managers.getDefault());
        kvs.stop();
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