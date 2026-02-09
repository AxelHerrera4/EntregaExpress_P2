# Microservicio de Notificaciones - LogiFlow

## Descripción
Microservicio de notificaciones que consume eventos de RabbitMQ y envía alertas por correo electrónico. Implementa patrones de mensajería idempotente con deduplicación basada en messageId.

## Arquitectura

### Componentes Principales

#### Config
- **RabbitMQConfig**: Configuración de exchanges, colas y bindings
  - Exchange: `order.exchange` (TopicExchange)
  - Colas:
    - `order.created.queue` → routing key: `pedido.creado`
    - `order.status.updated.queue` → routing key: `pedido.estado.actualizado`
  
- **CacheConfig**: Configuración de cache para notificaciones y mensajes procesados
  - Cache de notificaciones
  - Cache de mensajes procesados (deduplicación)

#### Model
- **Notification**: Entidad para almacenar notificaciones
  - Usa UUID como identificador
  - Estados: PENDING, SENT, FAILED
  - Tipos: ORDER_CREATED, ORDER_STATUS_UPDATED

- **ProcessedMessage**: Entidad para deduplicación de mensajes
  - Almacena messageId para evitar procesamiento duplicado
  - Índice único en messageId

#### DTOs
- **OrderCreatedEventDto**: Evento de pedido creado
- **OrderStatusUpdatedEventDto**: Evento de actualización de estado
- **NotificationDto**: DTO de respuesta para notificaciones

#### Services
- **OrderEventConsumer**: Consumidor de eventos RabbitMQ
  - Procesa eventos de pedido.creado y pedido.estado.actualizado
  - Implementa deduplicación con IdempotencyManager
  - Envía notificaciones por email

- **NotificationService**: Servicio de gestión de notificaciones
  - CRUD de notificaciones
  - Cache de notificaciones por orderId
  - Envío de notificaciones

- **EmailService**: Servicio de envío de correos
  - Construcción de mensajes de email
  - Integración con JavaMailSender

- **OrderEventPublisher**: Publicador de eventos (para testing)

## Docker

### Estructura de Docker

El proyecto incluye:
- **Dockerfile**: Construcción multi-stage optimizada para producción
- **docker-compose.yml**: Orquestación completa de servicios
- **.dockerignore**: Optimización de contexto de build

### Dockerfile Multi-Stage

#### Etapa 1: Build
- Imagen base: `maven:3.9.6-eclipse-temurin-21-alpine`
- Descarga dependencias (cacheadas si pom.xml no cambia)
- Compila el proyecto sin ejecutar tests
- Genera el JAR de la aplicación

#### Etapa 2: Runtime
- Imagen base: `eclipse-temurin:21-jre-alpine` (más ligera)
- Usuario no-root para seguridad
- Healthcheck integrado
- Variables de entorno configurables

### Uso con Docker

#### 1. Configurar variables de entorno

Crea un archivo `.env` basado en `.env.example`:

```bash
cp .env.example .env
```

Edita `.env` y agrega tus credenciales de email:
```env
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password
```

#### 2. Ejecutar con Docker Compose

```bash
# Construir y levantar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f ms-notifications

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes (borra la BD)
docker-compose down -v
```

#### 3. Construir solo la imagen Docker

```bash
# Construir la imagen
docker build -t ms-notifications:latest .

# Ejecutar el contenedor manualmente
docker run -d \
  --name ms-notifications \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/db_notification \
  -e SPRING_RABBITMQ_HOST=rabbitmq \
  -e MAIL_USERNAME=tu-email@gmail.com \
  -e MAIL_PASSWORD=tu-app-password \
  ms-notifications:latest
```

### Servicios en Docker Compose

El `docker-compose.yml` incluye:

1. **rabbitmq**: Broker de mensajería
   - Puerto: 5672 (AMQP), 15672 (UI Management)
   - Credenciales: admin/admin
   - UI disponible en: http://localhost:15672

2. **postgres**: Base de datos
   - Puerto: 5434 (host) → 5432 (contenedor)
   - Base de datos: db_notification
   - Credenciales: parkin/qwerty123
   - Volumen persistente: pgdata

3. **ms-notifications**: Microservicio
   - Puerto: 8080
   - Healthcheck habilitado
   - Depende de postgres y rabbitmq (espera healthchecks)
   - Restart automático

### Verificar el servicio

```bash
# Healthcheck
curl http://localhost:8080/actuator/health

# API de notificaciones
curl http://localhost:8080/api/notifications

# Swagger UI
# http://localhost:8080/swagger-ui.html
```

### Troubleshooting

#### El servicio no arranca
```bash
# Ver logs detallados
docker-compose logs -f ms-notifications

# Verificar que postgres y rabbitmq estén healthy
docker-compose ps
```

#### Reconstruir después de cambios en código
```bash
# Reconstruir la imagen
docker-compose build ms-notifications

# Recrear el contenedor
docker-compose up -d --force-recreate ms-notifications
```

