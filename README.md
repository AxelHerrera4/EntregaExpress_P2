# 🚚 EntregaExpress_P2

Plataforma de gestión logística basada en microservicios con **Spring Boot** para la administración eficiente de órdenes de entrega, autenticación, facturación y gestión de flota.

---

## 📊 Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    API GATEWAY (8080)                       │
└──────────┬──────────────────────────────────────────────────┘
           │
    ┌──────┼──────┬──────────────┬─────────────┐
    │      │      │              │             │
    ▼      ▼      ▼              ▼             ▼
┌────┐ ┌────┐ ┌────────┐ ┌──────────┐ ┌──────────┐
│Auth│ │Bill│ │Fleet   │ │Pedido    │ │ Otros    │
│8081│ │8082│ │8083    │ │8084      │ │Services  │
└────┘ └────┘ └────────┘ └──────────┘ └──────────┘
  │      │
  └──────┴────────────────────┐
                              ▼
                    ┌──────────────────┐
                    │ PostgreSQL       │
                    │ (Contenedor)     │
                    │ Puerto: 5432     │
                    └──────────────────┘
```

---

## 🔌 Puertos de los Microservicios

| Servicio | Puerto | Estado |
|----------|--------|--------|
| 🔑 API Gateway | 8080 | Activo |
| 🔐 Auth Service | 8081 | Activo |
| 💳 Billing Service | 8082 | ✅ Documentado |
| 🚗 Fleet Service | 8083 | Activo |
| 📦 Pedido Service | 8084 | Activo |
| 🗄️ PostgreSQL | 5432 | Contenedor Docker |

> ⚠️ **Nota:** Si algún puerto está en uso, cámbialo en el `application.yaml` correspondiente y actualiza las configuraciones de conexión.

---

## 🧪 Sesiones de Tests

 **Documentación completa de todos los tests y casos de prueba:**

### ➡️ [Ver Sesiones de Tests - TESTS_SESSION.md](TESTS_SESSION.md)

Consulta la documentación de tests para:
- ✅ Análisis de cobertura detallado
- 📊 Explicación de cada componente probado
- 🎯 Tipos de tests implementados

---

## 🏢 Microservicios

<details>
<summary><strong>💳 Billing Service (Puerto 8082)</strong></summary>

### Descripción General

Servicio encargado de la **gestión de facturación**, cálculo dinámico de tarifas según el tipo de entrega, y administración del estado de facturas. Es el corazón financiero del sistema de logística.

### ⚙️ Configuración Técnica

**Base de Datos:**
- **Motor:** PostgreSQL
- **Host:** `localhost`
- **Puerto:** `5433`
- **Base de datos:** `db_billing_users`
- **Usuario:** `billing`
- **Contraseña:** `qwerty123`

**Tecnologías:**
-  **Java 21**
-  **Spring Boot 4.0.0**
-  **Spring Data JPA** (ORM)
-  **Spring Security** (Autenticación)
-  **Spring Validation** (Validación de datos)
-  **SpringDoc OpenAPI** (Swagger/documentación)
-  **Lombok** (Reducción de código boilerplate)

### 📚 API Endpoints

<details>
<summary><strong>Gestión de Facturas (/api/facturas)</strong></summary>

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

</details>

<details>
<summary><strong>Gestión de Tarifas Base (/api/tarifas-base)</strong></summary>

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/tarifas-base` | Crear tarifa |
| `GET` | `/api/tarifas-base` | Listar tarifas |
| `GET` | `/api/tarifas-base/{id}` | Obtener tarifa por ID |
| `PUT` | `/api/tarifas-base/{id}` | Actualizar tarifa |

**Tipos de entrega soportados:**
-  **URBANA** - Entregas dentro de la ciudad
-  **INTERMUNICIPAL** - Entregas entre municipios
-  **NACIONAL** - Entregas a nivel nacional

</details>

### 🎨 Patrones de Diseño Implementados

<details>
<summary><strong>1 Patrón Strategy (Cálculo de Tarifas)</strong></summary>

El patrón **Strategy** implementa diferentes algoritmos de cálculo de tarifas, permitiendo cambiar el comportamiento en tiempo de ejecución según el tipo de entrega.

**Estrategias implementadas:**

| Estrategia | Fórmula | Uso |
|-----------|---------|-----|
|  **TarifaUrbanaStrategy** | Base + (0.5 × km) | Entregas urbanas |
|  **TarifaIntermunicipalStrategy** | Base + (1.0 × km) | Entregas entre municipios |
|  **TarifaNacionalStrategy** | Base + (1.5 × km) | Entregas nacionales |
|  **DefaultTarifaStrategy** | Base + (0.8 × km) | Tipos no clasificados |

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

</details>

<details>
<summary><strong>2 Patrón Factory (Selección de Estrategias)</strong></summary>

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
 Centralización de lógica de selección
 Fácil mantenimiento y extensión
 Desacoplamiento de componentes

</details>

### 📖 Guía de Uso Paso a Paso

<details>
<summary><strong>Paso 1️: Verificar Conexión a Base de Datos</strong></summary>

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

</details>

<details>
<summary><strong>Paso 2️: Iniciar el Servicio</strong></summary>

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

</details>

<details>
<summary><strong>Paso 3️: Crear una Tarifa Base</strong></summary>

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

</details>

<details>
<summary><strong>Paso 4️: Crear una Factura</strong></summary>

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

</details>

<details>
<summary><strong>Paso 5️: Obtener una Factura</strong></summary>

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

</details>

<details>
<summary><strong>Paso 6️: Actualizar Estado de Factura</strong></summary>

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

</details>

<details>
<summary><strong>Paso 7️: Acceder a Documentación Swagger/OpenAPI</strong></summary>

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

</details>

### 🏗️ Estructura del Código

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

### 🐛 Troubleshooting

<details>
<summary><strong> Error: "Conexión rechazada a PostgreSQL"</strong></summary>

**Solución:**
1. Verifica que PostgreSQL está corriendo
2. Revisa que el puerto 5433 es correcto
3. Confirma credenciales (usuario: `billing`, contraseña: `qwerty123`)
4. Comprueba la URL de conexión en `application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/db_billing_users
    username: billing
    password: qwerty123
```

</details>

<details>
<summary><strong> Error: "Puerto 8082 ya está en uso"</strong></summary>

**Solución:**
Cambia el puerto en `application.yaml`:

```yaml
server:
  port: 8085  # Cambiar a otro puerto disponible
```

</details>

<details>
<summary><strong> Error: "No se puede encontrar la clase TarifaStrategy"</strong></summary>

**Solución:**
Ejecuta:
```bash
./mvnw clean compile
```

Esto reconstruirá el proyecto y descargará las dependencias necesarias.

</details>

</details>

---

## 📚 Documentación Adicional

Para información sobre otros servicios, consulta:
- 🔐 **Auth Service** - Sistema de autenticación y autorización
- 🚗 **Fleet Service** - Gestión de vehículos y conductores
- 📦 **Pedido Service** - Gestión de órdenes de entrega
- 🔀 **API Gateway** - Enrutador centralizado de microservicios

---

<div align="center">

### 🎯 Estado del Proyecto

| Componente | Estado | Documentación |
|-----------|--------|---------------|
| Billing Service | ✅ Completo | ✅ Completa |
| Patrones | ✅ Implementados | ✅ Documentados |
| API Documentation | ✅ Swagger/OpenAPI | ✅ Disponible |



</div>