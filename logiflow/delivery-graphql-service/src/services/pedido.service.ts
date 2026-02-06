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
      if (filtro.ciudadOrigen) params.ciudadOrigen = filtro.ciudadOrigen;
      if (filtro.ciudadDestino) params.ciudadDestino = filtro.ciudadDestino;
      if (filtro.provinciaOrigen) params.provinciaOrigen = filtro.provinciaOrigen;
      if (filtro.provinciaDestino) params.provinciaDestino = filtro.provinciaDestino;

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

  /**
   * Obtiene pedidos por zona específica
   * GET /api/pedidos/zona/{zonaId}?estado={estado}
   */
  async obtenerPedidosPorZona(zonaId: string, estado?: string): Promise<Pedido[]> {
    try {
      const params: Record<string, string> = {};
      if (estado) params.estado = estado;

      const response = await pedidoClient.get<Pedido[]>(`/api/pedidos/zona/${zonaId}`, { params });
      return response.data;
    } catch (error) {
      console.error(`[PedidoService] Error al obtener pedidos por zona ${zonaId}:`, error);
      return [];
    }
  }

  /**
   * Obtiene pedidos por ciudad origen
   * GET /api/pedidos/ciudad-origen/{ciudad}?provincia={provincia}
   */
  async obtenerPedidosPorCiudadOrigen(ciudad: string, provincia?: string): Promise<Pedido[]> {
    try {
      const params: Record<string, string> = {};
      if (provincia) params.provincia = provincia;

      const response = await pedidoClient.get<Pedido[]>(`/api/pedidos/ciudad-origen/${ciudad}`, { params });
      return response.data;
    } catch (error) {
      console.error(`[PedidoService] Error al obtener pedidos por ciudad origen ${ciudad}:`, error);
      return [];
    }
  }

  /**
   * Obtiene pedidos por ciudad destino
   * GET /api/pedidos/ciudad-destino/{ciudad}?provincia={provincia}
   */
  async obtenerPedidosPorCiudadDestino(ciudad: string, provincia?: string): Promise<Pedido[]> {
    try {
      const params: Record<string, string> = {};
      if (provincia) params.provincia = provincia;

      const response = await pedidoClient.get<Pedido[]>(`/api/pedidos/ciudad-destino/${ciudad}`, { params });
      return response.data;
    } catch (error) {
      console.error(`[PedidoService] Error al obtener pedidos por ciudad destino ${ciudad}:`, error);
      return [];
    }
  }

  /**
   * Obtiene pedidos por ruta (origen -> destino)
   * GET /api/pedidos/ruta?ciudadOrigen={origen}&ciudadDestino={destino}
   */
  async obtenerPedidosPorRuta(ciudadOrigen: string, ciudadDestino: string): Promise<Pedido[]> {
    try {
      const params = {
        ciudadOrigen,
        ciudadDestino
      };

      const response = await pedidoClient.get<Pedido[]>('/api/pedidos/ruta', { params });
      return response.data;
    } catch (error) {
      console.error(`[PedidoService] Error al obtener pedidos por ruta ${ciudadOrigen} -> ${ciudadDestino}:`, error);
      return [];
    }
  }

  /**
   * Reasigna un pedido a un nuevo repartidor
   * PATCH /api/pedidos/{pedidoId}/reasignar
   */
  async reasignarPedido(pedidoId: string, nuevoRepartidorId: string, motivo?: string): Promise<Pedido> {
    try {
      const payload = { nuevoRepartidorId, motivo };
      const response = await pedidoClient.patch<Pedido>(`/api/pedidos/${pedidoId}/reasignar`, payload);
      
      console.log(`[PedidoService] Pedido ${pedidoId} reasignado a repartidor ${nuevoRepartidorId}`);
      return response.data;
    } catch (error) {
      console.error(`[PedidoService] Error al reasignar pedido ${pedidoId}:`, error);
      throw new Error(`Error al reasignar pedido: ${error}`);
    }
  }
}
