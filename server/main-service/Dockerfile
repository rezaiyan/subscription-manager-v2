FROM openjdk:21-jdk-slim

WORKDIR /app

COPY server/main-service/build/libs/*.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"] 