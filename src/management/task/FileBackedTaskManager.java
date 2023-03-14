package management.task;

import management.history.HistoryManager;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final Path csvPath = Paths.get(System.getProperty("user.dir"), "data.csv");

    public FileBackedTaskManager() {
    }
    public FileBackedTaskManager(Path csvPath) {
        loadFromFile(csvPath);
    }

    /***
     *Спасибо за комментарии!
     *
     * "Лишнюю" инициализацию удалил.
     * Она использовалась для тестирования сценария,
     * при котором мы имеем пустой файл data.csv
     * (первая инициализация создавала этот файл).
     *
     * По поводу switch - у меня сейчас Amazon Corretto 11,
     * согласно требованиям курса.
     * (Спринт 1/10: 1 → Тема 6/7: JDK и среда разработки → Урок 2/6)
     */
    public static void main(String[] args) {
        try {
            Files.deleteIfExists(csvPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("проверяем работу с пустыми данными");
        TestTaskManager ttm = new TestTaskManager(csvPath);
        System.out.println(ttm.getTaskList());
        System.out.println(ttm.getHistory());

        System.out.println("\n\n\nпроверяем создание задач");
        ttm.createTask(new Task(",,,,,,", "Проверяем конверсию \",\""));
        ttm.createRandomTasks(9);
        System.out.println(ttm.getTaskList());
        System.out.println(ttm.getHistory());

        System.out.println("\n\n\nпроверяем формирование истории");
        ttm = new TestTaskManager(csvPath);
        System.out.println(ttm.getTaskList());
        ttm.randomTaskGetter(10);
        ttm.hm.printHistoryList();

        System.out.println("\n\n\nпроверяем восстановление данных");
        ArrayList<String> oldTaskList = ttm.getTaskList();
        List<Task> oldHistory = ttm.getHistory();
        ttm = new TestTaskManager(csvPath);
        System.out.println("сравниваем листы задач: " + oldTaskList.equals(ttm.getTaskList()));
        System.out.println("сравниваем листы истории: " + ttm.getHistory().equals(oldHistory));

        System.out.println("\n\n\nпроверяем обновление задач");
        ttm = new TestTaskManager(csvPath);
        System.out.println(ttm.getTaskList());
        ttm.renewRandomTasks(60);
        System.out.println(ttm.getTaskList());
        ttm.hm.printHistoryList();

        System.out.println("\n\nпроверяем восстановление данных");
        ArrayList<String> oldTaskList2 = ttm.getTaskList();
        List<Task> oldHistory2 = ttm.getHistory();
        ttm = new TestTaskManager(csvPath);
        System.out.println("сравниваем листы задач: " + oldTaskList2.equals(ttm.getTaskList()));
        System.out.println("сравниваем листы истории: " + ttm.getHistory().equals(oldHistory2));

        System.out.println("\n\n\nпроверяем удаление задач");
        ttm = new TestTaskManager(csvPath);
        System.out.println(ttm.getTaskList());
        ttm.removeRandomTasks(30);
        System.out.println(ttm.getTaskList());
        ttm.hm.printHistoryList();

        System.out.println("\n\nпроверяем восстановление данных");
        ArrayList<String> oldTaskList3 = ttm.getTaskList();
        List<Task> oldHistory3 = ttm.getHistory();
        ttm = new TestTaskManager(csvPath);
        System.out.println("сравниваем листы задач: " + oldTaskList3.equals(ttm.getTaskList()));
        System.out.println("сравниваем листы истории: " + ttm.getHistory().equals(oldHistory3));
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

    private static <T extends Task> String taskToString(T task) { //в тз String toString(Task task)
        String data = null;

        try {
            data = task.getId() + "," + defineTypeById(task.getId())
                    + "," + task.getName().replace(',', '¶')//Если пользователь ввел в имени
                    //или описании задачи запятую, без замены символа обратная сборка поломается,
                    //т.к. сплит неправильно поделит поля.
                    + "," + task.getStatus()
                    + "," + task.getDescription().replace(',', '¶');
            if (TaskFamily.getEnumFromClass(task.getClass()).equals(TaskFamily.SUBTASK)) {
                SubTask sub = (SubTask) task;
                data += "," + sub.getMyEpicId();
            }
        } catch (NoMatchesFoundException e) {
            e.printStackTrace();
        }

        return data;
    }

    private <T extends Task> void writeCSVMapToFile() throws IOException {
        String toFile = "id,type,name,status,description,epic";
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

    private void writeHistoryToFile(String historyCSV) throws IOException {
        Files.writeString(csvPath, "History first-to-last\n",
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        Files.writeString(csvPath, historyCSV,
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    private void save() {
        try {
            writeCSVMapToFile();
            writeHistoryToFile(historyToString(histMan));
        } catch (IOException e) {
            throw new ManagerSaveException(e);
        }
    }

    private static Task fromString(String data) {
        Task value = null;
        String[] fields = data.split(",");

        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].replace('¶', ',');
        }
        //id,type,name,status,description,epic
        TaskFamily TF = TaskFamily.valueOf(fields[1]);
        switch (TF) {
            case TASK:
                Task task = new Task(fields[2], fields[4]);
                task.setId(Integer.parseInt(fields[0]));
                task.setStatus(Statuses.valueOf(fields[3]));
                value = task;
                break;
            case EPICTASK:
                EpicTask epic = new EpicTask(fields[2], fields[4]);
                epic.setId(Integer.parseInt(fields[0]));
                value = epic;
                break;
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                if (tasks.get(TaskFamily.EPICTASK).containsKey(epicId)) {
                    SubTask sub = new SubTask(getTaskNH(epicId), fields[2], fields[4]);
                    sub.setId(Integer.parseInt(fields[0]));
                    sub.setStatus(Statuses.valueOf(fields[3]));
                    sub.getMyEpic().setStatus();
                    value = sub;
                }
                break;
        }
        return value;
    }

    private static <T extends Task> void restoreTaskToMap(T task) {
        putTaskToMap(task);
        idCounter++;
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

    private static void idListToHistory(List<Integer> idList) {
        if (!idList.isEmpty()) {
            for (int id : idList) {
                histMan.add(getTaskNH(id));
            }
        }
    }

    private static void loadFromFile(Path csvPath) { //в тз в качестве аргумента File file
        try {
            if (!Files.exists(csvPath)) {
                Files.createFile(csvPath);
            }

            List<String> file = Files.readAllLines(csvPath);

            if (!file.isEmpty()) {
                List<String> subTaskStrings = new ArrayList<>();
                for (int i = 1; i < file.size() - 2; i++) {
                    String data = file.get(i);
                    if (data.equals("No tasks")) {
                        break;
                    }
                    if (data.startsWith(String.valueOf(TaskFamily.SUBTASK.ordinal() + 1))) {
                        subTaskStrings.add(data);
                    } else {
                        restoreTaskToMap(fromString(data));
                    }
                }
                for (String data : subTaskStrings) {
                    restoreTaskToMap(fromString(data));
                }
                idListToHistory(historyFromString(file.get(file.size() - 1)));
            }
        } catch (IOException e) {
            throw new ManagerSaveException(e);
        }
    }
}