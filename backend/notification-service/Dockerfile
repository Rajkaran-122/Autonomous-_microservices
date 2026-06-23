# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /build

COPY pom.xml .
COPY common ./common
COPY notification-service ./notification-service

RUN mvn -pl common install -DskipTests
RUN mvn -pl notification-service package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /build/notification-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]
