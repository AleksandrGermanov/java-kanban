import java.util.Random;

public class TestManager extends Manager {

    /*Создайте 2 задачи, один эпик с 2 подзадачами, а другой эпик с 1 подзадачей.
Распечатайте списки эпиков, задач и подзадач, через System.out.println(..)
Измените статусы созданных объектов, распечатайте. Проверьте, что статус задачи и подзадачи сохранился,
а статус эпика рассчитался по статусам подзадач.
И, наконец, попробуйте удалить одну из задач и один из эпиков.  */

    /**
     * поменял название на test0
     */
    public static void test0() {
        createTask(new Task(), randomTaskNameCreator());
        createTask(new Task(), randomTaskNameCreator());
        createTask(new EpicTask(), randomTaskNameCreator());
        createTask(new SubTask(getTask(200002)), randomTaskNameCreator());
        createTask(new SubTask(getTask(200002)), randomTaskNameCreator());
        createTask(new EpicTask(), randomTaskNameCreator());
        createTask(new SubTask(getTask(200005)), randomTaskNameCreator());
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
        Random random = new Random();

        String[] word1 = {"помыть", "купить", "убрать", "разобрать", "приготовить"};
        String[] word2 = {"квартиру", "машину", "овощи", "холодильник",
                "вещи", "ружье", "сковороду", "компьютер"};
        String[] word3 = {"родителей", "на даче", "жены", "в поход",
                "в первый раз", "для переезда", "за один день"};
        return (word1[random.nextInt(word1.length)] + ' ' + word2[random.nextInt(word2.length)]
                + ' ' + word3[random.nextInt(word3.length)]);
    }

    public static <T extends Task> void createTask(T task, String name) {//Создание.
        // Сам объект должен передаваться в качестве параметра.
        task.setName(name);
        task.setDescription("Нужно просто пойти и " + name);
        task.setId(generateId(task));
        putTaskToMap(task);
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
        Random random = new Random();
        return Statuses.values()[random.nextInt(Statuses.values().length)].toString();
    }
}
