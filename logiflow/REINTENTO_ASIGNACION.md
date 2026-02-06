# Documentación: Reintento de Asignación Automática

## 📋 Descripción General

Sistema de reintento de asignación automática para pedidos que quedaron en estado `PENDIENTE` debido a falta de recursos (repartidores/vehículos) disponibles al momento de su creación.

### 🎯 Problema
Cuando se crea un pedido y no hay repartidores disponibles, el pedido queda en estado `PENDIENTE`. Si posteriormente se agregan nuevos repartidores o vehículos, no existe forma automática de reasignar estos pedidos pendientes.

### ✅ Solución
Endpoint REST que permite solicitar manualmente el reintento de asignación. El sistema utiliza **arquitectura 100% event-driven** vía RabbitMQ para mantener el desacoplamiento entre servicios.

---

## 🏗️ Arquitectura

### Componentes Involucrados

```
┌─────────────────┐      ┌──────────────┐      ┌─────────────────┐
│  PedidoService  │─────▶│   RabbitMQ   │◀─────│  FleetService   │
│   (Port 8084)   │      │  (Port 5672) │      │   (Port 8083)   │
└─────────────────┘      └──────────────┘      └─────────────────┘
        │                        │                       │
        │                        │                       │
   PostgreSQL              Topic Exchange          PostgreSQL
   pedido_db              pedidos.exchange         fleet_db
```

### Eventos RabbitMQ

1. **pedido.reintento.asignacion**
   - Exchange: `pedidos.exchange` (TopicExchange)
   - Queue: `fleet.pedido.reintento`
   - Routing Key: `pedido.reintento.asignacion`
   - Producer: PedidoService
   - Consumer: FleetService

2. **asignacion.completada**
   - Exchange: `fleet.exchange` (TopicExchange)
   - Queue: `pedido.asignacion.completada`
   - Routing Key: `asignacion.completada`
   - Producer: FleetService
   - Consumer: PedidoService

---

## 🔄 Flujo Completo

### Paso 1: Creación de Pedido sin Recursos

```http
POST http://localhost:8082/api/pedidos
Headers:
  X-User-Id: cliente123
  X-User-Roles: CLIENTE
  Content-Type: application/json

Body:
{
  "clienteId": "cliente123",
  "modalidadServicio": "URBANA_RAPIDA",
  "tipoEntrega": "EXPRESS",
  "prioridad": "ALTA",
  "peso": 5.0,
  "direccionOrigen": {
    "calle": "Av. Principal",
    "ciudad": "Quito",
    "provincia": "Pichincha",
    "codigoPostal": "170101"
  },
  "direccionDestino": {
    "calle": "Calle Secundaria",
    "ciudad": "Quito",
    "provincia": "Pichincha",
    "codigoPostal": "170102"
  }
}
```

**Resultado:**
- ✅ Pedido creado con ID: `550e8400-e29b-41d4-a716-446655440000`
- ✅ Estado: `PENDIENTE`
- ✅ Evento `pedido.creado` publicado a RabbitMQ
- ⚠️ FleetService intenta asignar pero **no encuentra repartidores disponibles**
- ⚠️ Pedido permanece en `PENDIENTE`

**Logs FleetService:**
```log
INFO EVENTO RECIBIDO: pedido.creado
INFO Pedido: 550e8400-e29b-41d4-a716-446655440000
WARN No hay repartidores disponibles con estado DISPONIBLE
INFO Liberando asignación para pedido 550e8400-e29b-41d4-a716-446655440000
```

---

### Paso 2: Creación de Recursos (Repartidor y Vehículo)

#### 2.1 Crear Vehículo

```http
POST http://localhost:8082/api/vehiculos
Headers:
  X-User-Id: admin
  X-User-Roles: ADMINISTRADOR_SISTEMA
  Content-Type: application/json

Body:
{
  "placa": "ABC-123",
  "marca": "Toyota",
  "modelo": "Hilux",
  "anio": 2023,
  "tipoVehiculo": "CAMIONETA",
  "ciudadBase": "Quito",
  "estado": "DISPONIBLE",
  "capacidadCarga": 1000.0
}
```

#### 2.2 Crear Repartidor

