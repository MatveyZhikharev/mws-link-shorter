FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
CMD ["mvn", "clean", "package"]
EXPOSE 8080
CMD ["java", "-jar", "target/link-shorter-0.0.1-SNAPSHOT.jar"]