import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Manager {
    protected static HashMap<String, HashMap<Integer, ? super Task>> tasks;//Возможность хранить задачи всех
    // типов. Для этого вам нужно выбрать подходящую коллекцию.
    private static int idCounter = 0;

    public static void initializeTasksMap() {
        if (tasks == null || tasks.isEmpty()) {
            tasks = new HashMap<>();
            tasks.put("Task", new HashMap<>());
            tasks.put("EpicTask", new HashMap<>());
            tasks.put("SubTask", new HashMap<>());
        }
    }

    static int generateId(Task task) {
        DecimalFormat df = new DecimalFormat("00000");
        int id;

        switch (task.getClass().getName()) {
            case "Task":
                id = Integer.parseInt(1 + df.format(idCounter++));
                break;
            case "EpicTask":
                id = Integer.parseInt(2 + df.format(idCounter++));
                break;
            case "SubTask":
                id = Integer.parseInt(3 + df.format(idCounter++));
                break;
            default:
                id = 0;
        }
        return id;
    }

    static String defineTypeById(int id) {
        String type = null;

        try {
            switch (Integer.toString(id).charAt(0)) {
                case '1':
                    type = "Task";
                    break;
                case '2':
                    type = "EpicTask";
                    break;
                case '3':
                    type = "SubTask";
                    break;
                default:
                    throw new Exception("Сектор \"банкрот\" на барабане!");
            }
        } catch (Exception e) {
            System.out.println("type=null, defineTypeById=null");
            e.printStackTrace();
            System.exit(1);
        }
        return type;
    }

    public static <T extends Task> void createTask(T task) {//Создание.
        // Сам объект должен передаваться в качестве параметра.
        task.setName("Default");
        task.setDescription("No description");
        task.setId(generateId(task));
        putTaskToMap(task);
    }

    static <T extends Task> void putTaskToMap(T task) {
        initializeTasksMap();
        switch (task.getClass().getName()) {
            case "SubTask":
                SubTask subTask = (SubTask) task;
                subTask.getMyEpic().getMySubTaskMap().put(subTask.getId(), subTask);
            case "EpicTask":
            case "Task":
                tasks.get(task.getClass().getName()).put(task.getId(), task);
                break;
        }
    }

    public static <T extends Task> void renewTask(T task) {//Обновление.
        // Новая версия объекта с верным идентификатором передаётся в виде параметра.
        putTaskToMap(task);
    }

    static boolean isFoundType(String type) {
        try {
            for (String taskType : tasks.keySet()) {
                if (type.equals(taskType)) {
                    return true;
                }
            }
            throw new Exception("Задач типа " + type + " не существует!");
        } catch (Exception e) {
            System.out.println("Доступные типы задач:\n"
                    + tasks.keySet());
            e.printStackTrace();
        }
        return false;
    }

    public static ArrayList<String> getTaskList(String type) {//Получение списка всех задач.
        ArrayList<String> taskList = new ArrayList<>();
        Iterator<? extends Map.Entry<Integer, ? super Task>> iterator;

        if (isFoundType(type)) {
            iterator = tasks.get(type).entrySet().iterator();
            while (iterator.hasNext()) {
                taskList.add(iterator.next().toString().replace("^\b", System.lineSeparator()));
            }
        }
        return taskList;
    }

    public static ArrayList<String> getTaskList() {
        ArrayList<String> taskList = new ArrayList<>();
        Iterator<Map.Entry<String, HashMap<Integer, ? super Task>>> iterator;
        iterator = tasks.entrySet().iterator();

        iterator.forEachRemaining(E -> {
                    String str = (getTaskList(E.getKey()).isEmpty())
                            ? E.getKey() + ":\n" + "Таких заданий нет.\n"
                            : E.getKey() + ":\n" + getTaskList(E.getKey()) + "\n";
                    taskList.add(str);
                }
        );
        return taskList;
    }

    public static void removeAllTasks(String type) {//Удаление всех задач.
        if (isFoundType(type)) {
            tasks.get(type).clear();
        }
    }

    public static void removeAllTasks() {
        tasks.clear();
    }

    public static ArrayList<String> getEpicSubsList(EpicTask epic) { //Получение списка
        // всех подзадач определённого эпика.
        return epic.getMySubTaskList();
    }

    static boolean isFoundById(int id) {
        Object task = null;
        boolean found = false;

        for (int index : tasks.get(defineTypeById(id)).keySet()) {
            if (index == id) {
                task = tasks.get(defineTypeById(id)).get(id);
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Объект с Id " + id + " не найден!");
        }
        try {
            if (task == null) {
                throw new Exception("Сектор \"банкрот\" на барабане!");
            }
        } catch (Exception e) {
            System.out.println("task=null, isFoundById=false");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static <T extends Task> T getTask(int id) {//Получение по идентификатору
        T task = null;
        if (isFoundById(id))
            for (int index : tasks.get(defineTypeById(id)).keySet()) {
                if (index == id) {
                    task = (T) tasks.get(defineTypeById(id)).get(id);
                }
            }
        return task;
    }

    public static <T extends Task> void removeTask(int id) {
        if (isFoundById(id)) {
            T task = getTask(id);
            switch (defineTypeById(id)) {
                case "SubTask":
                    SubTask subTask = (SubTask) task;
                    subTask.getMyEpic().getMySubTaskMap().remove(id);
                    tasks.get(task.getClass().getName()).remove(id);
                    break;
                case "EpicTask":
                    EpicTask epic = (EpicTask) task;
                    for (SubTask mySub : epic.getMySubTaskMap().values()) {
                        mySub.removeMyEpicLink();// это для GRC
                        tasks.get(mySub.getClass().getName()).remove(mySub.getId());
                    }
                case "Task":
                    tasks.get(task.getClass().getName()).remove(id);
                    break;
            }
        }
    }
}


        /* Возможность хранить задачи всех типов. Для этого вам нужно выбрать подходящую коллекцию.
        Методы для каждого из типа задач(Задача/Эпик/Подзадача):

        Удаление по идентификатору.
        Управление статусами осуществляется по следующему правилу:
        Менеджер сам не выбирает статус для задачи. Информация о нём приходит менеджеру вместе с информацией о самой задаче.
        По этим данным в одних случаях он будет сохранять статус, в других будет рассчитывать.
        */
