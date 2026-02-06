import { 
  PedidoService, 
  IncidenciaServiceClient 
} from '../../services';

/**
 * Input para asignar pedido
 */
export interface AsignarPedidoInput {
  pedidoId: string;
  repartidorId: string;
  vehiculoId: string;
}

/**
 * Input para cancelar pedido
 */
export interface CancelarPedidoInput {
  pedidoId: string;
  motivo?: string;
}

/**
 * Input para registrar incidencia
 */
export interface RegistrarIncidenciaInput {
  pedidoId: string;
  descripcion: string;
  tipo: string;
}

/**
 * Contexto de GraphQL
 */
export interface GraphQLContext {
  pedidoService: PedidoService;
  incidenciaClient: IncidenciaServiceClient;
}

/**
 * Mutation Resolvers simplificados
 * 
 * Mutations disponibles:
 * - asignarPedido: Asigna repartidor y vehículo a un pedido
 * - cancelarPedido: Cancela un pedido
 * - registrarIncidencia: Registra una incidencia
 */
export const mutationResolvers = {
  /**
   * Mutation: asignarPedido(input: AsignarPedidoInput!): Pedido!
   * PATCH /api/pedidos/{id}/asignar
   */
  asignarPedido: async (
    _parent: unknown,
    args: { input: AsignarPedidoInput },
    context: GraphQLContext
  ) => {
    const { pedidoId, repartidorId, vehiculoId } = args.input;
    console.log(`[Mutation] asignarPedido(pedidoId: ${pedidoId}, repartidorId: ${repartidorId}, vehiculoId: ${vehiculoId})`);
    
    try {
      const pedido = await context.pedidoService.asignarRepartidorYVehiculo(
        pedidoId,
        repartidorId,
        vehiculoId
      );
      console.log(`[Mutation] Pedido ${pedidoId} asignado correctamente`);
      return pedido;
    } catch (error: any) {
      console.error(`[Mutation] Error al asignar pedido:`, error.message);
      throw new Error(`Error al asignar pedido: ${error.message}`);
    }
  },

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
  },

  /**
   * Mutation: registrarIncidencia(input: RegistrarIncidenciaInput!): Incidencia!
   */
  registrarIncidencia: async (
    _parent: unknown,
    args: { input: RegistrarIncidenciaInput },
    context: GraphQLContext
  ) => {
    const { pedidoId, descripcion, tipo } = args.input;
    console.log(`[Mutation] registrarIncidencia(pedidoId: ${pedidoId})`);
    
    try {
      const incidencia = await context.incidenciaClient.registrarIncidencia({
        pedidoId,
        descripcion,
        tipo: tipo as any
      });
      console.log(`[Mutation] Incidencia registrada con ID ${incidencia.id}`);
      return incidencia;
    } catch (error: any) {
      console.error(`[Mutation] Error al registrar incidencia:`, error.message);
      throw new Error(`Error al registrar incidencia: ${error.message}`);
    }
  }
};
