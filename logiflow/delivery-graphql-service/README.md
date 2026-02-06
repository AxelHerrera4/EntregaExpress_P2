# 🚀 Delivery GraphQL Service

Microservicio GraphQL (Apollo Server v4) que actúa como **BFF/Gateway** para el sistema de delivery, agregando datos de 3 microservicios Java REST.

## 📋 Características Implementadas

### ✅ Schema GraphQL Completo
- **Tipos**: `Pedido`, `Cliente`, `Repartidor`, `Vehiculo`, `FlotaResumen`, `KPI`
- **Enums**: `EstadoPedido`, `EstadoRepartidor`, `TipoVehiculo`
- **Queries implementadas**:
  - `pedido(id: ID!)`: Detalle de un pedido
  - `pedidos(filtro: FiltroPedido!)`: Pedidos filtrados por zona/estado/repartidor
  - `flotaActiva(zonaId: ID!)`: Repartidores en mapa con ubicación en tiempo real
  - `flotaResumen(zonaId: ID!)`: Resumen de flota (total, disponibles, en ruta)
  - `kpis(zonaId: ID!)`: KPIs por zona
  - `kpiDiario(fecha: String!, zonaId: ID)`: KPIs por fecha ✨ **NUEVO**
  - `cacheMetrics`: Métricas de rendimiento (hit/miss rates) ✨ **NUEVO**

### ✅ DataLoaders (Prevención N+1)
- **RepartidorLoader**: Agrupa carga de repartidores en batches automáticos
- Cache por request para evitar requests duplicados
- Implementado en `Pedido.repartidor` field resolver

### ✅ Sistema de Caché con Métricas
- **Caché en memoria** con TTL configurable
- **Métricas de rendimiento**: hits, misses, hit rate, size
- TTLs optimizados por tipo:
  - Pedidos: 20 segundos
  - Flota: 30 segundos
  - KPIs: 60 segundos

### ✅ Field Resolvers Eficientes
- `Pedido.repartidor`: Resuelve bajo demanda usando DataLoader
- Previene sobre-fetching y under-fetching

---

## 🗄️ ¿Necesita Base de Datos?

**NO.** Este es un **GraphQL Gateway/BFF** puro que:
- Agrega datos de 3 microservicios Java REST (Pedido, Fleet, Tracking)
- No persiste datos propios
- Caché en memoria (opcional: migrar a Redis para producción)

**Opcional**: Redis para caché distribuida en producción multi-instancia.

---

## 🛠️ Instalación

```bash
# Instalar dependencias
npm install

# Compilar TypeScript
npm run build

# Desarrollo con hot-reload
npm run dev:watch

# Producción
npm start
```

---

## 🔧 Configuración

Archivo [.env](.env):

```env
PORT=4000

# URLs de los microservicios Java
PEDIDO_SERVICE_URL=http://localhost:8084
FLEET_SERVICE_URL=http://localhost:8083
TRACKING_SERVICE_URL=http://localhost:8090

HTTP_TIMEOUT=5000
```

---

## 🧪 Ejemplos de Consultas

Ver archivo [queries.graphql](queries.graphql) para ejemplos completos.

### Dashboard Supervisor (Query principal de la documentación)

```graphql
query PedidosEnZona($zonaId: ID!, $estado: EstadoPedido) {
  pedidos(filtro: { zonaId: $zonaId, estado: $estado }) {
    id
    cliente { nombre }
    destino
    estado
    repartidor {
      nombre
      vehiculo { tipo }
    }
    tiempoTranscurrido
    retrasoMin
  }
  
  flotaResumen(zonaId: $zonaId) {
    total
    disponibles
    enRuta
  }
}
```

**Variables**:
```json
{
  "zonaId": "ZONA-01",
  "estado": "EN_RUTA"
}
```

### KPIs Diarios (Nueva feature)

```graphql
query KPIDiario($fecha: String!, $zonaId: ID) {
  kpiDiario(fecha: $fecha, zonaId: $zonaId) {
    zonaId
    fecha
    pedidosPendientes
    pedidosEnRuta
    pedidosEntregados
    tiempoPromedioEntrega
    repartidoresActivos
  }
}
```

