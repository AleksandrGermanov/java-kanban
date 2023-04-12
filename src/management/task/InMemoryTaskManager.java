package management.task;

import management.Managers;
import management.history.HistoryManager;
import management.time.TimeManager;
import myExceptions.NoMatchesFoundException;
import task.EpicTask;
import task.SubTask;
import task.Task;

import java.text.DecimalFormat;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected HistoryManager histMan;
    protected TimeManager timeMan;
    protected int idCounter = 0;
    protected HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks;

    /***
     * "Объекты HistoryManager и TimeManager можно передавать в конструкторе"
     * Вот этого комментария я не понял - они и так создаются в конструкторе.
     */
    public InMemoryTaskManager() {
        initializeTasksMap();
        histMan = Managers.getDefaultHistory();
        timeMan = Managers.getDefaultTime();
    }

    public InMemoryTaskManager(HistoryManager histMan, TimeManager timeMan){
        initializeTasksMap();
        this.histMan = histMan;
        this.timeMan = timeMan;
    }

    public HistoryManager getHistMan() {
        return histMan;
    }

    public TimeManager getTimeMan() {
        return timeMan;
    }

    public HashMap<TaskFamily, HashMap<Integer, ? super Task>> getTasks() {
        return tasks;
    }

    public void setTasks(HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks) {
        this.tasks = tasks;
    }

    protected static <T extends Task> T getTaskNH(int id, HashMap<TaskFamily,
            HashMap<Integer, ? super Task>> tasks) {//NH - no History
        T task = null;
        if (isFoundById(id, tasks))
            for (int index : tasks.get(defineTypeById(id)).keySet()) {
                if (index == id) {
                    task = (T) tasks.get(defineTypeById(id)).get(id);
                    break;
                }
            }
        return task;
    }

    protected static <T extends Task> void putTaskToMap(T task,
                                                        HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks) {
        try {
            switch (TaskFamily.getEnumFromClass(task.getClass())) {
                case SUBTASK:
                    SubTask subTask = (SubTask) task;
                    subTask.getMyEpic().getMySubTaskMap().put(subTask.getId(), subTask);
                    subTask.getMyEpic().setStatus();
                    subTask.getMyEpic().setTime();
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

    protected static TaskFamily defineTypeById(int id) {
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

    private static boolean isFoundById(int id, HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks) {
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
    public <T extends Task> void createTask(T task) {
        if (task.getName() == null) {
            task.setName("Default");
        }
        if (task.getDescription() == null) {
            task.setDescription("No description");
        }
        task.setId(generateId(task));
        putTaskToMap(task, tasks);
        if(!(task instanceof EpicTask)){
            timeMan.addToValidation(task);
        }
    }

    public <T extends Task> void createTask(String startDateTime, int durationInMin, T task) {
        createTask(task);
        timeMan.setTime(startDateTime, durationInMin, task);
    }

    @Override
    public <T extends Task> void renewTask(T task) {//Обновление.
        // Новая версия объекта с верным идентификатором передаётся в виде параметра.
        putTaskToMap(task, tasks);
    }

    public <T extends Task> void renewTask(String startDateTime, int durationInMin, T task) {
        putTaskToMap(task, tasks);
        timeMan.setTime(startDateTime, durationInMin, task);
    }

    @Override
    public <T extends Task> void removeTask(int id) {
        try {
            if (!isFoundById(id, tasks)) {
                throw new NoMatchesFoundException("ID не нашлось!");
            } else {
                T task = getTaskNH(id, tasks);

                switch (defineTypeById(id)) {
                    case SUBTASK:
                        SubTask subTask = (SubTask) task;
                        subTask.getMyEpic().getMySubTaskMap().remove(subTask.getId());
                        subTask.getMyEpic().setStatus();
                        subTask.getMyEpic().setTime();
                        break;
                    case EPICTASK:
                        EpicTask epic = (EpicTask) task;
                        for (SubTask mySub : epic.getMySubTaskMap().values()) {
                            histMan.remove(mySub.getId());
                            timeMan.removeFromValidation(task);
                            tasks.get(TaskFamily.getEnumFromClass(mySub.getClass())).remove(mySub.getId());
                            mySub.removeMyEpicLink();// это для GC
                        }
                        epic.removeMySubTaskMap();// это тоже для GC
                        break;
                }
                histMan.remove(id);
                timeMan.removeFromValidation(task);
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

    @Override
    public <T extends Task> T getTask(int id) {
        T task = getTaskNH(id, tasks);
        histMan.add(task);
        return task;
    }

    @Override
    public ArrayList<String> getTaskList(TaskFamily type) {//Получение списка всех задач.
        ArrayList<String> taskList = new ArrayList<>();
        Iterator<? extends Map.Entry<Integer, ? super Task>> iterator;

        if (isFoundType(type)) {
            iterator = tasks.get(type).entrySet().iterator();
            while (iterator.hasNext()) {
                taskList.add(iterator.next().toString().replace("" + System.lineSeparator() + "", System.lineSeparator()));
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
            for (Integer id : tasks.get(type).keySet()) {
                removeTask(id);
            }
        }
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
        histMan.clearHistory();
        timeMan.clear();
        initializeTasksMap();
    }

    public ArrayList<String> getEpicSubsList(EpicTask epic) { //Получение списка
        // всех подзадач определённого эпика.
        return epic.getMySubTaskList();
    }

    private void initializeTasksMap() {
        if (tasks == null || tasks.isEmpty()) {
            tasks = new HashMap<>();
            tasks.put(TaskFamily.TASK, new HashMap<>());
            tasks.put(TaskFamily.EPICTASK, new HashMap<>());
            tasks.put(TaskFamily.SUBTASK, new HashMap<>());
        }
    }

    private boolean isFoundType(TaskFamily type) {
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

    private int generateId(Task task) {
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
}
