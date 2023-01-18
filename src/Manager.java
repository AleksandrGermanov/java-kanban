import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Manager {
    static {
        initializeTasksMap();

    }

    protected static HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks;//Возможность хранить задачи всех
    // типов. Для этого вам нужно выбрать подходящую коллекцию.
    private static int idCounter = 0;

    /**
     * Теперь этот метод реализуется в static блоке,
     * и @removeAllTasks().
     * Проверки на null и на пустую Map оставил, иначе
     * возможна незапланированная потеря данных.
     */
    public static void initializeTasksMap() {
        if (tasks == null || tasks.isEmpty()) {
            tasks = new HashMap<>();
            tasks.put(TaskFamily.TASK, new HashMap<>());
            tasks.put(TaskFamily.EPICTASK, new HashMap<>());
            tasks.put(TaskFamily.SUBTASK, new HashMap<>());
        }
    }

    static int generateId(Task task) {
        DecimalFormat df = new DecimalFormat("00000");
        int id = 0;
        try {
            switch (TaskFamily.getEnumFromClass(task.getClass())) {
                case TASK:
                    id = Integer.parseInt((TaskFamily.TASK.ordinal() + 1) + df.format(idCounter++));
                    break;
                case EPICTASK:
                    id = Integer.parseInt((TaskFamily.EPICTASK.ordinal() + 1) + df.format(idCounter++));
                    break;
                case SUBTASK:
                    id = Integer.parseInt((TaskFamily.SUBTASK.ordinal() + 1) + df.format(idCounter++));
                    break;
            }
        } catch (NoMatchesFoundException e) {
            e.printStackTrace();
            System.out.println("В этом методе 1 ссылка на метод, который кидает исключение.");
            ;
        }
        return id;
    }

    /**
     * Исправлять не стал:
     * не вижу ничего плохого в том, чтобы ловить
     * исключения сразу после их возникновения.
     * Null может жить достаточно долго, и не всегда
     * printStackTrace() отслеживает его до метода,
     * в котором он возник. А если не прерывать программу,
     * можно узнать, например, сколько метод этих нулей сгенерировал,
     * что имеет большую ценность при отладке.
     * Для того чтобы показать работу с unhandled,
     * пробросил исключение @TaskFamily.getEnumFromClass(),
     * Дальше пробрасывать не стал.
     */
    static TaskFamily defineTypeById(int id) {
        TaskFamily type = null;
        int taskOrdinal = TaskFamily.TASK.ordinal();
        int epicTaskOrdinal = TaskFamily.EPICTASK.ordinal();
        int subTaskOrdinal = TaskFamily.SUBTASK.ordinal();
        int idToOrdinal = Integer.parseInt(String.valueOf(Integer.toString(id).charAt(0))) - 1;

        try {
            if (idToOrdinal == taskOrdinal) {
                type = TaskFamily.TASK;
            } else if (idToOrdinal == epicTaskOrdinal) {
                type = TaskFamily.EPICTASK;
            } else if (idToOrdinal == subTaskOrdinal) {
                type = TaskFamily.SUBTASK;
            } else {
                throw new NoMatchesFoundException("Сектор \"банкрот\" на барабане!");
            }
        } catch (NoMatchesFoundException e) {
            System.out.println("type=null, defineTypeById=null");
            e.printStackTrace();
        }
        return type;
    }

    /**
     * Вячеслав, спасибо за разъяснения.
     * Этот метод все равно надо параметризовать
     * (например enum'ом), иначе непонятно,
     * какого класса Task мы хотим создать. К тому же,
     * чтобы создать Subtask, обязательным условием является наличие
     * эпика, прийдется в ручную это указывать(передавать аргументом) -
     * метод надо будет перегружать. Либо делать отдельный
     * метод для каждого класса.
     * А вот перегруженный метод в TestManager действительно не нужен.
     * А создание рандомных Task'ов смотри @TestManager.task1().
     */
    public static <T extends Task> void createTask(T task) {
        //Допустим параметров нет:
        //T task = ?
        String name = TestManager.randomTaskNameCreator();
        task.setName(name);
        task.setDescription("Нужно просто пойти и " + name);
        task.setId(generateId(task));
        putTaskToMap(task);
    }

    static <T extends Task> void putTaskToMap(T task) {
        try {
            switch (TaskFamily.getEnumFromClass(task.getClass())) {
                case SUBTASK:
                    SubTask subTask = (SubTask) task;
                    subTask.getMyEpic().getMySubTaskMap().put(subTask.getId(), subTask);
                case EPICTASK:
                case TASK:
                    tasks.get(TaskFamily.getEnumFromClass(task.getClass())).put(task.getId(), task);
                    break;
            }
        } catch (NoMatchesFoundException e) {
            e.printStackTrace();
            System.out.println("В этом методе 2 ссылки на метод, который кидает исключение.");
        }
    }

    public static <T extends Task> void renewTask(T task) {//Обновление.
        // Новая версия объекта с верным идентификатором передаётся в виде параметра.
        putTaskToMap(task);
    }

    static boolean isFoundType(TaskFamily type) {
        try {
            for (TaskFamily taskType : tasks.keySet()) {
                if (type.equals(taskType)) {
                    return true;
                }
            }
            throw new NoMatchesFoundException("Задач типа " + type + " не существует!");
        } catch (NoMatchesFoundException e) {
            System.out.println("Доступные типы задач:\n"
                    + tasks.keySet());
            e.printStackTrace();
        }
        return false;
    }

    public static ArrayList<String> getTaskList(TaskFamily type) {//Получение списка всех задач.
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
        Iterator<Map.Entry<TaskFamily, HashMap<Integer, ? super Task>>> iterator;
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

    public static void removeAllTasks(TaskFamily type) {//Удаление всех задач.
        if (isFoundType(type)) {
            tasks.get(type).clear();
        }
    }

    public static void removeAllTasks() {
        tasks.clear();
        initializeTasksMap();
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
                throw new NoMatchesFoundException("Сектор \"банкрот\" на барабане!");
            }
        } catch (NoMatchesFoundException e) {
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
            try {
                switch (defineTypeById(id)) {
                    case SUBTASK:
                        SubTask subTask = (SubTask) task;
                        subTask.getMyEpic().getMySubTaskMap().remove(id);
                        tasks.get(TaskFamily.getEnumFromClass(task.getClass())).remove(id);
                        break;
                    case EPICTASK:
                        EpicTask epic = (EpicTask) task;
                        for (SubTask mySub : epic.getMySubTaskMap().values()) {
                            mySub.removeMyEpicLink();// это для GC
                            tasks.get(TaskFamily.getEnumFromClass(mySub.getClass())).remove(mySub.getId());
                            epic.removeMySubTaskMap();// это тоже для GC
                        }
                    case TASK:
                        tasks.get(TaskFamily.getEnumFromClass(task.getClass())).remove(id);
                        break;
                }
            } catch (NoMatchesFoundException e) {
                e.printStackTrace();
                System.out.println("В этом методе 3 ссылки на метод, который кидает исключение.");
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
