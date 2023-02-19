package management.history;

import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InMemoryHistoryManager implements HistoryManager {
    private static final CustomLinkedList<? super Task> taskLinkList = new CustomLinkedList<>();
    private static List<? super Task> historyList = taskLinkList.getTasks();


    @Override
    public <T extends Task> void add(T task) {
        taskLinkList.addNode(task);
    }

    //...добавить метод void remove(int id) для удаления задачи из просмотра. И
    // реализовать его в классе InMemoryHistoryManager.
    @Override
    public void remove(int id) {
        taskLinkList.removeNode(id);
    }

    @Override
    public List<? super Task> getHistory() {
        return historyList;
    }

    public void printHistoryList() {
        System.out.println("История обращений: ");
        System.out.println("***");
        if (historyList != null) {
            for (Object task : historyList) {
                System.out.print(task.toString().replace("^\b", System.lineSeparator()));
            }
        }
        System.out.println("***");
    }

    //Сначала напишите свою реализацию двусвязного списка задач с методами linkLast и getTasks.
// linkLast будет добавлять задачу в конец этого списка, а getTasks собирать все задачи из
// него в обычный ArrayList. Убедитесь, что решение работает. Отдельный класс для списка создавать
// не нужно — реализуйте его прямо в классе InMemoryHistoryManager.
    private static class CustomLinkedList<T extends Task> { //Этот класс не отдельный
        private final Map<Integer, Node<T>> nodeMap = new HashMap<>();
        private Node<T> head;
        private Node<T> tail;
        private int size = 0;

        int size() {
            return size;
        }

        void addNode(T task) {
            if (size == 0) {
                Node<T> node = new Node<>(true, task);
                head = node;
                tail = node;
                ++size;
                nodeMap.put(node.taskId, node);
            } else {
                linkLast(task);
            }
        }

        void linkLast(T task) {
            Node<T> node = new Node<>(false, task);
            tail.isTail = false;
            tail.next = node;
            node.prev = tail;
            tail = node;
            ++size;
            nodeMap.put(node.taskId, node);
        }

        ArrayList<? super Task> getTasks() {
            ArrayList<? super Task> list = new ArrayList<>();
            if(head != null){
                Node<T> iterator = head;
            do {
                list.add(iterator.task);
                iterator = iterator.next;
            } while (!iterator.isTail);
            return list;
            } else{
                return null;
            }
        }

        void removeNode(int id) {
            Node<T> task = nodeMap.get(id);
            if (task.isHead && !task.isTail) {
                task.next.prev = null;
                task.next.isHead = true;
                head = task.next;
            } else if(task.isTail && !task.isHead){
                task.prev.next = null;
                task.prev.isTail = true;
                tail = task.prev;
            } else if(!task.isTail) {
                task.prev.next = task.next;
                task.next.prev = task.prev;
            }
            nodeMap.remove(id);
            size--;
        }
    }
}

//А вот отдельный класс Node для узла списка необходимо добавить.
class Node<T extends Task> {
    boolean isHead;
    boolean isTail = true; //новый узел всегда добавляется в конец
    Node<T> prev;
    Node<T> next;
    final int taskId;
    final T task;

    public Node(boolean isHead, T task) {
        this.isHead = isHead;
        this.task = task;
        this.taskId = task.getId();
    }
}
