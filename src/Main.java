import exchange.CustomGson;
import exchange.HttpTaskServer;
import exchange.KVClient;
import exchange.KVServer;
import management.task.HttpTaskManager;
import task.EpicTask;
import task.Statuses;
import task.SubTask;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        KVServer kvs = new KVServer();
        kvs.start();
        KVClient client = new KVClient(URI.create("http://localhost:8078/"));
        System.out.println(client.loadState());
        HttpTaskManager taskMan = new HttpTaskManager(URI.create("http://localhost:8078/"));
        taskMan.createTask(new Task("task", "task_description"));
        EpicTask epic = new EpicTask("epic", "has 2 subs");
        taskMan.createTask(epic);
        SubTask subWithTime = new SubTask(epic, "sub with time", "epic's 1st");
        subWithTime.setStartTime(LocalDateTime.parse("12.04.2023 16:37",
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        subWithTime.setDuration(15);
        subWithTime.setEndTime(subWithTime.getStartTime().plusMinutes(subWithTime.getDuration()));
        epic.setTime();
        taskMan.createTask(subWithTime);
        taskMan.createTask(new SubTask(epic, "sub 2", "epic's 2nd"));
        taskMan.createTask(new Task());
        taskMan.getTask(100000).setStatus(Statuses.IN_PROGRESS);
        taskMan.renewTask(taskMan.getTask(100000));
        taskMan.getTask(300003).setStatus(Statuses.DONE);
        taskMan.renewTask(taskMan.getTask(300003));
        System.out.println(taskMan.getTaskList());
        CustomGson cg = new CustomGson(taskMan);
        System.out.println(cg.getGsonForHttpManager().toJson(taskMan.getTask(100000)));
        kvs.stop();
    }
}
