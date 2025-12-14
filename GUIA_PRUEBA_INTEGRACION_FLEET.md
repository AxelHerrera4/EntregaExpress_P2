# Guía de Prueba: Integración Fleet-Service

## Prerequisitos

### 1. Bases de Datos en Ejecución

```powershell
# PostgreSQL para Pedido Service (puerto 5433)
# PostgreSQL para Fleet Service (puerto 5432)
# Asegúrate de que ambas bases de datos estén corriendo
```

### 2. Servicios en Ejecución

Debes tener los siguientes servicios ejecutándose:

1. **Billing Service** (puerto 8082)
2. **Fleet Service** (puerto 8083)
3. **Pedido Service** (puerto 8084)

## Paso 1: Preparar Fleet Service

### 1.1 Crear Vehículo

```bash
curl -X POST http://localhost:8083/vehiculos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "tipo": "MOTOCICLETA",
    "placa": "ABC123",
    "marca": "Honda",
    "modelo": "CBR 250R",
    "anio": 2023,
    "kilometraje": 5000,
    "capacidadCargaKg": 50.0,
    "consumoCombustibleKmPorLitro": 35.0,
    "activo": true
  }'
```

**Respuesta esperada**: ID del vehículo (ej: 1)

### 1.2 Crear Repartidor

```bash
curl -X POST http://localhost:8083/repartidores \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "cedula": "1234567890",
    "nombre": "Juan",
    "apellido": "Pérez",
    "email": "juan.perez@logiflow.com",
    "telefono": "0987654321",
    "direccion": "Av. Principal 123",
    "fechaNacimiento": "1990-01-15",
    "fechaContratacion": "2024-01-01",
    "tipoLicencia": "TIPO_A",
    "numeroLicencia": "LIC123456",
    "fechaVencimientoLicencia": "2026-12-31",
    "zonaAsignada": "Norte",
    "activo": true
  }'
```

**Respuesta esperada**: ID del repartidor (ej: 1)

### 1.3 Asignar Vehículo al Repartidor

```bash
curl -X PATCH http://localhost:8083/repartidores/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "vehiculoAsignadoId": 1
  }'
```

### 1.4 Verificar Repartidor Disponible

```bash
curl -X GET http://localhost:8083/repartidores/1 \
  -H "Authorization: Bearer <TOKEN>"
```

Verifica que:
- `estado`: `DISPONIBLE`
- `activo`: `true`
- `vehiculoAsignado`: no null

## Paso 2: Crear Pedido con Asignación Automática

### 2.1 Request Completo

```bash
curl -X POST http://localhost:8084/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI001",
    "direccionOrigen": {
      "calle": "Av Amazonas",
      "numero": "N34120",
      "ciudad": "Quito",
      "provincia": "Pichincha"
    },
    "direccionDestino": {
      "calle": "Calle Sucre",
      "numero": "1508",
      "ciudad": "Guayaquil",
      "provincia": "Guayas"
    },
    "modalidadServicio": "URBANA_RAPIDA",
    "tipoEntrega": "EXPRESS",
    "peso": 2.5,
    "telefonoContacto": "0987654321",
    "nombreDestinatario": "Carlos Mendoza"
  }'
```

### 2.2 Respuesta Esperada

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "clienteId": "CLI001",
  "direccionOrigen": {
    "calle": "Av Amazonas",
    "numero": "N34120",
    "ciudad": "Quito",
    "provincia": "Pichincha"
  },
  "direccionDestino": {
    "calle": "Calle Sucre",
    "numero": "1508",
    "ciudad": "Guayaquil",
    "provincia": "Guayas"
  },
  "modalidadServicio": "URBANA_RAPIDA",
  "tipoEntrega": "EXPRESS",
  "estado": "ASIGNADO",
  "peso": 2.5,
  "cobertura": "PICHINCHA_GUAYAS",
  "repartidorId": "1",
  "vehiculoId": "1",
  "facturaId": "FAC-001",
  "tarifaCalculada": 15.50,
  "telefonoContacto": "0987654321",
  "nombreDestinatario": "Carlos Mendoza",
  "fechaCreacion": "2025-12-14T10:30:00",
  "prioridad": "ALTA"
}
```

### 2.3 Verificar en Fleet Service

```bash
curl -X GET http://localhost:8083/repartidores/1 \
  -H "Authorization: Bearer <TOKEN>"
