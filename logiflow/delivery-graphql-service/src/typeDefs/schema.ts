import gql from 'graphql-tag';

export const typeDefs = gql`
  # ==================== TYPES ====================

  type Pedido {
    id: ID!
    clienteId: String!
    cliente: Cliente
    origen: Ubicacion
    destino: String!
    ubicacionDestino: Ubicacion
    estado: EstadoPedido!
    repartidorId: String
    repartidor: RepartidorDetalle
    tiempoTranscurrido: Int
    retrasoMin: Int
    fechaCreacion: String
    fechaActualizacion: String
    zona: String
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

  type Usuario {
    id: ID!
    nombre: String!
    telefono: String!
    email: String!
    rol: String!
  }

  type Incidencia {
    id: ID!
    pedidoId: ID!
    descripcion: String!
    tipo: TipoIncidencia!
    fechaCreacion: String!
    resuelto: Boolean!
  }

  type Ubicacion {
    latitud: Float!
    longitud: Float!
    ciudad: String!
    provincia: String!
    direccion: String
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
    MANTENIMIENTO
  }

  enum TipoIncidencia {
    PAQUETE_DANADO
    DIRECCION_INCORRECTA
    CLIENTE_NO_ENCONTRADO
    VEHICULO_AVERIADO
    RETRASO_TRAFICO
    OTRO
  }

  # ==================== INPUT ====================

  input FiltroPedido {
    zonaId: ID
    estado: EstadoPedido
    repartidorId: ID
    ciudadOrigen: String
    ciudadDestino: String
    provinciaOrigen: String
    provinciaDestino: String
  }

  input ActualizarEstadoRepartidorInput {
    repartidorId: ID!
    estado: EstadoRepartidor!
    motivo: String
  }

  input ReasignarPedidoInput {
    pedidoId: ID!
    nuevoRepartidorId: ID!
    motivo: String
  }

  input ActualizarDatosContactoInput {
    usuarioId: ID!
    telefono: String!
    email: String!
    nombre: String
  }

  input RegistrarIncidenciaInput {
    pedidoId: ID!
    descripcion: String!
    tipo: TipoIncidencia!
  }

  # ==================== QUERIES ====================

  type Query {
    # Pedidos filtrados por zona, estado, repartidor, ciudades
    pedidos(filtro: FiltroPedido!): [Pedido]!

    # Pedidos por zona específica
    pedidosPorZona(zonaId: ID!, estado: EstadoPedido): [Pedido]!

    # Pedidos por ciudad origen
    pedidosPorCiudadOrigen(ciudad: String!, provincia: String): [Pedido]!

    # Pedidos por ciudad destino
    pedidosPorCiudadDestino(ciudad: String!, provincia: String): [Pedido]!

    # Pedidos por ruta (origen -> destino)
    pedidosPorRuta(ciudadOrigen: String!, ciudadDestino: String!): [Pedido]!

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

    # Estadísticas por ciudad/provincia
    estadisticasPorCiudad(ciudad: String!, tipo: String!): [KPI]!
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

  # ==================== MUTATIONS ====================

  type Mutation {
    # Gestión de Disponibilidad del Repartidor
    actualizarEstadoRepartidor(input: ActualizarEstadoRepartidorInput!): RepartidorDetalle!

    # Reasignación Manual de Pedidos
    reasignarPedido(input: ReasignarPedidoInput!): Pedido!

    # Actualización de Perfil y Preferencias
    actualizarDatosContacto(input: ActualizarDatosContactoInput!): Usuario!

    # Gestión de Incidencias
    registrarIncidencia(input: RegistrarIncidenciaInput!): Incidencia!
  }
`;
