# Multi-stage Dockerfile for fleet-service (Spring Boot 4, Java 21)

# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy Maven descriptor and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/fleet-service-0.0.1-SNAPSHOT.jar app.jar

# Expose Fleet Service port
EXPOSE 8083

# Run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
