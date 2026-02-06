import {
  PedidoService,
  FlotaService,
  KpiService,
} from '../../services';
import { FiltroPedidoInput, Pedido, RepartidorEnMapa, FlotaResumen, Kpi } from '../../entities';

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
    return context.pedidoService.obtenerPedidosFiltrados(args.filtro);
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
    return context.flotaService.obtenerFlotaActivaConUbicacion(args.zonaId);
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
    return context.flotaService.obtenerResumenFlota(args.zonaId);
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
    return context.kpiService.calcularKpis(args.zonaId);
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
    return context.pedidoService.obtenerPedidoPorId(args.id);
  },
};
