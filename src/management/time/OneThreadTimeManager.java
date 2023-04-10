package management.time;

import management.task.TaskFamily;
import myExceptions.NoMatchesFoundException;
import task.SubTask;
import task.Task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/***
 * Отсортируйте все задачи по приоритету — то есть по startTime. Если дата старта не задана,
 * добавьте задачу в конец списка задач, подзадач, отсортированных по startTime.
 * Напишите новый метод getPrioritizedTasks, возвращающий список задач и подзадач в заданном порядке.
 * Предполагается, что пользователь будет часто запрашивать этот список задач и подзадач,
 * поэтому подберите подходящую структуру данных для хранения.
 * Сложность получения должна быть уменьшена с O(n log n) до O(n).
 *
 * rangedSet хранит ВСЕ созданные задачи, вне зависимости от типа и наличия установленного времени выполнения.
 *
 * validationSet хранит только задачи Task и SubTask с заданным временем выполнения. Это сделано
 * для того, чтобы избежать пересечения времени эпиков с их подзадачами, а также для обработки ситуаций,
 * когда между двумя подзадачами одного эпика пользователь вносит обычную задачу или подзадачу другого
 * эпика (если нет пересечения по времени, такие ситуации разрешены).
 */
public class OneThreadTimeManager implements TimeManager {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final Set<Task> rangedSet = new TreeSet<>();
    private final Set<Task> validationSet = new TreeSet<>();

    @Override
    public <T extends Task> void setTime(String startString, Integer duration, T task) {
        try {
            TaskFamily TF = TaskFamily.getEnumFromClass(task.getClass());
            LocalDateTime start = LocalDateTime.parse(startString, DATE_TIME_FORMATTER);
            switch (TF) {
                case TASK:
                    setTimeValidated(start, duration, task);
                    if (isTimeSet(task)) {
                        validationSet.add(task);
                    }
                    break;
                case SUBTASK:
                    setTimeValidated(start, duration, task);
                    if (isTimeSet(task)) {
                        validationSet.add(task);
                    }
                    SubTask sub = (SubTask) task;
                    sub.getMyEpic().setTime();
                    break;
            }
        } catch (NoMatchesFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(rangedSet);
    }

    @Override
    public boolean isTimeSet(Task task) {
        return task.getStartTime() != null
                && task.getDuration() != null
                && task.getEndTime() != null;
    }

    @Override
    public void addToRanged(Task task) {
        rangedSet.add(task);
    }

    @Override
    public void removeFromRanged(Task task) {
        rangedSet.remove(task);
    }

    @Override
    public void removeFromValidation(Task task) {
        validationSet.remove(task);
    }

    @Override
    public void clear() {
        rangedSet.clear();
        validationSet.clear();
    }

    private <T extends Task> void setTimeDefault(LocalDateTime start, Integer duration, T task) {
        task.setStartTime(start);
        task.setDuration(duration);
        task.setEndTime(start.plusMinutes(duration));
    }

    private <T extends Task> void setTimeValidated(LocalDateTime start, Integer duration, T task) {
        boolean wasInValidationSet = false;
        LocalDateTime oldStart = null;
        Integer oldDuration = null;
        LocalDateTime oldEnd = null;

        if (validationSet.contains(task)) {
            validationSet.remove(task);//если не удалить таску, будут проблемы с обновлением задачи
            // (возможны пересечения старого и нового времени одной и той же задачи).
            wasInValidationSet = true;
            oldStart = task.getStartTime();
            oldDuration = task.getDuration();
            oldEnd = task.getEndTime();
        }
        setTimeDefault(start, duration, task);
        if (isValidatedByTime(task)) {
            validationSet.add(task);
        } else {
            System.out.println("Выполнение двух задач одновременно невозможно, мой однопоточный друг!\n" +
                    "Время выполнения задачи не будет изменено.");
            task.setStartTime(oldStart);
            task.setDuration(oldDuration);
            task.setEndTime(oldEnd);
            if (wasInValidationSet) {
                validationSet.add(task);
            }
        }
    }

    private boolean isValidatedByTime(Task task) {
        List<Task> list = new ArrayList<>(validationSet);
        int binSearch = Collections.binarySearch(list, task);
        if (binSearch >= 0) {
            return false;
        }
        int possiblePlace = -(binSearch + 1);
        if (possiblePlace > 0) {
            if (list.get(possiblePlace - 1).getEndTime()
                    .isAfter(task.getStartTime())) {
                return false;
            }
        }
        if (possiblePlace < list.size()) {
            return (list.get(possiblePlace).getStartTime()
                    .isAfter(task.getEndTime()));
        }
        return true;
    }
}

