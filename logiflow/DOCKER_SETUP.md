# 🐳 Docker Compose Setup - EntregaExpress

## ⚠️ Situación Actual

El `docker-compose.yml` en la raíz ha sido actualizado con las siguientes mejoras:

1. **Rutas de Microservicios Corregidas**
   - Billing Service: `/billing/**` → `/api/**`
   - Pedido Service: `/pedido/**` → `/api/**`
   - Fleet Service: `/fleet/**` → `/api/**`

2. **URLs de Servicios en Docker**
   - Auth Service: `http://authservice:8081/api/auth`
   - Billing Service: `http://billing-service:8082/api`
   - Fleet Service: `http://fleet-service:8083/api`
   - Pedido Service: `http://pedido-service:8084/api`
   - Tracking Service: `http://tracking-service:8090`

3. **Configuración de Healthchecks**
   - Simplificados para ser más robustos
   - `start_period` aumentado a 120-180 segundos
   - Dependencias sin `condition: service_healthy` (usando `service_started`)

---

## 🚀 Cómo Ejecutar

### Opción 1: Docker Compose (Recomendado)

```bash
# Desde la carpeta raíz del proyecto (logiflow)
cd c:\Users\DELL\Documents\7moSemestre\Distribuidas\Parcial\ 2\Proyecto\ 2\ Parcial\EntregaExpress_P2\logiflow

# Iniciar todos los servicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f authservice
docker-compose logs -f billing-service
docker-compose logs -f fleet-service
docker-compose logs -f pedido-service
docker-compose logs -f delivery-graphql-service
docker-compose logs -f api-gateway
```

### Opción 2: Ejecución Manual (Sin Docker)

Si prefieres ejecutar manualmente sin Docker, abre 6 terminales diferentes:

```bash
# Terminal 1 - Auth Service (Puerto 8081)
cd authservice
mvn spring-boot:run

# Terminal 2 - Billing Service (Puerto 8082)
cd billing-service
mvn spring-boot:run

# Terminal 3 - Fleet Service (Puerto 8083)
cd fleet-service
mvn spring-boot:run

# Terminal 4 - Pedido Service (Puerto 8084)
cd pedido-service
mvn spring-boot:run

# Terminal 5 - API Gateway (Puerto 8000)
cd api-gateway
mvn spring-boot:run

# Terminal 6 - Delivery GraphQL Service (Puerto 4000)
cd delivery-graphql-service
npm install && npm run dev
```

---

## 📝 Puertos Disponibles

### Base de Datos PostgreSQL
- **Auth Service DB**: localhost:5432 (usuario: admin)
- **Billing Service DB**: localhost:5433 (usuario: billing)
- **Fleet Service DB**: localhost:5435 (usuario: fleet_user)
- **Pedido Service DB**: localhost:5436 (usuario: pedido_user)
- **Notifications DB**: localhost:5437 (usuario: parkin)

### Microservicios Java
- **Api Gateway**: http://localhost:8000
- **Auth Service**: http://localhost:8081
- **Billing Service**: http://localhost:8082
- **Fleet Service**: http://localhost:8083 (context-path: /api)
- **Pedido Service**: http://localhost:8084
- **Tracking Service**: http://localhost:8090
- **Notifications**: http://localhost:8085

### Otros Servicios
- **Delivery GraphQL Service**: http://localhost:4000/graphql
- **RabbitMQ Management**: http://localhost:15672 (usuario: admin/admin)

---

## 🧪 Verificar que todo funciona

### 1️⃣ Esperar a que todos los servicios estén listos (2-3 minutos)
```bash
# Ver estado de los contenedores
docker-compose ps
```

esperaste a que todos escriban un mensaje como:
- `authservice` → "Started AuthServiceApplication"
- `billing-service` → "Started BillingServiceApplication"
- `fleet-service` → "Started FleetServiceApplication"
- `pedido-service` → "Started PedidoServiceApplication"
- `delivery-graphql-service` → "Server running at http://localhost:4000/graphql"

### 2️⃣ Probar Autenticación

```bash
# LOGIN - Obtener JWT Token
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Respuesta esperada:
# {
#   "accessToken": "eyJhbGc...",
#   "username": "admin",
#   "email": "admin@logiflow.com",
#   "roles": ["ADMINISTRADOR_SISTEMA"]
# }
```

### 3️⃣ Probar Rutas del API Gateway

```bash
# Obtener un token primero (ver paso anterior)
TOKEN="eyJhbGc..."

# Probar Billing Service a través del Gateway
curl -X GET http://localhost:8000/billing/facturas \
  -H "Authorization: Bearer $TOKEN"

# Probar Pedido Service a través del Gateway
curl -X GET http://localhost:8000/pedido/pedidos \
 -H "Authorization: Bearer $TOKEN"

# Probar Fleet Service a través del Gateway
curl -X GET http://localhost:8000/fleet/vehiculos \
  -H "Authorization: Bearer $TOKEN"
```

### 4️⃣ Probar GraphQL Service

```bash
curl -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ obtenerPedidos { id cliente estado } }"
  }'
```

---

## 🛑 Detener los Servicios

```bash
# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes (limpia bases de datos)
docker-compose down -v

# Ver servicios que están corriendo
docker-compose ps
```

---

## 🐛 Troubleshooting

### Error: ECONNREFUSED 192.168.112.9:8081

**Causa**: El servicio `authservice` aún no ha inicializado completamente cuando el `delivery-graphql-service` intenta conectarse.

**Solución**:
1. Espera 2-3 minutos después de ejecutar `docker-compose up`
2. Revisa los logs del authservice: `docker-compose logs authservice`
3. Si ves errores de base de datos, verifica que la BD está sana: `docker ps`

### Error: pg_isready failed

**Causa**: Las bases de datos PostgreSQL están tardando en iniciar.

**Solución**:
```bash
# Espera más tiempo
docker-compose up -d
sleep 60  # Espera 60 segundos
docker-compose ps
```

### El delivery-graphql-service sigue fallando

**Causa**: Node.js no puede conectar a los servicios Java.

**Solución**:
1. Verifica que `authservice` esté completamente iniciado:
   ```bash
   docker-compose logs authservice | grep "Started"
   ```

2. **Ejecutalo manualmente sin Docker** (más fácil para debugging):
   ```bash
   cd delivery-graphql-service
   npm install
   npm run dev
   ```

3. Si eso tampoco funciona, revisa las URLs en `src/utils/config.ts`

---

## 📚 Documentación de Cada Servicio

Ver los README individuales para más detalles:
- [Auth Service](./authservice/README.md)
- [Billing Service](./billing-service/README.md)
- [Fleet Service](./fleet-service/README.md)
- [Pedido Service](./pedido-service/README.md)
- [API Gateway](./api-gateway/README.md)

---

## 🎯 Estado Actual

✅ **Docker Compose Configurado Correctamente:**
- Todas las URLs de microservicios están actualizadas
- Puertos mapeados correctamente
- Healthchecks simplificados
- Dependencias entre servicios definidas

🔄 **Próximos Pasos:**
1. Ejecutar `docker-compose up -d`
2. Esperar 2-3 minutos
3. Verificar logs: `docker-compose logs -f`
4. Probar endpoints con curl (ver sección "Verificar que todo funciona")

---

**Última actualización**: Febrero 8, 2026
