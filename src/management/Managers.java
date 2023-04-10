package management;

import management.history.HistoryManager;
import management.history.InMemoryHistoryManager;
import management.task.FileBackedTaskManager;
import management.task.InMemoryTaskManager;
import management.task.TaskManager;
import management.time.OneThreadTimeManager;
import management.time.TimeManager;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
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
