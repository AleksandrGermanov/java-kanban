package management.task;

import management.Managers;
import management.history.HistoryManager;
import myExceptions.NoMatchesFoundException;
import task.EpicTask;
import task.SubTask;
import task.Task;

import java.text.DecimalFormat;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected static HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks;
    protected static HistoryManager histMan;
    protected static int idCounter = 0;

    public InMemoryTaskManager() {
        initializeTasksMap();
        histMan = Managers.getDefaultHistory();
    }

    public void initializeTasksMap() {
        if (tasks == null || tasks.isEmpty()) {
            tasks = new HashMap<>();
            tasks.put(TaskFamily.TASK, new HashMap<>());
            tasks.put(TaskFamily.EPICTASK, new HashMap<>());
            tasks.put(TaskFamily.SUBTASK, new HashMap<>());
        }
    }

    public int generateId(Task task) {
        DecimalFormat df = new DecimalFormat("00000");
        int id = 0;
        try {
            TaskFamily TF = TaskFamily.getEnumFromClass(task.getClass());
            id = Integer.parseInt((TF.ordinal() + 1) + df.format(idCounter++));
        } catch (NoMatchesFoundException e) {
            e.printStackTrace();
            System.out.println("В этом методе 1 ссылка на метод, который кидает исключение.");
        }
        return id;
    }

    public static TaskFamily defineTypeById(int id) {
        TaskFamily type = null;
        int idToOrdinal = Integer.parseInt(String.valueOf(Integer.toString(id).charAt(0))) - 1;

        try {
            for (TaskFamily TF : TaskFamily.values()) {
                if (idToOrdinal == TF.ordinal()) {
                    type = TF;
                    break;
                }
            }
            if (type == null) {
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

    public static <T extends Task> void putTaskToMap(T task) {
        try {
            switch (TaskFamily.getEnumFromClass(task.getClass())) {
                case SUBTASK:
                    SubTask subTask = (SubTask) task;
                    subTask.getMyEpic().getMySubTaskMap().put(subTask.getId(), subTask);
                    subTask.getMyEpic().setStatus();
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
            for(Integer id : tasks.get(type).keySet()){
                histMan.remove(id);
            }
            tasks.get(type).clear();
        }
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
        histMan.clearHistory();
        initializeTasksMap();
    }

    public ArrayList<String> getEpicSubsList(EpicTask epic) { //Получение списка
        // всех подзадач определённого эпика.
        return epic.getMySubTaskList();
    }

    public static boolean isFoundById(int id) {
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
    public <T extends Task> T getTask(int id) {
        T task = getTaskNH(id);
        histMan.add(task);
        return task;
    }

    public static <T extends Task> T getTaskNH(int id) {//NH - no History
        T task = null;
        if (isFoundById(id))
            for (int index : tasks.get(defineTypeById(id)).keySet()) {
                if (index == id) {
                    task = (T) tasks.get(defineTypeById(id)).get(id);
                    break;
                }
            }
        return task;
    }

    //...добавить метод void remove(int id) для удаления задачи из просмотра.
    // И реализовать его в классе InMemoryHistoryManager.
    // Добавьте его вызов при удалении задач, чтобы они также удалялись из истории просмотров.
    @Override
    public <T extends Task> void removeTask(int id) {
        try {
            if (!isFoundById(id)) {
                throw new NoMatchesFoundException("ID не нашлось!");
            } else {
                T task = getTaskNH(id);

                switch (defineTypeById(id)) {
                    case SUBTASK:
                        SubTask subTask = (SubTask) task;
                        subTask.getMyEpic().getMySubTaskMap().remove(id);
                        subTask.getMyEpic().setStatus();
                        break;
                    case EPICTASK:
                        EpicTask epic = (EpicTask) task;
                        for (SubTask mySub : epic.getMySubTaskMap().values()) {
                            histMan.remove(mySub.getId());
                            tasks.get(TaskFamily.getEnumFromClass(mySub.getClass())).remove(mySub.getId());
                            mySub.removeMyEpicLink();// это для GC
                        }
                        epic.removeMySubTaskMap();// это тоже для GC
                        break;
                }
                histMan.remove(id);
                tasks.get(TaskFamily.getEnumFromClass(task.getClass())).remove(id);
            }
        } catch (NoMatchesFoundException e) {
            e.printStackTrace();
            System.out.println("В этом методе 3 ссылки на метод, который кидает исключение.");
        }
    }

    @Override
    public List<Task> getHistory() {
        return histMan.getHistory();
    }
}
