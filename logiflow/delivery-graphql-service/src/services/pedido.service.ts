import { pedidoClient } from '../utils';

/**
 * Interfaz de respuesta del PedidoService
 * Basado en PedidoResponse.java
 */
export interface PedidoResponse {
  id: string;
  clienteId: string;
  direccionOrigen: {
    calle: string;
    numero: string;
    ciudad: string;
    provincia: string;
    latitud: number;
    longitud: number;
  };
  direccionDestino: {
    calle: string;
    numero: string;
    ciudad: string;
    provincia: string;
    latitud: number;
    longitud: number;
  };
  modalidadServicio: string;
  tipoEntrega: string;
  estado: string;
  peso: number;
  telefonoContacto: string;
  nombreDestinatario?: string;
  fechaCreacion: string;
  fechaActualizacion: string;
  cobertura: string;
  repartidorId?: string;
  vehiculoId?: string;
  facturaId?: string;
  tarifa?: number;
}

/**
 * PedidoService - Comunicación con el microservicio de Pedidos a través del API Gateway
 * 
 * Endpoints disponibles en PedidoController.java:
 * - GET /api/pedidos - Obtener todos los pedidos
 * - GET /api/pedidos/{id} - Obtener pedido por ID
 * - GET /api/pedidos/cliente/{clienteId} - Obtener pedidos por cliente
 * - GET /api/pedidos/repartidor/{repartidorId} - Obtener pedidos por repartidor
 * - GET /api/pedidos/modalidad/{modalidad} - Obtener pedidos por modalidad
 * - GET /api/pedidos/pendientes-asignacion - Obtener pedidos pendientes de asignación
 * - PATCH /api/pedidos/{id}/asignar?repartidorId=X&vehiculoId=Y - Asignar repartidor y vehículo
 * - PATCH /api/pedidos/{id}/cancelar - Cancelar pedido
 */
export class PedidoService {
  
  /**
   * Obtiene todos los pedidos
   * GET /api/pedidos
   */
  async obtenerTodosLosPedidos(): Promise<PedidoResponse[]> {
    try {
      console.log('[PedidoService] GET /pedidos');
      const response = await pedidoClient.get<PedidoResponse[]>('/pedidos');
      console.log(`[PedidoService] Obtenidos ${response.data.length} pedidos`);
      return response.data;
    } catch (error: any) {
      console.error('[PedidoService] Error al obtener todos los pedidos:', error.message);
      if (error.response) {
        console.error('[PedidoService] Status:', error.response.status);
        console.error('[PedidoService] Data:', error.response.data);
      }
      return [];
    }
  }

