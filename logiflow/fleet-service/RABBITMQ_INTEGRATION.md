# Fleet Service - Integración con RabbitMQ

## 📋 Descripción General

FleetService implementa mensajería asíncrona mediante RabbitMQ para comunicación event-driven con otros microservicios del sistema LogiFlow.

### Rol en la Arquitectura

- **CONSUMIDOR**: Recibe eventos del ciclo de vida de pedidos desde PedidoService
- **PRODUCTOR**: Publica eventos sobre disponibilidad y estado de recursos logísticos

---

## 🔄 Eventos Consumidos (de PedidoService)

FleetService escucha los siguientes eventos del exchange `pedidos.exchange`:

### 1. `pedido.creado`
**Queue**: `fleet.pedido.creado`  
**Routing Key**: `pedido.creado`

```json
{
  "pedidoId": "uuid",
  "clienteId": "uuid",
  "peso": 25.5,
  "origen": "Dirección origen",
  "destino": "Dirección destino",
  "prioridad": "ALTA",
  "fechaCreacion": "2026-02-06T10:30:00"
}
```

**Acción**: Inicia proceso de asignación de vehículo y repartidor según peso y zona.

---

### 2. `pedido.estado.actualizado`
**Queue**: `fleet.pedido.estado.actualizado`  
**Routing Key**: `pedido.estado.actualizado`

```json
{
  "pedidoId": "uuid",
  "estadoAnterior": "PENDIENTE",
  "estadoNuevo": "EN_CAMINO",
  "repartidorId": "uuid",
  "vehiculoId": "uuid",
  "fechaActualizacion": "2026-02-06T11:00:00",
  "motivo": "Asignación completada"
}
```

**Acción**: Confirma asignación de recursos o libera recursos según cambio de estado.

---

### 3. `pedido.cancelado`
**Queue**: `fleet.pedido.cancelado`  
**Routing Key**: `pedido.cancelado`

```json
{
  "pedidoId": "uuid",
  "repartidorId": "uuid",
  "vehiculoId": "uuid",
  "motivo": "Cliente canceló orden",
  "fechaCancelacion": "2026-02-06T11:15:00"
}
```

**Acción**: Libera repartidor y vehículo asignados, cambiando su estado a DISPONIBLE.

---

## 📤 Eventos Publicados (por FleetService)

FleetService publica eventos al exchange `fleet.exchange`:

### 1. `vehiculo.estado.actualizado`
**Exchange**: `fleet.exchange`  
**Routing Key**: `vehiculo.estado.actualizado`

```json
{
  "vehiculoId": "uuid",
  "placa": "ABC-123",
  "tipoVehiculo": "Motorizado",
  "estadoAnterior": "ACTIVO",
  "estadoNuevo": "EN_RUTA",
  "disponible": false,
  "fechaActualizacion": "2026-02-06T11:30:00"
}
```

**Consumidores potenciales**: PedidoService, TrackingService

---

### 2. `repartidor.ubicacion.actualizada`
**Exchange**: `fleet.exchange`  
**Routing Key**: `repartidor.ubicacion.actualizada`

```json
{
  "repartidorId": "uuid",
  "nombreCompleto": "Juan Pérez",
  "latitud": -0.1807,
  "longitud": -78.4678,
  "zona": "NORTE",
  "estado": "EN_RUTA",
  "fechaActualizacion": "2026-02-06T11:35:00"
}
```

**Consumidores potenciales**: PedidoService (tracking), TrackingService, NotificationService

---

## 🏗️ Arquitectura de Implementación

### Estructura de Paquetes

```
com.logiflow.fleetservice/
├── config/
│   └── RabbitMQConfig.java          # Configuración de exchanges, queues y bindings
├── event/
│   ├── PedidoCreadoEvent.java
│   ├── PedidoEstadoActualizadoEvent.java
│   ├── PedidoCanceladoEvent.java
│   ├── VehiculoEstadoActualizadoEvent.java
│   └── RepartidorUbicacionActualizadaEvent.java
├── service/
│   ├── messaging/
│   │   ├── FleetEventPublisher.java    # Publica eventos de fleet
│   │   └── PedidoEventListener.java    # Escucha eventos de pedidos
│   ├── VehiculoServiceImpl.java        # Publica eventos al cambiar estado
│   └── RepartidorServiceImpl.java      # Publica eventos al actualizar ubicación
```

---

## 🔧 Configuración

### application.yaml (Local)

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin
    listener:
      simple:
        acknowledge-mode: auto
        prefetch: 1
        retry:
          enabled: true
          initial-interval: 3000
          max-attempts: 3
          multiplier: 2

rabbitmq:
  exchange:
    pedidos: pedidos.exchange
    fleet: fleet.exchange
  queue:
    pedido-creado: fleet.pedido.creado
    pedido-estado: fleet.pedido.estado.actualizado
    pedido-cancelado: fleet.pedido.cancelado
  routing-key:
    pedido-creado: pedido.creado
    pedido-estado: pedido.estado.actualizado
    pedido-cancelado: pedido.cancelado
    vehiculo-estado: vehiculo.estado.actualizado
    repartidor-ubicacion: repartidor.ubicacion.actualizada
```

### application-docker.yaml (Docker)

```yaml
spring:
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: admin
    password: admin
