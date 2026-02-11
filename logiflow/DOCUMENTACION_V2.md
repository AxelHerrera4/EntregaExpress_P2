# 📦 DOCUMENTACIÓN DEL PROYECTO - LogiFlow v2.0

## 🎯 Descripción General

LogiFlow es un **Sistema de Gestión Logística Distribuido** basado en arquitectura de microservicios con Spring Boot, diseñado para la administración eficiente de órdenes de entrega, autenticación centralizada, facturación dinámica, gestión de flota en tiempo real y seguimiento de ubicaciones mediante eventos.

**Stack Tecnológico:** Java 21 + Spring Boot 4.0.0 + PostgreSQL 16 + RabbitMQ + Docker + GraphQL

---

## 📁 Estructura del Proyecto

```
EntregaExpress_P2/
├── logiflow/                       # Directorio principal del proyecto
│   ├── api-gateway/                # Gateway de enrutamiento (Puerto 8080)
│   ├── authservice/                # Servicio de autenticación JWT (Puerto 8081)
│   ├── billing-service/            # Servicio de facturación (Puerto 8082)
│   ├── fleet-service/              # Gestión de flota (Puerto 8083)
│   ├── pedido-service/             # Gestión de pedidos (Puerto 8084)
│   ├── ms-notifications/           # Notificaciones por email (Puerto 8085)
│   ├── delivery-graphql-service/   # API GraphQL (Puerto 8086)
│   ├── tracking-service/           # Seguimiento en tiempo real (Puerto 8090)
│   ├── docker-compose.yml          # Orquestación completa de contenedores
│   ├── DOCKER_SETUP.md             # Instrucciones detalladas de Docker
│   └── REINTENTO_ASIGNACION.md    # Documentación de reintentos automáticos
├── kubernets/                      # Archivos de despliegue en Kubernetes
├── docker-compose.yml              # Docker Compose raíz
├── README.md                       # Documentación general del sistema
└── imagenes/                       # Recursos visuales y diagramas
```

### Estructura Interna Estándar de Microservicios

Todos los microservicios Java siguen esta estructura:

```
[microservicio]/
├── src/
│   ├── main/
│   │   ├── java/com/logiflow/[servicio]/
│   │   │   ├── config/          # Configuraciones (Security, CORS, RabbitMQ)
│   │   │   ├── controller/      # Endpoints REST API
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── model/           # Entidades JPA
│   │   │   ├── repository/      # Repositorios Spring Data
│   │   │   ├── service/         # Lógica de negocio
│   │   │   ├── exception/       # Excepciones personalizadas
│   │   │   └── [Servicio]Application.java
│   │   └── resources/
│   │       ├── application.yml  # Configuración del servicio
│   │       └── static/          # Recursos estáticos (si aplica)
│   └── test/
│       └── java/                # Tests unitarios y de integración
├── .mvn/                         # Maven Wrapper
├── Dockerfile                    # Imagen Docker del servicio
├── docker-compose.yml            # Compose individual (opcional)
├── pom.xml                       # Dependencias Maven
├── mvnw / mvnw.cmd              # Maven Wrapper scripts
└── README.md                     # Documentación específica
```

---

## 🏗️ Arquitectura del Sistema

### Diagrama de Microservicios

```
                    ┌─────────────────────────┐
                    │    CLIENTES EXTERNOS    │
                    └───────────┬─────────────┘
                                │
                                ▼
                    ┌─────────────────────────┐
                    │   🔀 API GATEWAY        │
                    │   (Puerto 8080)         │
                    │   Spring Cloud Gateway  │
                    └───┬────────────────┬────┘
                        │                │
        ┌───────────────┼────────────────┼─────────────────┐
        │               │                │                 │
        ▼               ▼                ▼                 ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐  ┌──────────────┐
│ 🔐 Auth     │ │ 💳 Billing  │ │ 🚗 Fleet    │  │ 📦 Pedido    │
│ Service     │ │ Service     │ │ Service     │  │ Service      │
│ (8081)      │ │ (8082)      │ │ (8083)      │  │ (8084)       │
└─────┬───────┘ └─────┬───────┘ └─────┬───────┘  └──────┬───────┘
      │               │               │                  │
      ▼               ▼               ▼                  ▼
┌──────────┐    ┌──────────┐    ┌──────────┐      ┌──────────┐
│PostgreSQL│    │PostgreSQL│    │PostgreSQL│      │PostgreSQL│
│jwt_demo  │    │billing_db│    │fleet_db  │      │pedidos_db│
│(5432)    │    │(5433)    │    │(5435)    │      │(5436)    │
└──────────┘    └──────────┘    └──────────┘      └──────────┘

        ┌────────────────────────────────────────────┐
        │         📨 RabbitMQ (5672)                 │
        │         Exchange: order.exchange           │
        │         Exchange: fleet.exchange           │
        └────────────────┬───────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐  ┌─────────────┐  ┌──────────────┐
│ 📧 Notif     │  │ 📍 Tracking │  │ 🔍 GraphQL   │
│ Service      │  │ Service     │  │ Service      │
│ (8085)       │  │ (8090)      │  │ (8086)       │
└──────────────┘  └─────────────┘  └──────────────┘
```

