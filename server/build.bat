@echo off
cd BuildTools
curl -z BuildTools.jar -o BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
set /p Input=Enter the version: || (echo Nothing entered, abort & exit 1)
del spigot*.jar
del craftbukkit*.jar
java -jar BuildTools.jar --rev %Input%
copy /b .\spigot*.jar ..\spigot.jar

cd Bukkit
call mvn install
cd ..
cd CraftBukkit
call mvn install
cd ..
cd Spigot/Spigot-Api
call mvn install
cd ../..

cd ../../plugin

if not exist pom.xml (
  cd archetype
  call generate.bat
  cd ..
)

for /f "tokens=1,2 delims=." %%a in ("%Input%") do (
  echo %%a%%b
  mvn versions:set-property -Dproperty=spigot.api -DnewVersion=%%a.%%b
)
