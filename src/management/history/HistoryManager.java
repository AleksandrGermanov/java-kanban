package management.history;

import task.Task;

import java.util.List;

public interface HistoryManager {

    <T extends Task> void add(T task);

    void remove(int id);

    List<Task> getHistory();

    void clearHistory();
}