```http
POST http://localhost:8082/api/repartidores
Headers:
  X-User-Id: admin
  X-User-Roles: ADMINISTRADOR_SISTEMA
  Content-Type: application/json

Body:
{
  "nombre": "Juan Pérez",
  "cedula": "1234567890",
  "ciudadBase": "Quito",
  "estado": "DISPONIBLE",
  "licenciaConducir": "A1234567",
  "telefono": "0987654321"
}
```

**Resultado:**
- ✅ Repartidor creado con ID: `1`
- ✅ Vehículo creado con ID: `1`
- ✅ Ambos con estado `DISPONIBLE` en ciudad `Quito`
- ❗ **El pedido PENDIENTE NO se asigna automáticamente**

---

### Paso 3: Reintentar Asignación (Endpoint Principal)

```http
POST http://localhost:8082/api/pedidos/550e8400-e29b-41d4-a716-446655440000/reintentar-asignacion
Headers:
  X-User-Id: supervisor123
  X-User-Roles: SUPERVISOR
```

**Validaciones del Endpoint:**
1. ✅ Pedido existe
2. ✅ Pedido está en estado `PENDIENTE`
3. ✅ Usuario tiene rol autorizado (SUPERVISOR, GERENTE, ADMINISTRADOR_SISTEMA)

**Respuesta:**
```json
HTTP/2 202 Accepted
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "clienteId": "cliente123",
  "estado": "PENDIENTE",
  "modalidadServicio": "URBANA_RAPIDA",
  "tipoEntrega": "EXPRESS",
  "prioridad": "ALTA",
  ...
}
```

**Nota:** HTTP 202 (Accepted) indica que la solicitud fue aceptada y el evento fue publicado a RabbitMQ, pero el procesamiento es asíncrono.

---

### Paso 4: Procesamiento en PedidoService

#### 4.1 Validación y Construcción del Evento

**Clase:** `PedidoServiceImpl.reintentarAsignacionAutomatica()`

```java
// Validar que pedido existe
Pedido pedido = findPedidoOrThrow(pedidoId);

// Validar estado PENDIENTE
if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
    throw new IllegalStateException("El pedido debe estar en estado PENDIENTE");
}

// Construir evento
ReintentarAsignacionEvent evento = ReintentarAsignacionEvent.builder()
    .messageId(UUID.randomUUID().toString())
    .timestamp(LocalDateTime.now())
    .pedidoId(pedido.getId())
    .clienteId(pedido.getClienteId())
    .usuarioSolicitante("supervisor123")
    .modalidadServicio("URBANA_RAPIDA")
    .tipoEntrega("EXPRESS")
    .prioridad("ALTA")
    .peso(5.0)
    .ciudadOrigen("Quito")
    .ciudadDestino("Quito")
    .numeroReintento(1)
    .motivoReintento("SOLICITUD_MANUAL")
    .build();
```

#### 4.2 Publicación del Evento

**Clase:** `PedidoEventPublisher.publishReintentarAsignacionEvent()`

```
Exchange: pedidos.exchange
Routing Key: pedido.reintento.asignacion
Queue Destino: fleet.pedido.reintento
```

**Logs PedidoService:**
```log
INFO [REINTENTO-ASIGNACION] Iniciando reintento para pedido=550e8400-... | Usuario=supervisor123
INFO =====================================================
INFO [RABBIT-PRODUCER] Publicando evento REINTENTAR ASIGNACION
INFO MessageID     : a1b2c3d4-e5f6-7890-abcd-ef1234567890
INFO Pedido ID     : 550e8400-e29b-41d4-a716-446655440000
INFO Cliente       : cliente123
INFO Usuario       : supervisor123
INFO Tipo Entrega  : EXPRESS
INFO Modalidad     : URBANA_RAPIDA
INFO Prioridad     : ALTA
INFO Origen        : Quito
INFO Destino       : Quito
INFO Reintento #   : 1
INFO Motivo        : SOLICITUD_MANUAL
INFO Exchange      : pedidos.exchange
INFO RoutingKey    : pedido.reintento.asignacion
INFO [RABBIT-PRODUCER] Evento pedido.reintento.asignacion enviado EXITOSAMENTE
```

---

