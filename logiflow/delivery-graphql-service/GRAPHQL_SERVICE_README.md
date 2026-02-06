# LogiFlow GraphQL Service

## Descripción
Microservicio GraphQL que proporciona una API unificada para consultas complejas y mutaciones del sistema LogiFlow. Implementa las 4 mutaciones específicas requeridas y queries avanzadas por zona/ciudad.

## Funcionalidades Implementadas

### 🔄 Mutaciones Específicas

#### 1. Gestión de Disponibilidad del Repartidor
```graphql
mutation ActualizarEstadoRepartidor($input: ActualizarEstadoRepartidorInput!) {
  actualizarEstadoRepartidor(input: $input) {
    id
    nombre
    disponible
  }
}
```
- **Estados posibles**: DISPONIBLE, EN_RUTA, DESCONECTADO, MANTENIMIENTO
- **Ideal para**: Supervisor cambie estado de conductor que tuvo inconveniente o terminó turno

#### 2. Reasignación Manual de Pedidos
```graphql
mutation ReasignarPedido($input: ReasignarPedidoInput!) {
  reasignarPedido(input: $input) {
    id
    estado
    repartidor {
      nombre
    }
  }
}
```
- **Funcionalidad clave**: Herramienta de reasignación manual mediante "arrastrar y soltar"
- **Impacto**: Dispara internamente eventos en RabbitMQ para notificar al nuevo repartidor

#### 3. Actualización de Perfil y Preferencias
```graphql
mutation ActualizarDatosContacto($input: ActualizarDatosContactoInput!) {
  actualizarDatosContacto(input: $input) {
    id
    nombre
    telefono
    email
  }
}
```
- **Para**: Cliente o Administrador gestione datos básicos sin orquestación compleja

#### 4. Gestión de Incidencias
```graphql
mutation RegistrarIncidencia($input: RegistrarIncidenciaInput!) {
  registrarIncidencia(input: $input) {
    id
    pedidoId
    descripcion
    tipo
  }
}
```
- **Tipos de incidencia**: PAQUETE_DANADO, DIRECCION_INCORRECTA, CLIENTE_NO_ENCONTRADO, VEHICULO_AVERIADO, RETRASO_TRAFICO, OTRO

### 📍 Queries por Zona/Ciudad

#### Queries por Zona
- `pedidosPorZona(zonaId: ID!, estado: EstadoPedido)`

#### Queries por Ciudad
- `pedidosPorCiudadOrigen(ciudad: String!, provincia: String)`
- `pedidosPorCiudadDestino(ciudad: String!, provincia: String)`
- `pedidosPorRuta(ciudadOrigen: String!, ciudadDestino: String!)`

#### Estadísticas
- `estadisticasPorCiudad(ciudad: String!, tipo: String!)`

## Configuración API Gateway

El servicio está configurado en el API Gateway para ser accesible a través de:

```yaml
# GraphQL Service - delivery-graphql-service
- id: graphql-service
  uri: http://localhost:4000
  predicates:
    - Path=/graphql/**
  filters:
    - RewritePath=/graphql(?<path>.*), ${path}
```

### URLs de Acceso:
- **GraphQL Endpoint**: `http://localhost:8000/graphql`
- **GraphQL Playground**: `http://localhost:8000/graphql` (interfaz web para testing)

## Arquitectura

### Microservicios Integrados:
- **Auth Service** (puerto 8081): Gestión de usuarios y autenticación
- **Pedido Service** (puerto 8084): Operaciones de pedidos y incidencias  
- **Fleet Service** (puerto 8083): Gestión de repartidores y vehículos
- **Tracking Service** (puerto 8090): Ubicaciones GPS en tiempo real

### Características Técnicas:
- ✅ **DataLoaders**: Prevención de problema N+1
- ✅ **Caché en memoria**: Con métricas de hit/miss rate
- ✅ **Apollo Server v4**: Framework GraphQL moderno
- ✅ **TypeScript**: Tipado fuerte en toda la aplicación
- ✅ **Resolvers eficientes**: Con field resolvers especializados

## Ejecución

### Desarrollo:
```bash
cd delivery-graphql-service
npm run dev
```

### Producción:
```bash
npm run build
npm start
```

### Puertos:
- **GraphQL Service**: 4000 (directo)
- **A través de API Gateway**: 8000/graphql

## Queries de Ejemplo

Ver archivo `queries-examples.graphql` para ejemplos completos de:
- Dashboard de Supervisor
- Filtros por ciudad/zona
- Mutaciones con variables
- Métricas de rendimiento

## Universidad de las Fuerzas Armadas ESPE
**Departamento de Ciencias de la Computación**  
**Carrera de Ingeniería en Software**  
**Aplicaciones Distribuidas**  

**Proyecto Integrador Parcial II**  
**LogiFlow – Plataforma Integral de Gestión de Operaciones para EntregaExpress S.A.**