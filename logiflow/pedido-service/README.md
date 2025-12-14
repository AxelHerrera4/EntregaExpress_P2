# Pedido Service

Microservicio de gestión de pedidos para el sistema LogiFlow de entregas express.

## 📋 Descripción

El **Pedido Service** es un microservicio REST desarrollado con Spring Boot que gestiona el ciclo de vida completo de los pedidos de entrega. Permite crear, consultar, actualizar y cancelar pedidos con diferentes modalidades de servicio (urbana rápida, intermunicipal y nacional) y tipos de entrega (express, normal y programada).

### Características principales

- ✅ Gestión completa de pedidos (CRUD)
- ✅ Validación automática de datos de entrada
- ✅ Soporte para múltiples modalidades de servicio
- ✅ Estados de pedido en tiempo real
- ✅ Integración con PostgreSQL
- ✅ Documentación API con Swagger/OpenAPI
- ✅ Seguridad con Spring Security
- ✅ Logging detallado con SLF4J

## 🏗️ Arquitectura

### Tecnologías utilizadas

- **Java 21**
- **Spring Boot 4.0.0**
- **Spring Data JPA** - Persistencia de datos
- **Spring Security** - Seguridad
- **Spring Validation** - Validación de datos
- **PostgreSQL** - Base de datos
- **Lombok** - Reducción de código boilerplate
- **SpringDoc OpenAPI** - Documentación de la API

### Estructura del proyecto

```
pedido-service/
├── src/
│   ├── main/
│   │   ├── java/com/logiflow/pedidoservice/
│   │   │   ├── config/          # Configuraciones
│   │   │   ├── controller/      # Controladores REST
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # Manejo de excepciones
│   │   │   ├── model/           # Entidades JPA
│   │   │   ├── repository/      # Repositorios JPA
│   │   │   ├── service/         # Lógica de negocio
│   │   │   └── PedidoServiceApplication.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── static/openapi/
│   └── test/                    # Tests unitarios
├── docker-compose.yaml
├── pom.xml
└── README.md
```

## 🚀 Guía de instalación y ejecución

### Requisitos previos

- ☑️ **Java 21** o superior
- ☑️ **Maven 3.9+** (incluido en el wrapper)
- ☑️ **Docker** y **Docker Compose** (para la base de datos)
- ☑️ **PostgreSQL** (opcional si no usas Docker)

### Paso 1: Levantar la base de datos

El servicio requiere PostgreSQL. Puedes levantar una instancia con Docker Compose:

```powershell
# Desde el directorio pedido-service
cd D:\EntregaExpress_P2\logiflow\pedido-service
docker-compose up -d
```

Esto creará:
- **Contenedor**: `pedido_db`
- **Base de datos**: `pedidos_db`
- **Usuario**: `pedido_user`
- **Contraseña**: `pedido_pass`
- **Puerto**: `5433` (mapeado al 5432 interno)

Para verificar que el contenedor está corriendo:

```powershell
docker ps
```

### Paso 2: Compilar el proyecto

```powershell
# Usando Maven Wrapper (recomendado)
.\mvnw.cmd clean install

# O con Maven global
mvn clean install
```

### Paso 3: Ejecutar el servicio

```powershell
# Usando Maven Wrapper
.\mvnw.cmd spring-boot:run

# O con el JAR compilado
java -jar target/pedido-service-0.0.1-SNAPSHOT.jar
```

El servicio estará disponible en: **http://localhost:8084**

### Paso 4: Verificar que el servicio está corriendo

```powershell
# Verificar el estado
curl http://localhost:8084/actuator/health

# O abrir en el navegador
start http://localhost:8084/swagger-ui.html
```

## 📚 Documentación de la API

### Swagger UI

Una vez iniciado el servicio, accede a la documentación interactiva:

**URL**: http://localhost:8084/swagger-ui.html

### Endpoints principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/pedidos` | Crear un nuevo pedido |
| GET | `/api/pedidos/{id}` | Obtener pedido por ID |
| GET | `/api/pedidos` | Listar todos los pedidos |
| GET | `/api/pedidos/cliente/{clienteId}` | Listar pedidos de un cliente |
| PATCH | `/api/pedidos/{id}` | Actualizar parcialmente un pedido |
| PATCH | `/api/pedidos/{id}/cancelar` | Cancelar un pedido |
| DELETE | `/api/pedidos/{id}` | Eliminar un pedido |

## 💡 Ejemplos de uso

