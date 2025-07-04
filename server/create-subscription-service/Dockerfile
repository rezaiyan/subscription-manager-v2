FROM openjdk:21-jdk-slim

WORKDIR /app

COPY create-subscription-service/build/libs/*.jar app.jar

EXPOSE 3001

ENTRYPOINT ["java", "-jar", "app.jar"] 