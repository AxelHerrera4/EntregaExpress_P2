# 💳 Billing Service

**Servicio de Gestión de Facturación y Cálculo de Tarifas**

Servicio encargado de la **gestión de facturación**, cálculo dinámico de tarifas según el tipo de entrega, y administración del estado de facturas. Es el corazón financiero del sistema de logística.

**Puerto:** 8082 | **Versión:** 1.0 | **Estado:** ✅ Producción Ready

---

## ⚙️ Configuración Técnica

### Base de Datos

| Propiedad | Valor |
|-----------|-------|
| **Motor** | PostgreSQL |
| **Host** | `localhost` |
| **Puerto** | `5433` |
| **Base de datos** | `db_billing_users` |
| **Usuario** | `billing` |
| **Contraseña** | `qwerty123` |

### Stack Tecnológico

- **Java 21**
- **Spring Boot 4.0.0**
- **Spring Data JPA** (ORM)
- **Spring Security** (Autenticación)
- **Spring Validation** (Validación de datos)
- **SpringDoc OpenAPI** (Swagger/documentación)
- **Lombok** (Reducción de código boilerplate)

---

## 📚 API Endpoints

### Gestión de Facturas (`/api/facturas`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/facturas` | Crear factura |
| `GET` | `/api/facturas` | Listar todas las facturas |
| `GET` | `/api/facturas/{id}` | Obtener factura por ID |
| `PATCH` | `/api/facturas/{id}/estado` | Actualizar estado de factura |

**Estados disponibles:**
- 📝 **BORRADOR** - Recién creada
- ⏳ **PENDIENTE** - Esperando pago
- ✅ **PAGADA** - Pagada correctamente
- ❌ **CANCELADA** - Cancelada

### Gestión de Tarifas Base (`/api/tarifas-base`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/tarifas-base` | Crear tarifa |
| `GET` | `/api/tarifas-base` | Listar tarifas |
| `GET` | `/api/tarifas-base/{id}` | Obtener tarifa por ID |
| `PUT` | `/api/tarifas-base/{id}` | Actualizar tarifa |

**Tipos de entrega soportados:**
- **URBANA** - Entregas dentro de la ciudad
- **INTERMUNICIPAL** - Entregas entre municipios
- **NACIONAL** - Entregas a nivel nacional

---

## 🎨 Patrones de Diseño Implementados

### 1️⃣ Patrón Strategy (Cálculo de Tarifas)

El patrón **Strategy** implementa diferentes algoritmos de cálculo de tarifas, permitiendo cambiar el comportamiento en tiempo de ejecución según el tipo de entrega.

**Estrategias implementadas:**

| Estrategia | Fórmula | Uso |
|-----------|---------|-----|
| **TarifaUrbanaStrategy** | Base + (0.5 × km) | Entregas urbanas |
| **TarifaIntermunicipalStrategy** | Base + (1.0 × km) | Entregas entre municipios |
| **TarifaNacionalStrategy** | Base + (1.5 × km) | Entregas nacionales |
| **DefaultTarifaStrategy** | Base + (0.8 × km) | Tipos no clasificados |

**Interfaz:**
```java
public interface TarifaStrategy {
    BigDecimal calcularTarifa(TarifaBase tarifaBase, Double distanciaKm);
}
```

**Ejemplo de uso:**
```java
// La estrategia se selecciona automáticamente según tipoEntrega
TarifaStrategy strategy = factory.obtenerStrategy("URBANA");
BigDecimal montoTotal = strategy.calcularTarifa(tarifa, 15.5);
// Resultado: 5.00 + (0.5 × 15.5) = $12.75
```

### 2️⃣ Patrón Factory (Selección de Estrategias)

El patrón **Factory** encapsula la lógica de creación de estrategias, proporcionando un punto centralizado para obtener la instancia correcta.