  /**
   * Obtiene el detalle de un pedido específico
   * GET /api/pedidos/{id}
   */
  async obtenerPedidoPorId(id: string): Promise<PedidoResponse | null> {
    try {
      console.log(`[PedidoService] GET /pedidos/${id}`);
      const response = await pedidoClient.get<PedidoResponse>(`/pedidos/${id}`);
      return response.data;
    } catch (error: any) {
      console.error(`[PedidoService] Error al obtener pedido ${id}:`, error.message);
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  }

  /**
   * Obtiene pedidos por cliente
   * GET /api/pedidos/cliente/{clienteId}
   */
  async obtenerPedidosPorCliente(clienteId: string): Promise<PedidoResponse[]> {
    try {
      console.log(`[PedidoService] GET /pedidos/cliente/${clienteId}`);
      const response = await pedidoClient.get<PedidoResponse[]>(`/pedidos/cliente/${clienteId}`);
      return response.data;
    } catch (error: any) {
      console.error(`[PedidoService] Error al obtener pedidos del cliente ${clienteId}:`, error.message);
      return [];
    }
  }

  /**
   * Obtiene pedidos por repartidor
   * GET /api/pedidos/repartidor/{repartidorId}
   */
  async obtenerPedidosPorRepartidor(repartidorId: string): Promise<PedidoResponse[]> {
    try {
      console.log(`[PedidoService] GET /pedidos/repartidor/${repartidorId}`);
      const response = await pedidoClient.get<PedidoResponse[]>(`/pedidos/repartidor/${repartidorId}`);
      return response.data;
    } catch (error: any) {
      console.error(`[PedidoService] Error al obtener pedidos del repartidor ${repartidorId}:`, error.message);
      return [];
    }
  }

  /**
   * Obtiene pedidos por modalidad de servicio
   * GET /api/pedidos/modalidad/{modalidad}
   */
  async obtenerPedidosPorModalidad(modalidad: string): Promise<PedidoResponse[]> {
    try {
      console.log(`[PedidoService] GET /pedidos/modalidad/${modalidad}`);
      const response = await pedidoClient.get<PedidoResponse[]>(`/pedidos/modalidad/${modalidad}`);
      return response.data;
    } catch (error: any) {
      console.error(`[PedidoService] Error al obtener pedidos por modalidad ${modalidad}:`, error.message);
      return [];
    }
  }

  /**
   * Obtiene pedidos pendientes de asignación
   * GET /api/pedidos/pendientes-asignacion
   */
  async obtenerPedidosPendientesAsignacion(): Promise<PedidoResponse[]> {
    try {
      console.log('[PedidoService] GET /pedidos/pendientes-asignacion');
      const response = await pedidoClient.get<PedidoResponse[]>('/pedidos/pendientes-asignacion');
      return response.data;
    } catch (error: any) {
      console.error('[PedidoService] Error al obtener pedidos pendientes:', error.message);
      return [];
    }
  }

  /**
   * Asigna un repartidor y vehículo a un pedido
   * PATCH /api/pedidos/{pedidoId}/asignar?repartidorId={repartidorId}&vehiculoId={vehiculoId}
   */
  async asignarRepartidorYVehiculo(
    pedidoId: string,
    repartidorId: string,
    vehiculoId: string
  ): Promise<PedidoResponse> {
    try {
      console.log(`[PedidoService] PATCH /pedidos/${pedidoId}/asignar?repartidorId=${repartidorId}&vehiculoId=${vehiculoId}`);
      const response = await pedidoClient.patch<PedidoResponse>(
        `/pedidos/${pedidoId}/asignar`,
        null,
        { params: { repartidorId, vehiculoId } }
      );
      
      console.log(`[PedidoService] Pedido ${pedidoId} asignado a repartidor ${repartidorId} con vehículo ${vehiculoId}`);
      return response.data;
    } catch (error: any) {
      console.error(`[PedidoService] Error al asignar repartidor al pedido ${pedidoId}:`, error.message);
      if (error.response) {
        console.error('[PedidoService] Status:', error.response.status);
        console.error('[PedidoService] Data:', error.response.data);
      }
      throw new Error(`Error al asignar repartidor: ${error.message}`);
    }
  }

  /**
   * Cancela un pedido
   * PATCH /api/pedidos/{pedidoId}/cancelar
   */
  async cancelarPedido(pedidoId: string, motivo?: string): Promise<PedidoResponse> {
    try {
      console.log(`[PedidoService] PATCH /pedidos/${pedidoId}/cancelar (motivo: ${motivo || 'N/A'})`);
      const response = await pedidoClient.patch<PedidoResponse>(`/pedidos/${pedidoId}/cancelar`, { motivo });
      console.log(`[PedidoService] Pedido ${pedidoId} cancelado`);
      return response.data;
    } catch (error: any) {
      console.error(`[PedidoService] Error al cancelar pedido ${pedidoId}:`, error.message);
      throw new Error(`Error al cancelar pedido: ${error.message}`);
    }
  }

  /**
   * Actualiza un pedido parcialmente
   * PATCH /api/pedidos/{pedidoId}
   */
  async actualizarPedido(pedidoId: string, datos: Partial<PedidoResponse>): Promise<PedidoResponse> {
    try {
      console.log(`[PedidoService] PATCH /pedidos/${pedidoId}`);
      const response = await pedidoClient.patch<PedidoResponse>(`/pedidos/${pedidoId}`, datos);
      console.log(`[PedidoService] Pedido ${pedidoId} actualizado`);
      return response.data;
    } catch (error: any) {
      console.error(`[PedidoService] Error al actualizar pedido ${pedidoId}:`, error.message);
      throw new Error(`Error al actualizar pedido: ${error.message}`);
    }
  }
}
