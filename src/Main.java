public class Main {

    public static void main(String[] args) {
        TestInMemoryTaskManager testImtm = new TestInMemoryTaskManager();

        testImtm.createTask(new Task("Захватить мир",
                "Это то, что мы попробуем сделать завтра, Пинки!"));
        testImtm.createTask(new EpicTask("Сдать проект ревьюеру с первого раза",
                "А это вообще возможно?"));
        testImtm.test1(20,65,50);
    }
}