**Clase:**
```java
@Component
public class TarifaStrategyFactory {
    
    public TarifaStrategy obtenerStrategy(String tipoEntrega) {
        return switch (tipoEntrega.toUpperCase()) {
            case "URBANA" -> urbanaStrategy;
            case "INTERMUNICIPAL" -> intermunicipalStrategy;
            case "NACIONAL" -> nacionalStrategy;
            default -> defaultTarifaStrategy;
        };
    }
}
```

**Ventajas:**
- Centralización de lógica de selección
- Fácil mantenimiento y extensión
- Desacoplamiento de componentes

---

## 📋 Diagrama Entidad-Relación (ER)

**Base de Datos:** `db_billing_users` • **Puerto:** 5433 • **Usuario:** billing / **Contraseña:** qwerty123

```
        ╔═════════════════════════════════════╗
        ║       tarifas_base                  ║
        ╠═════════════════════════════════════╣
        ║ id                    UUID [PK]     ║
        ║ tipo_entrega          VARCHAR(50)   ║ ← [UNIQUE]
        ║                       (ENUM)        ║
        ║ tarifa_base           DECIMAL(10,2) ║
        ║ created_at            TIMESTAMP     ║
        ║ updated_at            TIMESTAMP     ║
        ╚═════════════════════════════════════╝
                        △
                        │
                        │ 1 Tarifa
                        │ N Facturas
                        │
        ╔══════════════════════════════════════════════════════╗
        ║                 facturas                             ║
        ╠══════════════════════════════════════════════════════╣
        ║ id                     UUID [PK]                     ║
        ║ pedido_id              VARCHAR(50) [UQ]  (Ext. Ref) ║
        ║ tipo_entrega           VARCHAR(50) [FK] ────────┐   ║
        ║ monto_total            DECIMAL(12,2) [NOT NULL]│   ║
        ║ estado                 VARCHAR(20) [DEFAULT]    │   ║
        ║                        (ENUM)                   │   ║
        ║ distancia_km           NUMERIC(8,2)             │   ║
        ║ created_at             TIMESTAMP [NOT NULL]     │   ║
        ║ updated_at             TIMESTAMP [NOT NULL]     │   ║
        ╚══════════════════════════════════════════════════════╝
                                             │
                                        (referencia a
                                      tarifas_base)

ENUMERADOS TIPO ENTREGA:
┌──────────────────────────────────────────────────┐
│ Valor              │ Factor de Cálculo          │
├──────────────────────────────────────────────────┤
│ URBANA             │ 0.5× (tarifa_base)        │
│ INTERMUNICIPAL     │ 1.0× (tarifa_base)        │
│ NACIONAL           │ 1.5× (tarifa_base)        │
│ DEFAULT            │ 0.8× (tarifa_base)        │
└──────────────────────────────────────────────────┘

ENUMERADOS ESTADO FACTURA:
┌──────────────────────────────────────────────────┐
│ Valor              │ Descripción                │
├──────────────────────────────────────────────────┤
│ BORRADOR           │ Recién creada              │
│ PENDIENTE          │ Esperando pago             │
│ PAGADA             │ Pagada completamente      │
│ CANCELADA          │ Cancelada/Anulada         │
└──────────────────────────────────────────────────┘

ÍNDICES PARA OPTIMIZACIÓN:
  ✓ CREATE UNIQUE INDEX idx_tarifas_tipo ON tarifas_base(tipo_entrega);
  ✓ CREATE UNIQUE INDEX idx_facturas_pedido ON facturas(pedido_id);
  ✓ CREATE INDEX idx_facturas_estado ON facturas(estado);
  ✓ CREATE INDEX idx_facturas_created ON facturas(created_at DESC);
  ✓ CREATE INDEX idx_facturas_tipo_entrega ON facturas(tipo_entrega);

VOLUMEN DE DATOS ESTIMADO:
  • Tarifas Base:  ~5-10 registros (< 1KB)
  • Facturas:      ~50,000-100,000 registros (≈ 2-5MB)
  • Total BD:      ≈ 5-10MB con índices
```

---

## 🚀 Ejecución con Docker Compose

El Billing Service incluye un `docker-compose.yaml` que automatiza el levantamiento del servicio y su base de datos PostgreSQL.