```

El estado del repartidor debe cambiar a: `EN_RUTA`

## Paso 3: Casos de Prueba

### Caso 1: Validación de Dirección - Error en Calle

```bash
curl -X POST http://localhost:8084/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI001",
    "direccionOrigen": {
      "calle": "Av. Amazonas #$%",
      "numero": "N34120",
      "ciudad": "Quito",
      "provincia": "Pichincha"
    },
    "direccionDestino": {
      "calle": "Calle Sucre",
      "numero": "1508",
      "ciudad": "Guayaquil",
      "provincia": "Guayas"
    },
    "modalidadServicio": "URBANA_RAPIDA",
    "tipoEntrega": "EXPRESS",
    "peso": 2.5,
    "telefonoContacto": "0987654321"
  }'
```

**Error esperado**: 
```json
{
  "timestamp": "2025-12-14T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "La calle solo puede contener letras, números y espacios",
  "path": "/api/pedidos"
}
```

### Caso 2: Validación de Ciudad - Solo Letras

```bash
curl -X POST http://localhost:8084/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI001",
    "direccionOrigen": {
      "calle": "Av Amazonas",
      "numero": "N34120",
      "ciudad": "Quito123",
      "provincia": "Pichincha"
    },
    "direccionDestino": {
      "calle": "Calle Sucre",
      "numero": "1508",
      "ciudad": "Guayaquil",
      "provincia": "Guayas"
    },
    "modalidadServicio": "URBANA_RAPIDA",
    "tipoEntrega": "EXPRESS",
    "peso": 2.5,
    "telefonoContacto": "0987654321"
  }'
```

**Error esperado**: `"La ciudad solo puede contener letras y espacios"`

### Caso 3: Validación de Peso - Solo Números

```bash
curl -X POST http://localhost:8084/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI001",
    "direccionOrigen": {
      "calle": "Av Amazonas",
      "numero": "N34120",
      "ciudad": "Quito",
      "provincia": "Pichincha"
    },
    "direccionDestino": {
      "calle": "Calle Sucre",
      "numero": "1508",
      "ciudad": "Guayaquil",
      "provincia": "Guayas"
    },
    "modalidadServicio": "URBANA_RAPIDA",
    "tipoEntrega": "EXPRESS",
    "peso": -5,
    "telefonoContacto": "0987654321"
  }'
```

**Error esperado**: `"El peso debe ser un número positivo mayor a 0"`

### Caso 4: Validación de Teléfono - 10 Dígitos

```bash
curl -X POST http://localhost:8084/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI001",
    "direccionOrigen": {
      "calle": "Av Amazonas",
      "numero": "N34120",
      "ciudad": "Quito",
      "provincia": "Pichincha"
    },
    "direccionDestino": {
      "calle": "Calle Sucre",
      "numero": "1508",
      "ciudad": "Guayaquil",
      "provincia": "Guayas"
    },
    "modalidadServicio": "URBANA_RAPIDA",
    "tipoEntrega": "EXPRESS",
    "peso": 2.5,
    "telefonoContacto": "098765"
  }'
