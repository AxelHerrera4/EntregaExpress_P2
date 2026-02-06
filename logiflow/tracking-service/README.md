# Tracking Service - LogiFlow

Microservicio de tracking para el proyecto LogiFlow. Este servicio gestiona el envío de ubicaciones de repartidores a través de RabbitMQ.

## Características

- **Framework**: Spring Boot 3.2.5
- **Java**: 21
- **Puerto**: 8090
- **Mensajería**: RabbitMQ (AMQP)

## Configuración de RabbitMQ

- **Host**: localhost (puerto 5672)
- **Exchange**: exchange-tracking (tipo: Topic)
- **Queue**: tracking.ubicacion
- **Routing Key**: repartidor.ubicacion
- **Usuario**: guest
- **Contraseña**: guest

## Endpoints

### POST /api/tracking/track

Envía la ubicación de un repartidor a RabbitMQ.

**Request Body**:
```json
{
  "repartidorId": 1,
  "latitud": -0.1807,
  "longitud": -78.4678,
  "timestamp": "2026-02-05T23:30:00"
}
```

**Response**:
```json
"Ubicación enviada correctamente"
```

## Ejecutar localmente

### Prerequisitos

1. Tener RabbitMQ corriendo en Docker:
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

2. Verificar que RabbitMQ esté activo en http://localhost:15672 (usuario: guest, contraseña: guest)

### Iniciar el servicio

Desde IntelliJ IDEA:
1. Abrir el proyecto `tracking-service`
2. Ejecutar la clase `TrackingServiceApplication`
3. El servicio estará disponible en `http://localhost:8090`

Desde la línea de comandos:
```bash
cd tracking-service
mvnw spring-boot:run
```

## Probar el servicio

### Con curl:
```bash
curl -X POST http://localhost:8090/api/tracking/track \
  -H "Content-Type: application/json" \
  -d "{\"repartidorId\":1,\"latitud\":-0.1807,\"longitud\":-78.4678,\"timestamp\":\"2026-02-05T23:30:00\"}"
```

### Con Postman:
1. Método: POST
2. URL: http://localhost:8090/api/tracking/track
3. Headers: Content-Type: application/json
4. Body (raw JSON):
```json
{
  "repartidorId": 1,
  "latitud": -0.1807,
  "longitud": -78.4678,
  "timestamp": "2026-02-05T23:30:00"
}
```

## Estructura del proyecto

```
tracking-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ec/edu/espe/trackingservice/
│   │   │       ├── TrackingServiceApplication.java
│   │   │       ├── config/
│   │   │       │   └── RabbitConfig.java
│   │   │       ├── controller/
│   │   │       │   └── TrackingController.java
│   │   │       ├── dto/
│   │   │       │   └── UbicacionDTO.java
│   │   │       └── service/
│   │   │           └── TrackingProducer.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── ec/edu/espe/trackingservice/
│               └── TrackingServiceApplicationTests.java
└── pom.xml
```

## Dependencias principales

- `spring-boot-starter-web`: Para crear endpoints REST
- `spring-boot-starter-amqp`: Para integración con RabbitMQ
- `lombok`: Para reducir código boilerplate

## Notas técnicas

- El servicio usa `Jackson2JsonMessageConverter` para serializar/deserializar mensajes JSON en RabbitMQ
- Las queues y exchanges se crean automáticamente al iniciar la aplicación
- El nivel de logging está configurado en DEBUG para facilitar el desarrollo

