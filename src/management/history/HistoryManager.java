package management.history;

import task.Task;
import java.util.List;

public interface HistoryManager {

    <T extends Task> void add(T task);
// ...добавить метод void remove(int id) для удаления задачи из просмотра.
    void remove(int id);

    List<? super Task> getHistory();
}
