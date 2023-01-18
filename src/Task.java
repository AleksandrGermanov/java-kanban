import java.util.Objects;

public class Task {
    protected String name;
    protected String description;
    protected int Id;
    protected String status;

    public Task(){
        status = Statuses.NEW.toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public int getId() {
        return Id;
    }
    public void setId(int Id) {
        this.Id = Id;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Id == task.Id && name.equals(task.name) 
                && Objects.equals(description, task.description) 
                && status.equals(task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, Id, status);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\''
                + ", description='" + description + '\''
                + ", Id=" + Id
                +", status='" + status + '\'' + "}^\b";
    }
}

/*
        Название, кратко описывающее суть задачи (например, «Переезд»).
        Описание, в котором раскрываются детали.
        Уникальный идентификационный номер задачи, по которому её можно будет найти.
        Статус, отображающий её прогресс. Мы будем выделять следующие этапы жизни задачи:
        NEW — задача только создана, но к её выполнению ещё не приступили.
        IN_PROGRESS — над задачей ведётся работа.
        DONE — задача выполнена.
        */
