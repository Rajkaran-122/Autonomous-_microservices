# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /build

COPY pom.xml .
COPY common ./common
COPY api-gateway ./api-gateway

RUN mvn -pl common install -DskipTests
RUN mvn -pl api-gateway package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /build/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
