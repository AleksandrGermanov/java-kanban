package task;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

public class Task implements Comparable<Task> {
    protected int id;
    protected String name;
    protected Statuses status;
    protected String description;
    protected LocalDateTime startTime = null;
    protected Integer duration = null;
    protected LocalDateTime endTime = null;

    public Task() {
        status = Statuses.NEW;
    }

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        status = Statuses.NEW;
    }

    public Task(int id, String name, Statuses status, String description,
                LocalDateTime startTime, Integer duration, LocalDateTime endTime) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = endTime;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public int compareTo(Task task) {
        int result = byStartTimeExistence().compare(this, task);
        if (result != 0) {
            return result;
        }
        if (this.startTime != null) {
            result = byStartTime().compare(this, task);
            if (result != 0) {
                return result;
            }
        }
        return byCounter().compare(this, task);
    }

    private Comparator<Task> byStartTimeExistence() {
        return (t1, t2) -> {
            if (t1.startTime != null && t2.startTime == null) {
                return -1;
            }
            if (t1.startTime == null && t2.startTime != null) {
                return 1;
            }
            return 0;
        };
    }

    private Comparator<Task> byStartTime() {
        return Comparator.comparing(t -> t.startTime);
    }

    private Comparator<Task> byCounter() {
        return Comparator.comparingInt(task -> {
            try{
                return Integer.parseInt(String.valueOf(task.getId()).substring(1));
            } catch (NumberFormatException e){
                return 0;
            }
        });
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, name, description);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", " + System.lineSeparator() + "startTime=" + startTime +
                ", duration=" + duration +
                ", endTime=" + endTime +
                "}" + System.lineSeparator() + "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return id == task.id && name.equals(task.name)
                && status == task.status && description.equals(task.description)
                && Objects.equals(startTime, task.startTime) && Objects.equals(duration, task.duration)
                && Objects.equals(endTime, task.endTime);
    }
}
