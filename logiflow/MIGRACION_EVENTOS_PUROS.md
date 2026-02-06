# Migración a Arquitectura 100% Basada en Eventos

## Problema Resuelto

### ❌ Flujo Anterior (Híbrido con REST)
```
PedidoService → RabbitMQ ("pedido.creado")
                    ↓
                FleetService (escucha)
                    ↓ (asigna recursos)
                REST PATCH ← FleetService llama a PedidoService
                    ❌ ERROR 401: Token requerido
                    ↓
                PedidoService rechaza (GatewayAuthFilter)
```

**Problema**: FleetService no tiene token JWT para autenticarse con PedidoService.

---

## ✅ Nueva Solución: Eventos Puros

### Flujo Completo con Solo RabbitMQ

```
┌──────────────┐
│ 1. Cliente   │
│ POST /pedidos│
└──────┬───────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ PedidoService                           │
│  • Guarda pedido en DB (PENDIENTE)     │
│  • Publica: "pedido.creado"            │
└─────────────┬───────────────────────────┘
              │
              ▼
     ┌────────────────┐
     │  RabbitMQ      │
     │ pedidos.       │
     │ exchange       │
     └───────┬────────┘
             │ routing key: pedido.creado
             ▼
┌──────────────────────────────────────────┐
│ FleetService                             │
│  • Escucha: queue "fleet.pedido.creado" │
│  • Busca repartidor disponible          │
│  • Busca vehículo compatible            │
│  • Crea asignación en DB                │
│  • Publica: "asignacion.completada" ✨  │
└─────────────┬────────────────────────────┘
              │
              ▼
     ┌────────────────┐
     │  RabbitMQ      │
     │ fleet.         │
     │ exchange       │
     └───────┬────────┘
             │ routing key: asignacion.completada
             ▼
┌──────────────────────────────────────────┐
│ PedidoService                            │
│  • Escucha: "pedido.asignacion.         │
│    completada"                           │
│  • Actualiza pedido (ASIGNADO)          │
│  • Asigna repartidorId y vehiculoId     │
│  • Publica: "pedido.estado.actualizado" │
└──────────────────────────────────────────┘
             │
             ▼
     ┌────────────────┐
     │  RabbitMQ      │
     │ pedidos.       │
     │ exchange       │
     └───────┬────────┘
             │ routing key: pedido.estado.actualizado
             ▼
┌──────────────────────────────────────────┐
│ BillingService & FleetService            │
│  • Ambos escuchan cambio de estado      │
│  • Realizan acciones según nuevo estado │
└──────────────────────────────────────────┘
```

---

## Cambios Implementados

### 1. FleetService

#### Nuevo Evento Creado
```java
// AsignacionCompletadaEvent.java
{
  "messageId": "UUID",
  "timestamp": "2026-02-06T09:04:41",
  "pedidoId": "090a3d36-...",
  "repartidorId": "676ac137-...",
  "vehiculoId": "70791fd6-...",
  "repartidorNombre": "Carlos Mendoza",
  "vehiculoPlaca": "PBC-4567",
  "estadoPedido": "ASIGNADO",
  "servicioOrigen": "FLEET_SERVICE",
  "motivoAsignacion": "ASIGNACION_AUTOMATICA"
}
```

#### Publisher Actualizado
```java
// FleetEventPublisher.java
public void publishAsignacionCompletada(AsignacionCompletadaEvent event) {
    rabbitTemplate.convertAndSend(
        fleetExchange, 
        "asignacion.completada", 
        event
    );
}
```

#### Listener Modificado
```java
// PedidoEventListener.java - ANTES
restTemplate.patchForObject(url, null, Object.class); // ❌ Eliminado

// PedidoEventListener.java - AHORA
fleetEventPublisher.publishAsignacionCompletada(asignacionEvent); // ✅
```

#### Configuración application.yaml
```yaml
rabbitmq:
  routing-key:
    asignacion-completada: asignacion.completada
```

---

### 2. PedidoService

#### Nuevo Evento Recibido
```java
// AsignacionCompletadaEvent.java (idéntico al de FleetService)
```

#### Nueva Cola y Binding
```java
// RabbitMQConfig.java
@Bean
public TopicExchange fleetExchange() {
    return new TopicExchange("fleet.exchange");
}

@Bean
public Queue asignacionCompletadaQueue() {
    return new Queue("pedido.asignacion.completada", true);
}

@Bean
public Binding bindingAsignacionCompletada() {
    return BindingBuilder
        .bind(asignacionCompletadaQueue())
        .to(fleetExchange())
        .with("asignacion.completada");
}
```