### Paso 5: Consumo del Evento en FleetService

#### 5.1 Recepción del Evento

**Clase:** `PedidoEventListener.handleReintentoAsignacion()`

**Queue:** `fleet.pedido.reintento`

**Logs FleetService:**
```log
INFO =============================================================
INFO === EVENTO RECIBIDO: pedido.reintento.asignacion ===
INFO =============================================================
INFO MessageID          : a1b2c3d4-e5f6-7890-abcd-ef1234567890
INFO Timestamp          : 2026-02-06T10:30:00
INFO Pedido ID          : 550e8400-e29b-41d4-a716-446655440000
INFO Cliente            : cliente123
INFO Usuario Solicitante: supervisor123
INFO Modalidad          : URBANA_RAPIDA
INFO Tipo Entrega       : EXPRESS
INFO Prioridad          : ALTA
INFO Peso               : 5.0 kg
INFO Origen             : Quito
INFO Destino            : Quito
INFO Reintento #        : 1
INFO Motivo             : SOLICITUD_MANUAL
```

#### 5.2 Proceso de Asignación

**Servicio:** `AsignacionService.asignarRepartidorYVehiculo()`

**Criterios de Selección:**
1. Repartidor con estado `DISPONIBLE`
2. Ciudad base coincide con origen del pedido
3. Vehículo con estado `DISPONIBLE`
4. Ciudad base del vehículo coincide con origen
5. Capacidad de carga suficiente para el peso del pedido

**Logs FleetService:**
```log
INFO [REINTENTO-ASIGNACION] Iniciando proceso de asignación automática para pedido: 550e8400-...
INFO Buscando repartidores disponibles en ciudad: Quito
INFO Repartidor encontrado: ID=1, Nombre=Juan Pérez
INFO Buscando vehículo disponible en ciudad: Quito
INFO Vehículo encontrado: ID=1, Placa=ABC-123, Capacidad=1000.0kg
INFO [REINTENTO-ASIGNACION] ✅ Asignación EXITOSA - Pedido: 550e8400-... | Repartidor: 1 | Vehiculo: 1
INFO Actualizando estado de repartidor 1 a EN_RUTA
INFO Actualizando estado de vehículo 1 a EN_RUTA
```

#### 5.3 Publicación de Asignación Completada

**Clase:** `FleetEventPublisher.publishAsignacionCompletada()`

**Evento:** `AsignacionCompletadaEvent`

```
Exchange: fleet.exchange
Routing Key: asignacion.completada
Queue Destino: pedido.asignacion.completada
```

**Contenido del Evento:**
```json
{
  "messageId": "f1e2d3c4-b5a6-7890-1234-567890abcdef",
  "timestamp": "2026-02-06T10:30:05",
  "pedidoId": "550e8400-e29b-41d4-a716-446655440000",
  "repartidorId": "1",
  "vehiculoId": "1",
  "repartidorNombre": "Juan Pérez",
  "vehiculoPlaca": "ABC-123",
  "estadoPedido": "ASIGNADO",
  "servicioOrigen": "FLEET_SERVICE",
  "motivoAsignacion": "REINTENTO_MANUAL"
}
```

**Logs FleetService:**
```log
INFO =====================================================
INFO [EVENT-PUBLISH] Publicando evento de asignación completada a RabbitMQ
INFO MessageID: f1e2d3c4-b5a6-7890-1234-567890abcdef
INFO Pedido ID: 550e8400-e29b-41d4-a716-446655440000
INFO Repartidor: 1 - Juan Pérez
INFO Vehículo: 1 - ABC-123
INFO Estado: ASIGNADO
INFO Motivo: REINTENTO_MANUAL
INFO [RABBIT-PRODUCER] Evento publicado exitosamente a exchange: fleet.exchange
INFO [REINTENTO-ASIGNACION] Evento asignacion.completada publicado exitosamente
```

---

### Paso 6: Actualización del Pedido en PedidoService

#### 6.1 Consumo del Evento de Asignación

**Clase:** `AsignacionEventListener.handleAsignacionCompletada()`

**Queue:** `pedido.asignacion.completada`