```

**Error esperado**: `"El teléfono debe contener exactamente 10 dígitos numéricos"`

### Caso 5: Sin Repartidores Disponibles

Si todos los repartidores están ocupados (`EN_RUTA`), el pedido se crea en estado `PENDIENTE`:

```json
{
  "id": "...",
  "estado": "PENDIENTE",
  "repartidorId": null,
  "vehiculoId": null,
  "facturaId": "FAC-002",
  "tarifaCalculada": 15.50,
  "...": "..."
}
```

## Paso 4: Cancelar Pedido y Liberar Repartidor

```bash
curl -X POST http://localhost:8084/api/pedidos/{pedidoId}/cancelar
```

Esto debe:
1. Cambiar el estado del pedido a `CANCELADO`
2. Llamar a Fleet Service para liberar al repartidor
3. El repartidor vuelve a estado `DISPONIBLE`

## Paso 5: Verificar Logs

### Pedido Service

```
INFO  - Creando nuevo pedido para cliente: CLI001
INFO  - Pedido creado con ID: 550e8400-e29b-41d4-a716-446655440000
INFO  - Integrando con Billing Service para crear factura...
INFO  - Factura creada y asociada: ID=FAC-001, Monto=15.50
INFO  - Integrando con Fleet Service para asignar repartidor...
INFO  - Llamando a Fleet Service para asignar repartidor - pedidoId: 550e8400-e29b-41d4-a716-446655440000
INFO  - Repartidor asignado exitosamente - repartidorId: 1, vehiculoId: 1
INFO  - Repartidor asignado: ID=1, Vehículo=1
INFO  - Pedido creado exitosamente - ID: 550e8400-e29b-41d4-a716-446655440000, Estado: ASIGNADO
```

### Fleet Service

```
INFO  - POST /api/asignaciones - Asignando repartidor para pedido: 550e8400-e29b-41d4-a716-446655440000
INFO  - Iniciando asignación para pedido: 550e8400-e29b-41d4-a716-446655440000
INFO  - Asignación exitosa - Repartidor: 1 (Juan Pérez), Vehículo: 1 (ABC123)
```

## Resumen de Validaciones Implementadas

| Campo | Validación | Ejemplo Válido | Ejemplo Inválido |
|-------|-----------|----------------|------------------|
| **Calle** | Letras, números y espacios | `"Av Amazonas"` | `"Av. Amazonas #$"` |
| **Número** | Letras y números (sin espacios) | `"N34120"` | `"N34-120"` |
| **Ciudad** | Solo letras y espacios | `"Quito"` | `"Quito123"` |
| **Provincia** | Solo letras y espacios | `"Pichincha"` | `"Pichincha-1"` |
| **Peso** | Números positivos (enteros/decimales) | `2.5` | `-5` o `"abc"` |
| **Teléfono** | Exactamente 10 dígitos | `"0987654321"` | `"098765"` |

## Ejemplos de Pedidos Válidos

### Ejemplo 1: Entrega Urbana

```json
{
  "clienteId": "CLI001",
  "direccionOrigen": {
    "calle": "Av 6 de Diciembre",
    "numero": "N3412",
    "ciudad": "Quito",
    "provincia": "Pichincha"
  },
  "direccionDestino": {
    "calle": "Calle Colon",
    "numero": "E456",
    "ciudad": "Quito",
    "provincia": "Pichincha"
  },
  "modalidadServicio": "URBANA_RAPIDA",
  "tipoEntrega": "EXPRESS",
  "peso": 1.5,
  "telefonoContacto": "0991234567",
  "nombreDestinatario": "María López"
}
```

### Ejemplo 2: Entrega Intermunicipal

```json
{
  "clienteId": "CLI002",
  "direccionOrigen": {
    "calle": "Av De Los Shirys",
    "numero": "100",
    "ciudad": "Quito",
    "provincia": "Pichincha"
  },
  "direccionDestino": {
    "calle": "Av Eloy Alfaro",
    "numero": "S2N",
    "ciudad": "Latacunga",
    "provincia": "Cotopaxi"
  },
  "modalidadServicio": "INTERMUNICIPAL",
  "tipoEntrega": "NORMAL",
  "peso": 5.0,
  "telefonoContacto": "0981234567",
  "nombreDestinatario": "Pedro García"
}
```

### Ejemplo 3: Entrega Nacional

```json
{
  "clienteId": "CLI003",
  "direccionOrigen": {
    "calle": "Av Amazonas",
    "numero": "N5000",
    "ciudad": "Quito",
    "provincia": "Pichincha"
  },
  "direccionDestino": {
    "calle": "Av 9 de Octubre",
    "numero": "1234",
    "ciudad": "Guayaquil",
    "provincia": "Guayas"
  },
  "modalidadServicio": "NACIONAL",
  "tipoEntrega": "ECONOMICA",
  "peso": 10.5,
  "telefonoContacto": "0971234567",
  "nombreDestinatario": "Ana Martínez"
}
```

## Troubleshooting

### Problema: Pedido se crea pero sin repartidorId

**Solución**:
1. Verificar que Fleet Service esté corriendo: `curl http://localhost:8083/actuator/health`
2. Verificar que existan repartidores DISPONIBLES
3. Verificar que los repartidores tengan vehículos asignados
4. Revisar logs de ambos servicios

### Problema: Error 500 en Fleet Service

**Solución**:
1. Verificar que la base de datos de Fleet esté corriendo
2. Verificar que existan repartidores y vehículos en la BD
3. Revisar logs del Fleet Service para el error específico

### Problema: Validación de dirección falla

**Solución**:
- Asegúrate de que los campos no contengan caracteres especiales
- Calle: solo letras, números y espacios
- Número: solo letras y números (sin espacios ni guiones)
- Ciudad/Provincia: solo letras y espacios

## Conclusión

La integración está completa y funcionando. El sistema ahora:
1. ✅ Crea pedidos con validaciones estrictas
2. ✅ Genera facturas automáticamente (Billing Service)
3. ✅ Asigna repartidores y vehículos automáticamente (Fleet Service)
4. ✅ Maneja errores gracefully sin bloquear la creación del pedido
5. ✅ Libera recursos cuando se cancela un pedido

