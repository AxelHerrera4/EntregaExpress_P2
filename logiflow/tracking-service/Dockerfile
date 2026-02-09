# ==================================
# Dockerfile para Tracking Service
# Spring Boot + Java 21
# ==================================

# Etapa 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar pom.xml y código fuente
COPY pom.xml .
COPY src ./src

# Compilar
RUN mvn clean package -DskipTests -B

# ==================================
# Etapa 2: Runtime
# ==================================
FROM eclipse-temurin:21-jre-alpine

# Información del mantenedor
LABEL maintainer="logiflow@espe.edu.ec"
LABEL description="Tracking Service - LogiFlow"

# Instalamos CURL para healthcheck
RUN apk add --no-cache curl

# Crear usuario seguro
RUN addgroup -S spring && adduser -S spring -G spring

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR de la aplicación
COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring

# Exponer el puerto
EXPOSE 8090

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=docker
ENV SPRING_RABBITMQ_HOST=rabbitmq
ENV SPRING_RABBITMQ_PORT=5672
ENV SPRING_RABBITMQ_USERNAME=admin
ENV SPRING_RABBITMQ_PASSWORD=admin

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]