### Matriz de Comunicación entre Servicios

| Servicio | Auth | Billing | Fleet | Pedido | Gateway | RabbitMQ |
|----------|------|---------|-------|--------|---------|----------|
| **Auth Service** | - | Valida | Valida | Valida | ✓ | - |
| **Billing Service** | ✓ | - | - | REST | ✓ | - |
| **Fleet Service** | ✓ | - | - | REST | ✓ | ✓ (Consumer) |
| **Pedido Service** | ✓ | REST | REST | - | ✓ | ✓ (Producer) |
| **Notifications** | - | - | - | - | - | ✓ (Consumer) |
| **Tracking** | - | - | - | - | - | ✓ (Producer) |
| **GraphQL** | ✓ | ✓ | ✓ | ✓ | ✓ | - |

---

## 🔄 Flujo de Desarrollo del Proyecto

### Fase 1: Análisis y Diseño

**Actividades realizadas:**
- Definición de arquitectura de microservicios distribuidos
- Identificación de dominios bounded context (Auth, Billing, Fleet, Pedidos)
- Diseño de base de datos independiente por servicio (Database per Service pattern)
- Definición de contratos de API REST entre servicios
- Diseño de eventos asíncronos con RabbitMQ
- Selección de patrones de diseño (Strategy, Factory, Repository, DTO)

**Diagramas creados:**
- Diagrama de arquitectura general
- Diagramas de secuencia por caso de uso
- Modelo entidad-relación por servicio
- Diagrama de eventos RabbitMQ

### Fase 2: Configuración del Entorno

**Requisitos previos instalados:**
```bash
- Java 21 (OpenJDK o Oracle JDK)
- Maven 3.8+
- Docker Desktop 4.x
- PostgreSQL 16
- Node.js 18+ (para GraphQL Service)
- Git
- Postman (para testing)
```

**Configuración inicial:**
```bash
# Variables de entorno necesarias
JAVA_HOME=/path/to/java21
MAVEN_HOME=/path/to/maven
PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH

# Docker configurado con al menos 4GB RAM
```

### Fase 3: Desarrollo de Microservicios (Orden de Implementación)

#### 1. **Auth Service** - Autenticación y Autorización (Puerto 8081)

**Responsabilidades:**
- Registro de usuarios con validación de datos
- Login con credenciales (username/password)
- Generación de JWT (Access Token + Refresh Token)
- Validación de tokens para otros servicios
- Gestión de roles: USER, REPARTIDOR, ADMIN

**Tecnologías clave:**
- Spring Security 6
- JWT (io.jsonwebtoken:jjwt-api:0.12.6)
- BCrypt para encriptación de contraseñas
- PostgreSQL para almacenar usuarios

**Endpoints principales:**
```
POST   /api/auth/register        - Registro de usuario
POST   /api/auth/login           - Inicio de sesión
POST   /api/auth/token/refresh   - Refrescar token
GET    /api/protected/me         - Obtener info del usuario autenticado
GET    /api/protected/admin-only - Endpoint solo para ADMIN
```

**Base de datos:**
- BD: `jwt_demo`
- Puerto: 5432
- Usuario: admin / admin
- Tabla principal: `users` (id, username, email, password_hash, role, created_at)

#### 2. **Billing Service** - Facturación y Cálculo de Tarifas (Puerto 8082)

**Responsabilidades:**
- Cálculo dinámico de tarifas según tipo de entrega y distancia
- Gestión de tarifas base por modalidad
- Creación y actualización de facturas
- Implementación de patrones Strategy y Factory

**Patrones de Diseño Implementados:**

**Patrón Strategy** - Cálculo de Tarifas:
```java
// Estrategias disponibles:
- TarifaUrbanaStrategy:          Base + (0.5 × km)
- TarifaIntermunicipalStrategy:  Base + (1.0 × km)
- TarifaNacionalStrategy:        Base + (1.5 × km)
- DefaultTarifaStrategy:         Base + (0.8 × km)
```

**Endpoints principales:**
```
POST   /api/facturas                    - Crear factura
GET    /api/facturas/{id}               - Obtener factura
PATCH  /api/facturas/{id}/estado        - Actualizar estado
POST   /api/tarifas-base                - Crear tarifa base
GET    /api/tarifas-base                - Listar tarifas
```

**Estados de factura:**
- BORRADOR → PENDIENTE → PAGADA
- BORRADOR → CANCELADA

**Base de datos:**
- BD: `db_billing_users`
- Puerto: 5433
- Usuario: billing / qwerty123
- Tablas: `facturas`, `tarifas_base`

#### 3. **Fleet Service** - Gestión de Flota (Puerto 8083)

