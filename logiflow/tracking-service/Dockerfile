# Multi-stage Dockerfile: build con Maven y runtime con JRE 21 (Alpine)

# Stage 1: build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copiar pom y fuentes
COPY pom.xml ./
COPY src ./src

# Construir el JAR (sin tests para acelerar)
RUN mvn -B -DskipTests package

# Stage 2: runtime
FROM eclipse-temurin:21-jre-alpine

# Información del mantenedor
LABEL maintainer="logiflow@espe.edu.ec"
LABEL description="Tracking Service - LogiFlow"

# Instalar curl para healthchecks
RUN apk add --no-cache curl

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR construido desde la etapa de build
COPY --from=build /workspace/target/*.jar app.jar

# Exponer el puerto
EXPOSE 8090

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=docker
ENV RABBITMQ_HOST=rabbitmq
ENV RABBITMQ_PORT=5672
ENV RABBITMQ_USERNAME=guest
ENV RABBITMQ_PASSWORD=guest

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
