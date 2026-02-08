# Dockerfile para ms-notifications
# Usa el JAR ya compilado en target (construya localmente con ./mvnw package o mvn package)
# Imagen base con JRE 21
FROM eclipse-temurin:21-jre-alpine

# Metadatos
LABEL maintainer="equipo LogiFlow"

# Instalar curl para healthchecks
RUN apk add --no-cache curl

# Argumento opcional para el nombre del JAR
ARG JAR_FILE=target/ms-notifications-0.0.1-SNAPSHOT.jar

# Copia el JAR desde el contexto de construcción
COPY ${JAR_FILE} /app/app.jar

# Puerto por defecto (ajustable vía SPRING_APPLICATION_JSON o propiedades)
EXPOSE 8080

# Variables de entorno recomendadas
ENV JAVA_OPTS="-Xms128m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS

# Healthcheck simple (requiere curl dentro de la imagen; en Alpine usar wget si no se instala curl)
# Comentado por defecto; descomentar si se instala curl
# HEALTHCHECK --interval=30s --timeout=3s --start-period=10s CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
