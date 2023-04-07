package task;

import myExceptions.IllegalStatusChangeException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;


public class EpicTask extends Task {
    private Map<Integer, SubTask> mySubTaskMap;

    public EpicTask() {
        mySubTaskMap = new HashMap<>();
    }

    public EpicTask(String name, String description) {
        super(name, description);
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
    public void setStartTimeOpt(Optional<LocalDateTime> startTimeOpt) {
        setStartTimeOpt();
    }

    @Override
    public void setDurationOpt(Optional<Integer> durationOpt) {
        setDurationOpt();
    }

    @Override
    public void setEndTimeOpt(Optional<LocalDateTime> endTimeOpt) {
        setEndTimeOpt();
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
        mySubTaskMap = null;
    }

    public void setTime(){
        setStartTimeOpt();
        setDurationOpt();
        setEndTimeOpt();
    }

    public void setStartTimeOpt() {
            this.startTimeOpt = mySubTaskMap.values().stream()
                    .filter(subTask -> subTask.getStartTimeOpt().isPresent())
                    .map(subTask -> subTask.getStartTimeOpt().get())
                    .reduce(BinaryOperator.minBy((start1, start2) -> {
                        if (start1.isBefore(start2)) {
                            return -1;
                        } else if (start1.isAfter(start2)) {
                            return 1;}
                        else{
                            return 0;
                        }
                    }));
        }

    public void setDurationOpt() {
        Predicate<SubTask> containsDuration = subTask -> subTask.getDurationOpt().isPresent();
        Function<SubTask, Integer> getDuration = subTask -> subTask.getDurationOpt().get();
        BinaryOperator<Integer> add = Integer::sum;

        this.durationOpt = mySubTaskMap.values().stream()
                .filter(containsDuration)
                .map(getDuration)
                .reduce(add);
    }

    public void setEndTimeOpt(){
        this.endTimeOpt = mySubTaskMap.values().stream()
                .filter(subTask -> subTask.getEndTimeOpt().isPresent())
                .map(subTask -> subTask.getEndTimeOpt().get())
                .reduce(BinaryOperator.maxBy((end1, end2) -> {
                    if (end1.isBefore(end2)) {
                        return -1;
                    } else if (end1.isAfter(end2)) {
                        return 1;}
                    else{
                        return 0;
                    }
                }));
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
                ", ^\bstartTimeOpt=" + startTimeOpt +
                ", durationOpt=" + durationOpt +
                ", endTimeOpt=" + endTimeOpt +
                ", ^\bmySubTaskMap.size=" + mySubTaskMap.size()
                + ", mySubTaskMap.keySet=" + mySubTaskMap.keySet()
                + "}^\b";
    }
}
