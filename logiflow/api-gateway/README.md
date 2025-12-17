# 🔀 API Gateway (Puerto 8080)

**Punto de Entrada Centralizado del Sistema**

El **API Gateway** actúa como punto de entrada único y centralizado para todas las solicitudes externas hacia los microservicios del sistema LogiFlow. Implementa enrutamiento inteligente, autenticación, autorización y rate limiting.

---

## ⚙️ Configuración Técnica

**Puerto:** 8080  
**Framework:** Spring Boot 4.0.0  
**Lenguaje:** Java 21  
**Función:** Enrutamiento, autenticación y rate limiting

---

## 📚 Rutas Disponibles

El API Gateway redistribuye las solicitudes hacia los microservicios correspondientes:

| Ruta | Microservicio | Puerto | Descripción |
|------|------------------|--------|-------------|
| `/auth/**` | Auth Service | 8081 | Autenticación, registro y validación de usuarios |
| `/api/facturas/**` | Billing Service | 8082 | Gestión de facturas y cálculo de tarifas |
| `/api/vehicles/**` | Fleet Service | 8083 | Gestión de vehículos y repartidores |
| `/api/pedidos/**` | Pedido Service | 8084 | Creación y seguimiento de pedidos |

---

## 🔐 Características de Seguridad

### Autenticación y Autorización
- ✅ **Validación de JWT Tokens** - Verifica la identidad del usuario
- ✅ **Role-Based Access Control (RBAC)** - Control de acceso basado en roles
- ✅ **Token Refresh** - Renovación automática de tokens

### Protección y Limitación
- ✅ **Rate Limiting** - Limita solicitudes por IP/usuario para evitar abuso
- ✅ **CORS Configuration** - Control de origen cruzado
- ✅ **Request/Response Logging** - Auditoría de solicitudes

### Encriptación
- ✅ **Encriptación de Contraseñas** - BCrypt con 10+ salts rounds
- ✅ **HTTPS Ready** - Preparado para comunicación segura

---

## 🏗️ Flujo de Solicitud

```
┌─────────────────────────────────────────────────────────────┐
│                  CLIENTE EXTERNO                             │
│                  (Web, Mobile, etc)                          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ HTTP Request
                     ↓
┌─────────────────────────────────────────────────────────────┐
│                 API GATEWAY (8080)                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 1. Validar formato de solicitud                      │   │
│  │ 2. Extraer y validar token JWT                       │   │
│  │ 3. Verificar permisos del usuario (RBAC)             │   │
│  │ 4. Aplicar rate limiting                             │   │
│  │ 5. Enrutar a microservicio correspondiente            │   │
│  │ 6. Registrar en log de auditoría                      │   │
│  └──────────────────────────────────────────────────────┘   │
└────┬────────────┬─────────────┬──────────────┬────────────┘
     │            │             │              │
     │ /auth/**   │ /api/facturas/| /api/vehicles/| /api/pedidos/
     │            │             │              │
     ↓            ↓             ↓              ↓
  Auth Service Billing Service Fleet Service Pedido Service
   (8081)         (8082)        (8083)         (8084)
```

---

## 🚀 Cómo Iniciar

### Opción 1: Con Docker Compose (Recomendado)
```bash
cd logiflow/billing-service
docker-compose up -d
```

### Opción 2: Ejecución Manual
```bash
cd logiflow/api-gateway
mvn spring-boot:run
```

El API Gateway estará disponible en **http://localhost:8080**

---

## 📋 Roles de Control de Acceso

El API Gateway valida los siguientes roles definidos en Auth Service:

| Rol | Permisos |
|-----|----------|
| `ADMIN` | Acceso a todos los endpoints |
| `USER` | Acceso a endpoints de usuario estándar |
| `REPARTIDOR` | Acceso a endpoints de entrega y asignaciones |
| `CLIENTE` | Acceso a endpoints de creación y seguimiento de pedidos |

---

## 🔍 Monitoreo y Logs

El API Gateway registra:
- ✅ Todas las solicitudes entrantes
- ✅ Validaciones de token exitosas/fallidas
- ✅ Violaciones de rate limiting
- ✅ Errores de enrutamiento
- ✅ Tiempos de respuesta de microservicios

---

## 🔗 Integración con Otros Servicios

El API Gateway actúa como intermediario entre clientes externos y los microservicios internos:

```
Cliente → API Gateway → {Auth|Billing|Fleet|Pedido} Service
           ↓
    - Autentica usuario (si /auth/**)
    - Valida token (otros endpoints)
    - Aplica rate limiting
    - Enruta a servicio
    - Retorna respuesta
```

---

## 📖 Documentación Adicional

Para más información sobre cada microservicio:
- 🔐 [Auth Service](../auth-service/README.md)
- 💳 [Billing Service](../billing-service/README.md)
- 🚗 [Fleet Service](../fleet-service/README.md)
- 📦 [Pedido Service](../pedido-service/README.md)

---

<div align="center">

**API Gateway** • Puerto 8080 • Spring Boot 4.0.0 • Java 21

</div>