**Responsabilidades:**
- CRUD de vehículos (clasificación por tipo: Motorizado, Liviano, Camión)
- CRUD de repartidores con validación de licencias
- Asignación automática de vehículos a repartidores
- Gestión de estados de repartidores
- Métricas y estadísticas de desempeño
- Consumo de eventos RabbitMQ para asignaciones

**Estados de repartidor:**
- DISPONIBLE - Listo para recibir pedidos
- EN_RUTA - Realizando una entrega
- DESCANSO - En período de descanso
- OCUPADO - Ocupado con múltiples entregas
- DESCONECTADO - Fuera de servicio

**Endpoints principales:**
```
# Vehículos
POST   /vehiculos                     - Crear vehículo
GET    /vehiculos/disponibles         - Listar vehículos sin asignar
PATCH  /vehiculos/{id}/estado         - Cambiar estado

# Repartidores
POST   /repartidores                  - Crear repartidor
GET    /repartidores/disponibles      - Listar disponibles
GET    /repartidores/zona/{zona}      - Filtrar por zona
GET    /repartidores/{id}/metricas    - Métricas individuales
POST   /repartidores/{id}/asignar-vehiculo

# Estadísticas
GET    /estadisticas/flota            - Estadísticas generales
```

**Base de datos:**
- BD: `fleet_db`
- Puerto: 5435
- Usuario: fleet_user / fleet_password
- Tablas: `vehiculos`, `repartidores`

**Factory Pattern para Vehículos:**
- Creación polimórfica según tipo de vehículo
- Validación de capacidad de carga
- Asignación automática de características

#### 4. **Pedido Service** - Gestión de Pedidos (Puerto 8084) [CORE]

**Responsabilidades:**
- Creación de pedidos con validación completa
- Coordinación con Billing Service para cálculo de tarifa
- Coordinación con Fleet Service para asignación de repartidor
- Gestión de estados del ciclo de vida del pedido
- Publicación de eventos a RabbitMQ
- Sistema de reintento automático para asignaciones fallidas

**Flujo de creación de pedido:**
```
1. Cliente envía solicitud con datos de origen/destino
2. Validación de datos de entrada
3. Llamada a Billing Service → calcular tarifa
4. Llamada a Fleet Service → asignar repartidor
5. Guardar pedido en BD con factura_id y repartidor_id
6. Publicar evento "pedido.creado" a RabbitMQ
7. Retornar respuesta al cliente
```

**Estados del pedido:**
```
PENDIENTE → ASIGNADO → EN_CAMINO → EN_DESTINO → ENTREGADO
    ↓
CANCELADO (en cualquier momento antes de ENTREGADO)
```

**Modalidades de servicio:**
- URBANA_RAPIDA - Entrega dentro de la ciudad (< 2 horas)
- INTERMUNICIPAL - Entre ciudades cercanas (< 24 horas)
- NACIONAL - A nivel nacional (2-5 días)

**Tipos de entrega:**
- EXPRESS - Máxima prioridad
- NORMAL - Prioridad estándar
- PROGRAMADA - Fecha/hora específica

**Endpoints principales:**
```
POST   /api/pedidos                    - Crear pedido
GET    /api/pedidos/{id}               - Obtener por ID
GET    /api/pedidos/cliente/{id}       - Listar de un cliente
PATCH  /api/pedidos/{id}               - Actualizar parcialmente
PATCH  /api/pedidos/{id}/cancelar      - Cancelar pedido
POST   /api/pedidos/reintento-asignacion - Reintentar asignaciones
```

**Base de datos:**
- BD: `pedidos_db`
- Puerto: 5436
- Usuario: pedido_user / pedido_pass
- Tablas: `pedidos`, `direcciones`, `incidencias`

#### 5. **API Gateway** - Puerta de Entrada (Puerto 8080)

**Responsabilidades:**
- Punto de entrada único para todos los clientes
- Enrutamiento dinámico a microservicios
- Validación de JWT en cada request
- Rate limiting y throttling
- Logging centralizado de peticiones

**Rutas configuradas:**
```yaml
/api/auth/**     → authservice:8081
/api/facturas/** → billing-service:8082
/api/tarifas/**  → billing-service:8082
/api/vehiculos/** → fleet-service:8083
/api/repartidores/** → fleet-service:8083
/api/pedidos/**  → pedido-service:8084
/graphql/**      → delivery-graphql-service:8086
```

**Filtros aplicados:**
- AuthenticationFilter - Validación JWT
- RewritePath - Normalización de rutas
- CircuitBreaker - Tolerancia a fallos

#### 6. **Tracking Service** - Seguimiento GPS (Puerto 8090)

**Responsabilidades:**
- Recepción de ubicaciones GPS de repartidores
- Publicación de eventos de tracking a RabbitMQ
- Actualización en tiempo real de posiciones

**Tecnologías:**
- Spring Boot 3.2.5
- RabbitMQ AMQP
- Topic Exchange para routing flexible

