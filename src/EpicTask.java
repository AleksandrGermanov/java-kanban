import java.util.*;

public class EpicTask extends Task {
    private HashMap<Integer, SubTask> mySubTaskMap;

    public EpicTask() {
        mySubTaskMap = new HashMap<>();
    }

    public EpicTask(String name, String description){
        super(name, description);
        mySubTaskMap = new HashMap<>();
    }

    public HashMap<Integer, SubTask> getMySubTaskMap() {
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
            status = Statuses.NEW.toString();
            return;
        }

        int newCounter = 0;
        int doneCounter = 0;

        for (SubTask subTask : mySubTaskMap.values()) {
            if (subTask.getStatus().equals(Statuses.NEW.toString())) {
                ++newCounter;
            }
            if (subTask.getStatus().equals(Statuses.DONE.toString())) {
                ++doneCounter;
            }
        }
        if (newCounter == mySubTaskMap.size()) {
            status = Statuses.NEW.toString();
        } else if (doneCounter == mySubTaskMap.size()) {
            status = Statuses.DONE.toString();
        } else {
            status = Statuses.IN_PROGRESS.toString();
        }
    }

    @Override
    public void setStatus(String status) {
        try {
            throw new NoMatchesFoundException("Хорошая попытка, но статус эпиков не может быть установлен в ручную!");
        } catch (NoMatchesFoundException e) {
            e.printStackTrace();
            setStatus();
        }
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
        return "EpicTask{mySubTaskMap.size=" + mySubTaskMap.size()
                + ", mySubTaskMap.keySet=" + mySubTaskMap.keySet()
                + ", name='" + name + '\''
                + "^\b, description='" + description + '\''
                + ", Id=" + Id +
                ", status='" + status + '\'' + "}^\b";
    }

    public void removeMySubTaskMap() {
        mySubTaskMap = null;
    }
}
