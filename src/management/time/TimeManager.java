package management.time;

import task.Task;

import java.util.List;

public interface TimeManager {
    <T extends Task> void setTime(String start, Integer duration, T task);

    List<Task> getPrioritizedTasks();

    boolean isTimeSet(Task task);

    void addToValidation(Task task);

    void removeFromValidation(Task task);

    void clear();
}
