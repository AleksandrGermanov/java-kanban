package management;

import management.history.HistoryManager;
import management.history.InMemoryHistoryManager;
import management.task.FileBackedTaskManager;
import management.task.HttpTaskManager;
import management.task.TaskManager;
import management.time.OneThreadTimeManager;
import management.time.TimeManager;

import java.net.URI;

public class Managers {

    public static TaskManager getDefault() {
        return new HttpTaskManager(URI.create("http://localhost:8078/"));
    }

    public static TaskManager getFileBacked() {
        return new FileBackedTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TimeManager getDefaultTime() {
        return new OneThreadTimeManager();
    }
}
