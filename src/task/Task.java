package task;

import java.util.Objects;

public class Task {
    protected String name;
    protected String description;
    protected int id;
    protected Statuses status;

    public Task(){
        status = Statuses.NEW;
    }

    public Task(String name, String description){
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

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, status);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\''
                + ", description='" + description + '\''
                + ", Id=" + id
                +", status='" + status + '\'' + "}^\b";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(name, task.name)
                && Objects.equals(description, task.description)
                && status == task.status;
    }
}
