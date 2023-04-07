package management.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    static FileBackedTaskManager taskMan = new FileBackedTaskManager();

    @BeforeEach
    void createTaskMan() {
        super.taskMan = taskMan;
        taskMan.removeAllTasks();
    }

    @Test
    void createTask() {
        super.createTask();
    }

    @Test
    void renewTask() {
        super.renewTask();
    }

    @Test
    void getAndRemoveTask() {
        super.getAndRemoveTask();
    }

    @Test
    void removeAllTasks() {
        super.removeAllTasks();
    }
}