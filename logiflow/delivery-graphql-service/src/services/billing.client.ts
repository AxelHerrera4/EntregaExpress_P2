import { billingClient } from '../utils/axiosClients';

/**
 * Respuesta de factura del billing-service
 */
export interface FacturaResponse {
  id: string;
  pedidoId: string;
  tipoEntrega: string;
  montoTotal: number;
  estado: string;
  fechaCreacion: string;
  distanciaKm: number;
}

/**
 * Cliente para el billing-service
 * Endpoints:
 * - GET /billing/facturas/{id} -> Factura por ID
 * - GET /billing/facturas/pedido/{pedidoId} -> Factura por pedidoId
 */
export class BillingServiceClient {

  /**
   * Obtener factura por ID
   */
  async obtenerFacturaPorId(id: string): Promise<FacturaResponse | null> {
    try {
      console.log(`[BillingClient] GET /billing/facturas/${id}`);
      const response = await billingClient.get<FacturaResponse>(`/billing/facturas/${id}`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        console.log(`[BillingClient] Factura ${id} no encontrada`);
        return null;
      }
      console.error(`[BillingClient] Error obteniendo factura ${id}:`, error.message);
      throw error;
    }
  }

  /**
   * Obtener factura por pedidoId
   */
  async obtenerFacturaPorPedidoId(pedidoId: string): Promise<FacturaResponse | null> {
    try {
      console.log(`[BillingClient] GET /billing/facturas/pedido/${pedidoId}`);
      const response = await billingClient.get<FacturaResponse>(`/billing/facturas/pedido/${pedidoId}`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        console.log(`[BillingClient] Factura para pedido ${pedidoId} no encontrada`);
        return null;
      }
      console.error(`[BillingClient] Error obteniendo factura de pedido ${pedidoId}:`, error.message);
      throw error;
    }
  }
}
