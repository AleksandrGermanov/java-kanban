package exchange;

import com.google.gson.*;
import management.task.HttpTaskManager;
import management.task.TaskManager;
import myExceptions.ManagerIOException;
import myExceptions.NoMatchesFoundException;
import task.EpicTask;
import task.Statuses;
import task.SubTask;
import task.Task;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static management.time.OneThreadTimeManager.DATE_TIME_FORMATTER;

/**
 * Необходимость данного класса обуславливается сложной структурой
 * классов EpicTask и SubTask. Ниже будут представлены комментарии
 * по представленным сериалайзерам/десериалайзерам.
 */
public class CustomGson {
    private final HttpTaskManager taskMan;

    private final JsonSerializer<LocalDateTime> ldtSerializer = (ldt, type, context) -> {
        if (ldt != null) {
            return new JsonPrimitive(ldt.format(DATE_TIME_FORMATTER));
        } else {
            return new JsonPrimitive("null");
        }
    };

    private final JsonDeserializer<LocalDateTime> ldtDeserializer = (value, type, context) -> {
        if ("null".equals(value.getAsJsonPrimitive().getAsString())) {
            return null;
        } else {
            return LocalDateTime.parse(value.getAsJsonPrimitive().getAsString(), DATE_TIME_FORMATTER);
        }
    };

    /**
     * Экземпляр класса работает с конкретным HttpTaskManager
     * поскольку иначе невозможно будет создать или обновить SubTask - конструктор обязательно
     * требует объект EpicTask.
     * Этой зависимости не понадобилось бы, будь Task и его наследники реализованы как POJO.
     * Рефакторинг ключевых классов - это не то, чем хочется заниматься под конец проекта,
     * а кроме того, это противоречит OCP.
     */
    public CustomGson(TaskManager taskMan) {
        if (!(taskMan instanceof HttpTaskManager)) {
            throw new ManagerIOException("Ошибка сервера. Выбран неверный обработчик данных.");
        }
        this.taskMan = (HttpTaskManager) taskMan;
    }

