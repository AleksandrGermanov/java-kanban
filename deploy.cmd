chcp 65001
pushd %~dp0
javac -d ./jar -cp "./lib/*" -Xlint:unchecked -encoding utf8 -sourcepath ./src/ ./src/*.java
cd jar
jar cf TaskManager.jar *.class ./exchange/*.class ./management/*.class ./management/history/*.class ./management/task/*.class ./management/time/*.class ./myExceptions/*.class ./task/*.class
del *.class
del /f /s /q exchange;management;myExceptions;task
rmdir exchange;management\time;management\task;management\history;management;myExceptions;task
java -cp ../lib/gson-2.10.1.jar;../lib/*;TaskManager.jar Main
pause