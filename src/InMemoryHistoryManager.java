import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    //    Новый класс InMemoryHistoryManager должен реализовывать интерфейс HistoryManager.
    static ArrayList<? super Task> historyList;

    public InMemoryHistoryManager() {
        if (historyList == null) {
            historyList = new ArrayList<>(10);
        }
        //должен возвращать последние 10 просмотренных задач;
    }

    @Override
    public <T extends Task> void add(T task) {
        if (historyList.size() == 10){
            historyList.remove(0);
        }
        historyList.add(task);
    }

    @Override
    public ArrayList<? super Task> getHistory() {
        return historyList;
    }
}