#### Nuevo Listener
```java
// AsignacionEventListener.java
@RabbitListener(queues = "pedido.asignacion.completada")
public void handleAsignacionCompletada(AsignacionCompletadaEvent event) {
    UUID pedidoId = UUID.fromString(event.getPedidoId());
    UUID repartidorId = UUID.fromString(event.getRepartidorId());
    UUID vehiculoId = UUID.fromString(event.getVehiculoId());
    
    pedidoService.asignarRepartidorYVehiculo(
        pedidoId.toString(),
        repartidorId.toString(),
        vehiculoId.toString()
    );
}
```

#### Configuración application.yaml
```yaml
rabbitmq:
  exchange:
    fleet: fleet.exchange
  queue:
    asignacion-completada: pedido.asignacion.completada
  routing-key:
    asignacion-completada: asignacion.completada
```

---

## Ventajas de esta Arquitectura

### ✅ Sin Problemas de Autenticación
- No hay llamadas REST entre servicios
- No se requieren tokens JWT
- No hay endpoints expuestos para comunicación interna

### ✅ Desacoplamiento Total
- FleetService no conoce la URL de PedidoService
- Servicios no dependen de que el otro esté disponible en ese momento
- Cambios en un servicio no afectan al otro

### ✅ Resiliencia
- Si PedidoService está caído, los mensajes quedan en la cola
- Cuando PedidoService vuelve, procesa todos los mensajes pendientes
- No se pierden asignaciones

### ✅ Escalabilidad
- Puedes tener múltiples instancias de PedidoService escuchando la misma cola
- RabbitMQ distribuye los mensajes entre instancias
- Cada mensaje se procesa exactamente una vez

### ✅ Auditoría
- Todos los eventos quedan registrados en logs
- Puedes ver el flujo completo: creación → asignación → actualización
- Trazabilidad completa con `messageId`

### ✅ Consistencia Arquitectónica
- Todo el flujo usa el mismo patrón (eventos)
- Más fácil de entender y mantener
- Coherente con el resto del sistema

---

## Exchanges y Routing Keys

### pedidos.exchange (PedidoService publica)
```
routing key: pedido.creado
  ↓
  • Queue: pedido.creado (BillingService)
  • Queue: fleet.pedido.creado (FleetService)

routing key: pedido.estado.actualizado
  ↓
  • Queue: pedido.estado (BillingService)
  • Queue: fleet.pedido.estado (FleetService)
```

### fleet.exchange (FleetService publica)
```
routing key: asignacion.completada
  ↓
  • Queue: pedido.asignacion.completada (PedidoService)

routing key: repartidor.ubicacion.actualizada
  ↓
  • Queue: fleet.tracking.ubicacion (TrackingService - futuro)
```

---

## Logs de Ejemplo

### FleetService (Publicador)
```
INFO === EVENTO RECIBIDO: pedido.creado ===
INFO Pedido: 090a3d36-... | Cliente: CLI-001
INFO [ASIGNACION-AUTO] Iniciando asignación automática
INFO Asignación exitosa - Repartidor: Carlos Mendoza
INFO [EVENT-PUBLISH] Publicando evento de asignación completada a RabbitMQ
INFO === PUBLICANDO EVENTO: asignacion.completada ===
INFO MessageID: d7bed054-... | Timestamp: 2026-02-06T09:04:41
INFO Pedido: 090a3d36-... | Repartidor: Carlos Mendoza | Vehículo: PBC-4567
INFO [RABBIT-PRODUCER] Evento publicado en exchange: fleet.exchange
INFO [CONFIRMACION] Evento de asignación publicado exitosamente
```

### PedidoService (Consumidor)
```
INFO === EVENTO RECIBIDO: asignacion.completada ===
INFO MessageID: d7bed054-... | Timestamp: 2026-02-06T09:04:41
INFO Pedido: 090a3d36-... | Estado: ASIGNADO
INFO Repartidor: Carlos Mendoza (676ac137-...)
INFO Vehículo: PBC-4567 (70791fd6-...)
INFO Origen: FLEET_SERVICE | Motivo: ASIGNACION_AUTOMATICA
INFO [RABBIT-CONSUMER] Procesando asignación para pedido: 090a3d36-...
INFO [CONFIRMACION] Asignación procesada exitosamente - Pedido actualizado a ASIGNADO
INFO === EVENTO PROCESADO EXITOSAMENTE ===
```

