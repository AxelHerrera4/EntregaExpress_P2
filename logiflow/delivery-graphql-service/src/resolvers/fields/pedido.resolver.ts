import DataLoader from 'dataloader';
import { Pedido, RepartidorDetalle } from '../../entities';

/**
 * Field Resolver para Pedido - Resuelve el campo "repartidor" bajo demanda
 * Equivalente a PedidoFieldResolver.java
 *
 * Evita el problema N+1 usando DataLoader para batching automático
 */
export const pedidoFieldResolvers = {
  /**
   * Resolver para Pedido.repartidor
   * Si el pedido tiene repartidorId, usa DataLoader para batch loading
   */
  repartidor: async (
    parent: Pedido,
    _args: unknown,
    context: { repartidorLoader: DataLoader<string, RepartidorDetalle | null> }
  ): Promise<RepartidorDetalle | null> => {
    if (!parent.repartidorId) {
      return null;
    }
    // DataLoader agrupa automáticamente múltiples llamadas en un batch
    return context.repartidorLoader.load(parent.repartidorId);
  },
};