### 📋 Requisitos Previos

Antes de ejecutar el docker-compose, asegúrate de tener:
- ✅ **Docker** instalado y ejecutándose
- ✅ **Docker Compose** instalado (generalmente viene con Docker Desktop)
- ✅ **Puertos disponibles:** 8082 (aplicación) y 5433 (base de datos)

### 🚀 Pasos para Ejecutar Docker Compose

#### Paso 1️: Navegar al Directorio del Billing Service

Abre una terminal (PowerShell, CMD, o Bash) y navega a la carpeta del billing-service:

```bash
cd logiflow/billing-service
```

Verifica que ves el archivo `docker-compose.yaml`:

```bash
# En Windows (PowerShell)
Get-ChildItem | Select-Object Name

# O en CMD/Bash
dir  # CMD
ls   # Bash/PowerShell
```

Deberías ver:
```
docker-compose.yaml
Dockerfile
pom.xml
src/
...
```

#### Paso 2️: Construir la Imagen Docker

Primero, construye la imagen Docker del servicio:

```bash
docker-compose build
```

**Salida esperada:**
```
[+] Building 45.2s (14/14) FINISHED
 => [postgres internal] load build definition from Dockerfile
 => [billing-service] writing image sha256:abc123...
```

> ⏱️ **Nota:** La primera construcción puede tardar 2-5 minutos mientras descarga dependencias de Maven.

**Solución de problemas:**
- Si falla: Asegúrate de tener Docker ejecutándose
- Si falla por puerto en uso: Cambia los puertos en `docker-compose.yaml`

#### Paso 3️: Iniciar los Contenedores

Levanta tanto la base de datos como el servicio con un solo comando:

```bash
docker-compose up -d
```

**Parámetros:**
- `up` - Inicia los servicios definidos
- `-d` - Ejecuta en modo "detached" (background)

**Salida esperada:**
```
[+] Running 2/2
 ✔ Container billing_db    Started
 ✔ Container billing_app   Started
```

##### ✅ Verificar que los Contenedores Están Corriendo

```bash
docker ps
```

Deberías ver dos contenedores:
```
CONTAINER ID   IMAGE                    PORTS                    NAMES
abc123def456   billing-service:latest   0.0.0.0:8082->8082/tcp   billing_app
def789ghi012   postgres:16-alpine       0.0.0.0:5433->5432/tcp   billing_db
```

##### ⏳ Esperar a que PostgreSQL Esté Listo

A veces PostgreSQL tarda unos segundos en estar completamente disponible. Verifica los logs:

```bash
docker-compose logs -f postgres
```

Espera hasta ver este mensaje:
```
database system is ready to accept connections
```

Presiona `Ctrl+C` para salir de los logs.

#### Paso 4️: Verificar Conexión a PostgreSQL

Asegúrate de que PostgreSQL está corriendo correctamente:

```bash
# Verificar si PostgreSQL está ejecutándose
psql -h localhost -p 5433 -U billing -d db_billing_users
```

Credenciales de conexión:
```
Host: localhost
Puerto: 5433
Usuario: billing
Contraseña: qwerty123
Base de datos: db_billing_users
```

**Si tienes `psql` instalado:**
```sql
-- Una vez conectado, ejecuta:
\dt  -- Mostrar todas las tablas creadas
\q   -- Salir
```

**Si no tienes `psql`, verifica con Docker:**
```bash
docker exec -it billing_db psql -U billing -d db_billing_users -c "\dt"
```

Deberías ver las tablas creadas automáticamente por Spring Boot:
```
 public | factura        | table | billing
 public | tarifa_base    | table | billing
 public | flyway_...     | table | billing
```

#### Paso 5️: Verificar que la Aplicación Está Corriendo

Consulta los logs del servicio:

```bash
docker logs -f billing_app
```

**Salida esperada:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/