---

## Verificación en RabbitMQ Management

### Exchanges Creados
1. `pedidos.exchange` (tipo: topic)
   - Bindings: pedido.creado, pedido.estado.actualizado
   
2. `fleet.exchange` (tipo: topic)
   - Bindings: asignacion.completada, repartidor.ubicacion.actualizada

### Queues Creadas
1. `pedido.asignacion.completada` (PedidoService escucha)
   - Binding: fleet.exchange → asignacion.completada
   
2. `fleet.pedido.creado` (FleetService escucha)
   - Binding: pedidos.exchange → pedido.creado

### Ver en http://localhost:15672
```
Usuario: admin
Password: admin

Ir a: Exchanges → fleet.exchange → Bindings
Verificar: asignacion.completada → pedido.asignacion.completada
```

---

## Comparación de Enfoques

| Aspecto | REST Híbrido ❌ | Eventos Puros ✅ |
|---------|----------------|-----------------|
| **Autenticación** | Requiere JWT | No requiere |
| **Acoplamiento** | Alto (URLs hardcodeadas) | Bajo (solo eventos) |
| **Resiliencia** | Falla si servicio caído | Mensajes en cola |
| **Escalabilidad** | Limitada | Alta (load balancing) |
| **Debugging** | Complejo (401, 503, timeouts) | Simple (logs de eventos) |
| **Arquitectura** | Inconsistente (REST + eventos) | Consistente (solo eventos) |
| **Mantenimiento** | Difícil | Fácil |

---

## Testing

### 1. Crear un pedido
```bash
POST http://localhost:8082/api/pedidos
Authorization: Bearer <TOKEN>
Content-Type: application/json

{
  "clienteId": "CLI-001",
  "tipoEntrega": "EXPRESS",
  "modalidadServicio": "NACIONAL",
  "prioridad": "ALTA",
  "peso": 3.2,
  "ciudadOrigen": "Quito",
  "ciudadDestino": "Guayaquil",
  "direccionOrigen": "Av. 10 de Agosto",
  "direccionDestino": "Av. 9 de Octubre"
}
```

### 2. Ver logs de FleetService
```bash
docker compose logs -f fleet-service | grep -E "EVENTO|ASIGNACION|EVENT-PUBLISH"
```

Esperado:
```
INFO === EVENTO RECIBIDO: pedido.creado ===
INFO [ASIGNACION-AUTO] Iniciando asignación automática
INFO [EVENT-PUBLISH] Publicando evento de asignación completada
INFO === PUBLICANDO EVENTO: asignacion.completada ===
INFO [RABBIT-PRODUCER] Evento publicado en exchange: fleet.exchange
```

### 3. Ver logs de PedidoService
```bash
docker compose logs -f pedido-service | grep -E "EVENTO|ASIGNACION|CONFIRMACION"
```

Esperado:
```
INFO === EVENTO RECIBIDO: asignacion.completada ===
INFO [RABBIT-CONSUMER] Procesando asignación para pedido
INFO [CONFIRMACION] Asignación procesada exitosamente
INFO === EVENTO PROCESADO EXITOSAMENTE ===
```

### 4. Verificar en RabbitMQ Management
```
http://localhost:15672
- Queues → pedido.asignacion.completada
- Ver "Message rates" debe mostrar tráfico
- "Get messages" para ver mensajes pendientes
```

---

## Próximos Pasos

### 1. Eliminar código REST obsoleto (Opcional)
```java
// En FleetService - Ya no se usa
private final RestTemplate restTemplate; // ELIMINAR
@Value("${services.pedido.url}") // ELIMINAR
```

### 2. Agregar más eventos (Futuro)
```
- pedido.cancelado → FleetService libera recursos
- pedido.entregado → FleetService marca repartidor disponible
- repartidor.ubicacion → TrackingService actualiza mapa en tiempo real
```

### 3. Implementar Dead Letter Queues
```java
@Bean
public Queue asignacionCompletadaDLQ() {
    return new Queue("pedido.asignacion.completada.dlq", true);
}
```

---

## Conclusión

✅ **Problema resuelto**: No más errores 401  
✅ **Arquitectura mejorada**: 100% basada en eventos  
✅ **Más resiliente**: Mensajes en cola si servicio caído  
✅ **Más escalable**: Múltiples instancias procesando eventos  
✅ **Más fácil de mantener**: Un solo patrón de comunicación
