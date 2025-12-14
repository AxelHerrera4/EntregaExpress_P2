# Integración Pedido-Service con Fleet-Service

## Descripción General

Este documento describe la integración entre el **Pedido Service** y el **Fleet Service** para la asignación automática de repartidores y vehículos a los pedidos.

## Arquitectura de Comunicación

```
┌─────────────────┐         ┌─────────────────┐
│  Pedido Service │────────▶│  Fleet Service  │
│   (puerto 8084) │         │   (puerto 8083) │
└─────────────────┘         └─────────────────┘
        │                            │
        ▼                            ▼
  ┌──────────┐               ┌──────────┐
  │PostgreSQL│               │PostgreSQL│
  │  :5433   │               │  :5432   │
  └──────────┘               └──────────┘
```

## Flujo de Asignación de Pedidos

### 1. Creación de Pedido con Asignación Automática

Cuando se crea un nuevo pedido, el flujo es el siguiente:

1. **Cliente → Pedido Service**: POST `/api/pedidos`
2. **Pedido Service**: Guarda el pedido en la base de datos
3. **Pedido Service → Billing Service**: Solicita creación de factura
4. **Pedido Service → Fleet Service**: Solicita asignación de repartidor
5. **Fleet Service**: Busca el mejor repartidor disponible
6. **Fleet Service → Pedido Service**: Devuelve repartidorId y vehiculoId
7. **Pedido Service**: Actualiza el pedido con la asignación
8. **Pedido Service → Cliente**: Retorna pedido completo con factura y asignación

### 2. Endpoint de Asignación en Fleet Service

**URL**: `POST http://localhost:8083/api/asignaciones`

**Request Body**:
```json
{
  "pedidoId": "550e8400-e29b-41d4-a716-446655440000",
  "modalidadServicio": "URBANA_RAPIDA",
  "tipoEntrega": "EXPRESS",
  "prioridad": "ALTA",
  "ciudadOrigen": "Quito",
  "ciudadDestino": "Guayaquil",
  "peso": 2.3
}
```

**Response (Éxito)**:
```json
{
  "pedidoId": "550e8400-e29b-41d4-a716-446655440000",
  "repartidorId": "1",
  "vehiculoId": "5",
  "repartidorNombre": "Juan Pérez",
  "vehiculoPlaca": "ABC-123",
  "estado": "ASIGNADO",
  "mensaje": "Repartidor y vehículo asignados exitosamente"
}
```

**Response (Sin Repartidores)**:
```json
{
  "pedidoId": "550e8400-e29b-41d4-a716-446655440000",
  "estado": "RECHAZADO",
  "mensaje": "No hay repartidores disponibles en este momento"
}
```

### 3. Algoritmo de Asignación

El Fleet Service selecciona el mejor repartidor basándose en:

1. **Estado**: Debe estar DISPONIBLE
2. **Activo**: Debe estar activo en el sistema
3. **Vehículo**: Debe tener un vehículo asignado y activo
4. **Capacidad**: El vehículo debe soportar el peso del pedido
5. **Calificación**: Se prioriza por mejor calificación promedio
6. **Experiencia**: Se prioriza por más entregas completadas

### 4. Liberación de Asignación

Cuando un pedido es cancelado, se libera el repartidor:

**URL**: `DELETE http://localhost:8083/api/asignaciones/pedido/{pedidoId}/liberar`

Esto cambia el estado del repartidor de `EN_RUTA` a `DISPONIBLE`.

## Configuración

### Pedido Service (application.yaml)

```yaml
services:
  fleet:
    url: ${FLEET_SERVICE_URL:http://localhost:8083}

integration:
  fleet:
    enabled: ${FLEET_INTEGRATION_ENABLED:true}
```

### Variables de Entorno

```bash
# Habilitar/Deshabilitar integración con Fleet
FLEET_INTEGRATION_ENABLED=true

# URL del Fleet Service
FLEET_SERVICE_URL=http://localhost:8083
```

## Estados del Pedido

Después de la asignación, el pedido puede tener los siguientes estados:

- **PENDIENTE**: Pedido creado, esperando asignación
- **ASIGNADO**: Repartidor y vehículo asignados exitosamente
- **EN_RUTA**: El repartidor está en camino (actualización posterior)
- **ENTREGADO**: Pedido entregado exitosamente
- **CANCELADO**: Pedido cancelado, asignación liberada

