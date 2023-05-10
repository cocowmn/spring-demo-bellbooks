rm ./demo-0.0.1-SNAPSHOT.jar
docker-compose down
docker rmi demo:latest
./mvnw clean package -DskipTests
cp target/demo-0.0.1-SNAPSHOT.jar .
