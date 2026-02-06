import gql from 'graphql-tag';

export const typeDefs = gql`
  # ==================== TYPES ====================

  type Pedido {
    id: ID!
    clienteId: String!
    cliente: Cliente
    direccionOrigen: Direccion!
    direccionDestino: Direccion!
    modalidadServicio: ModalidadServicio!
    tipoEntrega: TipoEntrega!
    estado: EstadoPedido!
    peso: Float!
    telefonoContacto: String!
    nombreDestinatario: String
    fechaCreacion: String!
    fechaActualizacion: String!
    cobertura: String!
    repartidorId: String
    repartidor: Repartidor
    vehiculoId: String
    vehiculo: Vehiculo
    facturaId: String
    factura: Factura
    tarifa: Float
    tiempoTranscurrido: Int
    retrasoMin: Int
  }

  type Factura {
    id: ID!
    pedidoId: String!
    monto: Float!
    subtotal: Float
    impuestos: Float
    estado: String!
    fechaEmision: String
    fechaPago: String
    metodoPago: String
  }

  type Direccion {
    calle: String!
    numero: String!
    ciudad: String!
    provincia: String!
    latitud: Float!
    longitud: Float!
  }

  type Cliente {
    id: String!
    nombre: String!
    telefono: String
    email: String
    direccion: String
  }

  type Repartidor {
    id: ID!
    nombre: String!
    apellido: String
    documento: String
    tipoDocumento: String
    telefono: String
    email: String
    estado: String!
    zonaAsignada: String
    tipoLicencia: String
    vehiculoId: String
    vehiculo: VehiculoInfo
    ubicacionActual: UbicacionInfo
    fechaContratacion: String
    activo: Boolean
    createdAt: String
    updatedAt: String
  }

  type UbicacionInfo {
    latitud: Float!
    longitud: Float!
    ultimaActualizacion: String
  }

  type VehiculoInfo {
    placa: String
    tipo: String
    estado: String
  }

  type Vehiculo {
    id: ID!
    placa: String!
    tipo: String!
    marca: String
    modelo: String
    anio: Int
    capacidadCarga: Float
    estado: String!
    caracteristicasEspecificas: CaracteristicasEspecificas
    activo: Boolean
    createdAt: String
    updatedAt: String
  }

  type CaracteristicasEspecificas {
    cilindraje: Int
    tieneCajones: Boolean
    numeroPuertas: Int
    tipoCarroceria: String
    numeroEjes: Int
    capacidadVolumen: Float
  }

  type RepartidorEnMapa {
    id: ID!
    nombre: String!
    placa: String
    latitud: Float!
    longitud: Float!
    estado: String!
    velocidad: Float
    ultimaActualizacion: String
  }

  type FlotaResumen {
    total: Int!
    disponibles: Int!
    enRuta: Int!
    mantenimiento: Int!
    desconectados: Int!
  }

  type EstadisticasCobertura {
    cobertura: String!
    fecha: String
    pedidosTotal: Int!
    pedidosPendientes: Int!
    pedidosEnRuta: Int!
    pedidosEntregados: Int!
    pedidosCancelados: Int!
    tiempoPromedioEntrega: Float
    repartidoresActivos: Int!
  }

  type RutaPopular {
    origen: String!
    destino: String!
    cantidad: Int!
  }

  type Incidencia {
    id: ID!
    pedidoId: ID!
    pedido: Pedido
    descripcion: String!
    tipo: String!
    fechaCreacion: String!
    resuelto: Boolean!
  }

  # ==================== ENUMS ====================

  enum EstadoPedido {
    PENDIENTE
    ASIGNADO
    EN_RUTA
    ENTREGADO
    CANCELADO
  }

  enum ModalidadServicio {
    URBANA_RAPIDA
    INTERMUNICIPAL
    NACIONAL
  }

  enum TipoEntrega {
    STANDARD
    EXPRESS
    PREMIUM
  }

  # ==================== INPUT ====================

  input CancelarPedidoInput {
    pedidoId: ID!
    motivo: String
  }

  # ==================== QUERIES ====================

  type Query {
    # Obtener un pedido por ID con su factura
    pedido(id: ID!): Pedido

    # Listar todos los pedidos
    pedidos: [Pedido]!

    # Pedidos pendientes de asignación
    pedidosPendientesAsignacion: [Pedido]!

    # Obtener un repartidor por ID
    repartidor(id: ID!): Repartidor

    # Listar todos los repartidores
    repartidores: [Repartidor]!

    # Obtener un vehículo por ID
    vehiculo(id: ID!): Vehiculo

    # Listar todos los vehículos
    vehiculos: [Vehiculo]!

    # Flota activa (repartidores con ubicación)
    flotaActiva: [RepartidorEnMapa]!

    # Resumen de flota
    flotaResumen: FlotaResumen!

    # Estadísticas por cobertura
    estadisticasPorCobertura(cobertura: String!): EstadisticasCobertura!

    # Rutas más populares
    rutasPopulares(limite: Int = 10): [RutaPopular!]!

    # Métricas de caché
    cacheMetrics: CacheMetricsResult!
  }

  type CacheMetricsResult {
    flotaCache: CacheStats!
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
    # Cancelar un pedido
    cancelarPedido(input: CancelarPedidoInput!): Pedido!
  }
`;
