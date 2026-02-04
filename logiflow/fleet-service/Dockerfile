# ==================================
# Dockerfile para Fleet Service
# ==================================

# Etapa 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar dependencias y código
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ==================================
# Etapa 2: Runtime
# ==================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="LogiFlow Team" \
      service="auth-service"

# 1. INSTALAMOS CURL (CRÍTICO)
# Sin esto, el healthcheck de tu docker-compose fallará
RUN apk add --no-cache curl

# Crear usuario seguro
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copiar el JAR
COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring

# 2. PUERTO CORRECTO PARA AUTH SERVICE
# Es importante que sea el 8081
EXPOSE 8083

ENV SPRING_PROFILES_ACTIVE=docker \
    JAVA_OPTS="-Xms256m -Xmx512m"

# 3. SIN HEALTHCHECK INTERNO CONFLICTIVO
# Hemos borrado la línea "HEALTHCHECK CMD java..." que causaba el fallo.
# Ahora dejamos que docker-compose use curl.

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]