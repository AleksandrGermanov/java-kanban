package management.history;

import management.task.InMemoryTaskManager;
import task.Task;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    //    Новый класс InMemoryHistoryManager должен реализовывать интерфейс HistoryManager.
    private final static List<? super Task> HISTORY_LIST = new ArrayList<>(10);
    private InMemoryTaskManager taskMan = new InMemoryTaskManager();

        @Override
    public <T extends Task> void add(T task) {
        if (HISTORY_LIST.size() == 10){
            HISTORY_LIST.remove(0);
        }
        HISTORY_LIST.add(task);
    }

    //...добавить метод void remove(int id) для удаления задачи из просмотра. И
    // реализовать его в классе InMemoryHistoryManager.
    @Override
    public void remove(int id){
            HISTORY_LIST.remove(taskMan.getTaskNH(id));
    }

    @Override
    public List<? super Task> getHistory() {
        return HISTORY_LIST;
    }

    public void printHistoryList(){
        System.out.println("История обращений: ");
        System.out.println("***");
        for(Object task : HISTORY_LIST){
            System.out.print(task.toString().replace("^\b", System.lineSeparator()));
        }
        System.out.println("***");
    }


}
