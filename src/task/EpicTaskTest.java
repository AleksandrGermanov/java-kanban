package task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        for(SubTask sub : epic.getMySubTaskMap().values()){
            sub.setStatus(Statuses.IN_PROGRESS);
        }
        epic.setStatus();
        assertEquals(Statuses.IN_PROGRESS, epic.getStatus());
        epic.getMySubTaskMap().clear();
        epic.setStatus();
        assertEquals(Statuses.NEW, epic.getStatus());

    }

    @Test
    void setStartTimeOpt() {
        LocalDateTime testLDT = LocalDateTime.now();
        epic.setStartTimeOpt(Optional.of(testLDT));
        assertTrue(epic.getStartTimeOpt().isEmpty());
        sub.setStartTimeOpt(Optional.of(testLDT));
        epic.setStartTimeOpt();
        assertEquals(testLDT, epic.getStartTimeOpt().get());
    }

    @Test
    void setDurationOpt() {
        Integer testDuration = 10;
        epic.setDurationOpt(Optional.of(15));
        assertTrue(epic.getDurationOpt().isEmpty());
        sub.setDurationOpt(Optional.of(testDuration));
        epic.setDurationOpt();
        assertEquals(testDuration, epic.getDurationOpt().get());
    }

    @Test
    void setEndTimeOpt() {
        LocalDateTime testLDT = LocalDateTime.now();
        epic.setEndTimeOpt(Optional.of(testLDT));
        assertTrue(epic.getEndTimeOpt().isEmpty());
        sub.setEndTimeOpt(Optional.of(testLDT));
        epic.setEndTimeOpt();
        assertEquals(testLDT, epic.getEndTimeOpt().get());
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
        assertNull(epic.getMySubTaskMap());
    }

    @Test
    void setTime() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime start2 = start.plusMinutes(15);
        sub.setStartTimeOpt(Optional.of(start));
        sub.setDurationOpt(Optional.of(10));
        sub.setEndTimeOpt(Optional.of(sub.getStartTimeOpt().get()
                .plusMinutes(sub.getDurationOpt().get())));
        SubTask sub2 = new SubTask(epic);
        sub2.setId(2);
        epic.getMySubTaskMap().put(sub2.getId(), sub2);
        sub2.setStartTimeOpt(Optional.of(start2));
        sub2.setDurationOpt(Optional.of(10));
        LocalDateTime endTime = start2.plusMinutes(10);
        sub2.setEndTimeOpt(Optional.of(endTime));
        epic.setTime();
        assertEquals(start, epic.getStartTimeOpt().get());
        assertEquals(20, epic.getDurationOpt().get());
        assertEquals(endTime, epic.getEndTimeOpt().get());
    }
}