package management.task;


import task.*;
import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    <T extends Task> void createTask(T task);

    <T extends Task> void renewTask(T task);

    ArrayList<String> getTaskList(TaskFamily type);

    ArrayList<String> getTaskList();

    void removeAllTasks(TaskFamily type);

    void removeAllTasks();

    <T extends Task> T getTask(int id);

    <T extends Task> void removeTask(int id);

    List<Task> getHistory();
}