### Métricas de Caché (Monitoreo)

```graphql
query MetricasCache {
  cacheMetrics {
    flotaCache { hits misses total hitRate size }
    kpiCache { hits misses total hitRate size }
    pedidoCache { hits misses total hitRate size }
  }
}
```

---

## 📊 Playground GraphQL

Una vez iniciado el servidor:

```
🚀 http://localhost:4000
```

---

## 🚀 Arquitectura

```
┌─────────────────────────────────────────────┐
│  Cliente (Dashboard Supervisor)            │
└────────────────┬────────────────────────────┘
                 │ GraphQL Query
                 ▼
┌─────────────────────────────────────────────┐
│  Apollo Server (este microservicio)        │
│  • Resolvers con DataLoaders               │
│  • Caché en memoria + métricas             │
│  • Field resolvers (N+1 prevention)        │
└─┬─────────────┬─────────────┬───────────────┘
  │             │             │
  │ REST        │ REST        │ REST
  ▼             ▼             ▼
┌──────────┐ ┌──────────┐ ┌──────────────┐
│ Pedidos  │ │  Fleet   │ │  Tracking    │
│ (8084)   │ │ (8083)   │ │  (8090)      │
└──────────┘ └──────────┘ └──────────────┘
```

---

## 📈 Métricas de Rendimiento

El sistema incluye métricas de caché automáticas:

- **hit rate**: % de consultas servidas desde caché
- **hits**: Consultas exitosas desde caché
- **misses**: Consultas que requirieron fetch a microservicios
- **size**: Número de entradas en caché

Consulta `cacheMetrics` para ver estadísticas en tiempo real.

---

## 🔥 Optimizaciones Implementadas

1. **DataLoader**: Batching automático de requests a Fleet Service
2. **Caché por tipo**: TTLs optimizados según volatilidad de datos
3. **Field Resolvers**: Solo resuelve campos solicitados por el cliente
4. **Context por request**: DataLoaders frescos en cada request (evita cache stale)

---

## 📝 Cumplimiento de Requisitos

### ✅ Requisitos Cumplidos

- [x] Schema GraphQL con tipos relacionados (Pedido, Repartidor, Vehiculo, KPI)
- [x] Resolvers eficientes con DataLoaders (prevención N+1)
- [x] Query `PedidosEnZona` implementada según documentación
- [x] Métricas de rendimiento (cache hit/miss)
- [x] Queries implementadas:
  - [x] `pedido(id: ID!): Pedido`
  - [x] `pedidos(filtro: PedidoFiltro): [Pedido!]!`
  - [x] `flotaActiva(zonaId: ID!): FlotaResumen` (implementado como `flotaResumen`)
  - [x] `kpiDiario(fecha: Date!, zonaId: ID): KPIDiario`
- [x] Servidor GraphQL funcional con Apollo Server v4

---

## 🧰 Stack Tecnológico

- **Apollo Server v4**: Framework GraphQL
- **TypeScript**: Type-safety
- **DataLoader**: Batching y cache
- **Axios**: HTTP client para microservicios
- **Node.js**: Runtime

---

## 📦 Dependencias Agregadas

```json
{
  "dataloader": "^2.2.2"  // Para prevención N+1
}
```

---

## 🔄 Próximos Pasos (Opcionales)

- [ ] Migrar caché a **Redis** para multi-instancia
- [ ] Agregar **subscriptions** para updates en tiempo real
- [ ] Implementar **batch endpoint** en Fleet Service
- [ ] Agregar **Apollo Federation** si se escala a múltiples GraphQL servers
- [ ] Tests unitarios con Jest
- [ ] Instrumentación con OpenTelemetry

---

## 👨‍💻 Desarrollo

```bash
# Modo desarrollo con watch
npm run dev:watch

# Verificar compilación
npm run build

# Producción
npm start
```

---

## 📄 Licencia

ISC