**Logs PedidoService:**
```log
INFO ===========================================
INFO EVENTO RECIBIDO: asignacion.completada
INFO ===========================================
INFO MessageID : f1e2d3c4-b5a6-7890-1234-567890abcdef
INFO Timestamp : 2026-02-06T10:30:05
INFO Pedido    : 550e8400-e29b-41d4-a716-446655440000
INFO Repartidor: 1 - Juan Pérez
INFO Vehículo  : 1 - ABC-123
INFO Estado    : ASIGNADO
INFO Servicio  : FLEET_SERVICE
INFO Motivo    : REINTENTO_MANUAL
```

#### 6.2 Actualización en Base de Datos

**Servicio:** `PedidoService.asignarRepartidorYVehiculo()`

**Operaciones:**
1. Buscar pedido por ID
2. Validar que existe
3. Actualizar campos:
   - `estado` → `ASIGNADO`
   - `repartidorId` → `"1"`
   - `vehiculoId` → `"1"`
   - `fechaAsignacion` → `2026-02-06T10:30:05`
4. Guardar en base de datos

**Logs PedidoService:**
```log
INFO Actualizando pedido 550e8400-... con asignación
INFO Repartidor asignado: 1
INFO Vehículo asignado: 1
INFO Estado actualizado: PENDIENTE → ASIGNADO
INFO [ASIGNACION-COMPLETADA] Pedido actualizado exitosamente
```

---

### Paso 7: Verificación Final

#### 7.1 Consultar Pedido Actualizado

```http
GET http://localhost:8082/api/pedidos/550e8400-e29b-41d4-a716-446655440000
Headers:
  X-User-Id: cliente123
  X-User-Roles: CLIENTE
```

**Respuesta:**
```json
HTTP/2 200 OK
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "clienteId": "cliente123",
  "estado": "ASIGNADO",
  "modalidadServicio": "URBANA_RAPIDA",
  "tipoEntrega": "EXPRESS",
  "prioridad": "ALTA",
  "peso": 5.0,
  "repartidorId": "1",
  "vehiculoId": "1",
  "fechaCreacion": "2026-02-06T09:00:00",
  "fechaAsignacion": "2026-02-06T10:30:05",
  "direccionOrigen": {
    "calle": "Av. Principal",
    "ciudad": "Quito",
    "provincia": "Pichincha",
    "codigoPostal": "170101"
  },
  "direccionDestino": {
    "calle": "Calle Secundaria",
    "ciudad": "Quito",
    "provincia": "Pichincha",
    "codigoPostal": "170102"
  }
}
```

✅ **Estado:** `ASIGNADO`  
✅ **RepartidorId:** `1`  
✅ **VehiculoId:** `1`  
✅ **FechaAsignacion:** Actualizada

---

## 🎭 Casos de Error

### Error 1: Pedido No Existe

```http
POST http://localhost:8082/api/pedidos/99999999-9999-9999-9999-999999999999/reintentar-asignacion
```

**Respuesta:**
```json
HTTP/2 404 Not Found

{
  "error": "EntityNotFoundException",
  "message": "Pedido no encontrado: 99999999-9999-9999-9999-999999999999",
  "timestamp": "2026-02-06T10:30:00"
}
```

---

### Error 2: Pedido No Está en Estado PENDIENTE

```http
POST http://localhost:8082/api/pedidos/550e8400-e29b-41d4-a716-446655440000/reintentar-asignacion
```

Si el pedido ya está `ASIGNADO`, `EN_CAMINO`, `ENTREGADO` o `CANCELADO`:

**Respuesta:**
```json
HTTP/2 400 Bad Request

{
  "error": "IllegalStateException",
  "message": "El pedido debe estar en estado PENDIENTE para reintentar asignación. Estado actual: ASIGNADO",
  "timestamp": "2026-02-06T10:35:00"
}
```

---

### Error 3: Sin Recursos Disponibles (Aún)

```http
POST http://localhost:8082/api/pedidos/550e8400-e29b-41d4-a716-446655440000/reintentar-asignacion
```

Si aún no hay repartidores/vehículos disponibles:

**Respuesta:**
```json
HTTP/2 202 Accepted

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "estado": "PENDIENTE",
  ...
}
```

**Nota:** El endpoint responde 202 (evento publicado exitosamente), pero el pedido permanece en `PENDIENTE`.