**Configuración RabbitMQ:**
```
Exchange: exchange-tracking (Topic)
Queue: tracking.ubicacion
Routing Key: repartidor.ubicacion
```

**Endpoint:**
```
POST /api/tracking/track
Body: {
  "repartidorId": 1,
  "latitud": -0.1807,
  "longitud": -78.4678,
  "timestamp": "2026-02-05T23:30:00"
}
```

#### 7. **Notifications Service** - Notificaciones por Email (Puerto 8085)

**Responsabilidades:**
- Consumo de eventos RabbitMQ (pedido.creado, pedido.estado.actualizado)
- Envío de notificaciones por email usando JavaMailSender
- Deduplicación de mensajes para evitar duplicados
- Cache de notificaciones para performance

**Eventos consumidos:**
```
- pedido.creado → Notificar creación de pedido
- pedido.estado.actualizado → Notificar cambio de estado
```

**Tecnologías:**
- Spring Boot + Spring Mail
- RabbitMQ Consumer
- PostgreSQL para almacenar notificaciones
- Cache con @Cacheable

**Configuración Email:**
```yaml
spring.mail.host: smtp.gmail.com
spring.mail.port: 587
spring.mail.protocol: smtp
spring.mail.properties.mail.smtp.auth: true
spring.mail.properties.mail.smtp.starttls.enable: true
```

**Estados de notificación:**
- PENDING - En cola
- SENT - Enviada exitosamente
- FAILED - Falló el envío

**Patrón Idempotency:**
- Tabla `processed_messages` con messageId único
- Previene procesamiento duplicado de eventos

#### 8. **GraphQL Service** - API Unificada (Puerto 8086)

**Responsabilidades:**
- API GraphQL para consultas complejas y flexibles
- Federación de datos de múltiples microservicios
- Mutaciones para operaciones críticas
- Queries por zona/ciudad con filtros avanzados

**Tecnologías:**
- Node.js + TypeScript
- Apollo Server
- Axios para llamadas REST

**Mutaciones Implementadas:**

1. **actualizarEstadoRepartidor** - Cambiar disponibilidad de repartidor
2. **reasignarPedido** - Reasignación manual de pedidos
3. **actualizarDatosContacto** - Actualizar perfil de usuario
4. **registrarIncidencia** - Reportar problemas en entregas

**Queries Especializadas:**
```graphql
# Por zona
pedidosPorZona(zonaId: ID!, estado: EstadoPedido)

# Por ciudad
pedidosPorCiudadOrigen(ciudad: String!, provincia: String)
pedidosPorCiudadDestino(ciudad: String!, provincia: String)
pedidosPorRuta(ciudadOrigen: String!, ciudadDestino: String!)

# Estadísticas
estadisticasPorCiudad(ciudad: String!, tipo: String!)
```

**Acceso:**
- Endpoint: `http://localhost:8080/graphql` (vía Gateway)
- Playground: `http://localhost:4000/graphql` (directo)

---

## 🔗 Flujo Completo de Creación de Pedido

![Diagrama de Flujo]
*Insertar imagen del flujo de comunicación*

### Descripción Detallada del Flujo