## Campos de Integración en el Modelo Pedido

```java
// Integración con FleetService
private String repartidorId; // ID del repartidor asignado
private String vehiculoId;   // ID del vehículo asignado

// Integración con BillingService
private String facturaId;    // ID de la factura generada
private Double tarifaCalculada; // Tarifa calculada por BillingService
```

## Ejemplo de Uso Completo

### 1. Crear un Pedido

```bash
curl -X POST http://localhost:8084/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "cli-10235",
    "direccionOrigen": {
      "calle": "Av. Amazonas",
      "numero": "N34-120",
      "ciudad": "Quito",
      "provincia": "Pichincha"
    },
    "direccionDestino": {
      "calle": "Calle Sucre",
      "numero": "15-08",
      "ciudad": "Guayaquil",
      "provincia": "Guayas"
    },
    "modalidadServicio": "URBANA_RAPIDA",
    "tipoEntrega": "EXPRESS",
    "peso": 2.3,
    "telefonoContacto": "0987654321",
    "nombreDestinatario": "Carlos Mendoza"
  }'
```

### 2. Respuesta Esperada

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "clienteId": "cli-10235",
  "estado": "ASIGNADO",
  "repartidorId": "1",
  "vehiculoId": "5",
  "facturaId": "FAC-001",
  "tarifaCalculada": 15.50,
  "fechaCreacion": "2025-12-14T10:30:00",
  "...": "..."
}
```

## Manejo de Errores

### Fleet Service No Disponible

Si el Fleet Service no está disponible, el pedido se crea de todas formas en estado `PENDIENTE` y se registra un log de advertencia:

```
WARN  - Error al integrar con Fleet Service: Connection refused
WARN  - El pedido quedará en estado PENDIENTE para asignación manual
```

### Sin Repartidores Disponibles

Si no hay repartidores disponibles, el pedido se crea en estado `PENDIENTE` y la asignación se puede hacer manualmente más tarde.

## Monitoreo y Logs

### Pedido Service

```
INFO  - Creando nuevo pedido para cliente: cli-10235
INFO  - Pedido creado con ID: 550e8400-e29b-41d4-a716-446655440000
INFO  - Integrando con Fleet Service para asignar repartidor...
INFO  - Repartidor asignado: ID=1, Vehículo=5
```

### Fleet Service

```
INFO  - POST /api/asignaciones - Asignando repartidor para pedido: 550e8400-e29b-41d4-a716-446655440000
INFO  - Iniciando asignación para pedido: 550e8400-e29b-41d4-a716-446655440000
INFO  - Asignación exitosa - Repartidor: 1 (Juan Pérez), Vehículo: 5 (ABC-123)
```

## Extensiones Futuras

### Tabla de Asignaciones

Para tener un mejor control sobre las asignaciones, se recomienda crear una tabla `asignaciones` que registre:

- ID de asignación
- ID del pedido
- ID del repartidor
- ID del vehículo
- Fecha de asignación
- Fecha de liberación
- Estado (ACTIVA, COMPLETADA, LIBERADA)

### Notificaciones en Tiempo Real

Implementar WebSockets o Server-Sent Events para notificar a los repartidores cuando se les asigna un nuevo pedido.

### Geolocalización

Integrar con APIs de mapas (Google Maps, OpenStreetMap) para:
- Calcular distancias reales
- Optimizar rutas
- Asignar al repartidor más cercano al origen del pedido

## Troubleshooting

### Error: Fleet Service retorna 500

**Causa**: No hay repartidores en la base de datos o todos están ocupados.

**Solución**: 
1. Verificar que existan repartidores activos: `GET http://localhost:8083/repartidores`
2. Crear repartidores si es necesario
3. Verificar que tengan vehículos asignados

### Error: Connection refused

**Causa**: Fleet Service no está ejecutándose.

**Solución**: Iniciar Fleet Service en el puerto 8083.

### El pedido se crea pero sin repartidorId

**Causa**: La integración está deshabilitada o Fleet Service no está disponible.

**Solución**: 
1. Verificar `integration.fleet.enabled=true` en application.yaml
2. Verificar que Fleet Service esté ejecutándose
3. Revisar los logs para ver el error específico

## Conclusión

La integración entre Pedido Service y Fleet Service permite una asignación automática y eficiente de recursos, mejorando los tiempos de respuesta y optimizando la operación logística del sistema EntregaExpress.

