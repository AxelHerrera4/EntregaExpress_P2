import { PedidoService } from '../../services';

/**
 * Input para cancelar pedido
 */
export interface CancelarPedidoInput {
  pedidoId: string;
  motivo?: string;
}

/**
 * Contexto de GraphQL
 */
export interface GraphQLContext {
  pedidoService: PedidoService;
}

/**
 * Mutation Resolvers
 * 
 * Mutations disponibles:
 * - cancelarPedido: Cancela un pedido
 */
export const mutationResolvers = {
  /**
   * Mutation: cancelarPedido(input: CancelarPedidoInput!): Pedido!
   * PATCH /api/pedidos/{id}/cancelar
   */
  cancelarPedido: async (
    _parent: unknown,
    args: { input: CancelarPedidoInput },
    context: GraphQLContext
  ) => {
    const { pedidoId, motivo } = args.input;
    console.log(`[Mutation] cancelarPedido(${pedidoId}, motivo: ${motivo || 'N/A'})`);
    
    try {
      const pedido = await context.pedidoService.cancelarPedido(pedidoId, motivo);
      console.log(`[Mutation] Pedido ${pedidoId} cancelado`);
      return pedido;
    } catch (error: any) {
      console.error(`[Mutation] Error al cancelar pedido:`, error.message);
      throw new Error(`Error al cancelar pedido: ${error.message}`);
    }
  }
};
