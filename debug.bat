call mvn clean package || goto :end
copy .\target\*.jar ..\spigot\plugins
cd ..\spigot
call start.bat
:end