package management.task;

import myExceptions.NoMatchesFoundException;

public enum TaskFamily {
    TASK,
    EPICTASK,
    SUBTASK;

    public static TaskFamily getEnumFromClass(Class<?> clas) throws NoMatchesFoundException {
        TaskFamily TF = null;
        String suffix = clas.getName().split("\\.")[
                clas.getName().split("\\.").length - 1];

        for (TaskFamily name : values()) {
            if (suffix.equals(name.asClassName())) {
                TF = name;
                break;
            }
        }
        if (TF == null) {
            throw new NoMatchesFoundException("Ой! Здесь появился null!");
        }
        return TF;

    }

    private String asClassName() {
        String upperName = this.toString();
        String lowerName = upperName.toLowerCase();

        lowerName = lowerName.replaceFirst(lowerName.substring(0, 1), upperName.substring(0, 1));
        lowerName = lowerName.replaceFirst(
                String.valueOf(lowerName.charAt(lowerName.length() - 4)),
                String.valueOf(upperName.charAt(upperName.length() - 4))
        );
        return lowerName;
    }
}
