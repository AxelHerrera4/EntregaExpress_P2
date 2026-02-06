import DataLoader from 'dataloader';
import { 
  FleetServiceClient, 
  RepartidorResponse, 
  VehiculoResponse,
  BillingServiceClient,
  FacturaResponse
} from '../../services';

export interface Pedido {
  id: string;
  repartidorId?: string;
  vehiculoId?: string;
  facturaId?: string;
}

export interface GraphQLContext {
  repartidorLoader: DataLoader<string, RepartidorResponse | null>;
  vehiculoLoader: DataLoader<string, VehiculoResponse | null>;
  fleetClient: FleetServiceClient;
  billingClient: BillingServiceClient;
}

/**
 * Field Resolver para Pedido
 * Resuelve los campos repartidor, vehiculo y factura bajo demanda
 */
export const pedidoFieldResolvers = {
  /**
   * Resolver para Pedido.repartidor
   */
  repartidor: async (
    parent: Pedido,
    _args: unknown,
    context: GraphQLContext
  ): Promise<RepartidorResponse | null> => {
    if (!parent.repartidorId) {
      return null;
    }
    return context.repartidorLoader.load(parent.repartidorId);
  },

  /**
   * Resolver para Pedido.vehiculo
   */
  vehiculo: async (
    parent: Pedido,
    _args: unknown,
    context: GraphQLContext
  ): Promise<VehiculoResponse | null> => {
    if (!parent.vehiculoId) {
      return null;
    }
    return context.vehiculoLoader.load(parent.vehiculoId);
  },

  /**
   * Resolver para Pedido.factura
   * Llama al billing-service para obtener la factura
   */
  factura: async (
    parent: Pedido,
    _args: unknown,
    context: GraphQLContext
  ): Promise<{ id: string; pedidoId: string; monto: number; estado: string; fechaEmision?: string } | null> => {
    // Intentar por facturaId primero
    if (parent.facturaId) {
      try {
        const factura = await context.billingClient.obtenerFacturaPorId(parent.facturaId);
        if (factura) {
          return {
            id: factura.id,
            pedidoId: factura.pedidoId,
            monto: factura.montoTotal,
            estado: factura.estado,
            fechaEmision: factura.fechaCreacion
          };
        }
      } catch (error) {
        console.error(`[PedidoResolver] Error obteniendo factura por ID:`, error);
      }
    }
    
    // Intentar por pedidoId
    try {
      const factura = await context.billingClient.obtenerFacturaPorPedidoId(parent.id);
      if (factura) {
        return {
          id: factura.id,
          pedidoId: factura.pedidoId,
          monto: factura.montoTotal,
          estado: factura.estado,
          fechaEmision: factura.fechaCreacion
        };
      }
    } catch (error) {
      console.error(`[PedidoResolver] Error obteniendo factura por pedidoId:`, error);
    }
    
    return null;
  }
};
