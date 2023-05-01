package management.task;


import task.EpicTask;
import task.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    <T extends Task> void createTask(T task);

    <T extends Task> void renewTask(T task);

    <T extends Task> T getTask(int id);

    <T extends Task> void removeTask(int id);

    ArrayList<String> getTaskList(TaskFamily type);

    ArrayList<String> getTaskList();

    void removeAllTasks(TaskFamily type);

    void removeAllTasks();

    List<String> getEpicSubsList(EpicTask epic);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}

