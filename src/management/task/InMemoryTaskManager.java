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

import static management.time.OneThreadTimeManager.DATE_TIME_FORMATTER;

public class InMemoryTaskManager implements TaskManager {

    protected HistoryManager histMan;
    protected TimeManager timeMan;
    protected int idCounter = 0;
    protected HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks;

    public InMemoryTaskManager() {
        initializeTasksMap();
        histMan = Managers.getDefaultHistory();
        timeMan = Managers.getDefaultTime();
    }

    public InMemoryTaskManager(HistoryManager histMan, TimeManager timeMan) {
        initializeTasksMap();
        this.histMan = histMan;
        this.timeMan = timeMan;
    }

    public static <T extends Task> T getTaskNH(int id, HashMap<TaskFamily,
            HashMap<Integer, ? super Task>> tasks) throws NoMatchesFoundException {
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
    }

    protected static TaskFamily defineTypeById(int id) throws NoMatchesFoundException {
        TaskFamily type = null;
        int idToOrdinal = Integer.parseInt(String.valueOf(Integer.toString(id).charAt(0))) - 1;

        for (TaskFamily TF : TaskFamily.values()) {
            if (idToOrdinal == TF.ordinal()) {
                type = TF;
                break;
            }
        }
        if (type == null) {
            throw new NoMatchesFoundException("Сектор \"банкрот\" на барабане!");
        }
        return type;
    }

    private static boolean isFoundById(int id, HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks)
            throws NoMatchesFoundException {
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
        if (task == null) {
            throw new NoMatchesFoundException("Сектор \"банкрот\" на барабане!");
        }
        return true;
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
        if (!(task instanceof EpicTask)) {
            timeMan.addToValidation(task);
            if (!timeMan.isTimeSet(task) && task.getStartTime() != null) {
                timeMan.setTime(task.getStartTime().format(DATE_TIME_FORMATTER),
                        task.getDuration(), task);
            }
        } else {
            EpicTask epic = (EpicTask) task;
            epic.setTime();
        }
    }

    public <T extends Task> void createTask(String startDateTime, int durationInMin, T task) {
        createTask(task);
        timeMan.setTime(startDateTime, durationInMin, task);
    }

    @Override
    public <T extends Task> void renewTask(T task) {
        putTaskToMap(task, tasks);
    }

    public <T extends Task> void renewTask(String startDateTime, int durationInMin, T task) {
        putTaskToMap(task, tasks);
        timeMan.setTime(startDateTime, durationInMin, task);
    }

    @Override
    public <T extends Task> void removeTask(int id) throws NoMatchesFoundException {
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
                    }
                    epic.removeMySubTaskMap();
                    break;
            }
            histMan.remove(id);
            timeMan.removeFromValidation(task);
            tasks.get(TaskFamily.getEnumFromClass(task.getClass())).remove(id);
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
    public ArrayList<String> getTaskList(TaskFamily type) {
        ArrayList<String> taskList = new ArrayList<>();
        Iterator<? extends Map.Entry<Integer, ? super Task>> iterator;

        if (isFoundType(type)) {
            iterator = tasks.get(type).entrySet().iterator();
            while (iterator.hasNext()) {
                taskList.add(iterator.next().toString().replace("" + System.lineSeparator() + "",
                        System.lineSeparator()));
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
    public void removeAllTasks(TaskFamily type) {
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

    public List<String> getEpicSubsList(EpicTask epic) {
        // всех подзадач определённого эпика.
        return epic.getMySubTaskList();
    }

    public List<Task> getPrioritizedTasks() {
        return timeMan.getPrioritizedTasks();
    }

    private void initializeTasksMap() {
        if (tasks == null || tasks.isEmpty()) {
            tasks = new HashMap<>();
            tasks.put(TaskFamily.TASK, new HashMap<>());
            tasks.put(TaskFamily.EPICTASK, new HashMap<>());
            tasks.put(TaskFamily.SUBTASK, new HashMap<>());
        }
    }

    private boolean isFoundType(TaskFamily type) throws NoMatchesFoundException {
        for (TaskFamily taskType : tasks.keySet()) {
            if (type.equals(taskType)) {
                return true;
            }
        }
        throw new NoMatchesFoundException("Задач типа " + type + " не существует!");
    }

    private int generateId(Task task) {
        DecimalFormat df = new DecimalFormat("00000");
        int id;
        TaskFamily TF = TaskFamily.getEnumFromClass(task.getClass());
        id = Integer.parseInt((TF.ordinal() + 1) + df.format(idCounter++));
        return id;
    }
}
