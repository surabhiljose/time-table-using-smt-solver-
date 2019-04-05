javac -cp bin/com.microsoft.z3.jar -d bin src/*.java
rm -rf bin/configuration
mkdir -p bin/configuration 
cp configuration.properties bin/configuration/configuration.properties
cd bin
java -cp com.microsoft.z3.jar:. TimeTableScheduler
mv target/* ../target/ 2>/dev/null
rm -rf configuration
rm -rf target
cd ..

