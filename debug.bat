call mvn clean package
copy .\target\*.jar ..\spigot\plugins
cd ..\spigot
call start.bat