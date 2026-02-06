import { pedidoClient } from '../utils';
import { Pedido, FiltroPedidoInput } from '../entities';

/**
 * PedidoService - Comunicación con el microservicio de Pedidos (puerto 8084)
 */
export class PedidoService {
  /**
   * Obtiene pedidos filtrados por zona, estado y/o repartidor
   * GET /api/pedidos?zonaId={id}&estado={estado}&repartidorId={id}
   */
  async obtenerPedidosFiltrados(filtro: FiltroPedidoInput): Promise<Pedido[]> {
    try {
      const params: Record<string, string> = {};

      if (filtro.zonaId) params.zonaId = filtro.zonaId;
      if (filtro.estado) params.estado = filtro.estado;
      if (filtro.repartidorId) params.repartidorId = filtro.repartidorId;

      const response = await pedidoClient.get<Pedido[]>('/api/pedidos', { params });
      return response.data;
    } catch (error) {
      console.error('[PedidoService] Error al obtener pedidos filtrados:', error);
      return [];
    }
  }

  /**
   * Obtiene el detalle de un pedido específico
   * GET /api/pedidos/{id}
   */
  async obtenerPedidoPorId(id: string): Promise<Pedido | null> {
    try {
      const response = await pedidoClient.get<Pedido>(`/api/pedidos/${id}`);
      return response.data;
    } catch (error) {
      console.error(`[PedidoService] Error al obtener pedido ${id}:`, error);
      return null;
    }
  }
}
