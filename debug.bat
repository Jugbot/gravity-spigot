call mvn clean package || goto :end
del .\target\original*.jar
copy .\target\*.jar ..\spigot\plugins
cd ..\spigot
call start.bat
:end