Started BillingServiceApplication in 8.234 seconds
```

Presiona `Ctrl+C` para salir de los logs.

##### ✅ Verificación Rápida

Abre tu navegador o usa `curl` para verificar que el servicio responde:

```bash
curl http://localhost:8082/swagger-ui.html
```

O simplemente abre: **http://localhost:8082/swagger-ui.html** en tu navegador.

Deberías ver la documentación Swagger del Billing Service.

---

## 📖 Guía de Uso Paso a Paso

### Paso 6️: Iniciar el Servicio Manualmente (sin Docker)

Si prefieres no usar Docker, puedes iniciar el servicio directamente:

Navega a la carpeta del billing-service:

```bash
cd logiflow/billing-service
```

Inicia con Maven (Linux/Mac):
```bash
./mvnw spring-boot:run
```

O en Windows:
```bash
mvnw.cmd spring-boot:run
```

El servicio estará disponible en: **`http://localhost:8082`**

Verifica que se inició correctamente viendo este mensaje en los logs:
```
Started BillingServiceApplication in X seconds
```

### Paso 7️: Crear una Tarifa Base

Realiza una petición **POST** a `/api/tarifas-base`:

```bash
curl -X POST http://localhost:8082/api/tarifas-base \
  -H "Content-Type: application/json" \
  -d '{
  "tipoEntrega": "Multinacional",
  "tarifaBase": 5
      }'
```

**Respuesta exitosa (201 Created):**
```json
{
    "id": "8d7f67cd-573a-4625-a743-00f7cd15cd6b",
    "tipoEntrega": "MULTINACIONAL",
    "tarifaBase": 5
}
```

### Paso 8️: Crear una Factura

Realiza una petición **POST** a `/api/facturas`:

```bash
curl -X POST http://localhost:8082/api/facturas \
  -H "Content-Type: application/json" \
  -d '{
  "pedidoId":101210,
  "tipoEntrega": "Nacional",
  "distanciaKm": 55
}'
```

**Proceso interno en la aplicación:**
1. Obtiene la tarifa base para tipo "URBANA" → $5.00
2. El `TarifaStrategyFactory` selecciona `TarifaUrbanaStrategy`
3. La estrategia calcula: `5.00 + (0.5 × 15.5) = $12.75`
4. Crea la factura con estado **BORRADOR**

**Respuesta exitosa (201 Created):**
```json
{
    "id": "9b6da0ad-a599-4145-aa33-fc3e8c85faef",
    "pedidoId": 101210,
    "tipoEntrega": "Nacional",
    "montoTotal": 87.50,
    "estado": "BORRADOR",
    "fechaCreacion": "2025-12-13T17:25:56.5310398",
    "distanciaKm": 55.0
}
```

### Paso 9️: Obtener una Factura

Para obtener los detalles de una factura específica:

```bash
curl -X GET http://localhost:8082/api/facturas/b575a85f-ad0b-4369-a639-d9172c85193d
```

**Respuesta (200 OK):**
```json
{
  "id": "b575a85f-ad0b-4369-a639-d9172c85193d",
  "pedidoId": 10110,
  "tipoEntrega": "URBANA",
   "montoTotal": 87.50,
    "estado": "BORRADOR",
    "fechaCreacion": "2025-12-13T17:25:56.53104",
    "distanciaKm": 55.0
}
```

### Paso 10️: Actualizar Estado de Factura

Para cambiar el estado de una factura, realiza una petición **PATCH**:

```bash
curl -X PATCH "http://localhost:8082/api/facturas/b575a85f-ad0b-4369-a639-d9172c85193d/estado?estado=PENDIENTE" \
  -H "Content-Type: application/json"
```

**Transiciones válidas de estado:**
```
BORRADOR ──> PENDIENTE ──> PAGADA
    └─────────────────────> CANCELADA
         
PENDIENTE ──> PAGADA
    └────────> CANCELADA
```

**Ejemplo de cambio a PAGADA:**
```bash
curl -X PATCH "http://localhost:8082/api/facturas/b575a85f-ad0b-4369-a639-d9172c85193d/estado?estado=PAGADA"
```