### Crear un pedido (POST /api/pedidos)

```powershell
curl -X 'POST' `
  'http://localhost:8084/api/pedidos' `
  -H 'accept: application/json' `
  -H 'Content-Type: application/json' `
  -d '{
  "clienteId": "cli-12345",
  "direccionOrigen": {
    "calle": "Av. Principal",
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
}'
```

#### Valores válidos para enums:

**modalidadServicio:**
- `URBANA_RAPIDA` - Última milla con motorizados
- `INTERMUNICIPAL` - Dentro de la provincia con vehículos livianos
- `NACIONAL` - Nacional con furgonetas/camiones

**tipoEntrega:**
- `EXPRESS` - Entrega rápida (mismo día)
- `NORMAL` - Entrega estándar (1-3 días)
- `PROGRAMADA` - Entrega programada (fecha específica)

#### Validaciones:

- `clienteId`: Obligatorio, no puede estar vacío
- `direccionOrigen` y `direccionDestino`: Obligatorios
  - `calle`: Texto obligatorio
  - `numero`: Texto obligatorio (puede ser alfanumérico)
  - `ciudad`: Texto obligatorio
  - `provincia`: Texto obligatorio
- `modalidadServicio`: Obligatorio (URBANA_RAPIDA, INTERMUNICIPAL, NACIONAL)
- `tipoEntrega`: Obligatorio (EXPRESS, NORMAL, PROGRAMADA)
- `peso`: Obligatorio, debe ser mayor a 0
- `telefonoContacto`: Obligatorio, 7-15 dígitos numéricos
- `nombreDestinatario`: Opcional

### Obtener un pedido (GET /api/pedidos/{id})

```powershell
curl -X 'GET' `
  'http://localhost:8084/api/pedidos/abc123' `
  -H 'accept: application/json'
```

### Listar todos los pedidos (GET /api/pedidos)

```powershell
curl -X 'GET' `
  'http://localhost:8084/api/pedidos' `
  -H 'accept: application/json'
```

### Obtener pedidos por cliente (GET /api/pedidos/cliente/{clienteId})

```powershell
curl -X 'GET' `
  'http://localhost:8084/api/pedidos/cliente/cli-12345' `
  -H 'accept: application/json'
```

### Actualizar un pedido (PATCH /api/pedidos/{id})

```powershell
curl -X 'PATCH' `
  'http://localhost:8084/api/pedidos/abc123' `
  -H 'accept: application/json' `
  -H 'Content-Type: application/json' `
  -d '{
  "estado": "EN_TRANSITO",
  "repartidorId": "rep-001"
}'
```

#### Estados de pedido disponibles:

- `PENDIENTE` - Creado, esperando asignación
- `ASIGNADO` - Repartidor y vehículo asignados
- `EN_PREPARACION` - En proceso de preparación
- `EN_TRANSITO` - En camino al destino
- `EN_DISTRIBUCION` - En punto de distribución
- `ENTREGADO` - Entregado exitosamente
- `FALLIDO` - Intento de entrega fallido
- `CANCELADO` - Cancelado por cliente o sistema
- `DEVUELTO` - Devuelto al remitente

### Cancelar un pedido (PATCH /api/pedidos/{id}/cancelar)

```powershell
curl -X 'PATCH' `
  'http://localhost:8084/api/pedidos/abc123/cancelar' `
  -H 'accept: application/json'
```

### Eliminar un pedido (DELETE /api/pedidos/{id})

```powershell
curl -X 'DELETE' `
  'http://localhost:8084/api/pedidos/abc123' `
  -H 'accept: application/json'
```

## ⚙️ Configuración

### Variables de entorno

Puedes configurar el servicio mediante variables de entorno:

```powershell
# Base de datos
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/pedidos_db"
$env:SPRING_DATASOURCE_USERNAME="pedido_user"
$env:SPRING_DATASOURCE_PASSWORD="pedido_pass"

# Ejecutar el servicio
.\mvnw.cmd spring-boot:run
```

### Archivo application.yaml

El archivo de configuración está en `src/main/resources/application.yaml`:

