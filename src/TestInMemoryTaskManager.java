import java.util.ArrayList;
import java.util.Random;

public class TestInMemoryTaskManager extends InMemoryTaskManager {
    static Random random = new Random();

    public void test0() {
        createRandomTask(new Task());
        createRandomTask(new Task());
        createRandomTask(new EpicTask());
        createRandomTask(new SubTask(getTask(200002)));
        createRandomTask(new SubTask(getTask(200002)));
        createRandomTask(new EpicTask());
        createRandomTask(new SubTask(getTask(200005)));
        System.out.println(getTaskList());
        changeTask(100000);
        changeTask(300006);
        changeTask(300004);
        removeTask(100001);
        System.out.println(getTaskList());
        removeTask(200005);
        removeTask(300003);
        System.out.println(getTaskList());
    }

    public void test1(int quantity) {
        createRandomTasks(quantity);
        System.out.println(getTaskList());
        changeRandomTasks(50);
        System.out.println(getTaskList());
        removeRandomTasks(50);
        System.out.println(getTaskList());
        removeRandomTasks(100);
        System.out.println(getTaskList());
    }

    public void test1(int quantity, int percentOfChanges, int percentOfRemoves) {
        createRandomTasks(quantity);
        System.out.println(getTaskList());
        changeRandomTasks(percentOfChanges);
        System.out.println(getTaskList());
        removeRandomTasks(percentOfRemoves);
        System.out.println(getTaskList());
        removeRandomTasks(100);
        System.out.println(getTaskList());
    }

    public <T extends Task> void createRandomTask(T task) {
        task.setName(TestInMemoryTaskManager.randomTaskNameCreator());
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

    public <T extends Task> void changeTask(int id) {
        T task = getTask(id);

        task.setName("New name:" + randomTaskNameCreator());
        task.setDescription("А у тебя точно получится?");
        switch (task.getClass().getName()) {
            case "Task":
                task.setStatus(randomStatus());
                break;
            case "SubTask":
                task.setStatus(randomStatus());
                SubTask sub = (SubTask) task;
                sub.getMyEpic().setStatus();/*Менеджер сам не выбирает статус для задачи.
                Информация о нём приходит менеджеру вместе с информацией о самой задаче.
                По этим данным в одних случаях он будет сохранять статус, в других будет рассчитывать.*/
                break;
        }
    }

    static String randomStatus() {
        return Statuses.values()[random.nextInt(Statuses.values().length)].toString();
    }

    public static boolean coin() {
        return random.nextInt(2) == 0;
    }

    public int countAllTasks() {
        int sum = 0;
        for (TaskFamily tf : TaskFamily.values()) {
            sum += tasks.get(tf).size();
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
                        createRandomTask(new SubTask(getTask(randomEpicId)));
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

        for (TaskFamily tf : TaskFamily.values()) {
            for (int id : tasks.get(tf).keySet()) {
                list.add(id);
            }
        }
        return list;
    }

    public void changeRandomTasks(int percentage) {
        ArrayList<Integer> allKeys = getAllKeysList();
        Integer id;
        int count = 0;

        if (percentage > 100) {
            System.out.println("Некоторые объекты будут изменены дважды");
        }
        for (int i = 0; i < (countAllTasks() * percentage / 100); i++) {
            id = allKeys.get(random.nextInt(allKeys.size()));
            changeTask(id);
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
}