**Respuesta exitosa (200 OK):**
```json
{
  "id": "b575a85f-ad0b-4369-a639-d9172c85193d",
  "pedidoId": 10110,
  "tipoEntrega": "URBANA",
  "montoTotal": 12.75,
  "estado": "PAGADA",
  "distanciaKm": 15.5,
  "fechaCreacion": "2025-12-13T14:30:21"
}
```

### Paso 11️: Acceder a Documentación Swagger/OpenAPI

Una vez iniciado el servicio, accede a la documentación interactiva:

🌐 **URL:** `http://localhost:8082/swagger-ui.html`

**Características:**
- ✅ Ver todos los endpoints disponibles
- ✅ Probar endpoints directamente desde el navegador
- ✅ Ver esquemas de request/response
- ✅ Copiar ejemplos de curl
- ✅ Documentación de errores posibles

**Alternativas:**
- OpenAPI JSON: `http://localhost:8082/v3/api-docs`
- ReDoc (vista alternativa): `http://localhost:8082/swagger-ui/index.html`
- Documentación de pruebas unitarias Postman: `https://documenter.getpostman.com/view/41705034/2sB3dTrnW8`

---

## 🏗️ Estructura del Código

```
billing-service/
├── src/main/java/ec/edu/espe/billing_service/
│   ├── BillingServiceApplication.java      # Punto de entrada
│   ├── config/                             # Configuraciones
│   ├── controller/                         # Endpoints REST
│   │   ├── FacturaController.java
│   │   └── TarifaBaseController.java
│   ├── service/                            # Lógica de negocio
│   │   ├── FacturaService.java
│   │   ├── TarifaBaseService.java
│   │   └── impl/                           # Implementaciones
│   ├── repository/                         # Acceso a datos (JPA)
│   ├── model/
│   │   ├── entity/                         # Entidades JPA
│   │   ├── dto/                            # DTOs (request/response)
│   │   └── enums/                          # Enumeraciones
│   ├── factory/                            # Patrón Factory
│   │   └── TarifaStrategyFactory.java
│   └── strategy/                           # Patrón Strategy
│       ├── TarifaStrategy.java
│       ├── TarifaUrbanaStrategy.java
│       ├── TarifaIntermunicipalStrategy.java
│       ├── TarifaNacionalStrategy.java
│       └── DefaultTarifaStrategy.java
├── src/main/resources/
│   └── application.yaml                    # Configuración
├── pom.xml                                 # Dependencias Maven
└── mvnw / mvnw.cmd                        # Wrapper Maven
```

---

## 🛑 Detener y Limpiar los Contenedores

Cuando termines de trabajar, puedes detener los contenedores:

### Opción 1: Detener los Contenedores (sin eliminarlos)

```bash
docker-compose stop
```

**Ventaja:** Los datos se mantienen, puedes reiniciar rápidamente con `docker-compose start`

**Reiniciar:**
```bash
docker-compose start
```

### Opción 2: Eliminar los Contenedores (pero mantener datos)

```bash
docker-compose down
```

**Ventaja:** Libera más recursos que `stop`
**Nota:** Los datos persisten en el volumen `postgres_users_data_new`

**Reiniciar:**
```bash
docker-compose up -d
```

### Opción 3: Eliminar Todo (contenedores, volúmenes y datos)

```bash
docker-compose down -v
```

**Advertencia ⚠️:** Esto elimina la base de datos. Solo usa si quieres empezar de cero.

**Resultado:**
- ✓ Contenedores eliminados
- ✓ Volúmenes (datos) eliminados
- ✓ Redes eliminadas

---

## 📊 Monitoreo y Logs

### Ver Logs en Tiempo Real

**Todos los servicios:**
```bash
docker-compose logs -f
```

**Solo PostgreSQL:**
```bash
docker-compose logs -f postgres
```

**Solo Billing Service:**
```bash
docker-compose logs -f billing-service
```

**Últimas 50 líneas sin seguir:**
```bash
docker-compose logs --tail=50
```

### Verificar Estado de los Servicios

```bash
docker-compose ps
```

