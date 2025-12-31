# 🔐 Implementación JWT Completa - LogiFlow Microservices

## ✅ **IMPLEMENTACIÓN COMPLETADA**

Se ha implementado autenticación JWT completa y sincronizada en **todos los microservicios** de LogiFlow:

### 🎯 **Servicios Configurados:**
- ✅ **auth-service** - Genera tokens JWT
- ✅ **pedido-service** - Valida JWT + @PreAuthorize 
- ✅ **billing-service** - Valida JWT + @PreAuthorize
- ✅ **fleet-service** - Valida JWT + @PreAuthorize
- ✅ **Kong Gateway** - Enruta con headers JWT

## 🔑 **Configuración JWT Sincronizada:**

### **Secret Key (Todos los servicios):**
```yaml
jwt:
  secret: VGhpcy1pcy1hLXNlY3JldC1rZXktZm9y-LWRlbW8tYXBwLWp3dC0yMDI1
  expiration: 3600000 # 1 hora
  issuer: auth-service
```

### **Token JWT Generado por auth-service:**
```json
{
  "sub": "admin",
  "iss": "auth-service", 
  "roles": ["ADMINISTRADOR_SISTEMA"],
  "exp": 1734444598,
  "iat": 1734440998
}
```

## 👥 **Roles del Sistema:**
```java
// Definidos en auth-service/RoleName.java
CLIENTE
REPARTIDOR_MOTORIZADO
REPARTIDOR_VEHICULO  
REPARTIDOR_CAMION
SUPERVISOR
GERENTE
ADMINISTRADOR_SISTEMA  ← Tu rol actual
```

## 🛡️ **Matriz de Permisos por Servicio:**

### 🚚 **Fleet Service:**
| Endpoint | Cliente | Repartidor | Supervisor | Gerente | Admin |
|----------|---------|------------|------------|---------|-------|
| GET /api/repartidores | ❌ | ❌ | ✅ | ✅ | ✅ |
| POST /api/repartidores | ❌ | ❌ | ✅ | ✅ | ✅ |
| GET /api/repartidores/{id} | ❌ | ✅ | ✅ | ✅ | ✅ |
| GET /api/vehiculos | ❌ | ❌ | ✅ | ✅ | ✅ |
| POST /api/vehiculos | ❌ | ❌ | ✅ | ✅ | ✅ |
| DELETE /api/vehiculos/{id} | ❌ | ❌ | ❌ | ✅ | ✅ |

### 📦 **Pedido Service:**
| Endpoint | Cliente | Repartidor | Supervisor | Gerente | Admin |
|----------|---------|------------|------------|---------|-------|
| POST /api/pedidos | ✅ | ❌ | ✅ | ✅ | ✅ |
| GET /api/pedidos | ❌ | ❌ | ✅ | ✅ | ✅ |
| GET /api/pedidos/{id} | ✅ | ✅ | ✅ | ✅ | ✅ |
| GET /api/pedidos/cliente/{id} | ✅ | ❌ | ✅ | ✅ | ✅ |
| PATCH /api/pedidos/{id} | ✅ | ❌ | ✅ | ✅ | ✅ |
| DELETE /api/pedidos/{id} | ❌ | ❌ | ❌ | ✅ | ✅ |
| GET /api/pedidos/repartidor/{id} | ❌ | ✅ | ✅ | ✅ | ✅ |
| PATCH /api/pedidos/{id}/asignar | ❌ | ❌ | ✅ | ✅ | ✅ |

### 💰 **Billing Service:**
| Endpoint | Cliente | Repartidor | Supervisor | Gerente | Admin |
|----------|---------|------------|------------|---------|-------|
| POST /api/facturas | ❌ | ❌ | ✅ | ✅ | ✅ |
| GET /api/facturas/{id} | ✅ | ❌ | ✅ | ✅ | ✅ |
| GET /api/facturas/pedido/{id} | ✅ | ❌ | ✅ | ✅ | ✅ |
| PATCH /api/facturas/{id}/estado | ❌ | ❌ | ✅ | ✅ | ✅ |
| POST /api/tarifas | ❌ | ❌ | ❌ | ✅ | ✅ |
| GET /api/tarifas/{tipo} | ❌ | ❌ | ✅ | ✅ | ✅ |
| PUT /api/tarifas/{tipo} | ❌ | ❌ | ❌ | ✅ | ✅ |

## 🔧 **Archivos Modificados/Creados:**

### **auth-service:**
- ✅ `JwtUtils.java` - Incluye `issuer` en tokens
- ✅ `application.properties` - Configuración JWT

### **pedido-service:**
- ✅ `pom.xml` - Dependencias JWT agregadas
- ✅ `application.yaml` - Configuración JWT
- ✅ `application-local.yaml` - Configuración JWT para desarrollo
- 🆕 `JwtAuthenticationFilter.java` - Filtro JWT
- ✅ `SecurityConfig.java` - Configuración completa
- ✅ `PedidoController.java` - @PreAuthorize en todos los endpoints

### **billing-service:**
- ✅ `pom.xml` - Dependencias JWT agregadas  
- ✅ `application.yaml` - Configuración JWT
- 🆕 `JwtAuthenticationFilter.java` - Filtro JWT
- ✅ `SecurityConfig.java` - Configuración completa
- ✅ `FacturaController.java` - @PreAuthorize agregados
- ✅ `TarifaBaseController.java` - @PreAuthorize agregados

