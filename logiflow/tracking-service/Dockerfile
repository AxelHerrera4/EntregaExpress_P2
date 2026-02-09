# ==================================
# Dockerfile para Tracking Service
# Spring Boot + Java 21
# ==================================

FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiamos primero el pom.xml para aprovechar la caché de capas de Docker
COPY pom.xml .
# Descargamos dependencias (esto se cacheará si no cambias el pom.xml)
RUN mvn dependency:go-offline

# Copiamos el código fuente
COPY src ./src

# Compilamos el proyecto y generamos el JAR (saltando tests para rapidez)
RUN mvn clean package -DskipTests

# ===============================
# ETAPA 2: Ejecución (Runtime)
# ===============================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Instalar curl para healthchecks (opcional)
RUN apk add --no-cache curl

# Copiamos SOLO el JAR generado en la etapa anterior
# Nota: El asterisco *.jar nos evita tener que saber la versión exacta (0.0.1-SNAPSHOT)
COPY --from=builder /app/target/*.jar /app/app.jar

# Exponer el puerto
EXPOSE 8090

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=docker
ENV SPRING_RABBITMQ_HOST=rabbitmq
ENV SPRING_RABBITMQ_PORT=5672
ENV SPRING_RABBITMQ_USERNAME=admin
ENV SPRING_RABBITMQ_PASSWORD=admin

ENV JAVA_OPTS="-Xms128m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

