package management.history;

import task.Task;

import java.util.*;


public class InMemoryHistoryManager implements HistoryManager {
    private static final CustomLinkedList<? super Task> taskLinkList = new CustomLinkedList<>();

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
        return taskLinkList.getTasks();
    }

    public void printHistoryList() {
        List<? super Task> historyList = getHistory();

        System.out.println("История обращений: ");
        System.out.println("***");
        if (historyList != null) {
            for (Object task : historyList) {
                System.out.print(task.toString().replace("^\b", System.lineSeparator()));
            }
        }
        System.out.println("***\n");
    }

    //Сначала напишите свою реализацию двусвязного списка задач с методами linkLast и getTasks.
// linkLast будет добавлять задачу в конец этого списка, а getTasks собирать все задачи из
// него в обычный ArrayList. Убедитесь, что решение работает. Отдельный класс для списка создавать
// не нужно — реализуйте его прямо в классе InMemoryHistoryManager.
    private static class CustomLinkedList<T extends Task> { //Этот класс не отдельный
        private final Map<Integer, Node<Task>> nodeMap = new HashMap<>();
        private Node<Task> head;
        private Node<Task> tail;

        void addNode(T task) {
            if (nodeMap.size() == 0) {
                Node<Task> node = new Node<>(true, task);
                head = node;
                tail = node;
                nodeMap.put(node.taskId, node);
            } else {
                linkLast(task);
            }
        }

        void linkLast(T task) {
            if (nodeMap.containsKey(task.getId())) {
                removeNode(task.getId());
            }
            Node<Task> node = new Node<>(false, task);
            tail.isTail = false;
            tail.next = node;
            node.prev = tail;
            tail = node;
            nodeMap.put(node.taskId, node);
        }

        ArrayList<? super Task> getTasks() {
            ArrayList<? super Task> list = new ArrayList<>();
            Node<Task> node = head;
            while (!node.isTail) {
                list.add(node.task);
                node = node.next;
            }
            list.add(node.task);
            return list;
        }

        void removeNode(int id) {
            if (nodeMap.containsKey(id)) {
                Node<Task> taskNode = nodeMap.get(id);
                if (!taskNode.isTail && !taskNode.isHead) {
                    taskNode.prev.next = taskNode.next;
                    taskNode.next.prev = taskNode.prev;
                } else if (taskNode.isHead && !taskNode.isTail) {
                    taskNode.next.prev = null;
                    taskNode.next.isHead = true;
                    head = taskNode.next;
                } else if (!taskNode.isHead) {
                    taskNode.prev.next = null;
                    taskNode.prev.isTail = true;
                    tail = taskNode.prev;
                }
                nodeMap.remove(id);
            }
        }

        void removeNode(Node<Task> taskNode) { // по ТЗ removeNode должен принимать Node
            removeNode(taskNode.taskId);
        }
    }
}

//А вот отдельный класс Node для узла списка необходимо добавить.
class Node<T extends Task> {
    /*
    C:\Users\Mailm\dev\java-kanban\src\management\history\InMemoryHistoryManager.java:110:8
    java: modifier static not allowed here
    */
    boolean isHead;
    boolean isTail = true; //новый узел всегда добавляется в конец
    Node<Task> prev;
    Node<Task> next;
    final int taskId;
    final Task task;

    public Node(boolean isHead, Task task) {
        this.isHead = isHead;
        this.task = task;
        this.taskId = task.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isHead, isTail, prev, next, taskId, task);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return isHead == node.isHead && isTail == node.isTail && taskId == node.taskId
                && Objects.equals(prev, node.prev) && Objects.equals(next, node.next)
                && task.equals(node.task);
    }
}
