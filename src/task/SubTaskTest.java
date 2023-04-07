package task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {
    EpicTask epic;
    SubTask sub;

    @BeforeEach
    void createNewSubTaskTask() {
        epic = new EpicTask();
        sub = new SubTask(epic);
    }

    @Test
    void getMyEpic() {
        assertEquals(epic, sub.getMyEpic());
    }

    @Test
    void removeMyEpicLink() {
        assertEquals(epic, sub.getMyEpic());
        sub.removeMyEpicLink();
        assertNull(sub.getMyEpic());
    }

    @Test
    void getMyEpicId() {
        epic.setId(987654321);
        assertEquals(987654321, sub.getMyEpicId());
    }
}