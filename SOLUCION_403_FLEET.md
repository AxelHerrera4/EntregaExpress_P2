# 🔐 Solución Problema 403 Forbidden - Fleet Service

## ✅ **PROBLEMA IDENTIFICADO Y SOLUCIONADO**

### 🎯 **Causa Principal:**
El token JWT tiene el rol `ADMINISTRADOR_SISTEMA` pero los controladores de `fleet-service` esperaban `ADMINISTRADOR`. Había un **mismatch de roles**.

### 🔍 **Tu Token JWT Decodificado:**
```json
{
  "sub": "admin",
  "roles": ["ADMINISTRADOR_SISTEMA"],  ← Este es el rol correcto
  "iss": "auth-service", 
  "exp": 1765977598,
  "iat": 1765973998
}
```

### ❌ **Lo que esperaba fleet-service:**
```java
@PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR')")
//                                                    ^^^^^^^^^^^
//                                                    Rol incorrecto
```

### ✅ **Lo que he corregido:**
```java
@PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
//                                                    ^^^^^^^^^^^^^^^^^^^
//                                                    Rol correcto
```

## 🛠️ **Cambios Aplicados:**

### 1. **RepartidorController.java** - Corregidos todos los endpoints:
- ✅ `GET /repartidores` → Ahora acepta `ADMINISTRADOR_SISTEMA`
- ✅ `POST /repartidores` → Ahora acepta `ADMINISTRADOR_SISTEMA` 
- ✅ `GET /repartidores/{id}` → Ahora acepta roles de repartidor específicos
- ✅ `PATCH /repartidores/{id}` → Ahora acepta `ADMINISTRADOR_SISTEMA`
- ✅ Y todos los demás endpoints...

### 2. **VehiculoController.java** - Corregidos todos los endpoints:
- ✅ Todos los endpoints ahora usan `ADMINISTRADOR_SISTEMA` en lugar de `ADMINISTRADOR`

### 3. **Configuración JWT actualizada:**
- ✅ Expiración corregida a 1 hora (3600000 ms)
- ✅ Secreto sincronizado entre servicios

## 🚀 **Pasos para Aplicar la Solución:**

### 1. **Reconstruir fleet-service:**
```bash
# Parar el servicio
docker compose stop fleet-service

# Reconstruir con los cambios
docker compose build fleet-service

# Reiniciar el servicio
docker compose up -d fleet-service

# Verificar que esté ejecutándose
docker compose ps fleet-service
```

### 2. **Obtener un nuevo token (recomendado):**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 3. **Probar el endpoint de repartidores:**

#### **Via Kong (Recomendado):**
```bash
curl -X GET http://localhost:8080/api/repartidores \
  -H "Authorization: Bearer TU_ACCESS_TOKEN_AQUI"
```

#### **Directo al servicio:**
```bash  
curl -X GET http://localhost:8083/api/repartidores \
  -H "Authorization: Bearer TU_ACCESS_TOKEN_AQUI"
```

## 📋 **En Postman:**

### **Flujo Correcto:**
1. **Ejecutar "Login User"** → Obtiene y guarda el `ACCESS_TOKEN`
2. **Ejecutar "List All Repartidores"** → Ahora debería funcionar ✅

### **URL Correcta en Postman:**
```
GET {{FLEET_URL}}/api/repartidores
Authorization: Bearer {{ACCESS_TOKEN}}
```

Donde:
- `{{FLEET_URL}}` = `http://localhost:8083` (directo) o `http://localhost:8080` (via Kong)
- `{{ACCESS_TOKEN}}` = Se llena automáticamente después del login

## 🎯 **Roles Válidos Ahora:**

Para **fleet-service** endpoints:

### **Lectura (GET):**
- `REPARTIDOR_MOTORIZADO`
- `REPARTIDOR_VEHICULO` 
- `REPARTIDOR_CAMION`
- `SUPERVISOR`
- `GERENTE`
- `ADMINISTRADOR_SISTEMA` ← **Tu rol**

### **Escritura (POST/PATCH/DELETE):**
- `SUPERVISOR` (operaciones limitadas)
- `GERENTE` 
- `ADMINISTRADOR_SISTEMA` ← **Tu rol (acceso completo)**

## 🔧 **Si Aún Obtienes 403:**

### 1. **Verificar que el token es válido:**
```bash
# Decodificar en https://jwt.io
# Verificar que:
# - No esté expirado (exp > timestamp actual)
# - Tenga el rol ADMINISTRADOR_SISTEMA
# - Tenga iss: "auth-service"
```

### 2. **Verificar logs del fleet-service:**
```bash
docker compose logs fleet-service --tail 20
```

### 3. **Probar endpoint más simple primero:**
```bash
# Health check (no requiere autenticación)
curl http://localhost:8083/actuator/health
```

### 4. **Verificar formato del header:**
El header **DEBE** ser exactamente así:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**NO** incluir comillas, espacios extra, ni otros caracteres.

## ✅ **Resultado Esperado:**

Después de aplicar estos cambios, el GET a `/repartidores` debería retornar:
```json
[
  {
    "id": 1,
    "cedula": "1234567890",
    "nombreCompleto": "Juan Pérez",
    "email": "juan.perez@logiflow.com",
    // ... más datos del repartidor
  }
]
```

O un array vacío `[]` si no hay repartidores registrados.

## 🎉 **¡Problema Resuelto!**

El 403 Forbidden era por el mismatch de roles. Con los cambios aplicados, tu token con `ADMINISTRADOR_SISTEMA` ahora será aceptado por todos los endpoints del fleet-service.

---
**Ejecuta los pasos de reconstrucción y prueba nuevamente. ¡Debería funcionar! 🚀**
