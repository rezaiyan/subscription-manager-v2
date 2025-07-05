FROM openjdk:21-jdk-slim

WORKDIR /app

COPY server/config-server/build/libs/*.jar app.jar
COPY server/config-server/src/main/resources/config ./config

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"] 