java -Xms1G -Xmx3G -XX:+UseConcMarkSweepGC \
-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 \
-jar spigot.jar nogui
