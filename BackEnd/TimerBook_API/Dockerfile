FROM eclipse-temurin:21-jdk as builder
WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
COPY --from=builder /app/target/*.jar library-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/library-0.0.1-SNAPSHOT.jar"]