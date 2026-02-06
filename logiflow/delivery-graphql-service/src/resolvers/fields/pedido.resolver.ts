import { FleetServiceClient } from '../../services';
import { Pedido, RepartidorDetalle } from '../../entities';

/**
 * Field Resolver para Pedido - Resuelve el campo "repartidor" bajo demanda
 * Equivalente a PedidoFieldResolver.java
 *
 * Evita el problema N+1: solo se llama cuando el cliente solicita el campo "repartidor"
 */
export const pedidoFieldResolvers = {
  /**
   * Resolver para Pedido.repartidor
   * Si el pedido tiene repartidorId, consulta Fleet Service para obtener detalle
   */
  repartidor: async (
    parent: Pedido,
    _args: unknown,
    context: { fleetClient: FleetServiceClient }
  ): Promise<RepartidorDetalle | null> => {
    if (!parent.repartidorId) {
      return null;
    }
    return context.fleetClient.obtenerRepartidor(parent.repartidorId);
  },
};
