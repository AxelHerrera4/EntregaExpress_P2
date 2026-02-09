# ==================================
# Dockerfile para Pedido Service
# Spring Boot 4.0.0 + Java 21
# ==================================

# Etapa 1: Build (construcción)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiamos pom.xml y código fuente
COPY pom.xml .
COPY src ./src

# Compilar con descarga de dependencias integrada
RUN mvn clean package -DskipTests -B

# ==================================
# Etapa 2: Runtime (ejecución)
# ==================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="LogiFlow Team" \
      service="pedido-service" \
      version="0.0.1-SNAPSHOT"

# 1. INSTALACIÓN DE CURL (CRÍTICO)
# Necesario para que el healthcheck del docker-compose funcione en Alpine
RUN apk add --no-cache curl

# Crear usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copiar el JAR. Usamos *.jar para ser flexibles con el nombre de la versión
COPY --from=build /app/target/*.jar app.jar

# Asignar permisos al usuario no-root
RUN chown spring:spring app.jar

# Cambiar a usuario no-root
USER spring:spring

EXPOSE 8084

ENV SPRING_PROFILES_ACTIVE=docker \
    JAVA_OPTS="-Xms256m -Xmx512m"

# Usamos exec form directo para que Java reciba las señales de apagado (SIGTERM) correctamente
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]