@echo off
if not exist server/spigot.jar (
  cd server
  call build.bat
  cd ..
)
cd plugin 
call mvn clean package || goto :end
cd ..
if not exist server/plugins mkdir server/plugins
del .\plugin\target\original*.jar
copy .\plugin\target\*.jar .\server\plugins
:end