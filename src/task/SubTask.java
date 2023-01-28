package task;

import java.util.Objects;

public class SubTask extends Task {

    private EpicTask myEpic;

    public SubTask(EpicTask epic) { //Subtask не бывает без эпика
        super();
        myEpic = epic;
    }

    public SubTask(EpicTask epic, String name, String description){
        super(name, description);
        myEpic = epic;
    }

    public EpicTask getMyEpic() {
        return myEpic;
    }

    public void removeMyEpicLink() {
        myEpic = null;
    }

    public int getMyEpicId() {
        return myEpic.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SubTask subTask = (SubTask) o;
        return myEpic == subTask.myEpic;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), myEpic);
    }

    @Override
    public String toString() {
        return "SubTask{myEpicId=" + getMyEpicId()
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", Id=" + Id
                + ", status='" + status + '\'' + "}^\b";
    }
}