```
┌─────────────────────────────────────────────────────────────┐
│ PASO 1: CLIENTE REALIZA SOLICITUD                          │
└─────────────────────────────────────────────────────────────┘
Cliente → POST http://localhost:8080/api/pedidos
Headers: 
  Authorization: Bearer <JWT_TOKEN>
  Content-Type: application/json
Body: {
  "clienteId": "cliente123",
  "modalidadServicio": "URBANA_RAPIDA",
  "tipoEntrega": "EXPRESS",
  "peso": 5.0,
  "direccionOrigen": {...},
  "direccionDestino": {...}
}

┌─────────────────────────────────────────────────────────────┐
│ PASO 2: API GATEWAY PROCESA                                │
└─────────────────────────────────────────────────────────────┘
1. Extrae JWT del header Authorization
2. Valida token con Auth Service (GET /api/auth/validate)
3. Verifica roles y permisos
4. Enruta a Pedido Service (http://pedido-service:8084/api/pedidos)

┌─────────────────────────────────────────────────────────────┐
│ PASO 3: PEDIDO SERVICE - CÁLCULO DE TARIFA                 │
└─────────────────────────────────────────────────────────────┘
1. Valida datos de entrada (direcciones, peso, modalidad)
2. Calcula distancia entre origen y destino (usando algoritmo)
3. Llama a Billing Service:
   POST http://billing-service:8082/api/facturas
   Body: {
     "pedidoId": "uuid-generado",
     "tipoEntrega": "URBANA",
     "distanciaKm": 15.5,
     "peso": 5.0
   }

┌─────────────────────────────────────────────────────────────┐
│ PASO 4: BILLING SERVICE - APLICACIÓN DE ESTRATEGIA         │
└─────────────────────────────────────────────────────────────┘
1. Obtiene tarifa base de la tabla tarifas_base (e.g., $5.00)
2. Selecciona estrategia según tipoEntrega:
   - URBANA → TarifaUrbanaStrategy → 5.00 + (0.5 × 15.5) = $12.75
3. Crea factura en estado BORRADOR
4. Retorna: {
     "facturaId": "uuid",
     "montoTotal": 12.75,
     "estado": "BORRADOR"
   }

┌─────────────────────────────────────────────────────────────┐
│ PASO 5: PEDIDO SERVICE - ASIGNACIÓN DE REPARTIDOR          │
└─────────────────────────────────────────────────────────────┘
1. Guarda factura_id en el pedido
2. Llama a Fleet Service:
   GET http://fleet-service:8083/api/repartidores/disponibles?zona=NORTE
3. Fleet Service busca repartidor con estado=DISPONIBLE
4. Si encuentra: Asigna vehículo y cambia estado a EN_RUTA
5. Retorna: {
     "repartidorId": 1,
     "vehiculoId": 5,
     "nombre": "Juan Pérez"
   }

┌─────────────────────────────────────────────────────────────┐
│ PASO 6: PEDIDO SERVICE - GUARDAR EN BASE DE DATOS          │
└─────────────────────────────────────────────────────────────┘
INSERT INTO pedidos (
  id, cliente_id, factura_id, repartidor_id, vehiculo_id,
  estado, modalidad_servicio, tipo_entrega,
  direccion_origen_id, direccion_destino_id, created_at
) VALUES (
  'uuid', 'cliente123', 'factura-uuid', 1, 5,
  'ASIGNADO', 'URBANA_RAPIDA', 'EXPRESS',
  origen_id, destino_id, NOW()
);

┌─────────────────────────────────────────────────────────────┐
│ PASO 7: PEDIDO SERVICE - PUBLICAR EVENTO RABBITMQ          │
└─────────────────────────────────────────────────────────────┘
RabbitTemplate.convertAndSend(
  exchange: "order.exchange",
  routingKey: "pedido.creado",
  message: {
    "pedidoId": "uuid",
    "clienteId": "cliente123",
    "repartidorId": 1,
    "estado": "ASIGNADO",
    "timestamp": "2026-02-10T10:30:00Z"
  }
);

┌─────────────────────────────────────────────────────────────┐
│ PASO 8: NOTIFICATIONS SERVICE - ENVIAR EMAIL               │
└─────────────────────────────────────────────────────────────┘
1. Consumer recibe evento de queue "order.created.queue"
2. Verifica si messageId ya fue procesado (idempotencia)
3. Construye email HTML:
   - Asunto: "Nuevo Pedido Creado - #{pedidoId}"
   - Cuerpo: Detalles del pedido, repartidor, tarifa
4. Envía email usando JavaMailSender
5. Guarda notificación con estado SENT

┌─────────────────────────────────────────────────────────────┐
│ PASO 9: RESPUESTA AL CLIENTE                               │
└─────────────────────────────────────────────────────────────┘
HTTP 201 Created
{
  "pedidoId": "uuid",
  "estado": "ASIGNADO",
  "factura": {
    "id": "factura-uuid",
    "montoTotal": 12.75
  },
  "repartidor": {
    "id": 1,
    "nombre": "Juan Pérez"
  },
  "vehiculo": {
    "id": 5,
    "placa": "ABC-123"
  },
  "tiempoEstimado": "45 minutos"
}
```

---

## 🔄 Sistema de Reintento Automático

### Problema
Cuando no hay repartidores disponibles, el pedido queda en estado PENDIENTE sin asignación.

### Solución Implementada

**Arquitectura Event-Driven 100% con RabbitMQ:**

```
Pedido Service                  RabbitMQ                  Fleet Service
     │                            │                            │
     │──(1) POST /pedidos────────→│                            │
     │                            │                            │
     │──(2) Publish──────────────→│                            │
     │   pedido.creado            │                            │
     │                            │──(3) Consume─────────────→│
     │                            │                            │
     │                            │                  (4) No hay repartidores
     │                            │                            │
     │                            │◄──(5) Ack sin asignar─────│
     │                            │                            │
     │                            │                            │
     │──(6) POST /reintento──────→│                            │
     │                            │                            │
     │──(7) Publish──────────────→│                            │
     │   pedido.reintento         │                            │
     │                            │──(8) Consume─────────────→│
     │                            │                            │
     │                            │               (9) Asigna repartidor
     │                            │                            │
     │                            │◄──(10) Publish────────────│
     │                            │    asignacion.completada   │
     │◄──(11) Consume────────────│                            │
     │                            │                            │
     │  (12) Actualiza pedido     │                            │
     │       a ASIGNADO           │                            │
```

**Endpoint de reintento:**
```
POST /api/pedidos/reintento-asignacion
Body: {
  "pedidoId": "uuid",
  "zonaPreferida": "NORTE"
}
```

**Eventos RabbitMQ involucrados:**
- `pedido.creado` → Primer intento de asignación
- `pedido.reintento.asignacion` → Reintento manual
- `asignacion.completada` → Fleet notifica éxito

