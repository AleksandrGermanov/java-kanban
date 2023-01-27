import java.util.Random;

public class Main {

    static{
        Main main = new Main();
    }

    public Main() {
        TaskManager taskMan = Managers.getDefault();
        HistoryManager histMan = Managers.getDefaultHistory();
        TestInMemoryTaskManager testImtm = new TestInMemoryTaskManager();
        Random random = new Random();

        testImtm.createTask(new Task("Захватить мир",
                "Это то, что мы попробуем сделать завтра, Пинки!"));
        testImtm.createTask(new EpicTask("Сдать проект ревьюеру с первого раза",
                "А это вообще возможно?"));
        testImtm.test2(20, 75, 15);

        int quantity = 11;
        for (int i = 0; i<quantity; i++) {
            taskMan.getTask(testImtm.getAllKeysList().get(random.nextInt(testImtm.getAllKeysList().size())));
        }
        System.out.println(histMan.getHistory());
    }

    public static void main(String[] args) {
    // Я в отпуске, прошу не беспокоить :)
    }
}
