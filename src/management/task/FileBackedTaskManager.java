package management.task;

import management.history.HistoryManager;
import management.time.TimeManager;
import myExceptions.ManagerSaveException;
import myExceptions.NoMatchesFoundException;
import task.EpicTask;
import task.Statuses;
import task.SubTask;
import task.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static management.time.OneThreadTimeManager.DATE_TIME_FORMATTER;


public class FileBackedTaskManager extends InMemoryTaskManager {

    protected final Path csvPath;

    public Path getCsvPath() {
        return csvPath;
    }

    public FileBackedTaskManager() {
        csvPath = Paths.get(System.getProperty("user.dir"), "data.csv");
        loadFromFile(csvPath, tasks, histMan, this);
    }

    public FileBackedTaskManager(Path csvPath) {
        this.csvPath = csvPath;
        loadFromFile(csvPath, tasks, histMan, this);
    }

    public FileBackedTaskManager(Path csvPath, HistoryManager histMan, TimeManager timeMan){
        super(histMan, timeMan);
        this.csvPath = csvPath;
        loadFromFile(csvPath, tasks, histMan, this);
    }

    /***
     */
    public static void main(String[] args) {
        FileBackedTaskManager taskMan = new FileBackedTaskManager();
        taskMan.createTask(new Task("task", "task_description"));
        EpicTask epic = new EpicTask("epic", "has 2 subs");
        taskMan.createTask(epic);
        SubTask subWithTime = new SubTask(epic, "sub with time", "epic's 1st");
        subWithTime.setStartTime(LocalDateTime.now());
        subWithTime.setDuration(15);
        subWithTime.setEndTime(subWithTime.getStartTime().plusMinutes(subWithTime.getDuration()));
        epic.setTime();
        taskMan.createTask(subWithTime);
        taskMan.createTask(new SubTask(epic, "sub 2", "epic's 2nd"));
        taskMan.createTask(new Task());
        taskMan.getTask(300002);
    }

    private static <T extends Task> String taskToString(T task) { //в тз String toString(Task task)
        String data = null;
        String startTime = task.getStartTime() == null
                ? "null" : task.getStartTime().format(DATE_TIME_FORMATTER);
        String duration = task.getStartTime() == null
                ? "null" : task.getDuration().toString();
        String endTime = task.getEndTime() == null
                ? "null" : task.getEndTime().format(DATE_TIME_FORMATTER);

        try {
            data = task.getId() + "," + defineTypeById(task.getId())
                    + "," + task.getName().replace(',', '¶')//Если пользователь ввел в имени
                    //или описании задачи запятую, без замены символа обратная сборка поломается,
                    //т.к. сплит неправильно поделит поля.
                    + "," + task.getStatus()
                    + "," + task.getDescription().replace(',', '¶')
                    + "," + startTime + "," + duration + "," + endTime;
            if (TaskFamily.getEnumFromClass(task.getClass()).equals(TaskFamily.SUBTASK)) {
                SubTask sub = (SubTask) task;
                data += "," + sub.getMyEpicId();
            }
        } catch (NoMatchesFoundException e) {
            e.printStackTrace();
        }

        return data;
    }

    private static String historyToString(HistoryManager histMan) {
        StringBuilder history = new StringBuilder();
        for (Task task : histMan.getHistory()) {
            history.append(task.getId()).append(",");
        }
        if (history.length() > 0) {
            history.delete(history.length() - 1, history.length());
        } else {
            history.append("No history");
        }
        return history.toString();
    }

