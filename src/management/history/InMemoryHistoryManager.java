package management.history;

import task.Task;

import java.util.*;


public class InMemoryHistoryManager implements HistoryManager {
    private static final CustomLinkedList<? super Task> taskLinkList = new CustomLinkedList<>();

    @Override
    public <T extends Task> void add(T task) {
        taskLinkList.addNode(task);
    }

    @Override
    public void remove(int id) {
        taskLinkList.removeNode(id);
    }

    @Override
    public List<Task> getHistory() {
        return taskLinkList.getTasks();
    }

    @Override
    public void clearHistory() {
        taskLinkList.clearHistory();
    }

    public void printHistoryList() {
        List<Task> historyList = getHistory();

        System.out.println("История обращений: ");
        System.out.println("***");
        if (historyList != null) {
            for (Task task : historyList) {
                System.out.print(task.toString().replace("^\b", System.lineSeparator()));
            }
        }
        System.out.println("***\n");
    }

    private static class CustomLinkedList<T extends Task> { //Этот класс не отдельный
        private final Map<Integer, Node<Task>> nodeMap = new HashMap<>();
        private static Node<Task> head;
        private static Node<Task> tail;

        void addNode(T task) {
            if (nodeMap.isEmpty()) {
                Node<Task> node = new Node<>(true, task, task.getId());
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
            if(!nodeMap.isEmpty()) {
                Node<Task> node = new Node<>(false, task, task.getId());
                tail.isTail = false;
                tail.next = node;
                node.prev = tail;
                tail = node;
                nodeMap.put(node.taskId, node);
            } else{
                addNode(task);
            }
        }

        ArrayList<Task> getTasks() {
            ArrayList<Task> list = new ArrayList<>();
            Node<Task> node = head;
            if (node != null) {
                while (!node.isTail) {
                    list.add(node.task);
                    node = node.next;
                }
                list.add(node.task);
            }
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
                } else {
                    head = null;
                    tail = null;
                }
                nodeMap.remove(id);
            }
        }

        void removeNode(Node<Task> taskNode) { // по ТЗ removeNode должен принимать Node
            removeNode(taskNode.taskId);
        }

        void clearHistory() {
            while (!nodeMap.isEmpty()) {
                removeNode(tail);
            }
        }
    }

    private static class Node<T extends Task> {
        boolean isHead;
        boolean isTail = true; //новый узел всегда добавляется в конец
        Node<T> prev;
        Node<T> next;
        final int taskId;
        final T task;

        public Node(boolean isHead, T task, int taskId) {
            this.isHead = isHead;
            this.task = task;
            this.taskId = taskId;
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

        @Override
        public int hashCode() {
            return Objects.hash(isHead, isTail, prev, next, taskId, task);
        }
    }
}

