# 🐳 Docker - Pedido Service

## 📋 Descripción

Este directorio contiene el Dockerfile para construir y ejecutar el microservicio **Pedido Service** en un contenedor Docker.

## 🏗️ Características del Dockerfile

- **Multi-stage build**: Reduce el tamaño de la imagen final
- **Etapa 1 (Build)**: Maven + JDK 21 para compilar
- **Etapa 2 (Runtime)**: JRE 21 Alpine (imagen ligera)
- **Usuario no-root**: Mayor seguridad
- **Health check**: Monitoreo automático del estado del contenedor
- **Optimizado**: Uso de cache de capas de Docker

## 🚀 Construcción de la Imagen

### Construcción básica

```bash
# Desde el directorio pedido-service
docker build -t pedido-service:latest .
```

### Construcción con tag específico

```bash
docker build -t pedido-service:0.0.1-SNAPSHOT .
```

### Construcción sin cache

```bash
docker build --no-cache -t pedido-service:latest .
```

## 🎯 Ejecución del Contenedor

### Modo desarrollo (conexión a BD local)

```bash
docker run -d \
  --name pedido-service \
  -p 8084:8084 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/pedidos_db \
  -e SPRING_DATASOURCE_USERNAME=pedido_user \
  -e SPRING_DATASOURCE_PASSWORD=pedido_pass \
  -e BILLING_SERVICE_URL=http://host.docker.internal:8082 \
  pedido-service:latest
```

### Modo producción (con variables de entorno)

```bash
docker run -d \
  --name pedido-service \
  -p 8084:8084 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-pedidos:5432/pedidos_db \
  -e SPRING_DATASOURCE_USERNAME=pedido_user \
  -e SPRING_DATASOURCE_PASSWORD=pedido_pass \
  -e BILLING_SERVICE_URL=http://billing-service:8082 \
  -e FLEET_SERVICE_URL=http://fleet-service:8083 \
  -e JAVA_OPTS="-Xms512m -Xmx1024m" \
  pedido-service:latest
```

## 🔧 Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring Boot | `default` |
| `SPRING_DATASOURCE_URL` | URL de PostgreSQL | `jdbc:postgresql://localhost:5433/pedidos_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de BD | `pedido_user` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de BD | `pedido_pass` |
| `BILLING_SERVICE_URL` | URL del Billing Service | `http://localhost:8082` |
| `FLEET_SERVICE_URL` | URL del Fleet Service | `http://localhost:8083` |
| `BILLING_INTEGRATION_ENABLED` | Activar integración Billing | `true` |
| `FLEET_INTEGRATION_ENABLED` | Activar integración Fleet | `false` |
| `JAVA_OPTS` | Opciones JVM | `-Xms256m -Xmx512m` |

## 📊 Verificación del Contenedor

### Ver logs

```bash
docker logs pedido-service
docker logs -f pedido-service  # Seguir logs en tiempo real
```

### Verificar estado del health check

```bash
docker inspect --format='{{.State.Health.Status}}' pedido-service
```

### Acceder al contenedor

```bash
docker exec -it pedido-service sh
```

### Verificar el servicio

```bash
# Health check
curl http://localhost:8084/actuator/health

# Swagger UI
curl http://localhost:8084/swagger-ui.html
```

## 🛑 Detener y Eliminar

```bash
# Detener el contenedor
docker stop pedido-service

# Eliminar el contenedor
docker rm pedido-service

# Eliminar la imagen
docker rmi pedido-service:latest
```

## 🔍 Troubleshooting

### El contenedor no arranca

```bash
# Ver logs detallados
docker logs pedido-service

# Verificar variables de entorno
docker inspect pedido-service | grep -A 20 "Env"
```

### Problemas de conexión a la base de datos

Si estás en **Windows o Mac**, usa `host.docker.internal` en lugar de `localhost`:

```bash
-e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/pedidos_db
```

Si estás en **Linux**, usa `--network host` o crea una red Docker:

```bash
# Crear red
docker network create logiflow-network

# Ejecutar con la red
docker run -d \
  --name pedido-service \
  --network logiflow-network \
  -p 8084:8084 \
  pedido-service:latest
```

### El health check falla

El health check requiere que el actuator esté habilitado. Verifica en `pom.xml` que exista:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## 📦 Tamaño de la Imagen

La imagen final ocupa aproximadamente:
- **Etapa Build**: ~600 MB (no se incluye en la imagen final)
- **Etapa Runtime**: ~200-250 MB (Alpine + JRE 21 + JAR)

## 🎯 Para Docker Compose General

Este Dockerfile está diseñado para ser usado en un `docker-compose.yml` general. Ejemplo:

```yaml
version: '3.8'

services:
  pedido-service:
    build:
      context: ./pedido-service
      dockerfile: Dockerfile
    container_name: pedido-service
    ports:
      - "8084:8084"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-pedidos:5432/pedidos_db
      - SPRING_DATASOURCE_USERNAME=pedido_user
      - SPRING_DATASOURCE_PASSWORD=pedido_pass
      - BILLING_SERVICE_URL=http://billing-service:8082
      - FLEET_SERVICE_URL=http://fleet-service:8083
    depends_on:
      - postgres-pedidos
      - billing-service
    networks:
      - logiflow-network
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8084/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  postgres-pedidos:
    image: postgres:16-alpine
    container_name: postgres-pedidos
    environment:
      - POSTGRES_DB=pedidos_db
      - POSTGRES_USER=pedido_user
      - POSTGRES_PASSWORD=pedido_pass
    ports:
      - "5433:5432"
    volumes:
      - postgres_pedidos_data:/var/lib/postgresql/data
    networks:
      - logiflow-network

volumes:
  postgres_pedidos_data:

networks:
  logiflow-network:
    driver: bridge
```

## 📝 Notas

- El Dockerfile usa **multi-stage build** para optimizar el tamaño
- La imagen base es **Alpine** (más ligera)
- Se ejecuta con un **usuario no-root** (spring:spring) para mayor seguridad
- El **health check** verifica automáticamente que el servicio esté funcionando
- Las dependencias de Maven se cachean para acelerar builds posteriores

---

**¡Listo para usar en tu docker-compose general!** 🚀

