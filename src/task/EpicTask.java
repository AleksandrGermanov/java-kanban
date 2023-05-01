package task;

import myExceptions.IllegalStatusChangeException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;


public class EpicTask extends Task {
    private final Map<Integer, SubTask> mySubTaskMap;

    public EpicTask() {
        mySubTaskMap = new HashMap<>();
    }

    public EpicTask(String name, String description) {
        super(name, description);
        mySubTaskMap = new HashMap<>();
    }

    public EpicTask(int id, String name, Statuses status, String description,
                    LocalDateTime startTime, Integer duration, LocalDateTime endTime) {
        super(id, name, status, description, startTime, duration, endTime);
        mySubTaskMap = new HashMap<>();
    }

    @Override
    public void setStatus(Statuses status) {
        try {
            throw new IllegalStatusChangeException(
                    "Хорошая попытка, но статус эпиков не может быть установлен в ручную!");
        } catch (IllegalStatusChangeException e) {
            e.printStackTrace();
            setStatus();
        }
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        setStartTime();
    }

    @Override
    public void setDuration(Integer duration) {
        setDuration();
    }

    @Override
    public void setEndTime(LocalDateTime endTime) {
        setEndTime();
    }

    public Map<Integer, SubTask> getMySubTaskMap() {
        return mySubTaskMap;
    }

    public ArrayList<String> getMySubTaskList() {
        ArrayList<String> taskList = new ArrayList<>();
        Iterator<Map.Entry<Integer, SubTask>> iterator;
        iterator = mySubTaskMap.entrySet().iterator();
        iterator.forEachRemaining(E -> taskList.add(E.toString()));
        return taskList;
    }

    public void setStatus() {
        if (mySubTaskMap.isEmpty()) {
            status = Statuses.NEW;
            return;
        }

        int newCounter = 0;
        int doneCounter = 0;

        for (SubTask subTask : mySubTaskMap.values()) {
            if (subTask.getStatus().equals(Statuses.NEW)) {
                ++newCounter;
            }
            if (subTask.getStatus().equals(Statuses.DONE)) {
                ++doneCounter;
            }
        }
        if (newCounter == mySubTaskMap.size()) {
            status = Statuses.NEW;
        } else if (doneCounter == mySubTaskMap.size()) {
            status = Statuses.DONE;
        } else {
            status = Statuses.IN_PROGRESS;
        }
    }

    public void removeMySubTaskMap() {
        mySubTaskMap.clear();
    }

    public void setTime() {
        setStartTime();
        setDuration();
        setEndTime();
    }

    public void setStartTime() {
        this.startTime = mySubTaskMap.values().stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    public void setDuration() {
        Predicate<Integer> containsDuration = Objects::nonNull;
        Function<SubTask, Integer> getDuration = SubTask::getDuration;
        BinaryOperator<Integer> add = Integer::sum;

        this.duration = mySubTaskMap.values().stream()
                .map(getDuration)
                .filter(containsDuration)
                .reduce(add)
                .orElse(null);
    }

    public void setEndTime() {
        this.endTime = mySubTaskMap.values().stream()
                .map(SubTask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EpicTask epicTask = (EpicTask) o;
        return Objects.equals(mySubTaskMap, epicTask.mySubTaskMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mySubTaskMap);
    }

    @Override
    public String toString() {
        return "EpicTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", " + System.lineSeparator() + "startTime=" + startTime +
                ", duration=" + duration +
                ", endTime=" + endTime +
                ", " + System.lineSeparator() + "mySubTaskMap.size=" + mySubTaskMap.size()
                + ", mySubTaskMap.keySet=" + mySubTaskMap.keySet()
                + "}" + System.lineSeparator() + "";
    }
}