---

## 🚀 Despliegue y Ejecución

### Opción 1: Docker Compose (Recomendado)

```bash
# Navegar al directorio logiflow
cd /home/jairobonilla/Escritorio/software\ projects/EntregaExpress_P2/logiflow

# Levantar todos los servicios
docker-compose up -d

# Verificar que todos los contenedores estén activos
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f pedido-service

# Detener servicios
docker-compose down

# Limpiar volúmenes (CUIDADO: borra datos)
docker-compose down -v
```

### Opción 2: Ejecución Manual (Desarrollo Local)

**1. Iniciar bases de datos:**
```bash
# Auth DB
docker run --name postgres-auth -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=jwt_demo -p 5432:5432 -d postgres:16-alpine

# Billing DB
docker run --name postgres-billing -e POSTGRES_USER=billing -e POSTGRES_PASSWORD=qwerty123 -e POSTGRES_DB=db_billing_users -p 5433:5432 -d postgres:16-alpine

# Fleet DB
docker run --name postgres-fleet -e POSTGRES_USER=fleet_user -e POSTGRES_PASSWORD=fleet_password -e POSTGRES_DB=fleet_db -p 5435:5432 -d postgres:16-alpine

# Pedido DB
docker run --name postgres-pedido -e POSTGRES_USER=pedido_user -e POSTGRES_PASSWORD=pedido_pass -e POSTGRES_DB=pedidos_db -p 5436:5432 -d postgres:16-alpine

# RabbitMQ
docker run --name rabbitmq -p 5672:5672 -p 15672:15672 -d rabbitmq:3-management
```

**2. Compilar y ejecutar cada microservicio:**
```bash
# Auth Service
cd authservice
./mvnw clean package -DskipTests
./mvnw spring-boot:run

# Billing Service (nueva terminal)
cd billing-service
./mvnw clean package -DskipTests
./mvnw spring-boot:run

# Fleet Service (nueva terminal)
cd fleet-service
./mvnw clean package -DskipTests
./mvnw spring-boot:run

# Pedido Service (nueva terminal)
cd pedido-service
./mvnw clean package -DskipTests
./mvnw spring-boot:run

# API Gateway (nueva terminal)
cd api-gateway
./mvnw clean package -DskipTests
./mvnw spring-boot:run

# Tracking Service
cd tracking-service
./mvnw spring-boot:run

# Notifications Service
cd ms-notifications
./mvnw spring-boot:run

# GraphQL Service
cd delivery-graphql-service
npm install
npm run dev
```

### Verificación de Servicios Activos

```bash
# Health checks
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/api/auth/health  # Auth Service
curl http://localhost:8082/actuator/health  # Billing Service
curl http://localhost:8083/health           # Fleet Service
curl http://localhost:8084/actuator/health  # Pedido Service

# RabbitMQ Management UI
open http://localhost:15672  # guest/guest
```

---

## 📊 Endpoints Completos por Servicio

### Auth Service (8081)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | /api/auth/register | Registro de usuario | No |
| POST | /api/auth/login | Inicio de sesión | No |
| POST | /api/auth/token/refresh | Refrescar token | No |
| GET | /api/protected/me | Info usuario autenticado | Sí |
| GET | /api/protected/admin-only | Recurso solo ADMIN | Sí (ADMIN) |
| GET | /swagger-ui/index.html | Documentación Swagger | No |

### Billing Service (8082)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | /api/facturas | Crear factura | Sí |
| GET | /api/facturas | Listar facturas | Sí |
| GET | /api/facturas/{id} | Obtener por ID | Sí |
| PATCH | /api/facturas/{id}/estado | Cambiar estado | Sí |
| POST | /api/tarifas-base | Crear tarifa base | Sí (ADMIN) |
| GET | /api/tarifas-base | Listar tarifas | Sí |
| GET | /api/tarifas-base/{id} | Obtener tarifa | Sí |
| PUT | /api/tarifas-base/{id} | Actualizar tarifa | Sí (ADMIN) |

### Fleet Service (8083)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | /vehiculos | Crear vehículo | Sí (ADMIN) |
| GET | /vehiculos | Listar todos | Sí |
| GET | /vehiculos/disponibles | Sin asignar | Sí |
| PATCH | /vehiculos/{id}/estado | Cambiar estado | Sí |
| POST | /repartidores | Crear repartidor | Sí (ADMIN) |
| GET | /repartidores/disponibles | Listar disponibles | Sí |
| GET | /repartidores/zona/{zona} | Filtrar por zona | Sí |
| GET | /repartidores/{id}/metricas | Métricas individual | Sí |
| GET | /repartidores/top-performers | Top 10 mejores | Sí |
| POST | /repartidores/{id}/asignar-vehiculo | Asignar vehículo | Sí |
| GET | /estadisticas/flota | Estadísticas generales | Sí |

