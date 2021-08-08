@echo off
call mvn clean install archetype:update-local-catalog
cd ..
call mvn archetype:generate -DarchetypeCatalog=local -DarchetypeArtifactId="sample-archetype" -DarchetypeGroupId="io.github.jugbot" -DoutputDirectory=tmp

for /d %%a in (tmp/*) do (
  xcopy /s tmp\%%a .
)
rmdir tmp /s /q