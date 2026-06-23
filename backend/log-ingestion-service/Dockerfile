# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /build

COPY pom.xml .
COPY common ./common
COPY log-ingestion-service ./log-ingestion-service

RUN mvn -pl common install -DskipTests
RUN mvn -pl log-ingestion-service package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /build/log-ingestion-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
