source mvn clean package || goto :end
rm .\target\original*.jar
cp .\target\*.jar ..\spigot\plugins
cd ..\spigot
source start.sh
:end