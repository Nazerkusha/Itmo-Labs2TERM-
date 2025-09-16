FROM ubuntu:latest
LABEL authors="Nazerke"
ENTRYPOINT ["top", "-b"]
FROM openjdk:11-jre-slim
COPY target/your-app.jar /app.jar
CMD ["java", "-jar", "/app.jar"]