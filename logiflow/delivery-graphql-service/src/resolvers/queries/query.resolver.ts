import {
  PedidoService,
  FlotaService,
  KpiService,
} from '../../services';
import { FiltroPedidoInput, Pedido, RepartidorEnMapa, FlotaResumen, Kpi } from '../../entities';
import { flotaCache, kpiCache, pedidoCache, CacheMetrics } from '../../utils';

/**
 * Query Resolvers - Equivalente al QueryResolver.java
 */
export const queryResolvers = {
  /**
   * Query: pedidos(filtro: FiltroPedido!): [Pedido]!
   * Obtiene pedidos filtrados por zona, estado, repartidor
   */
  pedidos: async (
    _parent: unknown,
    args: { filtro: FiltroPedidoInput },
    context: { pedidoService: PedidoService }
  ): Promise<Pedido[]> => {
    const cacheKey = `pedidos:${JSON.stringify(args.filtro)}`;
    return pedidoCache.getOrCompute(cacheKey, () =>
      context.pedidoService.obtenerPedidosFiltrados(args.filtro)
    );
  },

  /**
   * Query: flotaActiva(zonaId: ID!): [RepartidorEnMapa]!
   * Flota activa en mapa con ubicación en tiempo real
   */
  flotaActiva: async (
    _parent: unknown,
    args: { zonaId: string },
    context: { flotaService: FlotaService }
  ): Promise<RepartidorEnMapa[]> => {
    const cacheKey = `flotaActiva:${args.zonaId}`;
    return flotaCache.getOrCompute(cacheKey, () =>
      context.flotaService.obtenerFlotaActivaConUbicacion(args.zonaId)
    );
  },

  /**
   * Query: flotaResumen(zonaId: ID!): FlotaResumen!
   * Resumen de flota por zona
   */
  flotaResumen: async (
    _parent: unknown,
    args: { zonaId: string },
    context: { flotaService: FlotaService }
  ): Promise<FlotaResumen> => {
    const cacheKey = `flotaResumen:${args.zonaId}`;
    return flotaCache.getOrCompute(cacheKey, () =>
      context.flotaService.obtenerResumenFlota(args.zonaId)
    );
  },

  /**
   * Query: kpis(zonaId: ID!): KPI!
   * KPIs calculados para una zona
   */
  kpis: async (
    _parent: unknown,
    args: { zonaId: string },
    context: { kpiService: KpiService }
  ): Promise<Kpi> => {
    const cacheKey = `kpis:${args.zonaId}`;
    return kpiCache.getOrCompute(cacheKey, () =>
      context.kpiService.calcularKpis(args.zonaId)
    );
  },

  /**
   * Query: kpiDiario(fecha: String!, zonaId: ID): KPI!
   * KPIs diarios calculados para una fecha y zona específica
   */
  kpiDiario: async (
    _parent: unknown,
    args: { fecha: string; zonaId?: string },
    context: { kpiService: KpiService }
  ): Promise<Kpi> => {
    const cacheKey = `kpiDiario:${args.fecha}:${args.zonaId || 'all'}`;
    return kpiCache.getOrCompute(cacheKey, () =>
      context.kpiService.calcularKpisDiarios(args.fecha, args.zonaId)
    );
  },

  /**
   * Query: pedido(id: ID!): Pedido
   * Detalle de un pedido específico
   */
  pedido: async (
    _parent: unknown,
    args: { id: string },
    context: { pedidoService: PedidoService }
  ): Promise<Pedido | null> => {
    const cacheKey = `pedido:${args.id}`;
    return pedidoCache.getOrCompute(cacheKey, () =>
      context.pedidoService.obtenerPedidoPorId(args.id)
    );
  },

  /**
   * Query: cacheMetrics: CacheMetricsResult!
   * Métricas de rendimiento del caché (hit/miss rates)
   */
  cacheMetrics: async (): Promise<{
    flotaCache: CacheMetrics & { size: number };
    kpiCache: CacheMetrics & { size: number };
    pedidoCache: CacheMetrics & { size: number };
  }> => {
    return {
      flotaCache: { ...flotaCache.getMetrics(), size: flotaCache.size() },
      kpiCache: { ...kpiCache.getMetrics(), size: kpiCache.size() },
      pedidoCache: { ...pedidoCache.getMetrics(), size: pedidoCache.size() },
    };
  },
};
