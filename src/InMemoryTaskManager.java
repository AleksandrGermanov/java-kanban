import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager{

    protected HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks;
    private static int idCounter = 0;

    public InMemoryTaskManager(){
        initializeTasksMap();
    }
    @Override
    public void initializeTasksMap() {
        if (tasks == null || tasks.isEmpty()) {
            tasks = new HashMap<>();
            tasks.put(TaskFamily.TASK, new HashMap<>());
            tasks.put(TaskFamily.EPICTASK, new HashMap<>());
            tasks.put(TaskFamily.SUBTASK, new HashMap<>());
        }
    }

    @Override
    public int generateId(Task task) {
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
        }
        return id;
    }

    @Override
    public TaskFamily defineTypeById(int id) {
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

    @Override
       public <T extends Task> void createTask(T task) {
        if (task.getName() == null) {
            task.setName("Default");
        }
        if (task.getDescription() == null) {
            task.setDescription("No description");
        }
        task.setId(generateId(task));
        putTaskToMap(task);
    }

    @Override
    public <T extends Task> void putTaskToMap(T task) {
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

    @Override
    public <T extends Task> void renewTask(T task) {//Обновление.
        // Новая версия объекта с верным идентификатором передаётся в виде параметра.
        putTaskToMap(task);
    }

    @Override
    public boolean isFoundType(TaskFamily type) {
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

    @Override
    public ArrayList<String> getTaskList(TaskFamily type) {//Получение списка всех задач.
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

    @Override
    public ArrayList<String> getTaskList() {
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

    @Override
    public void removeAllTasks(TaskFamily type) {//Удаление всех задач.
        if (isFoundType(type)) {
            tasks.get(type).clear();
        }
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
        initializeTasksMap();
    }

    @Override
    public ArrayList<String> getEpicSubsList(EpicTask epic) { //Получение списка
        // всех подзадач определённого эпика.
        return epic.getMySubTaskList();
    }

    @Override
    public boolean isFoundById(int id) {
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

    @Override
    public <T extends Task> T getTask(int id) {//Получение по идентификатору
        T task = null;
        if (isFoundById(id))
            for (int index : tasks.get(defineTypeById(id)).keySet()) {
                if (index == id) {
                    task = (T) tasks.get(defineTypeById(id)).get(id);
                }
            }
        return task;
    }

    @Override
    public <T extends Task> void removeTask(int id) {
        try {
            if (!isFoundById(id)) {
                throw new NoMatchesFoundException("ID не нашлось!");
            } else {
                T task = getTask(id);

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
            }
        } catch (NoMatchesFoundException e) {
            e.printStackTrace();
            System.out.println("В этом методе 3 ссылки на метод, который кидает исключение.");
        }
    }
}
