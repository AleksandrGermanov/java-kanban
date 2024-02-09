# java-kanban

### Планировщик заданий

Web-приложение с собственным сервером, обменом данными в формате Json (с использованием Gson),
с сохранением и чтением данных из файла в формате CSV, собственной реализацией связного списка.

---


> [!NOTE]<br>
> приложение на Java SE 11<br>
> для запуска приложения необходимо:<br>
>  - склонировать репозиторий
>  - в терминале с поддержкой JDK 11 или выше выполнить
     (из папки с клонированным репозиторием)<br>
     `javac -d ./bin -cp "./lib/*" -Xlint:unchecked -encoding utf8 -sourcepath ./src/ ./src/*.java`<br>
     `java -cp ./lib/gson-2.10.1.jar;./lib/*;./bin Main`
>
> После запуска приложения приложение создаст 2 простые задачи и одну задачу с подзадачами и сохранит
     их в файле data.csv.
>
> Для пользователей Windows можно запустить файл `deploy.cmd` - скрипт
     создаст и запустит jar c проектом (при условии установленного JDK).

### Функциональность

- Приложение умеет создавать обычные задачи(Task), и задачи(EpicTask) с подзадачами(SubTask)
- Умеет сохранять созданные задачи в файл.
- Формирует и сохраняет историю просмортров задач.
- Возвращает список задач по запросу.
- Умеет отправлять свое состояние на сервер для сохранения в формате Gson (сервер слушает 8078 порт,
  функция сохранеия не реализована).
