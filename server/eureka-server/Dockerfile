FROM openjdk:21-jdk-slim

WORKDIR /app

COPY server/eureka-server/build/libs/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"] 