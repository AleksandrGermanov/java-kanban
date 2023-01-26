public class Managers {
/*
    У Managersбудет метод  getDefault().
    При этом вызывающему неизвестен конкретный класс, только то,
    что объект, который возвращает getDefault(),
    реализует интерфейс TaskManager.*/
    public static TaskManager getDefault(){
        return new InMemoryTaskManager();
    }

    /*
    Добавьте в служебный класс Managers статический метод HistoryManager getDefaultHistory().
    Он должен возвращать объект InMemoryHistoryManager — историю просмотров.
     */
    public static HistoryManager getDefaultHistory(){
        return new InMemoryHistoryManager();
    }
}