**Logs FleetService:**
```log
INFO [REINTENTO-ASIGNACION] Iniciando proceso de asignación automática
WARN [REINTENTO-ASIGNACION] ⚠️ No se pudo asignar - Pedido: 550e8400-... | Motivo: No hay repartidores disponibles
INFO [REINTENTO-ASIGNACION] El pedido 550e8400-... permanece en estado PENDIENTE
```

---

### Error 4: Usuario No Autorizado

```http
POST http://localhost:8082/api/pedidos/550e8400-e29b-41d4-a716-446655440000/reintentar-asignacion
Headers:
  X-User-Id: cliente123
  X-User-Roles: CLIENTE
```

**Respuesta:**
```json
HTTP/2 403 Forbidden

{
  "error": "AccessDeniedException",
  "message": "Acceso denegado. Requiere rol: SUPERVISOR, GERENTE o ADMINISTRADOR_SISTEMA",
  "timestamp": "2026-02-06T10:40:00"
}
```

**Roles Autorizados:**
- ✅ `SUPERVISOR`
- ✅ `GERENTE`
- ✅ `ADMINISTRADOR_SISTEMA`
- ❌ `CLIENTE` (no autorizado)
- ❌ `REPARTIDOR` (no autorizado)

---

## 📊 Configuración RabbitMQ

### PedidoService Configuration

**Archivo:** `pedido-service/src/main/resources/application.yaml`

```yaml
rabbitmq:
  exchange:
    pedidos: pedidos.exchange
    fleet: fleet.exchange
  queue:
    asignacion-completada: pedido.asignacion.completada
  routing-key:
    pedido-creado: pedido.creado
    pedido-estado: pedido.estado.actualizado
    reintento-asignacion: pedido.reintento.asignacion
```

### FleetService Configuration

**Archivo:** `fleet-service/src/main/resources/application.yaml`

```yaml
rabbitmq:
  exchange:
    pedidos: pedidos.exchange
    fleet: fleet.exchange
  queue:
    pedido-creado: fleet.pedido.creado
    pedido-estado: fleet.pedido.estado.actualizado
    pedido-reintento: fleet.pedido.reintento
  routing-key:
    pedido-creado: pedido.creado
    pedido-estado: pedido.estado.actualizado
    pedido-reintento: pedido.reintento.asignacion
    asignacion-completada: asignacion.completada
```

---

## 🔧 Comandos de Prueba (cURL)

### 1. Crear Pedido (Estado PENDIENTE)

```bash
curl -X POST http://localhost:8082/api/pedidos \
  -H "Content-Type: application/json" \
  -H "X-User-Id: cliente123" \
  -H "X-User-Roles: CLIENTE" \
  -d '{
    "clienteId": "cliente123",
    "modalidadServicio": "URBANA_RAPIDA",
    "tipoEntrega": "EXPRESS",
    "prioridad": "ALTA",
    "peso": 5.0,
    "direccionOrigen": {
      "calle": "Av. Principal",
      "ciudad": "Quito",
      "provincia": "Pichincha",
      "codigoPostal": "170101"
    },
    "direccionDestino": {
      "calle": "Calle Secundaria",
      "ciudad": "Quito",
      "provincia": "Pichincha",
      "codigoPostal": "170102"
    }
  }'
```

### 2. Crear Vehículo

```bash
curl -X POST http://localhost:8082/api/vehiculos \
  -H "Content-Type: application/json" \
  -H "X-User-Id: admin" \
  -H "X-User-Roles: ADMINISTRADOR_SISTEMA" \
  -d '{
    "placa": "ABC-123",
    "marca": "Toyota",
    "modelo": "Hilux",
    "anio": 2023,
    "tipoVehiculo": "CAMIONETA",
    "ciudadBase": "Quito",
    "estado": "DISPONIBLE",
    "capacidadCarga": 1000.0
  }'
```

### 3. Crear Repartidor

```bash
curl -X POST http://localhost:8082/api/repartidores \
  -H "Content-Type: application/json" \
  -H "X-User-Id: admin" \
  -H "X-User-Roles: ADMINISTRADOR_SISTEMA" \
  -d '{
    "nombre": "Juan Pérez",
    "cedula": "1234567890",
    "ciudadBase": "Quito",
    "estado": "DISPONIBLE",
    "licenciaConducir": "A1234567",
    "telefono": "0987654321"
  }'
```