#### Limpiar todo y empezar de cero
```bash
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

#### Controllers
- **NotificationController**: API REST para consultar notificaciones
  - GET `/api/notifications` - Listar todas
  - GET `/api/notifications/{id}` - Por ID
  - GET `/api/notifications/order/{orderId}` - Por pedido
  - GET `/api/notifications/recipient/{email}` - Por destinatario
  - POST `/api/notifications/{id}/send` - Reenviar notificación

- **TestEventController**: API REST para testing
  - POST `/api/test/events/order-created/sample` - Evento de prueba
  - POST `/api/test/events/order-status-updated/sample` - Evento de prueba

#### Utils
- **IdempotencyManager**: Gestión de idempotencia
  - Verifica si un mensaje ya fue procesado (cache + BD)
  - Marca mensajes como procesados
  - Previene procesamiento duplicado

- **NotificationMapper**: Mapeo entre entidades y DTOs

## Características Implementadas

### ✅ Mensajería con RabbitMQ
- Exchange tipo Topic para routing flexible
- Colas durables con bindings específicos
- Configuración de reintentos y timeout
- Conversión automática a JSON

### ✅ Idempotencia y Deduplicación
- messageId UUID en todos los eventos
- Verificación en cache antes de base de datos
- Tabla de mensajes procesados con índice único
- Prevención de procesamiento duplicado

### ✅ Notificaciones por Email
- Integración con JavaMailSender
- Plantillas de email personalizadas
- Tracking de estado de envío
- Registro de intentos fallidos

### ✅ Cache
- Cache de notificaciones por orderId
- Cache de mensajes procesados
- Invalidación automática al crear/actualizar

### ✅ UUIDs
- Todos los IDs son UUID
- messageId para deduplicación
- orderId, customerId como UUID

## Configuración

### Base de Datos (PostgreSQL)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/db_notification
    username: parkin
    password: qwerty123
```

### RabbitMQ
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin
```

### Email (SMTP)
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

## Ejecución

### Pre-requisitos
1. PostgreSQL corriendo en puerto 5433
2. RabbitMQ corriendo en puerto 5672
3. Configurar credenciales de email en variables de entorno

### Docker Compose (recomendado)
```bash
docker-compose up -d
```

### Maven
```bash
./mvnw spring-boot:run
```

## Testing

### 1. Publicar evento de pedido creado
```bash
curl -X POST http://localhost:8080/api/test/events/order-created/sample
```

### 2. Publicar evento de actualización de estado
```bash
curl -X POST http://localhost:8080/api/test/events/order-status-updated/sample
```

### 3. Consultar notificaciones
```bash
curl http://localhost:8080/api/notifications
```

### 4. Consultar notificaciones por pedido
```bash
curl http://localhost:8080/api/notifications/order/{orderId}
```

## Flujo de Procesamiento

1. **Evento publicado** → RabbitMQ Exchange
2. **Routing** → Cola específica según routing key
3. **Consumer recibe** → Verifica idempotencia (cache → BD)
4. **Si no procesado** → Crea notificación y envía email
5. **Marca como procesado** → Actualiza cache y BD
6. **Actualiza cache** → Invalida cache de notificaciones

## Endpoints API

### Notificaciones
- `GET /api/notifications` - Listar todas las notificaciones
- `GET /api/notifications/{id}` - Obtener notificación por ID
- `GET /api/notifications/order/{orderId}` - Notificaciones de un pedido
- `GET /api/notifications/recipient/{email}` - Notificaciones de un destinatario
- `POST /api/notifications/{id}/send` - Reenviar una notificación

### Testing (Desarrollo)
- `POST /api/test/events/order-created` - Publicar evento personalizado
- `POST /api/test/events/order-created/sample` - Publicar evento de ejemplo
- `POST /api/test/events/order-status-updated` - Publicar evento personalizado
- `POST /api/test/events/order-status-updated/sample` - Publicar evento de ejemplo

## Estructura del Proyecto

```
src/main/java/logiflow/ms_notifications/
├── config/
│   ├── RabbitMQConfig.java
│   └── CacheConfig.java
├── controller/
│   ├── NotificationController.java
│   └── TestEventController.java
├── dto/
│   ├── OrderCreatedEventDto.java
│   ├── OrderStatusUpdatedEventDto.java
│   └── NotificationDto.java
├── model/
│   ├── Notification.java
│   └── ProcessedMessage.java
├── repository/
│   ├── NotificationRepository.java
│   └── ProcessedMessageRepository.java
├── service/
│   ├── NotificationService.java
│   ├── EmailService.java
│   ├── OrderEventConsumer.java
│   └── OrderEventPublisher.java
├── utils/
│   ├── NotificationMapper.java
│   └── IdempotencyManager.java
└── MsNotificationsApplication.java
```

## Tecnologías Utilizadas
- Spring Boot 4.0.2
- Spring AMQP (RabbitMQ)
- Spring Data JPA
- Spring Mail
- PostgreSQL
- Lombok
- SpringDoc OpenAPI (Swagger)

## Documentación API
Acceder a: http://localhost:8080/swagger-ui.html