**Salida esperada:**
```
NAME                COMMAND                  SERVICE             STATUS              PORTS
billing_app         "java -jar /app/b..."    billing-service     Up About a minute   0.0.0.0:8082->8082/tcp
billing_db          "docker-entrypoint..."   postgres            Up About a minute   0.0.0.0:5433->5432/tcp
```

---

## 🔧 Troubleshooting Docker

### ❌ Error: "Port 8082 is already allocated"

**Problema:** Otro proceso está usando el puerto 8082.

**Soluciones:**

1. **Opción A: Usar otro puerto**
   
   Edita `docker-compose.yaml` y cambia:
   ```yaml
   services:
     billing-service:
       ports:
         - "8085:8082"  # Puerto local: 8085, puerto contenedor: 8082
   ```
   
   Luego accede a `http://localhost:8085`

2. **Opción B: Encontrar y detener el proceso**
   
   ```bash
   # En Windows (PowerShell)
   netstat -ano | findstr :8082
   
   # En Linux/Mac
   lsof -i :8082
   ```

### ❌ Error: "Cannot connect to the Docker daemon"

**Problema:** Docker no está ejecutándose.

**Solución:** 
1. Abre **Docker Desktop** (Windows/Mac)
2. En Linux, ejecuta: `sudo systemctl start docker`
3. Espera 30 segundos a que Docker inicie completamente
4. Intenta nuevamente con `docker ps`

### ❌ Error: "No such file or directory: 'docker-compose.yaml'"

**Problema:** No estás en la carpeta correcta.

**Solución:**
```bash
# Asegúrate de estar en la carpeta del billing-service
cd logiflow/billing-service

# Verifica que ves el archivo
dir | findstr docker-compose.yaml
```

### ❌ Error: "PostgreSQL connection refused"

**Problema:** PostgreSQL está iniciando pero aún no está listo.

**Solución:**
```bash
# Espera a que PostgreSQL esté listo
docker-compose logs postgres

# Deberías ver: "database system is ready to accept connections"

# Si tarda mucho, reinicia:
docker-compose restart postgres
```

---

## 📋 Configuración de `docker-compose.yaml`

La configuración completa del `docker-compose.yaml` para el Billing Service:

```yaml
version: '3.8'
services:
  # Base de datos PostgreSQL
  postgres:
    image: postgres:16-alpine          # Imagen oficial de PostgreSQL 16
    container_name: billing_db
    environment:
      POSTGRES_DB: db_billing_users    # Nombre de la base de datos
      POSTGRES_USER: billing           # Usuario
      POSTGRES_PASSWORD: qwerty123     # Contraseña
    ports:
      - "5433:5432"                    # Puerto externo:puerto interno
    volumes:
      - postgres_users_data_new:/var/lib/postgresql/data  # Persistencia de datos

  # Aplicación Spring Boot
  billing-service:
    build: .                            # Construir desde el Dockerfile local
    container_name: billing_app
    ports:
      - "8082:8082"                    # Puerto externo:puerto interno
    depends_on:
      - postgres                        # Espera a que postgres esté listo
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/db_billing_users
      SPRING_DATASOURCE_USERNAME: billing
      SPRING_DATASOURCE_PASSWORD: qwerty123

# Volúmenes persistentes
volumes:
  postgres_users_data_new:              # Nombre del volumen para datos de PostgreSQL
```

**Explicación de configuraciones clave:**

| Propiedad | Significado |
|-----------|------------|
| `version: '3.8'` | Versión del formato de Docker Compose |
| `services` | Define los servicios (contenedores) a ejecutar |
| `image` | Imagen Docker a usar (descargada de Docker Hub) |
| `container_name` | Nombre del contenedor para identificarlo fácilmente |
| `ports` | Mapeo de puertos `externo:interno` |
| `volumes` | Mapeo de volúmenes para persistencia de datos |
| `depends_on` | Asegura el orden de inicio (postgres antes que app) |
| `environment` | Variables de entorno dentro del contenedor |

---

✨ **Para más información sobre el proyecto general, consulta el [README principal](../../README.md)**
