FROM openjdk:21-jdk-slim

WORKDIR /app

COPY server/api-gateway/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 