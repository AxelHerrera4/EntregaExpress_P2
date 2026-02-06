# Fleet Service - Documentación Completa

## Índice
1. [Descripción General](#descripción-general)
2. [Responsabilidades](#responsabilidades)
3. [Modelo de Datos](#modelo-de-datos)
4. [Diseño Orientado a Objetos](#diseño-orientado-a-objetos)
5. [API REST - Endpoints](#api-rest---endpoints)
6. [Flujos de Operación](#flujos-de-operación)
7. [Requisitos Técnicos](#requisitos-técnicos)
8. [Integración con Otros Servicios](#integración-con-otros-servicios)

---

## Descripción General

El **Fleet Service** es el microservicio responsable de la gestión administrativa de repartidores y vehículos en la plataforma LogiFlow. Gestiona el ciclo de vida completo de la flota, incluyendo alta, baja, actualización de estados y consultas de disponibilidad para asignación de entregas.

### Alcance del Servicio
- Gestión de repartidores (CRUD completo)
- Gestión de vehículos (CRUD completo)
- Control de estados operativos (DISPONIBLE, EN_RUTA, MANTENIMIENTO)
- Caché de última ubicación conocida del repartidor
- Consultas de disponibilidad por proximidad geográfica
- Asignación dinámica de flota según tipo de vehículo y disponibilidad

### Lo que NO incluye
- Recepción de coordenadas GPS en tiempo real (Tracking Service)
- Generación de rutas (Routing Service)
- Asignación de pedidos (Pedido Service)
- Notificaciones (Notification Service)

---

## Responsabilidades

### Responsabilidades Principales

1. **Gestión de Repartidores**
   - Alta de nuevos repartidores en el sistema
   - Actualización de información personal y operativa
   - Baja lógica de repartidores
   - Control de estados operativos
   - Mantenimiento de caché de ubicación

2. **Gestión de Vehículos**
   - Registro de vehículos en el sistema
   - Clasificación por jerarquía (Motorizado, VehículoLiviano, Camión)
   - Actualización de información técnica
   - Control de estados (ACTIVO, MANTENIMIENTO, FUERA_DE_SERVICIO)
   - Asociación repartidor-vehículo

3. **Asignación Dinámica de Flota**
   - Determinar repartidores aptos según:
     - Tipo de vehículo requerido
     - Estado de disponibilidad actual
     - Proximidad geográfica (usando caché de ubicación)
   - Proveer listados filtrados para asignación de pedidos

4. **Gestión de Ubicación (Caché)**
   - Almacenar última ubicación conocida del repartidor
   - Actualizar caché mediante consumo de eventos del bus de mensajes
   - Proveer consultas de proximidad para asignaciones rápidas

---

## Modelo de Datos

### Entidad: Repartidor

```json
{
  "id": "string (UUID o código único)",
  "nombre": "string",
  "apellido": "string",
  "documento": "string (único)",
  "tipoDocumento": "enum (CEDULA, PASAPORTE)",
  "telefono": "string",
  "email": "string",
  "estado": "enum (DISPONIBLE, EN_RUTA, MANTENIMIENTO)",
  "zonaAsignada": "string (ej: QUITO_NORTE)",
  "tipoLicencia": "enum (A, B, C, D, E)",
  "vehiculoId": "string (nullable, FK a Vehiculo)",
  "ubicacionActual": {
    "latitud": "double (nullable)",
    "longitud": "double (nullable)",
    "ultimaActualizacion": "datetime (nullable)"
  },
  "fechaContratacion": "date",
  "activo": "boolean",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Entidad: Vehículo

```json
{
  "id": "string (UUID)",
  "placa": "string (única)",
  "tipo": "enum (MOTORIZADO, VEHICULO_LIVIANO, CAMION)",
  "marca": "string",
  "modelo": "string",
  "anio": "integer",
  "capacidadCarga": "double (kg)",
  "estado": "enum (ACTIVO, MANTENIMIENTO, FUERA_DE_SERVICIO)",
  "caracteristicasEspecificas": {
    // Campos específicos según tipo
    // Para MOTORIZADO: cilindraje
    // Para VEHICULO_LIVIANO: numeroPuertas, tipoCarroceria
    // Para CAMION: numeroEjes, capacidadVolumen
  },
  "activo": "boolean",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Estados del Repartidor

| Estado | Descripción |
|--------|-------------|
| `DISPONIBLE` | Repartidor listo para recibir asignaciones |
| `EN_RUTA` | Repartidor actualmente realizando una entrega |
| `MANTENIMIENTO` | Repartidor no disponible temporalmente |

### Estados del Vehículo

| Estado | Descripción |
|--------|-------------|
| `ACTIVO` | Vehículo operativo y disponible |
| `MANTENIMIENTO` | Vehículo en reparación o revisión |
| `FUERA_DE_SERVICIO` | Vehículo dado de baja o no operativo |

---

## Diseño Orientado a Objetos

### Jerarquía de Clases - VehiculoEntrega (Obligatorio)

```java
/**
 * Clase abstracta que define el comportamiento común de todos los vehículos.
 * NO puede ser instanciada directamente.
 */
public abstract class VehiculoEntrega implements IRuteable {
    protected String id;
    protected String placa;
    protected String marca;
    protected String modelo;
    protected Integer anio;
    protected Double capacidadCarga;
    protected EstadoVehiculo estado;
    
    // Constructor protegido
    protected VehiculoEntrega(String placa, String marca, String modelo) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
    }
    
    // Métodos abstractos que las subclases DEBEN implementar
    public abstract TipoVehiculo getTipo();
    public abstract Double getCapacidadMaxima();
    public abstract boolean esAptoParaEntrega(TipoEntrega tipoEntrega);
    
    // Métodos concretos compartidos
    public boolean estaDisponible() {
        return this.estado == EstadoVehiculo.ACTIVO;
    }
    
    public void cambiarEstado(EstadoVehiculo nuevoEstado) {
        this.estado = nuevoEstado;
    }
}
```

### Subclases Concretas

#### 1. Motorizado (Entregas urbanas - última milla)

```java
public class Motorizado extends VehiculoEntrega {
    private Integer cilindraje;
    private boolean tieneCajones; // Para delivery
    
    public Motorizado(String placa, String marca, String modelo, Integer cilindraje) {
        super(placa, marca, modelo);
        this.cilindraje = cilindraje;
        this.capacidadCarga = 30.0; // kg máximo
    }
    
    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.MOTORIZADO;
    }
    
    @Override
    public Double getCapacidadMaxima() {
        return this.capacidadCarga;
    }
    
    @Override
    public boolean esAptoParaEntrega(TipoEntrega tipoEntrega) {
        return tipoEntrega == TipoEntrega.URBANA_RAPIDA;
    }
    
    @Override
    public InformacionRuta getInformacionRuta() {
        return new InformacionRuta(this.id, TipoVehiculo.MOTORIZADO, 
                                    this.capacidadCarga, true); // puede usar ciclovías
    }
}
```

#### 2. VehiculoLiviano (Entregas intermunicipales)

```java
public class VehiculoLiviano extends VehiculoEntrega {
    private Integer numeroPuertas;
    private TipoCarroceria tipoCarroceria; // SEDAN, SUV, PICKUP
    
    public VehiculoLiviano(String placa, String marca, String modelo, TipoCarroceria tipo) {
        super(placa, marca, modelo);
        this.tipoCarroceria = tipo;
        this.capacidadCarga = tipo == TipoCarroceria.PICKUP ? 1000.0 : 500.0;
    }
    
    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.VEHICULO_LIVIANO;
    }
    
    @Override
    public Double getCapacidadMaxima() {
        return this.capacidadCarga;
    }
    
    @Override
    public boolean esAptoParaEntrega(TipoEntrega tipoEntrega) {
        return tipoEntrega == TipoEntrega.INTERMUNICIPAL;
    }
    
    @Override
    public InformacionRuta getInformacionRuta() {
        return new InformacionRuta(this.id, TipoVehiculo.VEHICULO_LIVIANO, 
                                    this.capacidadCarga, false);
    }
}
```

#### 3. Camion (Entregas nacionales)

```java
public class Camion extends VehiculoEntrega {
    private Integer numeroEjes;
    private Double capacidadVolumen; // m³
    private boolean requiereLicenciaEspecial;
    
    public Camion(String placa, String marca, String modelo, Integer ejes, Double volumen) {
        super(placa, marca, modelo);
        this.numeroEjes = ejes;
        this.capacidadVolumen = volumen;
        this.capacidadCarga = ejes * 2000.0; // Aproximado por eje
        this.requiereLicenciaEspecial = true;
    }
    
    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.CAMION;
    }
    
    @Override
    public Double getCapacidadMaxima() {
        return this.capacidadCarga;
    }
    
    @Override
    public boolean esAptoParaEntrega(TipoEntrega tipoEntrega) {
        return tipoEntrega == TipoEntrega.NACIONAL;
    }
    
    @Override
    public InformacionRuta getInformacionRuta() {
        return new InformacionRuta(this.id, TipoVehiculo.CAMION, 
                                    this.capacidadCarga, false, 
                                    this.numeroEjes, this.capacidadVolumen);
    }
}
```

### Interface: IRuteable

```java
/**
 * Contrato que define las capacidades de un vehículo para ser incluido en rutas.
 * Permite al RoutingService obtener información necesaria sin conocer el tipo específico.
 */
public interface IRuteable {
    /**
     * Provee información estructurada del vehículo para cálculo de rutas
     */
    InformacionRuta getInformacionRuta();
}

/**
 * DTO que encapsula información de ruta
 */
public class InformacionRuta {
    private String vehiculoId;
    private TipoVehiculo tipo;
    private Double capacidadCarga;
    private boolean puedeUsarCiclovias;
    private Integer numeroEjes;
    private Double capacidadVolumen;
    
    // Constructores y getters
}
```

### Enumeraciones

```java
public enum TipoVehiculo {
    MOTORIZADO,
    VEHICULO_LIVIANO,
    CAMION
}

public enum EstadoVehiculo {
    ACTIVO,
    MANTENIMIENTO,
    FUERA_DE_SERVICIO
}

public enum EstadoRepartidor {
    DISPONIBLE,
    EN_RUTA,
    MANTENIMIENTO
}

public enum TipoEntrega {
    URBANA_RAPIDA,      // última milla
    INTERMUNICIPAL,      // dentro de provincia
    NACIONAL             // entre provincias
}
```

---

## API REST - Endpoints

### Base URL
```
/api/fleet
```

Todas las rutas están protegidas por el API Gateway y requieren JWT válido.

---

### 1. Gestión de Repartidores

#### 1.1 Crear Repartidor
```http
POST /api/fleet/repartidores
```

**Headers:**
```
Authorization: Bearer {JWT}
Content-Type: application/json
```

**Request Body:**
```json
{
  "nombre": "Juan Carlos",
  "apellido": "Pérez López",
  "documento": "1234567890",
  "tipoDocumento": "CEDULA",
  "telefono": "+593987654321",
  "email": "juan.perez@example.com",
  "zonaAsignada": "QUITO_NORTE",
  "tipoLicencia": "A",
  "vehiculoId": null
}
```

**Response 201 Created:**
```json
{
  "id": "REP-001",
  "nombre": "Juan Carlos",
  "apellido": "Pérez López",
  "documento": "1234567890",
  "tipoDocumento": "CEDULA",
  "telefono": "+593987654321",
  "email": "juan.perez@example.com",
  "estado": "DISPONIBLE",
  "zonaAsignada": "QUITO_NORTE",
  "tipoLicencia": "A",
  "vehiculoId": null,
  "ubicacionActual": null,
  "fechaContratacion": "2025-12-03",
  "activo": true,
  "createdAt": "2025-12-03T10:30:00Z",
  "updatedAt": "2025-12-03T10:30:00Z"
}
```

**Validaciones:**
- `nombre`, `apellido`, `documento`: obligatorios
- `documento`: único en el sistema
- `email`: formato válido y único
- `telefono`: formato E.164
- `tipoLicencia`: debe ser válido según enumeración

**Response 400 Bad Request:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "El documento ya está registrado",
  "field": "documento"
}
```

**Response 401 Unauthorized:**
```json
{
  "error": "UNAUTHORIZED",
  "message": "Token JWT inválido o expirado"
}
```

**Response 403 Forbidden:**
```json
{
  "error": "FORBIDDEN",
  "message": "No tiene permisos para crear repartidores",
  "requiredRole": "SUPERVISOR"
}
```

---

#### 1.2 Listar Repartidores
```http
GET /api/fleet/repartidores
```

**Query Parameters:**
- `estado` (opcional): DISPONIBLE | EN_RUTA | MANTENIMIENTO
- `zonaAsignada` (opcional): string
- `activo` (opcional): true | false
- `page` (opcional): número de página (default: 0)
- `size` (opcional): tamaño de página (default: 20)

**Ejemplo:**
```http
GET /api/fleet/repartidores?estado=DISPONIBLE&zonaAsignada=QUITO_NORTE&page=0&size=10
```

**Response 200 OK:**
```json
{
  "content": [
    {
      "id": "REP-001",
      "nombre": "Juan Carlos",
      "apellido": "Pérez López",
      "documento": "1234567890",
      "estado": "DISPONIBLE",
      "zonaAsignada": "QUITO_NORTE",
      "tipoLicencia": "A",
      "vehiculoId": "VEH-101",
      "vehiculo": {
        "placa": "ABC-1234",
        "tipo": "MOTORIZADO",
        "estado": "ACTIVO"
      },
      "ubicacionActual": {
        "latitud": -0.1807,
        "longitud": -78.4678,
        "ultimaActualizacion": "2025-12-03T10:25:00Z"
      }
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 45,
  "totalPages": 5
}
```

---

#### 1.3 Obtener Repartidor por ID
```http
GET /api/fleet/repartidores/{id}
```

**Path Parameters:**
- `id`: ID del repartidor

**Response 200 OK:**
```json
{
  "id": "REP-001",
  "nombre": "Juan Carlos",
  "apellido": "Pérez López",
  "documento": "1234567890",
  "tipoDocumento": "CEDULA",
  "telefono": "+593987654321",
  "email": "juan.perez@example.com",
  "estado": "EN_RUTA",
  "zonaAsignada": "QUITO_NORTE",
  "tipoLicencia": "A",
  "vehiculoId": "VEH-101",
  "vehiculo": {
    "id": "VEH-101",
    "placa": "ABC-1234",
    "tipo": "MOTORIZADO",
    "marca": "Honda",
    "modelo": "XR 150",
    "estado": "ACTIVO",
    "capacidadCarga": 30.0
  },
  "ubicacionActual": {
    "latitud": -0.1807,
    "longitud": -78.4678,
    "ultimaActualizacion": "2025-12-03T10:25:00Z"
  },
  "fechaContratacion": "2024-01-15",
  "activo": true,
  "createdAt": "2024-01-15T08:00:00Z",
  "updatedAt": "2025-12-03T10:25:00Z"
}
```

**Response 404 Not Found:**
```json
{
  "error": "NOT_FOUND",
  "message": "Repartidor no encontrado",
  "id": "REP-999"
}
```

---

#### 1.4 Actualizar Repartidor
```http
PATCH /api/fleet/repartidores/{id}
```

**Request Body (campos opcionales):**
```json
{
  "telefono": "+593987654322",
  "email": "nuevo.email@example.com",
  "zonaAsignada": "QUITO_SUR",
  "tipoLicencia": "B",
  "vehiculoId": "VEH-102"
}
```

**Response 200 OK:**
```json
{
  "id": "REP-001",
  "nombre": "Juan Carlos",
  "apellido": "Pérez López",
  "telefono": "+593987654322",
  "email": "nuevo.email@example.com",
  "zonaAsignada": "QUITO_SUR",
  "tipoLicencia": "B",
  "vehiculoId": "VEH-102",
  "updatedAt": "2025-12-03T11:00:00Z"
}
```

**Validaciones:**
- No se permite cambiar: `id`, `documento`, `nombre`, `apellido`, `fechaContratacion`
- `vehiculoId`: debe existir y estar ACTIVO
- `email`: formato válido

---

#### 1.5 Cambiar Estado del Repartidor
```http
PATCH /api/fleet/repartidores/{id}/estado
```

**Request Body:**
```json
{
  "estado": "EN_RUTA"
}
```

**Valores permitidos:**
- `DISPONIBLE`
- `EN_RUTA`
- `MANTENIMIENTO`

**Response 200 OK:**
```json
{
  "id": "REP-001",
  "estado": "EN_RUTA",
  "estadoAnterior": "DISPONIBLE",
  "cambiadoEn": "2025-12-03T11:15:00Z"
}
```

**Validaciones:**
- Transiciones válidas:
  - `DISPONIBLE` → `EN_RUTA` | `MANTENIMIENTO`
  - `EN_RUTA` → `DISPONIBLE` | `MANTENIMIENTO`
  - `MANTENIMIENTO` → `DISPONIBLE`

**Response 400 Bad Request:**
```json
{
  "error": "INVALID_STATE_TRANSITION",
  "message": "No se puede cambiar de MANTENIMIENTO a EN_RUTA directamente",
  "estadoActual": "MANTENIMIENTO",
  "estadoSolicitado": "EN_RUTA"
}
```

---

#### 1.6 Actualizar Ubicación del Repartidor (Caché)
```http
PATCH /api/fleet/repartidores/{id}/ubicacion
```

**Nota Importante:** Este endpoint NO es llamado directamente por la app del repartidor. Es actualizado por un **consumidor interno** que escucha eventos del Tracking Service desde el bus de mensajes.

**Request Body:**
```json
{
  "latitud": -0.1807,
  "longitud": -78.4678,
  "timestamp": "2025-12-03T11:20:00Z"
}
```

**Response 200 OK:**
```json
{
  "id": "REP-001",
  "ubicacionActual": {
    "latitud": -0.1807,
    "longitud": -78.4678,
    "ultimaActualizacion": "2025-12-03T11:20:00Z"
  }
}
```

**Validaciones:**
- `latitud`: rango -90 a 90
- `longitud`: rango -180 a 180
- `timestamp`: no puede ser futuro, máximo 5 minutos en el pasado

---

#### 1.7 Baja Lógica de Repartidor
```http
DELETE /api/fleet/repartidores/{id}
```

**Response 200 OK:**
```json
{
  "id": "REP-001",
  "activo": false,
  "fechaBaja": "2025-12-03T11:30:00Z",
  "message": "Repartidor dado de baja exitosamente"
}
```

**Validaciones:**
- No se puede dar de baja si `estado` = `EN_RUTA`

**Response 400 Bad Request:**
```json
{
  "error": "INVALID_OPERATION",
  "message": "No se puede dar de baja un repartidor en ruta",
  "estado": "EN_RUTA"
}
```

---

### 2. Gestión de Vehículos

#### 2.1 Crear Vehículo
```http
POST /api/fleet/vehiculos
```

**Request Body (Motorizado):**
```json
{
  "placa": "ABC-1234",
  "tipo": "MOTORIZADO",
  "marca": "Honda",
  "modelo": "XR 150",
  "anio": 2023,
  "capacidadCarga": 30.0,
  "caracteristicas": {
    "cilindraje": 150,
    "tieneCajones": true
  }
}
```

**Request Body (Vehículo Liviano):**
```json
{
  "placa": "XYZ-5678",
  "tipo": "VEHICULO_LIVIANO",
  "marca": "Toyota",
  "modelo": "Hilux",
  "anio": 2024,
  "capacidadCarga": 1000.0,
  "caracteristicas": {
    "numeroPuertas": 4,
    "tipoCarroceria": "PICKUP"
  }
}
```

**Request Body (Camión):**
```json
{
  "placa": "GHI-9012",
  "tipo": "CAMION",
  "marca": "Hino",
  "modelo": "Serie 500",
  "anio": 2023,
  "capacidadCarga": 8000.0,
  "caracteristicas": {
    "numeroEjes": 3,
    "capacidadVolumen": 40.0
  }
}
```

**Response 201 Created:**
```json
{
  "id": "VEH-101",
  "placa": "ABC-1234",
  "tipo": "MOTORIZADO",
  "marca": "Honda",
  "modelo": "XR 150",
  "anio": 2023,
  "capacidadCarga": 30.0,
  "estado": "ACTIVO",
  "caracteristicas": {
    "cilindraje": 150,
    "tieneCajones": true
  },
  "activo": true,
  "createdAt": "2025-12-03T12:00:00Z",
  "updatedAt": "2025-12-03T12:00:00Z"
}
```

**Validaciones:**
- `placa`: única, formato válido según país
- `tipo`: debe ser MOTORIZADO | VEHICULO_LIVIANO | CAMION
- `capacidadCarga`: mayor a 0
- `caracteristicas`: debe coincidir con el tipo de vehículo

---

#### 2.2 Listar Vehículos
```http
GET /api/fleet/vehiculos
```

**Query Parameters:**
- `tipo` (opcional): MOTORIZADO | VEHICULO_LIVIANO | CAMION
- `estado` (opcional): ACTIVO | MANTENIMIENTO | FUERA_DE_SERVICIO
- `activo` (opcional): true | false
- `disponible` (opcional): true (sin repartidor asignado)
- `page` (opcional): número de página
- `size` (opcional): tamaño de página

**Response 200 OK:**
```json
{
  "content": [
    {
      "id": "VEH-101",
      "placa": "ABC-1234",
      "tipo": "MOTORIZADO",
      "marca": "Honda",
      "modelo": "XR 150",
      "anio": 2023,
      "capacidadCarga": 30.0,
      "estado": "ACTIVO",
      "repartidorAsignado": {
        "id": "REP-001",
        "nombre": "Juan Carlos Pérez"
      }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 120,
  "totalPages": 6
}
```

---

#### 2.3 Obtener Vehículo por ID
```http
GET /api/fleet/vehiculos/{id}
```

**Response 200 OK:**
```json
{
  "id": "VEH-101",
  "placa": "ABC-1234",
  "tipo": "MOTORIZADO",
  "marca": "Honda",
  "modelo": "XR 150",
  "anio": 2023,
  "capacidadCarga": 30.0,
  "estado": "ACTIVO",
  "caracteristicas": {
    "cilindraje": 150,
    "tieneCajones": true
  },
  "repartidorAsignado": {
    "id": "REP-001",
    "nombre": "Juan Carlos Pérez López",
    "telefono": "+593987654321"
  },
  "activo": true,
  "createdAt": "2025-01-15T08:00:00Z",
  "updatedAt": "2025-12-03T10:00:00Z"
}
```

---

#### 2.4 Actualizar Vehículo
```http
PATCH /api/fleet/vehiculos/{id}
```

**Request Body (campos opcionales):**
```json
{
  "estado": "MANTENIMIENTO",
  "capacidadCarga": 35.0,
  "caracteristicas": {
    "tieneCajones": false
  }
}
```

**Response 200 OK:**
```json
{
  "id": "VEH-101",
  "placa": "ABC-1234",
  "tipo": "MOTORIZADO",
  "estado": "MANTENIMIENTO",
  "capacidadCarga": 35.0,
  "caracteristicas": {
    "cilindraje": 150,
    "tieneCajones": false
  },
  "updatedAt": "2025-12-03T13:00:00Z"
}
```

**Validaciones:**
- No se permite cambiar: `id`, `placa`, `tipo`
- Si se cambia a `MANTENIMIENTO` y tiene repartidor asignado, se debe liberar la asignación

---

#### 2.5 Baja Lógica de Vehículo
```http
DELETE /api/fleet/vehiculos/{id}
```

**Response 200 OK:**
```json
{
  "id": "VEH-101",
  "activo": false,
  "fechaBaja": "2025-12-03T14:00:00Z",
  "message": "Vehículo dado de baja exitosamente"
}
```

**Validaciones:**
- No puede tener repartidor asignado
- Estado debe ser diferente de `ACTIVO`

---

### 3. Consultas de Disponibilidad

#### 3.1 Obtener Repartidores Disponibles
```http
GET /api/fleet/repartidores/disponibles
```

**Query Parameters:**
- `tipoVehiculo` (opcional): MOTORIZADO | VEHICULO_LIVIANO | CAMION
- `zona` (opcional): string (ej: QUITO_NORTE)
- `cercaDe` (opcional): "lat,lng" (ej: "-0.1807,-78.4678")
- `radio` (opcional): metros (default: 5000)

**Ejemplo 1 - Por tipo:**
```http
GET /api/fleet/repartidores/disponibles?tipoVehiculo=MOTORIZADO&zona=QUITO_NORTE
```

**Ejemplo 2 - Por proximidad:**
```http
GET /api/fleet/repartidores/disponibles?tipoVehiculo=MOTORIZADO&cercaDe=-0.1807,-78.4678&radio=3000
```

**Response 200 OK:**
```json
{
  "disponibles": [
    {
      "id": "REP-001",
      "nombre": "Juan Carlos Pérez",
      "estado": "DISPONIBLE",
      "vehiculo": {
        "id": "VEH-101",
        "placa": "ABC-1234",
        "tipo": "MOTORIZADO",
        "capacidadCarga": 30.0
      },
      "ubicacionActual": {
        "latitud": -0.1807,
        "longitud": -78.4678,
        "ultimaActualizacion": "2025-12-03T10:25:00Z"
      },
      "distancia": 1250.5,
      "zonaAsignada": "QUITO_NORTE"
    },
    {
      "id": "REP-005",
      "nombre": "María González",
      "estado": "DISPONIBLE",
      "vehiculo": {
        "id": "VEH-105",
        "placa": "DEF-5678",
        "tipo": "MOTORIZADO",
        "capacidadCarga": 30.0
      },
      "ubicacionActual": {
        "latitud": -0.1820,
        "longitud": -78.4690,
        "ultimaActualizacion": "2025-12-03T10:28:00Z"
      },
      "distancia": 2890.3,
      "zonaAsignada": "QUITO_NORTE"
    }
  ],
  "total": 2,
  "criterios": {
    "tipoVehiculo": "MOTORIZADO",
    "puntoReferencia": {
      "latitud": -0.1807,
      "longitud": -78.4678
    },
    "radio": 3000
  }
}
```

**Notas:**
- Campo `distancia` calculado solo si se proporciona `cercaDe`
- Resultados ordenados por distancia (más cercano primero)
- Solo incluye repartidores con ubicación reciente (< 5 minutos)
- Solo incluye vehículos en estado `ACTIVO`

---

#### 3.2 Verificar Disponibilidad de Repartidor Específico
```http
GET /api/fleet/repartidores/{id}/disponibilidad
```

**Response 200 OK:**
```json
{
  "repartidorId": "REP-001",
  "disponible": true,
  "estado": "DISPONIBLE",
  "vehiculo": {
    "id": "VEH-101",
    "tipo": "MOTORIZADO",
    "estado": "ACTIVO",
    "aptoParaEntrega": true
  },
  "ubicacionReciente": true,
  "ubicacionActual": {
    "latitud": -0.1807,
    "longitud": -78.4678,
    "ultimaActualizacion": "2025-12-03T10:25:00Z",
    "antiguedad": "2 minutos"
  }
}
```

**Response 200 OK (No disponible):**
```json
{
  "repartidorId": "REP-002",
  "disponible": false,
  "estado": "EN_RUTA",
  "motivo": "Repartidor actualmente en ruta",
  "vehiculo": {
    "id": "VEH-102",
    "tipo": "MOTORIZADO",
    "estado": "ACTIVO"
  }
}
```

---

#### 3.3 Estadísticas de Flota por Zona
```http
GET /api/fleet/estadisticas/zona/{zonaId}
```

**Response 200 OK:**
```json
{
  "zona": "QUITO_NORTE",
  "fecha": "2025-12-03T14:30:00Z",
  "resumen": {
    "totalRepartidores": 45,
    "disponibles": 12,
    "enRuta": 28,
    "mantenimiento": 5
  },
  "porTipoVehiculo": {
    "MOTORIZADO": {
      "total": 30,
      "disponibles": 8,
      "enRuta": 20,
      "mantenimiento": 2
    },
    "VEHICULO_LIVIANO": {
      "total": 10,
      "disponibles": 3,
      "enRuta": 6,
      "mantenimiento": 1
    },
    "CAMION": {
      "total": 5,
      "disponibles": 1,
      "enRuta": 2,
      "mantenimiento": 2
    }
  },
  "tasaDisponibilidad": 26.67,
  "tasaUtilizacion": 62.22
}
```

---

## Flujos de Operación

### Flujo 1: Asignación de Pedido (Interacción con otros servicios)

```
1. Usuario crea pedido en Pedido Service
   └─> POST /api/pedidos
       {
         "destino": {"lat": -0.1807, "lng": -78.4678},
         "tipoEntrega": "URBANA_RAPIDA"
       }

2. Pedido Service solicita repartidor disponible
   └─> GET /api/fleet/repartidores/disponibles
       ?tipoVehiculo=MOTORIZADO
       &cercaDe=-0.1807,-78.4678
       &radio=5000

3. Fleet Service responde con repartidores aptos
   └─> [{id: "REP-001", distancia: 1250.5, ...}]

4. Routing Service (o Pedido Service) selecciona el más cercano

5. Pedido Service asigna pedido y cambia estado del repartidor
   └─> PATCH /api/fleet/repartidores/REP-001/estado
       {"estado": "EN_RUTA"}

6. Fleet Service actualiza estado
   └─> Response: {id: "REP-001", estado: "EN_RUTA"}
```

### Flujo 2: Actualización de Ubicación (Tracking → Fleet)

```
1. App del repartidor envía GPS a Tracking Service
   └─> POST /api/tracking/ubicacion
       {
         "repartidorId": "REP-001",
         "lat": -0.1807,
         "lng": -78.4678,
         "timestamp": "2025-12-03T10:25:00Z"
       }

2. Tracking Service valida y almacena la ubicación

3. Tracking Service publica evento al bus de mensajes
   └─> Evento: "repartidor.ubicacion.actualizada"
       Payload: {
         "repartidorId": "REP-001",
         "latitud": -0.1807,
         "longitud": -78.4678,
         "timestamp": "2025-12-03T10:25:00Z"
       }

4. Consumidor interno de Fleet Service escucha el evento

5. Fleet Service actualiza caché de ubicación
   └─> PATCH /api/fleet/repartidores/REP-001/ubicacion
       (llamada interna, no expuesta públicamente)

6. Ubicación actualizada en BD de Fleet Service
```

### Flujo 3: Finalización de Entrega

```
1. Repartidor confirma entrega en su app

2. Pedido Service procesa la confirmación

3. Pedido Service cambia estado del repartidor a disponible
   └─> PATCH /api/fleet/repartidores/REP-001/estado
       {"estado": "DISPONIBLE"}

4. Fleet Service valida transición de estado
   └─> EN_RUTA → DISPONIBLE (válido)

5. Fleet Service actualiza estado
   └─> Response: {
         id: "REP-001",
         estado: "DISPONIBLE",
         cambiadoEn: "2025-12-03T11:45:00Z"
       }

6. Repartidor queda disponible para nueva asignación
```

---

## Requisitos Técnicos

### 1. Transaccionalidad

**Todas las operaciones de escritura deben ser ACID:**

```java
@Service
@Transactional
public class RepartidorService {
    
    @Transactional
    public Repartidor crearRepartidor(CrearRepartidorDTO dto) {
        // Validaciones
        validarDocumentoUnico(dto.getDocumento());
        
        // Creación transaccional
        Repartidor repartidor = new Repartidor();
        // ... mapeo de datos
        
        return repartidorRepository.save(repartidor);
        // Si falla, se hace rollback automático
    }
    
    @Transactional
    public void cambiarEstado(String id, EstadoRepartidor nuevoEstado) {
        Repartidor rep = obtenerPorId(id);
        validarTransicionEstado(rep.getEstado(), nuevoEstado);
        rep.setEstado(nuevoEstado);
        repartidorRepository.save(rep);
    }
}
```

### 2. Validación de Entrada

**Usar anotaciones de validación:**

```java
public class CrearRepartidorDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50)
    private String nombre;
    
    @NotBlank(message = "El documento es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "Documento debe tener 10 dígitos")
    private String documento;
    
    @Email(message = "Email debe ser válido")
    @NotBlank
    private String email;
    
    @Pattern(regexp = "^\\+593[0-9]{9}$", message = "Teléfono debe ser formato +593XXXXXXXXX")
    private String telefono;
    
    @NotNull
    private TipoLicencia tipoLicencia;
}
```

### 3. Esquema de Base de Datos

**Tabla: repartidores**
```sql
CREATE TABLE repartidores (
    id VARCHAR(36) PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    documento VARCHAR(20) UNIQUE NOT NULL,
    tipo_documento VARCHAR(20) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    zona_asignada VARCHAR(50) NOT NULL,
    tipo_licencia VARCHAR(10) NOT NULL,
    vehiculo_id VARCHAR(36),
    latitud DECIMAL(10, 8),
    longitud DECIMAL(11, 8),
    ultima_actualizacion_ubicacion TIMESTAMP,
    fecha_contratacion DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_vehiculo FOREIGN KEY (vehiculo_id) 
        REFERENCES vehiculos(id) ON DELETE SET NULL,
    
    CONSTRAINT chk_estado CHECK (estado IN ('DISPONIBLE', 'EN_RUTA', 'MANTENIMIENTO')),
    CONSTRAINT chk_tipo_documento CHECK (tipo_documento IN ('CEDULA', 'PASAPORTE')),
    CONSTRAINT chk_latitud CHECK (latitud BETWEEN -90 AND 90),
    CONSTRAINT chk_longitud CHECK (longitud BETWEEN -180 AND 180)
);

CREATE INDEX idx_repartidores_estado ON repartidores(estado);
CREATE INDEX idx_repartidores_zona ON repartidores(zona_asignada);
CREATE INDEX idx_repartidores_ubicacion ON repartidores(latitud, longitud);
CREATE INDEX idx_repartidores_activo ON repartidores(activo);
```

**Tabla: vehiculos**
```sql
CREATE TABLE vehiculos (
    id VARCHAR(36) PRIMARY KEY,
    placa VARCHAR(20) UNIQUE NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    anio INTEGER NOT NULL,
    capacidad_carga DECIMAL(10, 2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    caracteristicas JSON,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_tipo CHECK (tipo IN ('MOTORIZADO', 'VEHICULO_LIVIANO', 'CAMION')),
    CONSTRAINT chk_estado_vehiculo CHECK (estado IN ('ACTIVO', 'MANTENIMIENTO', 'FUERA_DE_SERVICIO')),
    CONSTRAINT chk_capacidad CHECK (capacidad_carga > 0),
    CONSTRAINT chk_anio CHECK (anio BETWEEN 1990 AND 2100)
);

CREATE INDEX idx_vehiculos_tipo ON vehiculos(tipo);
CREATE INDEX idx_vehiculos_estado ON vehiculos(estado);
CREATE INDEX idx_vehiculos_activo ON vehiculos(activo);
```

### 4. Documentación OpenAPI

**Archivo: fleet-service-openapi.yaml**

```yaml
openapi: 3.0.0
info:
  title: Fleet Service API
  version: 1.0.0
  description: Servicio de gestión de flota para LogiFlow

servers:
  - url: http://localhost:8080/api/fleet
    description: Servidor local
  - url: https://api.logiflow.com/fleet
    description: Servidor producción

security:
  - bearerAuth: []

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    Repartidor:
      type: object
      required:
        - nombre
        - apellido
        - documento
        - tipoDocumento
        - telefono
        - email
        - zonaAsignada
        - tipoLicencia
      properties:
        id:
          type: string
          example: "REP-001"
        nombre:
          type: string
          minLength: 2
          maxLength: 50
          example: "Juan Carlos"
        apellido:
          type: string
          example: "Pérez López"
        documento:
          type: string
          example: "1234567890"
        tipoDocumento:
          type: string
          enum: [CEDULA, PASAPORTE]
        estado:
          type: string
          enum: [DISPONIBLE, EN_RUTA, MANTENIMIENTO]
        # ... resto de campos

paths:
  /repartidores:
    post:
      summary: Crear nuevo repartidor
      tags:
        - Repartidores
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Repartidor'
      responses:
        '201':
          description: Repartidor creado exitosamente
        '400':
          description: Datos inválidos
        '401':
          description: No autenticado
        '403':
          description: Sin permisos
```

### 5. Consumidor de Eventos (RabbitMQ/Kafka)

```java
@Component
public class UbicacionEventConsumer {
    
    @Autowired
    private RepartidorService repartidorService;
    
    @RabbitListener(queues = "${rabbitmq.queue.ubicacion}")
    public void handleUbicacionActualizada(UbicacionActualizadaEvent event) {
        log.info("Evento recibido: actualizar ubicación de {}", event.getRepartidorId());
        
        try {
            repartidorService.actualizarUbicacion(
                event.getRepartidorId(),
                event.getLatitud(),
                event.getLongitud(),
                event.getTimestamp()
            );
            log.info("Ubicación actualizada correctamente");
        } catch (Exception e) {
            log.error("Error actualizando ubicación: {}", e.getMessage());
            // Reencolar o manejar según estrategia de retry
        }
    }
}
```

### 6. Rate Limiting en API Gateway

```yaml
# Configuración Spring Cloud Gateway
spring:
  cloud:
    gateway:
      routes:
        - id: fleet-service
          uri: lb://fleet-service
          predicates:
            - Path=/api/fleet/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
                redis-rate-limiter.requestedTokens: 1
            - name: AuthenticationFilter
```

### 7. Pruebas Unitarias

```java
@SpringBootTest
@Transactional
public class RepartidorServiceTest {
    
    @Autowired
    private RepartidorService repartidorService;
    
    @Test
    public void testCrearRepartidor_Exitoso() {
        // Arrange
        CrearRepartidorDTO dto = new CrearRepartidorDTO();
        dto.setNombre("Juan");
        dto.setApellido("Pérez");
        dto.setDocumento("1234567890");
        // ... resto de datos
        
        // Act
        Repartidor resultado = repartidorService.crearRepartidor(dto);
        
        // Assert
        assertNotNull(resultado.getId());
        assertEquals("Juan", resultado.getNombre());
        assertEquals(EstadoRepartidor.DISPONIBLE, resultado.getEstado());
    }
    
    @Test
    public void testCrearRepartidor_DocumentoDuplicado() {
        // Arrange
        crearRepartidorConDocumento("1234567890");
        CrearRepartidorDTO dto = crearDTOConDocumento("1234567890");
        
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            repartidorService.crearRepartidor(dto);
        });
    }
    
    @Test
    public void testCambiarEstado_TransicionValida() {
        // Arrange
        Repartidor rep = crearRepartidorDisponible();
        
        // Act
        repartidorService.cambiarEstado(rep.getId(), EstadoRepartidor.EN_RUTA);
        
        // Assert
        Repartidor actualizado = repartidorService.obtenerPorId(rep.getId());
        assertEquals(EstadoRepartidor.EN_RUTA, actualizado.getEstado());
    }
}
```

### 8. Pruebas de Integración

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class FleetControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String jwtToken;
    
    @BeforeEach
    public void setup() {
        jwtToken = generarTokenSupervisor();
    }
    
    @Test
    public void testCrearRepartidor_SinAutenticacion_Retorna401() throws Exception {
        CrearRepartidorDTO dto = crearDTOValido();
        
        mockMvc.perform(post("/api/fleet/repartidores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    public void testCrearRepartidor_ConTokenValido_Retorna201() throws Exception {
        CrearRepartidorDTO dto = crearDTOValido();
        
        mockMvc.perform(post("/api/fleet/repartidores")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value(dto.getNombre()));
    }
    
    @Test
    public void testObtenerDisponibles_FiltroProximidad() throws Exception {
        // Arrange: crear repartidores con ubicaciones conocidas
        crearRepartidorEnUbicacion(-0.1807, -78.4678);
        crearRepartidorEnUbicacion(-0.2000, -78.5000); // lejos
        
        // Act & Assert
        mockMvc.perform(get("/api/fleet/repartidores/disponibles")
                .header("Authorization", "Bearer " + jwtToken)
                .param("tipoVehiculo", "MOTORIZADO")
                .param("cercaDe", "-0.1807,-78.4678")
                .param("radio", "5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponibles").isArray())
                .andExpect(jsonPath("$.disponibles[0].distancia").exists())
                .andExpect(jsonPath("$.disponibles[0].distancia").value(lessThan(5000.0)));
    }
}
```

---

## Integración con Otros Servicios

### Con API Gateway

**Todas las peticiones pasan por el Gateway:**
```
Cliente → API Gateway → Fleet Service
         ↓
    [Validación JWT]
    [Rate Limiting]
    [Logging]
```

**Configuración de rutas:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: fleet-repartidores
          uri: lb://fleet-service
          predicates:
            - Path=/api/fleet/repartidores/**
          filters:
            - AuthenticationFilter
            - RateLimitFilter
            
        - id: fleet-vehiculos
          uri: lb://fleet-service
          predicates:
            - Path=/api/fleet/vehiculos/**
          filters:
            - AuthenticationFilter
```

### Con Auth Service

**Fleet Service NO valida tokens, eso lo hace el Gateway:**
```
1. Cliente envía request con JWT
2. API Gateway extrae y valida JWT con Auth Service
3. Si válido, Gateway añade claims al header:
   - X-User-Id
   - X-User-Role
   - X-User-Zone
4. Fleet Service lee estos headers para autorización a nivel de negocio
```

### Con Tracking Service (vía Mensajería)

**Fleet Service como consumidor:**
```
Tracking Service → RabbitMQ → Fleet Service
                   (Queue: ubicacion.actualizada)
                   
Fleet Service escucha eventos y actualiza caché
```

**Configuración RabbitMQ:**
```java
@Configuration
public class RabbitMQConfig {
    
    @Bean
    public Queue ubicacionQueue() {
        return new Queue("ubicacion.actualizada", true);
    }
    
    @Bean
    public TopicExchange trackingExchange() {
        return new TopicExchange("tracking.events");
    }
    
    @Bean
    public Binding ubicacionBinding() {
        return BindingBuilder
            .bind(ubicacionQueue())
            .to(trackingExchange())
            .with("repartidor.ubicacion.*");
    }
}
```

### Con Pedido/Routing Service

**Fleet Service como proveedor de datos:**
```
Pedido Service necesita asignar repartidor
    ↓
GET /api/fleet/repartidores/disponibles?tipoVehiculo=X&cercaDe=Y,Z
    ↓
Fleet Service responde con lista ordenada por proximidad
    ↓
Pedido Service selecciona el óptimo
    ↓
PATCH /api/fleet/repartidores/{id}/estado → EN_RUTA
```

---

## Resumen de Responsabilidades

### ✅ SÍ es responsabilidad de Fleet Service:

1. CRUD completo de repartidores
2. CRUD completo de vehículos
3. Control de estados (DISPONIBLE, EN_RUTA, MANTENIMIENTO)
4. Jerarquía de clases VehiculoEntrega (abstracta) y subclases
5. Implementación de interfaces (IRuteable)
6. Caché de última ubicación conocida del repartidor
7. Consultas de disponibilidad por tipo y proximidad
8. Estadísticas de flota por zona
9. Validaciones de negocio (transiciones de estado, asignaciones)
10. Consumir eventos de ubicación desde el bus

### ❌ NO es responsabilidad de Fleet Service:

1. Recibir GPS en tiempo real desde apps (Tracking Service)
2. Calcular rutas óptimas (Routing Service)
3. Crear o gestionar pedidos (Pedido Service)
4. Calcular tarifas (Billing Service)
5. Enviar notificaciones (Notification Service)
6. Validar tokens JWT (API Gateway + Auth Service)
7. Gestionar WebSockets para seguimiento (API Gateway o Tracking)

---

## Patrones de Diseño Aplicados

### 1. Factory Pattern (Creación de Vehículos)

```java
public class VehiculoFactory {
    
    public static VehiculoEntrega crearVehiculo(CrearVehiculoDTO dto) {
        switch (dto.getTipo()) {
            case MOTORIZADO:
                return new Motorizado(
                    dto.getPlaca(),
                    dto.getMarca(),
                    dto.getModelo(),
                    dto.getCaracteristicas().getCilindraje()
                );
                
            case VEHICULO_LIVIANO:
                return new VehiculoLiviano(
                    dto.getPlaca(),
                    dto.getMarca(),
                    dto.getModelo(),
                    dto.getCaracteristicas().getTipoCarroceria()
                );
                
            case CAMION:
                return new Camion(
                    dto.getPlaca(),
                    dto.getMarca(),
                    dto.getModelo(),
                    dto.getCaracteristicas().getNumeroEjes(),
                    dto.getCaracteristicas().getCapacidadVolumen()
                );
                
            default:
                throw new IllegalArgumentException("Tipo de vehículo no válido");
        }
    }
}
```

### 2. Strategy Pattern (Selección de Repartidor)

```java
public interface SeleccionRepartidorStrategy {
    Repartidor seleccionar(List<Repartidor> candidatos, CriteriosAsignacion criterios);
}

public class ProximidadStrategy implements SeleccionRepartidorStrategy {
    @Override
    public Repartidor seleccionar(List<Repartidor> candidatos, CriteriosAsignacion criterios) {
        return candidatos.stream()
            .min(Comparator.comparing(r -> 
                calcularDistancia(r.getUbicacion(), criterios.getDestino())))
            .orElse(null);
    }
}
```

---

Esta es la documentación completa del Fleet Service según los requisitos del proyecto LogiFlow. ¿Necesitas que profundice en alguna sección específica?