mvn clean install archetype:update-local-catalog
cd ..
mvn archetype:generate -DarchetypeCatalog=local -DarchetypeArtifactId="sample-archetype" -DarchetypeGroupId="io.github.jugbot" -DoutputDirectory=tmp

for f in ./tmp/*; do 
  cp -R $f/. .
done
rm -r tmp