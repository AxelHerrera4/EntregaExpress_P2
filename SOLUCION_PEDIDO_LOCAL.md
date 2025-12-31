# 🚀 Guía: Ejecutar Pedido-Service en Local

## ❌ **Problema Original:**
```
FATAL: password authentication failed for user "pedido_user"
```

## 🔍 **Causa del Error:**
El `pedido-service` está intentando conectarse a PostgreSQL con configuración incorrecta para desarrollo local.

### **Configuración Incorrecta:**
- Puerto: `5433` (incorrecto)
- Usuario/Password: Correctos pero BD no disponible

### **Configuración Correcta:**
- Puerto: `5434` (según docker-compose.yml)
- Usuario: `pedido_user` 
- Password: `pedido_pass`
- Base de datos: `pedidos_db`

## ✅ **Solución Aplicada:**

### 1. **Configuración Corregida:**
- ✅ `application.yaml` - Puerto corregido a 5434
- ✅ `application-local.yaml` - Profile específico para desarrollo
- ✅ `run-local.bat` - Script automatizado para ejecución

### 2. **Archivos Creados:**
- `application-local.yaml` - Configuración optimizada para desarrollo
- `run-local.bat` - Script para ejecutar fácilmente

## 🚀 **Cómo Ejecutar Localmente:**

### **Opción 1: Usando el Script (Recomendado)**
```bash
# Desde la carpeta pedido-service
cd D:\EntregaExpress_P2\logiflow\pedido-service
run-local.bat
```

### **Opción 2: Manualmente**

#### **Paso 1: Asegurar que PostgreSQL esté ejecutándose**
```bash
# Verificar estado
docker compose ps postgres-pedido

# Si no está ejecutándose, levantarlo
docker compose up postgres-pedido -d

# Verificar que esté saludable
docker compose logs postgres-pedido
```

#### **Paso 2: Configurar variables de entorno**
```bash
set SPRING_PROFILES_ACTIVE=local
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/pedidos_db
set SPRING_DATASOURCE_USERNAME=pedido_user
set SPRING_DATASOURCE_PASSWORD=pedido_pass
```

#### **Paso 3: Ejecutar la aplicación**
```bash
# Desde la carpeta pedido-service
cd D:\EntregaExpress_P2\logiflow\pedido-service

# Opción A: Con Maven Wrapper
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local

# Opción B: Con Maven instalado
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Opción C: Desde IDE (IntelliJ/Eclipse)
# - Configurar VM options: -Dspring.profiles.active=local
# - Ejecutar PedidoServiceApplication.main()
```

### **Opción 3: Desde el IDE**

#### **IntelliJ IDEA:**
1. Abrir `PedidoServiceApplication.java`
2. Click en "Edit Configurations"
3. En "VM options" agregar: `-Dspring.profiles.active=local`
4. En "Environment variables" agregar:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/pedidos_db
   SPRING_DATASOURCE_USERNAME=pedido_user
   SPRING_DATASOURCE_PASSWORD=pedido_pass
   ```
5. Click "Run"

#### **Eclipse:**
1. Right-click en el proyecto → "Run As" → "Run Configurations"
2. En "Arguments" tab → "VM arguments": `-Dspring.profiles.active=local`
3. En "Environment" tab → Add variables como arriba
4. Click "Run"

## 🔧 **Configuración de Base de Datos:**

### **Parámetros de Conexión Local:**
```yaml
Host: localhost
Puerto: 5434
Base de datos: pedidos_db
Usuario: pedido_user
Password: pedido_pass
```

### **URL de Conexión JDBC:**
```
jdbc:postgresql://localhost:5434/pedidos_db
```

## ✅ **Verificación de Éxito:**

### **1. Aplicación iniciada correctamente:**
```
[main] INFO c.l.pedidoservice.PedidoServiceApplication - Started PedidoServiceApplication in X.XXX seconds
```

### **2. Endpoints disponibles:**
- **API Base:** http://localhost:8084/api/pedidos
- **Swagger UI:** http://localhost:8084/swagger-ui.html
- **Health Check:** http://localhost:8084/actuator/health

### **3. Probar con curl:**
```bash
# Health check
curl http://localhost:8084/actuator/health

# Listar pedidos (puede estar vacío)
curl http://localhost:8084/api/pedidos
```

## 🔍 **Troubleshooting:**

### **Error: "Connection refused"**
```bash
# Verificar que PostgreSQL esté ejecutándose
docker compose ps postgres-pedido

# Si no está, levantarlo
docker compose up postgres-pedido -d
```

### **Error: "Database does not exist"**
```bash
# Conectarse a PostgreSQL y crear la BD manualmente
docker exec -it postgres-pedido psql -U pedido_user -d postgres
CREATE DATABASE pedidos_db;
\q
```

### **Error: "Port already in use"**
- Verificar si hay otra instancia ejecutándose
- Cambiar puerto en `application-local.yaml` si es necesario

### **Logs detallados:**
```bash
# Ejecutar con debug habilitado
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local -Ddebug
```

## 📋 **Dependencias de Desarrollo:**

### **Base de Datos:**
- ✅ PostgreSQL (via Docker) - Puerto 5434
- ✅ Flyway migrations (si existen)
- ✅ Hibernate DDL auto-update habilitado

### **Microservicios (Opcional para desarrollo):**
- Billing Service: http://localhost:8082
- Fleet Service: http://localhost:8083  
- Auth Service: http://localhost:8081

## 🎯 **Flujo de Desarrollo Recomendado:**

1. **Levantar solo la BD:** `docker compose up postgres-pedido -d`
2. **Ejecutar pedido-service localmente:** `run-local.bat`
3. **Desarrollar/probar** con hot-reload automático
4. **Para pruebas completas:** Levantar otros servicios según necesidad

## 🔄 **Sincronización con Docker:**

### **Cuando cambies configuración:**
```bash
# Rebuild para Docker
docker compose build pedido-service

# Test local
run-local.bat
```

---
**¡Ahora deberías poder ejecutar pedido-service localmente sin problemas! 🚀**
