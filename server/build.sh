cd BuildTools
curl -z BuildTools.jar -o BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
echo Enter the version: 
read Input
rm spigot*.jar
rm craftbukkit*.jar
java -jar BuildTools.jar --rev $Input
cp ./spigot*.jar ../spigot.jar

(cd Bukkit; mvn install)
(cd CraftBukkit; mvn install)
(cd Spigot/Spigot-Api; mvn install)