set -e 
set -o pipefail
if [ ! -f ./server/spigot.jar ]; then
  cd server
  source build.sh
  cd ..
fi
cd plugin
mvn clean package
cd ..
rm ./plugin/target/original*.jar
mkdir -p server/plugins
cp ./plugin/target/*.jar ./server/plugins/