### Pedido Service (8084)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | /api/pedidos | Crear pedido | Sí |
| GET | /api/pedidos/{id} | Obtener por ID | Sí |
| GET | /api/pedidos | Listar todos | Sí |
| GET | /api/pedidos/cliente/{id} | Pedidos de cliente | Sí |
| PATCH | /api/pedidos/{id} | Actualizar parcial | Sí |
| PATCH | /api/pedidos/{id}/cancelar | Cancelar pedido | Sí |
| POST | /api/pedidos/reintento-asignacion | Reintentar asignación | Sí |
| DELETE | /api/pedidos/{id} | Eliminar pedido | Sí (ADMIN) |

### Tracking Service (8090)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | /api/tracking/track | Enviar ubicación GPS | Sí |

### GraphQL Service (8086)

| Tipo | Query/Mutation | Descripción |
|------|----------------|-------------|
| Mutation | actualizarEstadoRepartidor | Cambiar disponibilidad |
| Mutation | reasignarPedido | Reasignar manualmente |
| Mutation | actualizarDatosContacto | Actualizar perfil |
| Mutation | registrarIncidencia | Reportar problema |
| Query | pedidosPorZona | Filtrar por zona |
| Query | pedidosPorCiudadOrigen | Por ciudad origen |
| Query | pedidosPorCiudadDestino | Por ciudad destino |
| Query | estadisticasPorCiudad | Estadísticas agregadas |

---

## 🔐 Seguridad y Autenticación

### JWT (JSON Web Tokens)

**Estructura del token:**
```json
{
  "header": {
    "alg": "HS512",
    "typ": "JWT"
  },
  "payload": {
    "sub": "username",
    "userId": "123",
    "roles": ["ROLE_USER"],
    "iat": 1707570000,
    "exp": 1707573600
  }
}
```

**Flujo de autenticación:**
```
1. Cliente → POST /api/auth/login {username, password}
2. Auth Service valida credenciales con BCrypt
3. Si correcto → Genera accessToken (1h) + refreshToken (7d)
4. Cliente guarda tokens en localStorage/cookies
5. Cliente → Añade header: Authorization: Bearer <accessToken>
6. API Gateway valida token en cada request
7. Si expirado → Cliente usa refreshToken para obtener nuevo accessToken
```

**Configuración Spring Security:**
- CSRF deshabilitado (API REST stateless)
- CORS configurado para permitir orígenes específicos
- JwtAuthenticationFilter intercepta requests
- Roles verificados con @PreAuthorize

---

## 🧪 Testing y Validación

### Tests Unitarios
```bash
# Ejecutar tests de un servicio
cd pedido-service
./mvnw test

# Con cobertura de código
./mvnw clean test jacoco:report

# Reporte en: target/site/jacoco/index.html
```

### Tests de Integración
```bash
./mvnw verify
```

### Testing Manual con Postman

**Colecciones disponibles:**
- `Tracking-Service.postman_collection.json`
- Requests de ejemplo en cada README de servicio

**Flujo de testing típico:**
```
1. Registrar usuario → POST /api/auth/register
2. Login → POST /api/auth/login (guardar token)
3. Crear tarifa base → POST /api/tarifas-base
4. Crear vehículo → POST /vehiculos
5. Crear repartidor → POST /repartidores
6. Crear pedido → POST /api/pedidos
7. Verificar en RabbitMQ Management → eventos publicados
8. Verificar email → inbox del cliente
9. Consultar pedido → GET /api/pedidos/{id}
```

---

## 📈 Monitoreo y Observabilidad

### Spring Boot Actuator

Endpoints habilitados:
```
/actuator/health        - Estado del servicio
/actuator/info          - Información del servicio
/actuator/metrics       - Métricas de JVM
/actuator/env           - Variables de entorno
/actuator/loggers       - Configuración de logs
```

### Logs Estructurados

**Formato:**
```
2026-02-10 10:30:15.123 INFO [pedido-service,trace-id,span-id] 
  com.logiflow.pedido.service.PedidoServiceImpl : 
  Creando pedido para cliente: cliente123
```

**Niveles de log por entorno:**
- Desarrollo: DEBUG
- Producción: INFO
- Logging.level.com.logiflow: DEBUG

### RabbitMQ Management

```
URL: http://localhost:15672
User: guest / guest

Monitoreo:
- Exchanges activos
- Queues y mensajes pendientes
- Consumers conectados
- Tasa de mensajes/segundo
```

---

## 🛠️ Tecnologías Detalladas

### Backend (Microservicios Java)
- **Java 21** - LTS version con Virtual Threads
- **Spring Boot 4.0.0** - Framework principal
- **Spring Data JPA** - ORM con Hibernate
- **Spring Security 6** - Autenticación y autorización
- **Spring Cloud Gateway** - API Gateway reactivo
- **Spring Validation** - Validación de DTOs
- **SpringDoc OpenAPI** - Documentación automática
- **Lombok** - Reducción de boilerplate