```

---

## 🐳 Docker Compose

RabbitMQ se ejecuta como contenedor:

```yaml
rabbitmq:
  image: rabbitmq:3.13-management-alpine
  container_name: logiflow-rabbitmq
  environment:
    RABBITMQ_DEFAULT_USER: admin
    RABBITMQ_DEFAULT_PASS: admin
  ports:
    - "5672:5672"   # AMQP protocol
    - "15672:15672" # Management UI
  volumes:
    - rabbitmq_data:/var/lib/rabbitmq
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "ping"]
    interval: 30s
    timeout: 10s
    retries: 5
```

---

## 🚀 Uso

### Publicar Evento de Cambio de Estado de Vehículo

```java
@Autowired
private FleetEventPublisher eventPublisher;

public void cambiarEstadoVehiculo(UUID vehiculoId, EstadoVehiculo nuevoEstado) {
    VehiculoEntrega vehiculo = buscarVehiculo(vehiculoId);
    EstadoVehiculo estadoAnterior = vehiculo.getEstado();
    
    vehiculo.setEstado(nuevoEstado);
    vehiculoRepository.save(vehiculo);
    
    // Publicar evento
    VehiculoEstadoActualizadoEvent event = VehiculoEstadoActualizadoEvent.builder()
        .vehiculoId(vehiculo.getId().toString())
        .placa(vehiculo.getPlaca())
        .estadoAnterior(estadoAnterior.name())
        .estadoNuevo(nuevoEstado.name())
        .disponible(nuevoEstado == EstadoVehiculo.ACTIVO)
        .fechaActualizacion(LocalDateTime.now())
        .build();
    
    eventPublisher.publishVehiculoEstadoActualizado(event);
}
```

### Publicar Evento de Actualización de Ubicación

```java
@PostMapping("/{id}/coordenadas")
public ResponseEntity<Void> actualizarCoordenadas(
    @PathVariable UUID id,
    @Valid @RequestBody CoordenadasUpdateRequest request) {
    
    repartidorService.actualizarCoordenadas(id, request.getLatitud(), request.getLongitud());
    // El servicio automáticamente publica el evento RepartidorUbicacionActualizadaEvent
    
    return ResponseEntity.ok().build();
}
```

---

## 🧪 Verificación

### 1. Verificar RabbitMQ UI
Accede a http://localhost:15672 (admin/admin)

### 2. Verificar Exchanges
```bash
curl -u admin:admin http://localhost:15672/api/exchanges | jq '.[] | select(.name | contains("fleet") or contains("pedidos"))'
```

### 3. Verificar Queues
```bash
curl -u admin:admin http://localhost:15672/api/queues | jq '.[] | select(.name | contains("fleet"))'
```

### 4. Verificar Bindings
```bash
curl -u admin:admin http://localhost:15672/api/bindings | jq '.[] | select(.source | contains("pedidos"))'
```

---

## 📊 Monitoring

### Logs de Eventos

Los listeners registran automáticamente información sobre eventos recibidos:

```
INFO: Evento recibido: Pedido creado - ID: abc-123, Peso: 25.5kg, Origen: Quito, Destino: Cuenca
INFO: Evento recibido: Estado de pedido actualizado - ID: abc-123, Estado anterior: PENDIENTE, Estado nuevo: EN_CAMINO
INFO: Publicando evento: Vehículo xyz-456 cambió estado de ACTIVO a EN_RUTA
INFO: Publicando evento: Ubicación de repartidor rep-789 actualizada a [-0.1807, -78.4678]
```

---

## 🔐 Seguridad

- **Usuario RabbitMQ**: admin/admin (cambiar en producción)
- **Exchanges**: Tipo `topic` para routing flexible
- **Queues**: Durables (persistent=true)
- **Acknowledge Mode**: Auto (cambiar a manual en producción para mayor control)

---

## 📦 Dependencias Agregadas

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

---

## 🎯 Próximos Pasos (TODOs en código)

1. **Implementar lógica de negocio en listeners**:
   - Asignación automática de vehículo según peso
   - Búsqueda de repartidor disponible en zona
   - Liberación de recursos al cancelar pedido

2. **Agregar Dead Letter Queue (DLQ)**:
   - Para eventos que fallen después de reintentos
   - Implementar estrategia de recuperación

3. **Mejorar manejo de errores**:
   - Logging detallado de excepciones
   - Notificaciones de fallos críticos

4. **Agregar métricas**:
   - Eventos procesados/rechazados
   - Tiempo de procesamiento
   - Tamaño de queues

---

## ✅ Estado de Implementación

- ✅ Configuración de RabbitMQ
- ✅ Definición de exchanges y queues
- ✅ Bindings configurados correctamente
- ✅ Event DTOs creados
- ✅ FleetEventPublisher implementado
- ✅ PedidoEventListener implementado
- ✅ Integración en VehiculoServiceImpl
- ✅ Integración en RepartidorServiceImpl
- ✅ Docker Compose actualizado
- ✅ Compilación exitosa
- ✅ Conexión verificada
- ⏳ Lógica de negocio completa (parcial - TODOs en listeners)

---

## 📞 Contacto

Para dudas sobre eventos de dominio o integración con FleetService, consultar documentación de arquitectura del proyecto LogiFlow.