### 4. Reintentar Asignación

```bash
curl -X POST http://localhost:8082/api/pedidos/{PEDIDO_ID}/reintentar-asignacion \
  -H "X-User-Id: supervisor123" \
  -H "X-User-Roles: SUPERVISOR"
```

### 5. Verificar Pedido Actualizado

```bash
curl -X GET http://localhost:8082/api/pedidos/{PEDIDO_ID} \
  -H "X-User-Id: cliente123" \
  -H "X-User-Roles: CLIENTE"
```

---

## 🔍 Monitoreo y Logs

### Ver Logs en Tiempo Real

```bash
# Todos los servicios
cd /path/to/EntregaExpress_P2/logiflow
docker compose logs -f

# Solo PedidoService
docker compose logs -f pedido-service

# Solo FleetService
docker compose logs -f fleet-service

# Filtrar por REINTENTO
docker compose logs -f | grep REINTENTO

# Filtrar por eventos específicos
docker compose logs -f | grep "pedido.reintento.asignacion"
docker compose logs -f | grep "asignacion.completada"
```

### Ver Estado de RabbitMQ

```bash
# Acceder a la interfaz web
http://localhost:15672
Usuario: admin
Password: admin

# Ver colas
- fleet.pedido.reintento
- pedido.asignacion.completada

# Ver exchanges
- pedidos.exchange (tipo: topic)
- fleet.exchange (tipo: topic)

# Ver bindings
- fleet.pedido.reintento → pedidos.exchange (key: pedido.reintento.asignacion)
- pedido.asignacion.completada → fleet.exchange (key: asignacion.completada)
```

---

## 🎯 Beneficios de la Arquitectura Event-Driven

### 1. **Desacoplamiento Total**
- PedidoService no conoce la URL de FleetService
- FleetService no conoce la URL de PedidoService
- Comunicación únicamente vía eventos RabbitMQ

### 2. **Sin Problemas de Autenticación**
- No hay llamadas REST inter-servicio
- No se requiere propagación de JWT tokens
- Cada servicio maneja su propia autenticación con el API Gateway

### 3. **Resiliencia**
- Si FleetService está caído, los eventos quedan en cola
- Cuando FleetService se recupera, procesa los eventos pendientes
- No se pierden solicitudes de reintento

### 4. **Escalabilidad Horizontal**
- Múltiples instancias de FleetService pueden consumir eventos
- RabbitMQ distribuye la carga automáticamente
- No hay punto único de falla

### 5. **Auditabilidad**
- Cada evento tiene `messageId` único
- Timestamps en cada paso del proceso
- Logs correlacionables entre servicios
- Historial completo en RabbitMQ (opcional con plugins)

### 6. **Asincronía**
- El endpoint responde inmediatamente (HTTP 202)
- El procesamiento ocurre en background
- Mejor experiencia de usuario (no timeout)

---

## 📈 Métricas y KPIs

### Métricas Sugeridas

1. **Tasa de Éxito de Reintentos**
   ```
   (Reintentos Exitosos / Total Reintentos) * 100
   ```

2. **Tiempo Promedio de Reintento**
   ```
   Tiempo entre publicación de evento y asignación completada
   ```

3. **Pedidos Pendientes por Ciudad**
   ```sql
   SELECT ciudad_origen, COUNT(*) 
   FROM pedidos 
   WHERE estado = 'PENDIENTE'
   GROUP BY ciudad_origen;
   ```

4. **Recursos Disponibles vs Demanda**
   ```sql
   SELECT 
     r.ciudad_base,
     COUNT(r.id) as repartidores_disponibles,
     COUNT(p.id) as pedidos_pendientes
   FROM repartidores r
   LEFT JOIN pedidos p ON p.ciudad_origen = r.ciudad_base AND p.estado = 'PENDIENTE'
   WHERE r.estado = 'DISPONIBLE'
   GROUP BY r.ciudad_base;
   ```

---

## 🚀 Casos de Uso Avanzados

