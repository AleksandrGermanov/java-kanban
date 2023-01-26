import java.util.ArrayList;

public interface TaskManager {

    void initializeTasksMap();

    int generateId(Task task);

    TaskFamily defineTypeById(int id);

    <T extends Task> void createTask(T task);

    <T extends Task> void putTaskToMap(T task);

    <T extends Task> void renewTask(T task);

    boolean isFoundType(TaskFamily type);

    ArrayList<String> getTaskList(TaskFamily type);

    ArrayList<String> getTaskList();

    void removeAllTasks(TaskFamily type);

    void removeAllTasks();

    ArrayList<String> getEpicSubsList(EpicTask epic);

    boolean isFoundById(int id);

    <T extends Task> T getTask(int id);

    <T extends Task> void removeTask(int id);
}

