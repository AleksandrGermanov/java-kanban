package management.task;

import com.google.gson.reflect.TypeToken;
import exchange.CustomGson;
import exchange.KVClient;
import management.history.HistoryManager;
import myExceptions.ManagerIOException;
import task.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static management.time.OneThreadTimeManager.DATE_TIME_FORMATTER;

public class HttpTaskManager extends FileBackedTaskManager {

    protected final KVClient client;

    public HttpTaskManager(URI kvServerURI) {
        try {
            client = new KVClient(kvServerURI);
            loadFromServer(client, tasks, histMan, this);
        } catch (IOException | InterruptedException e) {
            throw new ManagerIOException(e);
        }
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

    private static void loadFromServer(KVClient client, HashMap<TaskFamily, HashMap<Integer, ? super Task>> tasks,
                                       HistoryManager histMan, InMemoryTaskManager taskMan) {
        try {
            String gson = client.loadState();
            Type listOfStrings = new TypeToken<List<String>>() {
            }.getType();
            List<String> file = CustomGson.getSimplePrettyGson().fromJson(gson, listOfStrings);

            if (file != null && !file.isEmpty()) {
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
                taskMan.idCounter = getIdCounterStateFromFile(file.get(file.size() - 3));
            }
        } catch (IOException | InterruptedException e) {
            throw new ManagerIOException(e);
        }
    }

    public <T extends Task> void setTime(LocalDateTime start, int duration, T task) {
        if (start != null) {
            timeMan.setTime(start.format(DATE_TIME_FORMATTER), duration, task);
        }
    }

    private <T extends Task> List<String> writeCSVMapToList() {
        List<String> state = new ArrayList<>(20);
        String toList = "id,type,name,status,description,start,duration,end,epic";
        state.add(toList);
        if (!tasks.isEmpty()) {
            for (TaskFamily TF : TaskFamily.values()) {
                for (Object task : tasks.get(TF).values()) {
                    toList = taskToString((T) task);
                    state.add(toList);
                }
            }
        } else {
            state.add("No tasks");
        }
        return state;
    }

    private void writeHistoryToList(String historyCSV, List<String> state) throws IOException {
        state.add("History first-to-last");
        state.add(historyCSV);
    }

    private void writeIdCounterStateToList(List<String> state) throws IOException {
        state.add("idCounter");
        state.add(String.valueOf(idCounter));
    }

    private void save() {
        try {
            List<String> state = writeCSVMapToList();
            writeIdCounterStateToList(state);
            writeHistoryToList(historyToString(histMan), state);
            Type listOfStrings = new TypeToken<List<String>>() {
            }.getType();
            client.saveState(CustomGson.getSimplePrettyGson().toJson(state, listOfStrings));
        } catch (IOException | InterruptedException e) {
            throw new ManagerIOException(e);
        }
    }
}