### Caso 1: Reintento Automático por Batch Job

```java
@Scheduled(fixedDelay = 300000) // Cada 5 minutos
public void reintentarPedidosPendientes() {
    List<Pedido> pedidosPendientes = pedidoRepository
        .findByEstadoAndFechaCreacionBefore(
            EstadoPedido.PENDIENTE, 
            LocalDateTime.now().minusHours(1)
        );
    
    for (Pedido pedido : pedidosPendientes) {
        pedidoService.reintentarAsignacionAutomatica(
            pedido.getId(), 
            "BATCH_JOB"
        );
    }
}
```

### Caso 2: Notificación al Cliente

Extender el listener de `asignacion.completada` para enviar notificación:

```java
@RabbitListener(queues = "pedido.asignacion.completada")
public void handleAsignacionCompletada(AsignacionCompletadaEvent event) {
    // Actualizar pedido
    pedidoService.asignarRepartidorYVehiculo(...);
    
    // Enviar notificación
    notificationService.enviarNotificacion(
        event.getClienteId(),
        "Tu pedido ha sido asignado al repartidor: " + event.getRepartidorNombre()
    );
}
```

### Caso 3: Límite de Reintentos

Modificar el evento para incluir contador:

```java
if (event.getNumeroReintento() > 3) {
    log.warn("Pedido {} alcanzó límite de reintentos ({})", 
        event.getPedidoId(), event.getNumeroReintento());
    // Marcar pedido para revisión manual
    pedidoService.marcarParaRevisionManual(event.getPedidoId());
    return;
}
```

---

## 📝 Checklist de Implementación

- [x] **PedidoService**
  - [x] Crear ReintentarAsignacionEvent.java
  - [x] Actualizar PedidoEventPublisher con publishReintentarAsignacionEvent()
  - [x] Crear método reintentarAsignacionAutomatica() en PedidoService
  - [x] Crear endpoint POST /api/pedidos/{id}/reintentar-asignacion
  - [x] Configurar routing key pedido.reintento.asignacion

- [x] **FleetService**
  - [x] Crear ReintentarAsignacionEvent.java (mirror)
  - [x] Configurar cola fleet.pedido.reintento
  - [x] Crear binding pedido.reintento.asignacion → fleet.pedido.reintento
  - [x] Implementar handleReintentoAsignacion() en PedidoEventListener
  - [x] Reutilizar AsignacionService.asignarRepartidorYVehiculo()

- [x] **RabbitMQ**
  - [x] Exchange pedidos.exchange (ya existe)
  - [x] Queue fleet.pedido.reintento (nueva)
  - [x] Binding con routing key pedido.reintento.asignacion

- [x] **Testing**
  - [x] Compilación exitosa de ambos servicios
  - [x] Despliegue con Docker Compose
  - [x] Verificación de logs

---

## 🔗 Referencias

### Documentos Relacionados
- `README.md` - Documentación general del proyecto
- `docker-compose.yml` - Configuración de infraestructura
- `README_Postman.md` - Colección de endpoints para pruebas

### Endpoints Relacionados
- `POST /api/pedidos` - Crear pedido
- `GET /api/pedidos/{id}` - Consultar pedido
- `POST /api/repartidores` - Crear repartidor
- `POST /api/vehiculos` - Crear vehículo
- `POST /api/pedidos/{id}/reintentar-asignacion` - **Reintento de asignación**

### Tecnologías Utilizadas
- **Spring Boot 3.2.0** / **4.0.0** - Framework principal
- **Spring AMQP** - Integración con RabbitMQ
- **RabbitMQ 3.x** - Message broker
- **PostgreSQL 16** - Base de datos
- **Docker & Docker Compose** - Contenedorización

---

## 📞 Soporte

Para dudas o problemas con el sistema de reintento de asignación:

1. Verificar logs de ambos servicios
2. Verificar estado de colas en RabbitMQ Management (http://localhost:15672)
3. Validar que los servicios estén corriendo: `docker compose ps`
4. Reiniciar servicios si es necesario: `docker compose restart pedido-service fleet-service`

---

**Última actualización:** 6 de febrero de 2026  
**Versión:** 1.0.0  
**Autor:** LogiFlow Development Team