### Bases de Datos
- **PostgreSQL 16** - Base de datos relacional
- **Flyway** - Migraciones de esquema (opcional)
- **HikariCP** - Connection pooling

### Mensajería
- **RabbitMQ 3.x** - Message broker AMQP
- **Spring AMQP** - Cliente Spring para RabbitMQ

### GraphQL Service
- **Node.js 18+** - Runtime JavaScript
- **TypeScript** - Superset tipado de JavaScript
- **Apollo Server** - Servidor GraphQL
- **Axios** - Cliente HTTP para REST

### Contenedorización
- **Docker 24.x** - Plataforma de contenedores
- **Docker Compose** - Orquestación multi-contenedor
- **Multi-stage builds** - Optimización de imágenes

### Build Tools
- **Maven 3.9+** - Gestión de dependencias Java
- **Maven Wrapper** - Versión embebida de Maven
- **npm** - Gestión de paquetes Node.js

### Observabilidad
- **SLF4J + Logback** - Logging estructurado
- **Actuator** - Métricas y health checks

---

## 🗄️ Configuración de Bases de Datos

### Tabla Resumen

| Servicio | BD | Puerto | Usuario | Password | Tablas Principales |
|----------|----|----|---------|----------|-------------------|
| Auth | jwt_demo | 5432 | admin | admin | users, roles |
| Billing | db_billing_users | 5433 | billing | qwerty123 | facturas, tarifas_base |
| Fleet | fleet_db | 5435 | fleet_user | fleet_password | vehiculos, repartidores |
| Pedido | pedidos_db | 5436 | pedido_user | pedido_pass | pedidos, direcciones, incidencias |
| Notifications | notifications_db | 5437 | notif_user | notif_pass | notifications, processed_messages |

### Estrategia de Persistencia

**Database per Service Pattern:**
- Cada microservicio tiene su propia BD independiente
- Desacoplamiento total entre servicios
- Escalabilidad horizontal por servicio
- Consistencia eventual mediante eventos

---

## 📝 Convenciones y Mejores Prácticas

### Nomenclatura de Código

**Packages:**
```
com.logiflow.[servicio].config
com.logiflow.[servicio].controller
com.logiflow.[servicio].dto
com.logiflow.[servicio].model
com.logiflow.[servicio].repository
com.logiflow.[servicio].service
com.logiflow.[servicio].exception
```

**DTOs:**
- Request: `CrearPedidoRequest`, `ActualizarEstadoRequest`
- Response: `PedidoResponse`, `FacturaResponse`
- Simple: `PedidoDTO`, `RepartidorDTO`

**Entities:**
```java
@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    // ...
}
```

**Services:**
```java
public interface PedidoService {
    PedidoResponse crearPedido(CrearPedidoRequest request);
}

@Service
public class PedidoServiceImpl implements PedidoService {
    // ...
}
```

**Controllers:**
```java
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService pedidoService;
    
    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody CrearPedidoRequest request) {
        // ...
    }
}
```

### Manejo de Excepciones

**Global Exception Handler:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
    }
}
```

---

## 🔄 Gestión de Versiones y Git

**Estrategia de branching:**
```
main (producción)
  └── develop (desarrollo)
       ├── feature/auth-jwt
       ├── feature/billing-strategy
       ├── feature/fleet-assignment
       └── hotfix/pedido-validation
```

**Commits semánticos:**
```
feat: Implementar sistema de reintento de asignación
fix: Corregir cálculo de tarifa urbana
docs: Actualizar README con ejemplos de GraphQL
refactor: Extraer lógica de validación a service
test: Añadir tests para PedidoService
```

**Versionado Semántico:**
- **Major** (2.x.x): Cambios incompatibles en API
- **Minor** (x.1.x): Nueva funcionalidad compatible
- **Patch** (x.x.1): Correcciones de bugs

---

## 📚 Documentación Adicional

- [DOCKER_SETUP.md](DOCKER_SETUP.md) - Guía detallada de Docker Compose
- [REINTENTO_ASIGNACION.md](REINTENTO_ASIGNACION.md) - Sistema de reintentos event-driven
- [README.md principal](../README.md) - Documentación completa del sistema
- READMEs individuales en cada carpeta de microservicio

---

## 🚧 Trabajo Futuro y Mejoras

### Fase 3 (Planificada)
- [ ] Kubernetes deployment (YAML en carpeta kubernets/)
- [ ] Circuit breaker con Resilience4j
- [ ] Distributed tracing con Spring Cloud Sleuth + Zipkin
- [ ] Cache distribuido con Redis
- [ ] API rate limiting por usuario
- [ ] WebSockets para tracking en tiempo real
- [ ] Saga pattern para transacciones distribuidas
- [ ] Event Sourcing para auditoría completa

---

## 👥 Equipo de Desarrollo

*[Añadir información del equipo aquí]*

---

**Última actualización:** 10 de Febrero de 2026  
**Versión del Sistema:** 2.0  
**Estado:** ✅ Producción Ready
