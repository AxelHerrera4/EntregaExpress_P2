# ==================================
# Dockerfile para Pedido Service
# Spring Boot 4.0.0 + Java 21
# ==================================

# Etapa 1: Build (construcción)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven y descargar dependencias
COPY pom.xml mvnw .mvn/ ./
RUN mvn dependency:go-offline -B && rm -rf /root/.m2/repository

# Copiar código fuente y compilar (sin ejecutar tests para acelerar el build)
COPY src ./src
RUN mvn clean package -DskipTests

# ==================================
# Etapa 2: Runtime (ejecución)
# Imagen más ligera solo con JRE
# ==================================
FROM eclipse-temurin:21-jre-alpine

# Metadata del contenedor
LABEL maintainer="LogiFlow Team" \
      service="pedido-service" \
      version="0.0.1-SNAPSHOT"

# Crear usuario no-root para mayor seguridad y establecer permisos
RUN addgroup -S spring && adduser -S spring -G spring

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR compilado desde la etapa de build
COPY --from=build /app/target/pedido-service-*.jar app.jar
RUN chown spring:spring app.jar

# Cambiar a usuario no-root
USER spring:spring

# Exponer el puerto del servicio
EXPOSE 8084

# Variables de entorno por defecto (pueden sobreescribirse)
ENV SPRING_PROFILES_ACTIVE=default \
    JAVA_OPTS="-Xms256m -Xmx512m"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
