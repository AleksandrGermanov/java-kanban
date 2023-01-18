import java.util.Random;
import java.util.ArrayList;

public class TestManager extends Manager {
    static Random random = new Random();
    /*Создайте 2 задачи, один эпик с 2 подзадачами, а другой эпик с 1 подзадачей.
Распечатайте списки эпиков, задач и подзадач, через System.out.println(..)
Измените статусы созданных объектов, распечатайте. Проверьте, что статус задачи и подзадачи сохранился,
а статус эпика рассчитался по статусам подзадач.
И, наконец, попробуйте удалить одну из задач и один из эпиков.  */

    /**
     * поменял название на test0
     */
    public static void test0() {
        createTask(new Task());
        createTask(new Task());
        createTask(new EpicTask());
        createTask(new SubTask(getTask(200002)));
        createTask(new SubTask(getTask(200002)));
        createTask(new EpicTask());
        createTask(new SubTask(getTask(200005)));
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


    static String randomTaskNameCreator() {
        String[] word1 = {"помыть", "купить", "убрать", "разобрать", "приготовить"};
        String[] word2 = {"квартиру", "машину", "овощи", "холодильник",
                "вещи", "ружье", "сковороду", "компьютер"};
        String[] word3 = {"родителей", "на даче", "жены", "в поход",
                "в первый раз", "для переезда", "за один день"};
        return (word1[random.nextInt(word1.length)] + ' ' + word2[random.nextInt(word2.length)]
                + ' ' + word3[random.nextInt(word3.length)]);
    }

    public static <T extends Task> void changeTask(int id) {
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

    public static void createRandomTasks(int quantity) {
        for (int i = 0; i < quantity; i++) {
            switch (TaskFamily.values()[random.nextInt(TaskFamily.values().length)]) {
                case SUBTASK:
                    if (!tasks.get(TaskFamily.EPICTASK).isEmpty()) {
                        int randomEpicId;
                        int epicsSize = tasks.get(TaskFamily.EPICTASK).size();
                        Integer[] epicsArray = (Integer[]) tasks.get(TaskFamily.EPICTASK)
                                .keySet().toArray();
                        randomEpicId = epicsArray[random.nextInt(epicsSize)];
                        createTask(new SubTask(getTask(randomEpicId)));
                        break;
                    }
                case TASK:
                case EPICTASK:
                    if (coin()) {//нужна, чтобы итерации на несозданные SubTask распределялись равномерно
                        // между Task и EpicTask
                        createTask(new Task());
                    } else {
                        createTask(new EpicTask());
                    }
                    break;
            }
        }
        System.out.println("Создано " + countAllTasks() + " объектов.");
    }

    public static boolean coin() {
        return random.nextInt(2) == 0;
    }

    public static int countAllTasks(){
        int sum = 0;
        for (TaskFamily tf: TaskFamily.values()){
            sum += tasks.get(tf).size();
        }
        return sum;
    }


}