    public static Gson getSimplePrettyGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    public Gson getGsonForHttpManager() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, ldtSerializer)
                .registerTypeAdapter(LocalDateTime.class, ldtDeserializer)
                .registerTypeAdapter(Task.class, new TaskSerializer())
                .registerTypeAdapter(Task.class, new TaskDeserializer())
                .registerTypeAdapter(SubTask.class, new SubTaskSerializer())
                .registerTypeAdapter(SubTask.class, new SubTaskDeserializer())
                .registerTypeAdapter(EpicTask.class, new EpicTaskSerializer())
                .registerTypeAdapter(EpicTask.class, new EpicTaskDeserializer())
                .create();
    }

    /**
     * Этот сериалайзер самый необязательный, однако я его добавил
     * т.к. Task не является POJO и не совсем понятно, как gson работает с Enum, а также
     * здесь неявно происходит сериализация нулевых полей startTime и endTime (через context)
     */
    static class TaskSerializer implements JsonSerializer<Task> {
        @Override
        public JsonElement serialize(Task task, Type type, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.addProperty("id", task.getId());
            result.addProperty("name", task.getName());
            result.addProperty("status", task.getStatus().name());
            result.addProperty("description", task.getDescription());
            result.add("startTime", context.serialize(task.getStartTime()));
            result.addProperty("duration", task.getDuration());
            result.add("endTime", context.serialize(task.getEndTime()));
            return result;
        }
    }

    /**
     * Этот десериалайзер создает нулевой объект, даже если входящий json пустой,
     * без него скорее всего вылезло бы NPE, если статус не определен.
     */
    static class TaskDeserializer implements JsonDeserializer<Task> {
        @Override
        public Task deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();
            int id = obj.has("id") ? obj.get("id").getAsInt() : 0;
            String name = obj.has("name") ? obj.get("name").getAsString() : "";
            String status = obj.has("status") ? obj.get("status").getAsString() : "NEW";
            String description = obj.has("description") ? obj.get("description").getAsString() : "";
            LocalDateTime startTime = obj.has("startTime")
                    ? context.deserialize(obj.get("startTime").getAsJsonPrimitive(), LocalDateTime.class) : null;
            int duration = obj.has("duration") ? obj.get("duration").getAsInt() : 0;
            LocalDateTime endTime = obj.has("endTime") ? context.deserialize
                    (obj.get("endTime").getAsJsonPrimitive(), LocalDateTime.class) : null;

            Task task = new Task(name, description);
            task.setId(id);
            task.setStatus(Statuses.valueOf(status));
            task.setStartTime(startTime);
            task.setDuration(duration);
            task.setEndTime(endTime);
            return task;
        }
    }

    /**
     * Этот сериалайзер практически идентичен TaskSerializer, добавлена еще одна строка.
     * В добавленной строке мы передаем Id эпика, в то время как сам экземпляр SubTask
     * хранит ссылку на эпик. Без этой строки gson начал бы сериализовывать еще и
     * родительский эпик, а в нем есть map с его субтасками - рекурсия.
     */
    static class SubTaskSerializer implements JsonSerializer<SubTask> {
        @Override
        public JsonElement serialize(SubTask subTask, Type type, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.addProperty("id", subTask.getId());
            result.addProperty("name", subTask.getName());
            result.addProperty("status", subTask.getStatus().name());
            result.addProperty("description", subTask.getDescription());
            result.add("startTime", context.serialize(subTask.getStartTime()));
            result.addProperty("duration", subTask.getDuration());
            result.add("endTime", context.serialize(subTask.getEndTime()));
            result.add("epicId", new JsonPrimitive(subTask.getMyEpicId()));
            return result;
        }
    }

    /**
     * Этот десериалайзер самый необходимый - невозможно создать сущность SubTask без
     * экземпляра EpicTask. Десериализация пройдет успешно только если в json передан
     * правильный id эпика.
     */
    class SubTaskDeserializer implements JsonDeserializer<SubTask> {
        @Override
        public SubTask deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int id = obj.has("id") ? obj.get("id").getAsInt() : 0;
            String name = obj.has("name") ? obj.get("name").getAsString() : "";
            String status = obj.has("status") ? obj.get("status").getAsString() : "NEW";
            String description = obj.has("description") ? obj.get("description").getAsString() : "";
            LocalDateTime startTime = obj.has("startTime")
                    ? context.deserialize(obj.get("startTime").getAsJsonPrimitive(), LocalDateTime.class) : null;
            int duration = obj.has("duration") ? obj.get("duration").getAsInt() : 0;
            LocalDateTime endTime = obj.has("endTime") ? context.deserialize
                    (obj.get("endTime").getAsJsonPrimitive(), LocalDateTime.class) : null;
            int epicId = obj.has("epicId") ? obj.get("epicId").getAsInt() : 0;

            if (epicId == 0) {
                throw new JsonParseException("Передайте субтаск с id эпика.");
            }
            EpicTask epic;
            SubTask sub;
            try {
                epic = HttpTaskManager.getTaskNH(epicId, taskMan.getTasks());
            } catch (NoMatchesFoundException e) {
                throw new JsonParseException("Передайте правильный id родительского эпика.", e);
            }
            sub = new SubTask(epic, name, description);
            sub.setId(id);
            sub.setStatus(Statuses.valueOf(status));
            sub.setStartTime(startTime);
            sub.setDuration(duration);
            sub.setEndTime(endTime);
            return sub;
        }
    }

    /**
     * Этот сериалайзер передает только Id субтасков, а не всю мапу.
     */
    static class EpicTaskSerializer implements JsonSerializer<EpicTask> {
        @Override
        public JsonElement serialize(EpicTask epicTask, Type type, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.addProperty("id", epicTask.getId());
            result.addProperty("name", epicTask.getName());
            result.addProperty("status", epicTask.getStatus().name());
            result.addProperty("description", epicTask.getDescription());
            result.add("startTime", context.serialize(epicTask.getStartTime()));
            result.addProperty("duration", epicTask.getDuration());
            result.add("endTime", context.serialize(epicTask.getEndTime()));
            JsonArray subsIds = new JsonArray();
            if (!epicTask.getMySubTaskMap().isEmpty()) {
                epicTask.getMySubTaskMap().keySet().forEach(subsIds::add);
            }
            result.add("mySubsIds", subsIds);
            return result;
        }
    }

    /**
     * Этот десериалайзер восстанавливает map из ключей - id субтасков, и высчитывает статус и время эпика,
     * исходя из данных в этой мапе, а не просто копирует поля.
     */
    class EpicTaskDeserializer implements JsonDeserializer<EpicTask> {
        @Override
        public EpicTask deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int id = obj.has("id") ? obj.get("id").getAsInt() : 0;
            String name = obj.has("name") ? obj.get("name").getAsString() : "";
            String description = obj.has("description") ? obj.get("description").getAsString() : "";
            Set<Integer> keySet = new HashSet<>();
            JsonArray subsKeySet = obj.has("mySubsIds")
                    ? obj.get("mySubsIds").getAsJsonArray() : null;
            if (subsKeySet != null) {
                for (JsonElement elem : subsKeySet) {
                    keySet.add(elem.getAsInt());
                }
            }

            EpicTask epic = new EpicTask(name, description);
            for (Integer key : keySet) {
                try {
                    epic.getMySubTaskMap()
                            .put(key, HttpTaskManager.getTaskNH(key, taskMan.getTasks()));
                } catch (NoMatchesFoundException e) {
                    throw new JsonParseException("Субтаск для эпика с переданным id(=" + key + ") не найден.", e);
                }
            }
            epic.setId(id);
            epic.setStatus();
            epic.setTime();
            return epic;
        }
    }
}


