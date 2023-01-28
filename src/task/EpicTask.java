package task;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;


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

    @Override
    public void setStatus(Statuses status) {
        try {
            throw new Exception("Хорошая попытка, но статус эпиков не может быть установлен в ручную!");
        } catch (Exception e) {
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