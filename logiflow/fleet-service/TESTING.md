# Pruebas Rápidas - Fleet Service API

Este archivo contiene ejemplos de cURL para probar los nuevos endpoints implementados.

## Configuración

```bash
# Variables de entorno
export BASE_URL="http://localhost:8082/api"
export JWT_TOKEN="tu_token_jwt_aqui"
```

## 1. Health Check (Público)

```bash
# Verificar estado del servicio
curl -X GET "$BASE_URL/health"
```

**Respuesta esperada:**
```json
{
  "status": "UP",
  "service": "fleet-service",
  "timestamp": "2025-12-14T...",
  "version": "1.0.0"
}
```

## 2. Estadísticas de Flota

```bash
# Obtener estadísticas generales (requiere autenticación)
curl -X GET "$BASE_URL/estadisticas/flota" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Respuesta esperada:**
```json
{
  "totalVehiculos": 50,
  "vehiculosActivos": 45,
  "vehiculosDisponibles": 10,
  "totalRepartidores": 40,
  "repartidoresDisponibles": 15,
  "repartidoresEnRuta": 20,
  "motorizados": 20,
  "vehiculosLivianos": 25,
  "camiones": 5,
  "tasaExito": 95.5
}
```

## 3. Métricas de Repartidor Individual

```bash
# Obtener métricas de un repartidor específico
REPARTIDOR_ID=1
curl -X GET "$BASE_URL/repartidores/$REPARTIDOR_ID/metricas" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Respuesta esperada:**
```json
{
  "repartidorId": 1,
  "nombreCompleto": "Juan Pérez",
  "entregasCompletadas": 150,
  "entregasFallidas": 5,
  "calificacionPromedio": 4.8,
  "totalCalificaciones": 120,
  "kilometrosRecorridos": 1250.5,
  "tasaExito": 96.77,
  "promedioEntregasPorDia": 7.5
}
```

## 4. Top Performers

```bash
# Obtener los mejores repartidores
curl -X GET "$BASE_URL/repartidores/top-performers" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Respuesta esperada:**
```json
[
  {
    "repartidorId": 1,
    "nombreCompleto": "Juan Pérez",
    "entregasCompletadas": 150,
    "calificacionPromedio": 4.9,
    "tasaExito": 98.5,
    ...
  },
  ...
]
```

## 5. Crear Repartidor (Ejemplo Completo)

```bash
curl -X POST "$BASE_URL/repartidores" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cedula": "1234567890",
    "nombre": "Carlos",
    "apellido": "González",
    "email": "carlos.gonzalez@example.com",
    "telefono": "0999999999",
    "direccion": "Av. Principal 123",
    "fechaNacimiento": "1990-05-15",
    "fechaContratacion": "2025-01-01",
    "tipoLicencia": "TIPO_B",
    "numeroLicencia": "LIC12345",
    "fechaVencimientoLicencia": "2027-12-31",
    "zonaAsignada": "Norte",
    "diasLaborales": ["LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"],
    "horaInicioTurno": "08:00",
    "horaFinTurno": "17:00"
  }'
```

## 6. Crear Vehículo (Ejemplo Completo)

```bash
curl -X POST "$BASE_URL/vehiculos" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "placa": "ABC-1234",
    "marca": "Toyota",
    "modelo": "Hilux",
    "anio": 2023,
    "tipo": "VEHICULO_LIVIANO",
    "capacidadCargaKg": 1000.0,
    "consumoCombustibleKmPorLitro": 12.5
  }'
```

## 7. Asignar Vehículo a Repartidor

```bash
REPARTIDOR_ID=1
VEHICULO_ID=1

curl -X POST "$BASE_URL/repartidores/$REPARTIDOR_ID/asignar-vehiculo?vehiculoId=$VEHICULO_ID" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## 8. Listar Repartidores Disponibles

```bash
curl -X GET "$BASE_URL/repartidores/disponibles" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## 9. Listar Vehículos Disponibles

```bash
curl -X GET "$BASE_URL/vehiculos/disponibles" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## 10. Cambiar Estado de Repartidor

```bash
REPARTIDOR_ID=1

curl -X PATCH "$BASE_URL/repartidores/$REPARTIDOR_ID/estado?estado=EN_RUTA" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Estados Disponibles para Repartidores

- `DISPONIBLE`
- `EN_RUTA`
- `DESCANSO`
- `ALMUERZO`
- `MANTENIMIENTO`
- `INACTIVO`

## Tipos de Vehículo

- `MOTORIZADO`
- `VEHICULO_LIVIANO`
- `CAMION`

## Tipos de Licencia

- `TIPO_A` - Motocicletas
- `TIPO_B` - Vehículos livianos
- `TIPO_C` - Vehículos pesados
- `TIPO_E` - Todas las categorías

## Notas Importantes

1. **JWT Token**: Obtén el token desde el servicio de autenticación antes de usar estos endpoints.
2. **Roles**: Asegúrate de tener los permisos adecuados según tu rol.
3. **Validaciones**: Todos los endpoints tienen validaciones. Revisa los mensajes de error para debugging.

## Documentación Interactiva

Para una experiencia más visual, accede a Swagger UI:

```
http://localhost:8082/api/swagger-ui.html
```

## Verificar Compilación

```bash
cd fleet-service
./mvnw clean compile
```

## Ejecutar la Aplicación

```bash
cd fleet-service
./mvnw spring-boot:run
```

O con IDE (IntelliJ IDEA / Eclipse):
- Run: `FleetServiceApplication.main()`

---

**Nota:** Asegúrate de que PostgreSQL esté ejecutándose en `localhost:5432` con la base de datos `fleet_db` creada.
