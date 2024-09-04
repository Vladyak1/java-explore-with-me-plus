FROM amazoncorretto:21
WORKDIR /app
COPY target/*.jar app.jar
COPY src/main/resources /app/resources
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 9090
