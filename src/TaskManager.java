import java.util.ArrayList;

/*Класс TaskManager должен стать интерфейсом.
В нём нужно собрать список методов,
которые должны быть у любого объекта-менеджера.  Вспомогательные методы,
если вы их создавали, переносить в интерфейс не нужно.
 */
public interface TaskManager {

    <T extends Task> void createTask(T task);

    <T extends Task> void renewTask(T task);

    ArrayList<String> getTaskList(TaskFamily type);

    ArrayList<String> getTaskList();

    void removeAllTasks(TaskFamily type);

    void removeAllTasks();

    <T extends Task> T getTask(int id);

    <T extends Task> void removeTask(int id);
}

