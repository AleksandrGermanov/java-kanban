package management.time;

import management.task.TaskFamily;
import myExceptions.NoMatchesFoundException;
import task.SubTask;
import task.Task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OneThreadTimeManager implements TimeManager {
    private static final Set<Task> RANGED_SET = new TreeSet<>();
    private static final Set<Task> VALIDATION_SET = new TreeSet<>();
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public <T extends Task> void setTime(String startString, Integer duration, T task) {
        try {
            TaskFamily TF = TaskFamily.getEnumFromClass(task.getClass());
            LocalDateTime start = LocalDateTime.parse(startString, DATE_TIME_FORMATTER);
            switch (TF) {
                case TASK:
                    setTimeValidated(start, duration, task);
                    if (isTimeSet(task)) {
                        VALIDATION_SET.add(task);
                    }
                    break;
                case SUBTASK:
                    setTimeValidated(start, duration, task);
                    if (isTimeSet(task)) {
                        VALIDATION_SET.add(task);
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
        return new ArrayList<>(RANGED_SET);
    }

    @Override
    public boolean isTimeSet(Task task) {
        return task.getStartTimeOpt().isPresent()
                && task.getDurationOpt().isPresent()
                && task.getEndTimeOpt().isPresent();
    }

    @Override
    public void addToRanged(Task task) {
        RANGED_SET.add(task);
    }

    @Override
    public void removeFromRanged(Task task) {
        RANGED_SET.remove(task);
    }

    @Override
    public void removeFromValidation(Task task) {
        VALIDATION_SET.remove(task);
    }

    @Override
    public void clear() {
        RANGED_SET.clear();
        VALIDATION_SET.clear();
    }

    private <T extends Task> void setTimeDefault(LocalDateTime start, Integer duration, T task) {
        task.setStartTimeOpt(Optional.of(start));
        task.setDurationOpt(Optional.of(duration));
        task.setEndTimeOpt(Optional.of(start.plusMinutes(duration)));
    }

    private <T extends Task> void setTimeValidated(LocalDateTime start, Integer duration, T task) {
        boolean wasInValidationSet = false;
        Optional<LocalDateTime> oldStart = Optional.empty();
        Optional<Integer> oldDuration = Optional.empty();
        Optional<LocalDateTime> oldEnd = Optional.empty();

        if (VALIDATION_SET.contains(task)) {
            VALIDATION_SET.remove(task);//если не удалить таску, будут проблемы с обновлением задачи
            // (возможны пересечения старого и нового времени одной и той же задачи).
            wasInValidationSet = true;
            oldStart = task.getStartTimeOpt();
            oldDuration = task.getDurationOpt();
            oldEnd = task.getEndTimeOpt();
        }
        setTimeDefault(start, duration, task);
        if (isValidatedByTime(task)) {
            VALIDATION_SET.add(task);
        } else {
            System.out.println("Выполнение двух задач одновременно невозможно, мой однопоточный друг!\n" +
                    "Время выполнения задачи не будет изменено.");
            task.setStartTimeOpt(oldStart);
            task.setDurationOpt(oldDuration);
            task.setEndTimeOpt(oldEnd);
            if (wasInValidationSet) {
                VALIDATION_SET.add(task);
            }
        }
    }

    private boolean isValidatedByTime(Task task) {
        List<Task> list = new ArrayList<>(VALIDATION_SET);
        int binSearch = Collections.binarySearch(list, task);
        if (binSearch >= 0) {
            return false;
        }
        int possiblePlace = -(binSearch + 1);
        if (possiblePlace > 0) {
            if (list.get(possiblePlace - 1).getEndTimeOpt().get()
                    .isAfter(task.getStartTimeOpt().get())) {
                return false;
            }
        }
        if (possiblePlace < list.size()) {
            return (list.get(possiblePlace).getStartTimeOpt().get()
                    .isAfter(task.getEndTimeOpt().get()));
        }
        return true;
    }
}

