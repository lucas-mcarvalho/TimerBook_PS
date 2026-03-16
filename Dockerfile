FROM eclipse-temurin:21-jdk
COPY BackEnd/TimerBook_API/target/*.jar library-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/library-0.0.1-SNAPSHOT.jar"]