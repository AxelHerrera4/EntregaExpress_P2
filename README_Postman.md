# 📋 LogiFlow Microservices - Postman Collection

Esta colección de Postman contiene todos los endpoints para probar los microservicios de LogiFlow de manera completa.

## 📁 Archivos Incluidos

1. **LogiFlow_Postman_Collection.json** - La colección completa con todos los endpoints
2. **LogiFlow_Environment.json** - Variables de entorno predefinidas
3. **README_Postman.md** - Este archivo con instrucciones

## 🚀 Configuración Inicial

### 1. Importar en Postman
1. Abrir Postman
2. Hacer clic en "Import"
3. Seleccionar ambos archivos JSON:
   - `LogiFlow_Postman_Collection.json`
   - `LogiFlow_Environment.json`

### 2. Seleccionar Environment
1. En la esquina superior derecha de Postman
2. Seleccionar "LogiFlow Local Environment"

### 3. Levantar los Servicios
Asegúrate de que todos los servicios estén ejecutándose:

```bash
# Desde la carpeta logiflow
docker compose up -d

# O levantar individualmente
# Auth Service - Puerto 8081
# Billing Service - Puerto 8082  
# Fleet Service - Puerto 8083
# Pedido Service - Puerto 8084
# Kong Gateway - Puerto 8000
```

## 🔐 Flujo de Autenticación

### Paso 1: Registrar Usuario
```
POST {{AUTH_URL}}/api/auth/register
```
Ejecutar el endpoint "Register User" para crear un usuario administrador.

### Paso 2: Login
```
POST {{AUTH_URL}}/api/auth/login
```
Ejecutar "Login User" - esto guardará automáticamente el `ACCESS_TOKEN` en las variables de entorno.

### Paso 3: Verificar Autenticación
```
GET {{AUTH_URL}}/api/protected/me
```
Ejecutar "Get Protected Me" para verificar que el token funciona.

## 📦 Orden de Prueba Recomendado

### 1. 🔐 Auth Service
- Register User
- Login User (guarda tokens automáticamente)
- Get Protected Me
- Admin Only Endpoint
- Refresh Token (opcional)

### 2. 💰 Billing Service
- Create Tarifa Base (EXPRESS, STANDARD, ECONOMICA)
- Get All Tarifas
- Create Factura (después de crear un pedido)
- Get Factura by ID

### 3. 🚚 Fleet Service

#### Repartidores:
- Create Repartidor
- List All Repartidores  
- Get Repartidor by ID (usar ID del response anterior)
- Update Repartidor
- Change Repartidor Status

#### Vehículos:
- Create Vehiculo
- List All Vehiculos
- Get Vehiculo by ID
- Assign Vehicle to Repartidor
- Update Vehiculo

#### Asignaciones:
- Assign Repartidor to Pedido (después de crear pedido)
- Release Assignment

### 4. 📦 Pedido Service

#### CRUD Básico:
- Create Pedido (guarda PEDIDO_ID automáticamente)
- Get Pedido by ID
- Get All Pedidos
- Update Pedido (PATCH)
- Cancel Pedido

#### Búsquedas y Filtros:
- Get Pedidos by Cliente
- Get Pedidos by Modalidad
- Get Pedidos Pendientes Asignacion
- Get Pedidos Sin Factura
- Get Pedidos Alta Prioridad

#### Integraciones:
- Associate Factura
- Assign Repartidor and Vehiculo

### 5. 🌐 Kong Gateway

**IMPORTANTE**: Antes de probar Kong, asegúrate de que las rutas estén configuradas correctamente.

#### Verificación de Kong:
1. **Kong Health Check** - Verificar que Kong responde
2. **Kong Admin - Services** - Ver servicios configurados  
3. **Kong Admin - Routes** - Ver rutas configuradas

#### Pruebas via Kong:
4. **Via Kong - Create Pedido** - Crear pedido a través del gateway

#### URLs Correctas via Kong:
- ✅ Auth: `http://localhost:8080/api/auth/login`
- ✅ Pedidos: `http://localhost:8080/api/pedidos`  
- ✅ Facturas: `http://localhost:8080/api/facturas`
- ✅ Repartidores: `http://localhost:8080/api/repartidores`
- ✅ Vehículos: `http://localhost:8080/api/vehiculos`

### 6. 🔧 Health Checks
- Verificar salud de todos los servicios

## 🔧 Variables de Entorno

| Variable | Valor por Defecto | Descripción |
|----------|-------------------|-------------|
| `AUTH_URL` | http://localhost:8081 | URL del Auth Service |
| `BILLING_URL` | http://localhost:8082 | URL del Billing Service |
| `FLEET_URL` | http://localhost:8083 | URL del Fleet Service |
| `PEDIDO_URL` | http://localhost:8084 | URL del Pedido Service |
| `KONG_URL` | http://localhost:8080 | URL del Kong Gateway |
| `KONG_ADMIN_URL` | http://localhost:8001 | URL Admin de Kong |
| `ACCESS_TOKEN` | (automático) | Token JWT para autenticación |
| `REFRESH_TOKEN` | (automático) | Token para renovar sesión |
| `PEDIDO_ID` | (automático) | ID del último pedido creado |
| `FACTURA_ID` | (manual) | ID de factura para pruebas |
| `REPARTIDOR_ID` | 1 | ID de repartidor para pruebas |
| `VEHICULO_ID` | 1 | ID de vehículo para pruebas |

## 📋 Datos de Ejemplo

### Usuario de Prueba
```json
{
  "username": "admin",
  "email": "admin@logiflow.com", 
  "password": "admin123",
  "roles": ["ADMINISTRADOR_SISTEMA"]
}
```

