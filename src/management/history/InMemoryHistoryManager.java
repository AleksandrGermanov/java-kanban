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
        private final Map<Integer, Node<T>> nodeMap = new HashMap<>();
        private Node<T> head;
        private Node<T> tail;
        private int size = 0;

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
            if (nodeMap.containsKey(task.getId())){
                removeNode(task.getId());
            }
            Node<T> node = new Node<>(false, task);
            tail.isTail = false;
            tail.next = node;
            node.prev = tail;
            tail = node;
            ++size;
            nodeMap.put(node.taskId, node);
            if (nodeMap.size()>10){
                removeNode(head);
                size--;
            }
        }

        ArrayList<? super Task> getTasks() {
            ArrayList<? super Task> list = new ArrayList<>();
            if(head != null){
                Node<T> iterator = head;
                while (!iterator.isTail){
                list.add(iterator.task);
                iterator = iterator.next;
            }
                list.add(iterator.task);
            return list;
            } else{
                return null;
            }
        }

        void removeNode(int id) {
            if (nodeMap.containsKey(id)){
                Node<T> taskNode = nodeMap.get(id);
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
                size--;
            }
        }

        void removeNode(Node<T> taskNode) { // по ТЗ removeNode должен принимать Node
            if (nodeMap.containsValue(taskNode)){
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
                nodeMap.remove(taskNode.taskId);
                size--;
            }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return isHead == node.isHead && isTail == node.isTail && taskId == node.taskId
                && Objects.equals(prev, node.prev) && Objects.equals(next, node.next)
                && Objects.equals(task, node.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isHead, isTail, prev, next, taskId, task);
    }
}
