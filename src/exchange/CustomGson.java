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

public class CustomGson {
    private final HttpTaskManager taskMan;

    private JsonSerializer<LocalDateTime> ldtSerializer = (ldt, type, context) -> {
        if (ldt != null) {
            return new JsonPrimitive(ldt.format(DATE_TIME_FORMATTER));
        } else {
            return new JsonPrimitive("null");
        }
    };

    private JsonDeserializer<LocalDateTime> ldtDeserializer = (value, type, context) -> {
        if ("null".equals(value.getAsJsonPrimitive().getAsString())) {
            return null;
        } else {
            return LocalDateTime.parse(value.getAsJsonPrimitive().getAsString(), DATE_TIME_FORMATTER);
        }
    };

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

    class TaskDeserializer implements JsonDeserializer<Task> {
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
                epic = HttpTaskManager.getTaskNH(epicId, taskMan.getTasks()); // весь класс написан ради этой строки...
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


