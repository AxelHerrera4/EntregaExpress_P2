FROM eclipse-temurin:21-jre-alpine

# Información del mantenedor
LABEL maintainer="logiflow@espe.edu.ec"
LABEL description="Tracking Service - LogiFlow"

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR de la aplicación
COPY target/*.jar app.jar

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

