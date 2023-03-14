package management;

import management.history.*;
import management.task.*;

public class Managers {

    public static TaskManager getDefault(){
        return new InMemoryTaskManager();
    }
    public static TaskManager getFileBacked(){
        return new FileBackedTaskManager();
    }

    public static HistoryManager getDefaultHistory(){
        return new InMemoryHistoryManager();
    }
}
