package management.task;

import management.history.InMemoryHistoryManager;
import task.EpicTask;
import task.Statuses;
import task.SubTask;
import task.Task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

public class ManualTestTaskManager extends FileBackedTaskManager {
    private static final Random random = new Random();
    public InMemoryHistoryManager hm = new InMemoryHistoryManager();

    public ManualTestTaskManager(){
    }
    public ManualTestTaskManager(Path path){
        super(path);
    }

    public <T extends Task> void createRandomTask(T task) {
        task.setName(ManualTestTaskManager.randomTaskNameCreator());
        task.setDescription("Нужно просто пойти и " + task.getName());
        createTask(task);
    }

    static String randomTaskNameCreator() {
        String[] word1 = {"помыть", "купить", "убрать", "разобрать", "приготовить"};
        String[] word2 = {"квартиру", "машину", "овощи", "холодильник",
                "вещи", "ружье", "сковороду", "компьютер"};
        String[] word3 = {"родителей", "на даче", "жены", "в поход",
                "в первый раз", "для переезда", "за один день"};
        return (word1[random.nextInt(word1.length)] + ' ' + word2[random.nextInt(word2.length)]
                + ' ' + word3[random.nextInt(word3.length)]);
    }

    public <T extends Task> T changeTask(int id) {
        T task = getTaskNH(id);

        task.setName("New name:" + randomTaskNameCreator());
        task.setDescription("А у тебя точно получится?");
        if (!defineTypeById(task.getId()).equals(TaskFamily.EPICTASK)) {
            task.setStatus(randomStatus());
        }
        return task;
    }

    static Statuses randomStatus() {
        return Statuses.values()[random.nextInt(Statuses.values().length)];
    }

    public static boolean coin() {
        return random.nextInt(2) == 0;
    }

    public int countAllTasks() {
        int sum = 0;
        for (TaskFamily TF : TaskFamily.values()) {
            sum += tasks.get(TF).size();
        }
        return sum;
    }

    public void createRandomTasks(int quantity) {
        ArrayList<Integer> epicsList = new ArrayList<>();
        int count = 0;

        for (int i = 0; i < quantity; i++) {
            switch (TaskFamily.values()[random.nextInt(TaskFamily.values().length)]) {
                case SUBTASK:
                    if (!tasks.get(TaskFamily.EPICTASK).isEmpty()) {
                        int randomEpicId;
                        int epicsSize = tasks.get(TaskFamily.EPICTASK).size();
                        for (int id : tasks.get(TaskFamily.EPICTASK).keySet()) {
                            if (!epicsList.contains(id)) epicsList.add(id);
                        }
                        randomEpicId = epicsList.get(random.nextInt(epicsSize));
                        createRandomTask(new SubTask(getTaskNH(randomEpicId)));
                        ++count;
                        break;
                    }
                case TASK:
                case EPICTASK:
                    if (coin()) {//нужна, чтобы итерации на несозданные SubTask распределялись равномерно
                        // между Task и EpicTask
                        createRandomTask(new Task());
                    } else {
                        createRandomTask(new EpicTask());
                    }
                    ++count;
                    break;
            }
        }
        System.out.println("Создано " + count + " объектов.");
        System.out.println("Общее количество заданий " + countAllTasks() + ".");
    }

    public ArrayList<Integer> getAllKeysList() {
        ArrayList<Integer> list = new ArrayList<>();

        for (TaskFamily TF : TaskFamily.values()) {
            list.addAll(tasks.get(TF).keySet());
        }
        return list;
    }

    public void renewRandomTasks(int percentage) {
        ArrayList<Integer> allKeys = getAllKeysList();
        Integer id;
        int count = 0;

        if (percentage > 100) {
            System.out.println("Некоторые объекты будут изменены дважды");
        }
        for (int i = 0; i < (countAllTasks() * percentage / 100); i++) {
            id = allKeys.get(random.nextInt(allKeys.size()));
            renewTask(changeTask(id));
            allKeys.remove(id);
            ++count;
            if (allKeys.isEmpty()) {
                allKeys = getAllKeysList();
            }
        }
        System.out.println("Изменено " + count + " объектов.");
    }

    public void removeRandomTasks(int percentage) {
        ArrayList<Integer> allKeys = getAllKeysList();
        Integer id;
        int count = 0;
        int plannedRemaining = countAllTasks() * (100 - percentage) / 100;
        int remaining = countAllTasks();

        if (percentage >= 100) {
            System.out.println("Объекты будут удаляться, пока не останется ни одного!");
        } else {
            System.out.println("Эпик сам удаляет свои субтаски, поэтому результат"
                    + " может отличаться от запланированного.");
        }
        while ((remaining > plannedRemaining) && !allKeys.isEmpty()) {
            id = allKeys.get(random.nextInt(allKeys.size()));
            removeTask(id);
            ++count;
            allKeys = getAllKeysList();
            remaining = countAllTasks();
        }
        if (remaining < plannedRemaining) {
            System.out.println("Ой, кажется мы немного перестарались:");
            System.out.println("хотели оставить " + plannedRemaining
                    + " заданий, а осталось " + remaining + '!');
        }
        System.out.println("Удалено " + count + " объектов.");
        System.out.println("Осталось " + remaining + " объектов.");
    }

    public void getRandomTasks(int quantity) {
        StringBuilder idsString = new StringBuilder();
        for (int i = 0; i < quantity; i++) {
            ArrayList<Integer> allKeys = getAllKeysList();
            int id = allKeys.get(random.nextInt(allKeys.size()));
            getTask(id);
            idsString.append(id).append(", ");
        }
        System.out.println("Были вызваны следующие задачи для формирования истории:");
        System.out.println(idsString+"\b\b");
    }
}


