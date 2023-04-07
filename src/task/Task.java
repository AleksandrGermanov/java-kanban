package task;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class Task implements Comparable<Task> {
    protected int id;
    protected String name;
    protected Statuses status;
    protected String description;
    protected Optional<LocalDateTime> startTimeOpt = Optional.empty();//опциональное поле
    protected Optional<Integer> durationOpt = Optional.empty();
    protected Optional<LocalDateTime> endTimeOpt = Optional.empty();// добавил это поле для более удобного поиска
    //пересечений.

    public Task() {
        status = Statuses.NEW;
    }

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        status = Statuses.NEW;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Statuses getStatus() {
        return status;
    }

    public void setStatus(Statuses status) {
        this.status = status;
    }

    public Optional<LocalDateTime> getStartTimeOpt() {
        return startTimeOpt;
    }

    public void setStartTimeOpt(Optional<LocalDateTime> startTimeOpt) {
        this.startTimeOpt = startTimeOpt;
    }

    public Optional<Integer> getDurationOpt() {
        return durationOpt;
    }

    public void setDurationOpt(Optional<Integer> durationOpt) {
        this.durationOpt = durationOpt;
    }

    public Optional<LocalDateTime> getEndTimeOpt() {
        return endTimeOpt;
    }

    public void setEndTimeOpt(Optional<LocalDateTime> endTimeOpt) {
        this.endTimeOpt = endTimeOpt;
    }

    @Override
    public int compareTo(Task task) {
        int result = byStartTimeExistence().compare(this, task);
        if (result != 0) {
            return result;
        }
        if (this.startTimeOpt.isPresent()) {
            result = byStartTime().compare(this, task);
            if (result != 0) {
                return result;
            }
        }
        return byCounter().compare(this, task);// задачам, созданным позже, будет присвоено большее
        // значение счетчика. Эпик всегда создается раньше подзадачи.
    }

    private Comparator<Task> byStartTimeExistence() {
        return (t1, t2) -> {
            if (t1.startTimeOpt.isPresent() && t2.startTimeOpt.isEmpty()) {
                return -1;
            }
            if (t1.startTimeOpt.isEmpty() && t2.startTimeOpt.isPresent()) {
                return 1;
            }
            return 0;
        };
    }

    private Comparator<Task> byStartTime() {
        return (t1, t2) -> {
            if (t1.startTimeOpt.get().isBefore(t2.startTimeOpt.get())) {
                return -1;
            }
            if (t1.startTimeOpt.get().isAfter(t2.startTimeOpt.get())) {
                return 1;
            }
            return 0;
        };
    }

    private Comparator<Task> byCounter() {
        return Comparator.comparingInt(task -> Integer.parseInt(String.valueOf(task.getId()).substring(1)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return id == task.id && status == task.status
                && Objects.equals(name, task.name) && Objects.equals(description, task.description)
                && startTimeOpt.equals(task.startTimeOpt) && durationOpt.equals(task.durationOpt)
                && endTimeOpt.equals(task.endTimeOpt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, name, description, startTimeOpt, durationOpt, endTimeOpt);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", ^\bstartTimeOpt=" + startTimeOpt +
                ", durationOpt=" + durationOpt +
                ", endTimeOpt=" + endTimeOpt +
                "}^\b";
    }
}
