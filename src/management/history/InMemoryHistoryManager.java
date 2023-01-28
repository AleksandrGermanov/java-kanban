package management.history;

import task.Task;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    //    Новый класс InMemoryHistoryManager должен реализовывать интерфейс HistoryManager.
    private final static ArrayList<? super Task> HISTORY_LIST = new ArrayList<>(10);

        @Override
    public <T extends Task> void add(T task) {
        if (HISTORY_LIST.size() == 10){
            HISTORY_LIST.remove(0);
        }
        HISTORY_LIST.add(task);
    }

    @Override
    public ArrayList<? super Task> getHistory() {
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