    private static Task fromString(String data, HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks) {
        Task value = null;
        String[] fields = data.split(",");

        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].replace('¶', ',');
        }
        //id,type,name,status,description,start,duration,end,epic
        TaskFamily TF = TaskFamily.valueOf(fields[1]);
        switch (TF) {
            case TASK:
                Task task = new Task(fields[2], fields[4]);
                task.setId(Integer.parseInt(fields[0]));
                task.setStatus(Statuses.valueOf(fields[3]));
                setTimeFromString(task, fields[5], fields[6], fields[7]);
                value = task;
                break;
            case EPICTASK:
                EpicTask epic = new EpicTask(fields[2], fields[4]);
                epic.setId(Integer.parseInt(fields[0]));
                value = epic;
                break;
            case SUBTASK:
                int epicId = Integer.parseInt(fields[8]);
                if (tasks.get(TaskFamily.EPICTASK).containsKey(epicId)) {
                    SubTask sub = new SubTask(getTaskNH(epicId, tasks), fields[2], fields[4]);
                    sub.setId(Integer.parseInt(fields[0]));
                    sub.setStatus(Statuses.valueOf(fields[3]));
                    setTimeFromString(sub, fields[5], fields[6], fields[7]);
                    value = sub;
                }
                break;
        }
        return value;
    }

    private static <T extends Task> void setTimeFromString(T task, String start, String duration, String end) {
        LocalDateTime startOpt = start.equals("null")
                ? null : LocalDateTime.parse(start, DATE_TIME_FORMATTER);
        task.setStartTime(startOpt);
        Integer duraOpt = duration.equals("null")
                ? null : Integer.parseInt(duration);
        task.setDuration(duraOpt);
        LocalDateTime endOpt = end.equals("null")
                ? null : LocalDateTime.parse(end, DATE_TIME_FORMATTER);
        task.setEndTime(endOpt);
    }

    private static <T extends Task> void restoreTaskToMap(T task,
                                                          HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks) {
        putTaskToMap(task, tasks);
    }

    private static List<Integer> historyFromString(String data) {
        List<Integer> idList = Collections.emptyList();

        if (!data.equals("No history")) {
            String[] ids = data.split(",");
            idList = new ArrayList<>(ids.length);
            for (String id : ids) {
                idList.add(Integer.parseInt(id));
            }
        }
        return idList;
    }

    private static void idListToHistory(List<Integer> idList,
                                        HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks,
                                        HistoryManager histMan) {
        if (!idList.isEmpty()) {
            for (int id : idList) {
                histMan.add(getTaskNH(id, tasks));
            }
        }
    }

    private static void loadFromFile(Path csvPath, HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks,
                                     HistoryManager histMan, InMemoryTaskManager tm) {
        try {
            if (!Files.exists(csvPath)) {
                Files.createFile(csvPath);
            }

            List<String> file = Files.readAllLines(csvPath);

            if (!file.isEmpty()) {
                List<String> subTaskStrings = new ArrayList<>();
                for (int i = 1; i < file.size() - 4; i++) {
                    String data = file.get(i);
                    if (data.equals("No tasks")) {
                        break;
                    }
                    if (data.startsWith(String.valueOf(TaskFamily.SUBTASK.ordinal() + 1))) {
                        subTaskStrings.add(data);
                    } else {
                        restoreTaskToMap(fromString(data, tasks), tasks);
                    }
                }
                for (String data : subTaskStrings) {
                    restoreTaskToMap(fromString(data, tasks), tasks);
                }
                idListToHistory(historyFromString(file.get(file.size() - 1)), tasks, histMan);
                tm.idCounter = getIdCounterStateFromFile(file.get(file.size() - 3));
            }
        } catch (IOException e) {
            throw new ManagerSaveException(e);
        }
    }

    private static int getIdCounterStateFromFile(String state) {
        return Integer.parseInt(state);
    }

    @Override
    public <T extends Task> void createTask(T task) {
        super.createTask(task);
        save();
    }

    @Override
    public <T extends Task> void renewTask(T task) {
        super.renewTask(task);
        save();
    }

    @Override
    public <T extends Task> void renewTask(String startDateTime, int durationInMin, T task) {
        super.renewTask(startDateTime, durationInMin, task);
        save();
    }

    @Override
    public <T extends Task> T getTask(int id) {
        T task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeAllTasks(TaskFamily type) {//Удаление всех задач.
        super.removeAllTasks(type);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    private <T extends Task> void writeCSVMapToFile() throws IOException {
        String toFile = "id,type,name,status,description,start,duration,end,epic";
        Files.writeString(csvPath, toFile + "\n",
                StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
        if (!tasks.isEmpty()) {
            for (TaskFamily TF : TaskFamily.values()) {
                for (Object task : tasks.get(TF).values()) {
                    toFile = taskToString((T) task);
                    Files.writeString(csvPath, toFile + "\n",
                            StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                }
            }
        } else {
            Files.writeString(csvPath, "No tasks\n",
                    StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        }
    }

    private void writeHistoryToFile(String historyCSV) throws IOException {
        Files.writeString(csvPath, "History first-to-last\n",
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        Files.writeString(csvPath, historyCSV,
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    private void writeIdCounterStateToFile() throws IOException {
        Files.writeString(csvPath, "idCounter\n",
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        Files.writeString(csvPath, String.valueOf(idCounter) + '\n',
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    private void save() {
        try {
            writeCSVMapToFile();
            writeIdCounterStateToFile();
            writeHistoryToFile(historyToString(histMan));
        } catch (IOException e) {
            throw new ManagerSaveException(e);
        }
    }
}