### Pedido de Prueba
```json
{
  "clienteId": "cli-12345",
  "direccionOrigen": {
    "calle": "Av Principal",
    "numero": "123", 
    "ciudad": "Quito",
    "provincia": "Pichincha"
  },
  "direccionDestino": {
    "calle": "Calle Secundaria",
    "numero": "456",
    "ciudad": "Guayaquil", 
    "provincia": "Guayas"
  },
  "modalidadServicio": "NACIONAL",
  "tipoEntrega": "EXPRESS",
  "peso": 2.5,
  "telefonoContacto": "0987654321",
  "nombreDestinatario": "Juan Pérez"
}
```

### Repartidor de Prueba
```json
{
  "cedula": "1234567890",
  "nombreCompleto": "Juan Pérez",
  "email": "juan.perez@logiflow.com",
  "telefono": "0999888777",
  "direccion": "Av. Principal 123",
  "tipoLicencia": "C",
  "fechaVencimientoLicencia": "2025-12-31",
  "zona": "NORTE"
}
```

### Vehículo de Prueba  
```json
{
  "placa": "ABC-1234",
  "marca": "Toyota",
  "modelo": "Hiace", 
  "year": 2020,
  "tipoVehiculo": "FURGONETA",
  "capacidadCarga": 1500.0,
  "zona": "NORTE"
}
```

## 🎯 Escenarios de Prueba Completos

### Escenario 1: Flujo Completo de Pedido
1. **Auth**: Register + Login
2. **Billing**: Create Tarifa Base (EXPRESS)
3. **Fleet**: Create Repartidor + Create Vehiculo
4. **Pedido**: Create Pedido
5. **Billing**: Create Factura (usando PEDIDO_ID)
6. **Pedido**: Associate Factura
7. **Fleet**: Assign Repartidor to Pedido
8. **Pedido**: Get Pedido by ID (verificar todo asociado)

### Escenario 2: Gestión de Fleet
1. **Fleet**: Create múltiples Repartidores
2. **Fleet**: Create múltiples Vehículos
3. **Fleet**: Assign Vehicle to Repartidor
4. **Fleet**: Change Repartidor Status (DISPONIBLE → EN_RUTA)
5. **Fleet**: Update Vehiculo
6. **Fleet**: List All (verificar cambios)

### Escenario 3: Búsquedas y Filtros
1. **Pedido**: Create múltiples pedidos (diferentes clientes, modalidades)
2. **Pedido**: Get Pedidos by Cliente
3. **Pedido**: Get Pedidos by Modalidad  
4. **Pedido**: Get Pedidos Pendientes Asignacion
5. **Pedido**: Get Pedidos Sin Factura
6. **Pedido**: Get Pedidos Alta Prioridad

## ⚠️ Problemas Comunes

### 401 Unauthorized
- Verificar que el `ACCESS_TOKEN` esté configurado
- Ejecutar "Login User" nuevamente
- Verificar que el usuario tenga los roles correctos

### 404 Not Found  
- Verificar que los servicios estén ejecutándose
- Revisar los puertos en las variables de entorno
- Verificar que los IDs en las URLs sean correctos

### 500 Internal Server Error
- Revisar logs de Docker: `docker compose logs -f [service-name]`
- Verificar conectividad entre servicios
- Verificar configuración de base de datos

### Variables No Actualizadas
- Algunos endpoints tienen scripts que guardan automáticamente IDs
- Si no se actualizan, copiar manualmente desde las respuestas
- Verificar que el environment correcto esté seleccionado

### 🌐 Kong Gateway - "no Route matched with those values"

**Problema**: Al usar Kong (puerto 8000) aparece este error, pero directamente al servicio (puerto 808X) funciona.

**Causa**: Kong no tiene configuradas las rutas correctas para los microservicios.

**Solución**:

1. **Verificar configuración de Kong**:
   ```bash
   # Verificar que kong.yml tiene las rutas correctas
   cat logiflow/kong.yml
   ```

2. **Reiniciar Kong con nueva configuración**:
   ```bash
   # Detener Kong
   docker compose stop kong
   
   # Reiniciar Kong para cargar nueva configuración
   docker compose up -d kong
   ```

3. **Verificar rutas cargadas**:
   ```bash
   # Ver rutas configuradas
   curl http://localhost:8001/routes
   
   # Ver servicios configurados  
   curl http://localhost:8001/services
   ```

4. **Rutas correctas configuradas**:
   - Auth: `http://localhost:8080/api/auth/*`
   - Billing: `http://localhost:8080/api/facturas/*` y `http://localhost:8080/api/tarifas/*`
   - Fleet: `http://localhost:8080/api/repartidores/*`, `http://localhost:8080/api/vehiculos/*`, `http://localhost:8080/api/asignaciones/*`
   - Pedidos: `http://localhost:8080/api/pedidos/*`

5. **Si persiste el problema**:
   ```bash
   # Ver logs de Kong
   docker compose logs kong
   
   # Recrear Kong completamente
   docker compose down kong
   docker compose up -d kong
   ```

## 📚 Documentación Adicional

- **OpenAPI/Swagger**: Cada servicio expone documentación en `/swagger-ui/index.html`
- **Actuator**: Health checks disponibles en `/actuator/health`
- **Kong Admin**: Documentación de rutas en `http://localhost:8001`

## 🔄 Actualización de la Colección

Para actualizar esta colección con nuevos endpoints:

1. Exportar la colección modificada desde Postman
2. Reemplazar el archivo `LogiFlow_Postman_Collection.json`
3. Actualizar este README si es necesario

---

**¡Happy Testing! 🚀**

> 💡 **Tip**: Usar la funcionalidad "Runner" de Postman para ejecutar toda la colección automáticamente y generar reportes de pruebas.