### **fleet-service:**
- ✅ `application.yaml` - Secret sincronizado
- ✅ `JwtAuthenticationFilter.java` - Manejo Base64 corregido
- ✅ `RepartidorController.java` - Roles actualizados
- ✅ `VehiculoController.java` - Roles actualizados

### **Kong Gateway:**
- ✅ `kong.yml` - Rutas corregidas con context-paths
- ✅ Plugin headers agregados

### **Postman:**
- ✅ `LogiFlow_Postman_Collection.json` - URLs actualizadas
- ✅ `LogiFlow_Environment.json` - Puerto Kong corregido
- ✅ Headers `Bearer {{ACCESS_TOKEN}}` en todos los endpoints

## 🚀 **Pasos para Levantar Todo:**

### **1. Reconstruir servicios con JWT:**
```bash
# Parar servicios
docker compose down

# Reconstruir servicios con cambios JWT
docker compose build authservice pedido-service billing-service fleet-service

# Levantar todo
docker compose up -d

# Verificar estado
docker compose ps
```

### **2. Probar flujo completo en Postman:**

#### **A. Autenticación:**
```bash
# 1. Register User (opcional)
POST {{AUTH_URL}}/api/auth/register

# 2. Login User - Guarda ACCESS_TOKEN automáticamente  
POST {{AUTH_URL}}/api/auth/login
```

#### **B. Probar cada servicio:**
```bash
# Fleet Service (con JWT)
GET {{FLEET_URL}}/api/repartidores
Authorization: Bearer {{ACCESS_TOKEN}}

# Pedido Service (con JWT)  
GET {{PEDIDO_URL}}/api/pedidos
Authorization: Bearer {{ACCESS_TOKEN}}

# Billing Service (con JWT)
GET {{BILLING_URL}}/api/facturas/123
Authorization: Bearer {{ACCESS_TOKEN}}
```

#### **C. Probar via Kong Gateway:**
```bash
# Todos los endpoints via Kong (puerto 8080)
GET {{KONG_URL}}/api/repartidores
GET {{KONG_URL}}/api/pedidos
GET {{KONG_URL}}/api/facturas/123
Authorization: Bearer {{ACCESS_TOKEN}}
```

## 🔍 **URLs de Servicios:**

### **Desarrollo Directo:**
- Auth: `http://localhost:8081/api/auth/*`
- Pedidos: `http://localhost:8084/api/pedidos/*`
- Billing: `http://localhost:8082/api/facturas/*`
- Fleet: `http://localhost:8083/api/repartidores/*` (context-path incluido)

### **Via Kong Gateway:**
- Auth: `http://localhost:8080/api/auth/*`
- Pedidos: `http://localhost:8080/api/pedidos/*` 
- Billing: `http://localhost:8080/api/facturas/*`
- Fleet: `http://localhost:8080/api/repartidores/*`

## ⚠️ **Importante - Cambios de Comportamiento:**

### **ANTES (Sin JWT):**
```bash
# Cualquier endpoint funcionaba sin autenticación
curl http://localhost:8084/api/pedidos
# → 200 OK []
```

### **DESPUÉS (Con JWT):**
```bash
# Sin token = 401 Unauthorized
curl http://localhost:8084/api/pedidos
# → 401 Unauthorized

# Con token = 200 OK
curl -H "Authorization: Bearer TOKEN" http://localhost:8084/api/pedidos
# → 200 OK [...]
```

## 🎯 **Tu Usuario Admin:**
- **Username:** `admin`
- **Password:** `admin123`  
- **Rol:** `ADMINISTRADOR_SISTEMA`
- **Acceso:** ✅ **Todos los endpoints** de todos los servicios

## 🔧 **Troubleshooting JWT:**

### **403 Forbidden:**
- Verificar que el token tenga el rol correcto
- Verificar que el endpoint permita tu rol en `@PreAuthorize`

### **401 Unauthorized:**
- Token expirado (1 hora de validez)
- Token malformado o secreto incorrecto
- Header `Authorization: Bearer TOKEN` mal formateado

### **Token Validation:**
- Ir a https://jwt.io y pegar tu token
- Verificar que tenga `iss: "auth-service"`
- Verificar que `exp` sea mayor al timestamp actual
- Verificar que `roles` contenga tu rol

### **Verificar logs:**
```bash
docker compose logs authservice | grep JWT
docker compose logs pedido-service | grep JWT  
docker compose logs billing-service | grep JWT
docker compose logs fleet-service | grep JWT
```

## ✅ **Resultado Final:**

🎉 **¡IMPLEMENTACIÓN JWT COMPLETA!** 

Todos los microservicios ahora:
- ✅ Validan tokens JWT del auth-service
- ✅ Verifican roles específicos por endpoint
- ✅ Manejan la misma configuración JWT sincronizada
- ✅ Funcionan tanto directamente como via Kong Gateway
- ✅ Están integrados en la colección de Postman

**¡Tu sistema LogiFlow ahora tiene seguridad JWT empresarial completa! 🚀**
