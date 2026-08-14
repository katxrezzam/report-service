# Imagen de runtime solamente - ver config-server/Dockerfile para el porque.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/report-service.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