```yaml
server:
  port: 8084

spring:
  application:
    name: pedido-service
  
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5433/pedidos_db}
    username: ${SPRING_DATASOURCE_USERNAME:pedido_user}
    password: ${SPRING_DATASOURCE_PASSWORD:pedido_pass}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## 🐳 Docker

### Dockerfile del servicio

El servicio incluye un `Dockerfile` optimizado con multi-stage build:
- **Etapa Build**: Maven + JDK 21 para compilar
- **Etapa Runtime**: JRE 21 Alpine (imagen ligera ~200-250 MB)
- **Seguridad**: Se ejecuta con usuario no-root
- **Health check**: Monitoreo automático del estado

### Construir la imagen del servicio

```powershell
# Construcción básica
docker build -t pedido-service:latest .
```

### Ejecutar el contenedor del servicio

```powershell
# Modo standalone (requiere BD externa)
docker run -d `
  --name pedido-service `
  -p 8084:8084 `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/pedidos_db `
  -e SPRING_DATASOURCE_USERNAME=pedido_user `
  -e SPRING_DATASOURCE_PASSWORD=pedido_pass `
  -e BILLING_SERVICE_URL=http://host.docker.internal:8082 `
  pedido-service:latest

# Ver logs
docker logs -f pedido-service

# Detener y eliminar
docker stop pedido-service
docker rm pedido-service
```

**📖 Para más detalles sobre Docker, consulta [DOCKER.md](DOCKER.md)**

### Gestionar la base de datos con Docker Compose

```powershell
# Iniciar la base de datos
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener la base de datos
docker-compose down

# Detener y eliminar volúmenes (borra los datos)
docker-compose down -v
```

### Conectarse a la base de datos

```powershell
# Con Docker
docker exec -it pedido_db psql -U pedido_user -d pedidos_db

# Con cliente PostgreSQL
psql -h localhost -p 5433 -U pedido_user -d pedidos_db
```

## 🧪 Testing

### Ejecutar tests

```powershell
# Ejecutar todos los tests
.\mvnw.cmd test

# Ejecutar tests con cobertura
.\mvnw.cmd verify
```

## 🔍 Troubleshooting

### Error: "Connection refused" o "Could not connect to database"

**Solución**: Verifica que PostgreSQL esté corriendo:
```powershell
docker ps
docker-compose up -d
```

### Error: "Port 8084 already in use"

**Solución**: Cambia el puerto en `application.yaml` o detén el proceso que usa el puerto:
```powershell
netstat -ano | findstr :8084
taskkill /PID <PID> /F
```

### Error: "Table does not exist"

**Solución**: Verifica que `ddl-auto` esté en `update` en `application.yaml`. Spring creará las tablas automáticamente.

### Error 500: "Internal Server Error"

**Solución**: Revisa los logs del servicio para ver el error específico:
```powershell
# Los logs aparecen en la consola donde ejecutaste el servicio
# Busca líneas con "ERROR" o "Exception"
```

Posibles causas:
- Base de datos no disponible
- Datos inválidos en el request
- Errores de validación

## 📊 Modelo de datos

### Entidad Pedido

```
Pedido
├── id (UUID)
├── clienteId (String)
├── direccionOrigen (Direccion embebida)
├── direccionDestino (Direccion embebida)
├── modalidadServicio (ENUM)
├── tipoEntrega (ENUM)
├── estado (ENUM)
├── peso (Double)
├── volumen (Double - opcional)
├── cobertura (String)
├── descripcion (String)
├── repartidorId (String)
├── vehiculoId (String)
├── prioridad (ENUM)
├── telefonoContacto (String)
├── nombreDestinatario (String)
├── fechaCreacion (LocalDateTime)
├── fechaActualizacion (LocalDateTime)
└── fechaEntregaEstimada (LocalDateTime)
```

## 🔐 Seguridad

El servicio utiliza Spring Security. Para desarrollo, puedes desactivar la seguridad o configurar usuarios básicos en `SecurityConfig`.

## 📝 Logging

Los logs se muestran en la consola con el siguiente formato:

```
2025-12-14 10:30:45 INFO  PedidoController - POST /api/pedidos - Pedido creado exitosamente con ID: abc123
```

## 🤝 Integración con otros servicios

El Pedido Service está diseñado para integrarse con:

- **Fleet Service**: Asignación de repartidores y vehículos
- **Billing Service**: Cálculo de tarifas
- **Auth Service**: Autenticación de usuarios
- **API Gateway**: Punto de entrada unificado

## 📄 Licencia

Este proyecto es parte del sistema LogiFlow - EntregaExpress P2.

## 👥 Contacto

Para soporte o consultas sobre este microservicio, consulta la documentación del proyecto principal.

---

**Última actualización**: Diciembre 2025
**Versión**: 0.0.1-SNAPSHOT

