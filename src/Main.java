import management.Managers;
import management.history.*;
import management.task.*;
import task.*;

// Еще исправления.
public class Main {
    public static void main(String[] args) {
    TaskManager taskMan = Managers.getDefault();
    InMemoryHistoryManager histMan = (InMemoryHistoryManager) Managers.getDefaultHistory();
    //проверяем вызов метода Managers, но пользоваться будем конкретной реализацией, чтобы было
    // удобнее читать историю.

    taskMan.createTask(new EpicTask());
    EpicTask tripleSub = new EpicTask();
    taskMan.createTask(tripleSub);
    taskMan.createTask(new SubTask(tripleSub));
    taskMan.createTask(new SubTask(tripleSub));
    taskMan.createTask(new SubTask(tripleSub));
    System.out.println(taskMan.getTaskList());
    taskMan.getTask(200001);
    taskMan.getTask(200000);
    taskMan.getTask(300002);
    histMan.printHistoryList();
    taskMan.getTask(300004);
    taskMan.getTask(200001);
    histMan.printHistoryList();
    taskMan.removeTask(300004);
    taskMan.removeTask(200001);
    histMan.printHistoryList();
    }
}
