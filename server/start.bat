@echo off
java -Xms1G -Xmx3G ^
-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 ^
-jar spigot.jar nogui