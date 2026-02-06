import gql from 'graphql-tag';

export const typeDefs = gql`
  # ==================== TYPES ====================

  type Pedido {
    id: ID!
    clienteId: String!
    cliente: Cliente
    destino: String!
    estado: EstadoPedido!
    repartidorId: String
    repartidor: RepartidorDetalle
    tiempoTranscurrido: Int
    retrasoMin: Int
    fechaCreacion: String
    fechaActualizacion: String
  }

  type Cliente {
    nombre: String!
    telefono: String
    direccion: String
  }

  type RepartidorDetalle {
    id: ID!
    nombre: String!
    vehiculo: Vehiculo
    disponible: Boolean!
  }

  type Vehiculo {
    id: ID!
    tipo: TipoVehiculo!
    placa: String!
    modelo: String
    capacidad: Float
  }

  type RepartidorEnMapa {
    id: ID!
    nombre: String!
    placa: String!
    latitud: Float!
    longitud: Float!
    estado: EstadoRepartidor!
    velocidad: Float
    ultimaActualizacion: String
  }

  type FlotaResumen {
    total: Int!
    disponibles: Int!
    enRuta: Int!
  }

  type KPI {
    zonaId: ID!
    pedidosPendientes: Int!
    pedidosEnRuta: Int!
    pedidosEntregados: Int!
    tiempoPromedioEntrega: Float
    repartidoresActivos: Int!
    fecha: String
  }

  # ==================== ENUMS ====================

  enum EstadoPedido {
    PENDIENTE
    ASIGNADO
    EN_RUTA
    ENTREGADO
    CANCELADO
  }

  enum TipoVehiculo {
    MOTO
    AUTO
    CAMIONETA
    BICICLETA
  }

  enum EstadoRepartidor {
    DISPONIBLE
    EN_RUTA
    DESCONECTADO
  }

  # ==================== INPUT ====================

  input FiltroPedido {
    zonaId: ID
    estado: EstadoPedido
    repartidorId: ID
  }

  # ==================== QUERIES ====================

  type Query {
    # Pedidos filtrados por zona, estado, repartidor
    pedidos(filtro: FiltroPedido!): [Pedido]!

    # Flota activa en mapa (con ubicacion en tiempo real)
    flotaActiva(zonaId: ID!): [RepartidorEnMapa]!

    # Resumen de flota
    flotaResumen(zonaId: ID!): FlotaResumen!

    # KPIs por zona
    kpis(zonaId: ID!): KPI!

    # KPIs diarios por fecha y zona (requiere fecha)
    kpiDiario(fecha: String!, zonaId: ID): KPI!

    # Detalle de un pedido
    pedido(id: ID!): Pedido

    # Métricas de caché (para monitoreo)
    cacheMetrics: CacheMetricsResult!
  }

  # Métricas de rendimiento del caché
  type CacheMetricsResult {
    flotaCache: CacheStats!
    kpiCache: CacheStats!
    pedidoCache: CacheStats!
  }

  type CacheStats {
    hits: Int!
    misses: Int!
    total: Int!
    hitRate: Float!
    size: Int!
  }
`;
