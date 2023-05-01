package test.task;

import myExceptions.IllegalStatusChangeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.Statuses;
import task.SubTask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class EpicTaskTest {
    EpicTask epic;
    SubTask sub;

    @BeforeEach
    void createNewEpicAndSub() {
        epic = new EpicTask();
        sub = new SubTask(epic);
        sub.setId(1);
        epic.getMySubTaskMap().put(sub.getId(), sub);
    }

    @Test
    void setStatus() {
        epic.setStatus(Statuses.IN_PROGRESS);
        assertEquals(Statuses.NEW, epic.getStatus());
        sub.setStatus(Statuses.DONE);
        epic.setStatus();
        assertEquals(Statuses.DONE, epic.getStatus());
        epic.getMySubTaskMap().put(2, new SubTask(epic));
        epic.setStatus();
        assertEquals(Statuses.IN_PROGRESS, epic.getStatus());
        for (SubTask sub : epic.getMySubTaskMap().values()) {
            sub.setStatus(Statuses.IN_PROGRESS);
        }
        epic.setStatus();
        assertEquals(Statuses.IN_PROGRESS, epic.getStatus());
        epic.getMySubTaskMap().clear();
        epic.setStatus();
        assertEquals(Statuses.NEW, epic.getStatus());

    }

    @Test
    void setStartTime() {
        LocalDateTime testLDT = LocalDateTime.now();
        epic.setStartTime(testLDT);
        assertNull(epic.getStartTime());
        sub.setStartTime(testLDT);
        epic.setStartTime();
        assertEquals(testLDT, epic.getStartTime());
    }

    @Test
    void setDuration() {
        Integer testDuration = 10;
        epic.setDuration(15);
        assertNull(epic.getDuration());
        sub.setDuration(testDuration);
        epic.setDuration();
        assertEquals(testDuration, epic.getDuration());
    }

    @Test
    void setEndTime() {
        LocalDateTime testLDT = LocalDateTime.now();
        epic.setEndTime(testLDT);
        assertNull(epic.getEndTime());
        sub.setEndTime(testLDT);
        epic.setEndTime();
        assertEquals(testLDT, epic.getEndTime());
    }

    @Test
    void getMySubTaskMap() {
        Map<Integer, SubTask> testMap = new HashMap<>();
        testMap.put(sub.getId(), sub);
        assertEquals(testMap, epic.getMySubTaskMap());
        epic.getMySubTaskMap().clear();
        assertTrue(epic.getMySubTaskMap().isEmpty());
    }

    @Test
    void getMySubTaskList() {
        ArrayList<String> testList = new ArrayList<>();
        Map<Integer, SubTask> testMap = Map.of(sub.getId(), sub);
        for (Map.Entry<Integer, SubTask> entry : testMap.entrySet()) {
            testList.add(entry.toString());
        }
        assertEquals(testList, epic.getMySubTaskList());
        epic.getMySubTaskMap().clear();
        assertTrue(epic.getMySubTaskList().isEmpty());
    }

    @Test
    void removeMySubTaskMap() {
        assertNotNull(epic.getMySubTaskMap());
        epic.removeMySubTaskMap();
        assertEquals(epic.getMySubTaskMap(), Collections.emptyMap());
    }

    @Test
    void setTime() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime start2 = start.plusMinutes(15);
        sub.setStartTime(start);
        sub.setDuration(10);
        sub.setEndTime(sub.getStartTime().plusMinutes(sub.getDuration()));
        SubTask sub2 = new SubTask(epic);
        sub2.setId(2);
        epic.getMySubTaskMap().put(sub2.getId(), sub2);
        sub2.setStartTime(start2);
        sub2.setDuration(10);
        LocalDateTime endTime = start2.plusMinutes(10);
        sub2.setEndTime(endTime);
        epic.setTime();
        assertEquals(start, epic.getStartTime());
        assertEquals(20, epic.getDuration());
        assertEquals(endTime, epic.getEndTime());
    }
}