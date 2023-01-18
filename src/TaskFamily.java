public enum TaskFamily {
    TASK,
    EPICTASK,
    SUBTASK;

    public String asClassName() {
        String upperName = this.toString();
        String lowerName = upperName.toLowerCase();

        lowerName = lowerName.replaceFirst(lowerName.substring(0,1), upperName.substring(0,1));// меняем 1 букву
        lowerName = lowerName.replaceFirst(
                lowerName.substring(lowerName.length() - 4,lowerName.length() - 3),
                upperName.substring(upperName.length() - 4, upperName.length() - 3));
        //меняем букву 'T' в слове task: 4-я позиция с конца
        return lowerName;
    }

    public static TaskFamily getEnumFromClass(Class<?> clas) throws NoMatchesFoundException {
        TaskFamily tf = null;
        for (TaskFamily name : values()) {
            if (name.asClassName().equals(clas.getName())) {
                tf = name;
            }
        }
        if (tf == null) {
            throw new NoMatchesFoundException("Ой! Здесь появился null!");
        }
        return tf;

    }
}
