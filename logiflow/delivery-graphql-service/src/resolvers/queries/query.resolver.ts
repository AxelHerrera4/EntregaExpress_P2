import { 
  PedidoService, 
  FleetServiceClient,
  FlotaService,
  KpiService
} from '../../services';
import { flotaCache, pedidoCache } from '../../utils';

/**
 * Query Resolvers simplificados
 * 
 * Queries disponibles:
 * - pedido(id): Pedido individual
 * - pedidos: Lista de todos los pedidos
 * - pedidosPendientesAsignacion: Pedidos sin asignar
 * - repartidor(id): Repartidor individual
 * - repartidores: Lista de repartidores
 * - vehiculo(id): Vehículo individual
 * - vehiculos: Lista de vehículos
 * - flotaActiva: Repartidores en mapa
 * - flotaResumen: Resumen de flota
 * - estadisticasPorCobertura: KPIs
 * - rutasPopulares: Rutas más usadas
 * - cacheMetrics: Métricas de caché
 */
export const queryResolvers = {
  /**
   * Query: pedido(id: ID!): Pedido
   */
  pedido: async (
    _parent: unknown,
    args: { id: string },
    context: { pedidoService: PedidoService }
  ) => {
    const cacheKey = `pedido:${args.id}`;
    return pedidoCache.getOrCompute(cacheKey, async () => {
      console.log(`[Query] pedido(${args.id})`);
      return context.pedidoService.obtenerPedidoPorId(args.id);
    });
  },

  /**
   * Query: pedidos: [Pedido]!
   */
  pedidos: async (
    _parent: unknown,
    _args: unknown,
    context: { pedidoService: PedidoService }
  ) => {
    const cacheKey = 'pedidos:all';
    return pedidoCache.getOrCompute(cacheKey, async () => {
      console.log('[Query] pedidos');
      const result = await context.pedidoService.obtenerTodosLosPedidos();
      return result || [];
    });
  },

  /**
   * Query: pedidosPendientesAsignacion: [Pedido]!
   */
  pedidosPendientesAsignacion: async (
    _parent: unknown,
    _args: unknown,
    context: { pedidoService: PedidoService }
  ) => {
    const cacheKey = 'pedidos:pendientes';
    return pedidoCache.getOrCompute(cacheKey, async () => {
      console.log('[Query] pedidosPendientesAsignacion');
      const result = await context.pedidoService.obtenerPedidosPendientesAsignacion();
      return result || [];
    });
  },

  /**
   * Query: repartidor(id: ID!): Repartidor
   */
  repartidor: async (
    _parent: unknown,
    args: { id: string },
    context: { fleetClient: FleetServiceClient }
  ) => {
    const cacheKey = `repartidor:${args.id}`;
    return flotaCache.getOrCompute(cacheKey, async () => {
      console.log(`[Query] repartidor(${args.id})`);
      return context.fleetClient.obtenerRepartidor(args.id);
    });
  },

  /**
   * Query: repartidores: [Repartidor]!
   */
  repartidores: async (
    _parent: unknown,
    _args: unknown,
    context: { fleetClient: FleetServiceClient }
  ) => {
    const cacheKey = 'repartidores:all';
    return flotaCache.getOrCompute(cacheKey, async () => {
      console.log('[Query] repartidores');
      const result = await context.fleetClient.obtenerTodosLosRepartidores();
      return result || [];
    });
  },

  /**
   * Query: vehiculo(id: ID!): Vehiculo
   */
  vehiculo: async (
    _parent: unknown,
    args: { id: string },
    context: { fleetClient: FleetServiceClient }
  ) => {
    const cacheKey = `vehiculo:${args.id}`;
    return flotaCache.getOrCompute(cacheKey, async () => {
      console.log(`[Query] vehiculo(${args.id})`);
      return context.fleetClient.obtenerVehiculo(args.id);
    });
  },

  /**
   * Query: vehiculos: [Vehiculo]!
   */
  vehiculos: async (
    _parent: unknown,
    _args: unknown,
    context: { fleetClient: FleetServiceClient }
  ) => {
    const cacheKey = 'vehiculos:all';
    return flotaCache.getOrCompute(cacheKey, async () => {
      console.log('[Query] vehiculos');
      const result = await context.fleetClient.obtenerTodosLosVehiculos();
      return result || [];
    });
  },

  /**
   * Query: flotaActiva: [RepartidorEnMapa]!
   */
  flotaActiva: async (
    _parent: unknown,
    _args: unknown,
    context: { flotaService: FlotaService }
  ) => {
    const cacheKey = 'flota:activa';
    return flotaCache.getOrCompute(cacheKey, async () => {
      console.log('[Query] flotaActiva');
      const result = await context.flotaService.obtenerFlotaActivaConUbicacion();
      return result || [];
    });
  },

  /**
   * Query: flotaResumen: FlotaResumen!
   */
  flotaResumen: async (
    _parent: unknown,
    _args: unknown,
    context: { flotaService: FlotaService }
  ) => {
    const cacheKey = 'flota:resumen';
    return flotaCache.getOrCompute(cacheKey, async () => {
      console.log('[Query] flotaResumen');
      return context.flotaService.obtenerResumenFlota();
    });
  },

  /**
   * Query: estadisticasPorCobertura(cobertura: String!): EstadisticasCobertura!
   */
  estadisticasPorCobertura: async (
    _parent: unknown,
    args: { cobertura: string },
    context: { kpiService: KpiService }
  ) => {
    const cacheKey = `kpi:cobertura:${args.cobertura}`;
    return pedidoCache.getOrCompute(cacheKey, async () => {
      console.log(`[Query] estadisticasPorCobertura(${args.cobertura})`);
      return context.kpiService.calcularKpisPorCobertura(args.cobertura);
    });
  },

  /**
   * Query: rutasPopulares(limite: Int): [RutaPopular!]!
   */
  rutasPopulares: async (
    _parent: unknown,
    args: { limite?: number },
    context: { pedidoService: PedidoService }
  ) => {
    const limite = args.limite || 10;
    console.log(`[Query] rutasPopulares(limite: ${limite})`);
    
    try {
      const pedidos = await context.pedidoService.obtenerTodosLosPedidos();
      
      // Calcular rutas populares
      const rutasMap = new Map<string, number>();
      for (const pedido of pedidos || []) {
        const origen = pedido.direccionOrigen?.ciudad || 'Desconocido';
        const destino = pedido.direccionDestino?.ciudad || 'Desconocido';
        const key = `${origen}-${destino}`;
        rutasMap.set(key, (rutasMap.get(key) || 0) + 1);
      }
      
      const rutas = Array.from(rutasMap.entries())
        .map(([key, cantidad]) => {
          const [origen, destino] = key.split('-');
          return { origen, destino, cantidad };
        })
        .sort((a, b) => b.cantidad - a.cantidad)
        .slice(0, limite);
      
      return rutas;
    } catch (error) {
      console.error('[Query] Error en rutasPopulares:', error);
      return [];
    }
  },

  /**
   * Query: cacheMetrics: CacheMetricsResult!
   */
  cacheMetrics: () => {
    console.log('[Query] cacheMetrics');
    return {
      flotaCache: flotaCache.getMetrics(),
      pedidoCache: pedidoCache.getMetrics()
    };
  }
};
