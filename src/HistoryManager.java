import java.util.ArrayList;

public interface HistoryManager {
    /*
    Создайте отдельный интерфейс для управления историей просмотров — HistoryManager.
    У него будет два метода. Первый add(Task task) должен помечать задачи как просмотренные,
    а второй getHistory() — возвращать их список.
    */

    /*
        Просмотром будем считаться вызов у менеджера методов
        получения задачи по идентификатору  —
        getTaskNH(), getSubtask() и getEpic()
        */

    
    <T extends Task> void add(T task);    

    ArrayList<? super Task> getHistory();
}
