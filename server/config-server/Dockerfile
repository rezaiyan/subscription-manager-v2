FROM openjdk:21-jdk-slim

WORKDIR /app

COPY config-server/build/libs/*.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"] 