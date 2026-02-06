import { pedidoClient } from '../utils';
import { Incidencia, RegistrarIncidenciaInput } from '../entities';

/**
 * IncidenciaServiceClient - Comunicación con el microservicio de Incidencias
 * Las incidencias se manejan a través del pedido-service via API Gateway
 */
export class IncidenciaServiceClient {
  /**
   * Registra una nueva incidencia
   * POST /incidencias (se convierte a /api/incidencias via API Gateway)
   */
  async registrarIncidencia(input: RegistrarIncidenciaInput): Promise<Incidencia> {
    try {
      const payload = {
        pedidoId: input.pedidoId,
        descripcion: input.descripcion,
        tipo: input.tipo
      };
      
      const response = await pedidoClient.post<Incidencia>('/incidencias', payload);
      
      console.log(`[IncidenciaServiceClient] Incidencia registrada para pedido ${input.pedidoId}`);
      return response.data;
    } catch (error) {
      console.error(`[IncidenciaServiceClient] Error al registrar incidencia para pedido ${input.pedidoId}:`, error);
      throw new Error(`Error al registrar incidencia: ${error}`);
    }
  }

  /**
   * Obtiene incidencias de un pedido específico
   * GET /incidencias/pedido/{pedidoId} (se convierte a /api/incidencias/pedido/{pedidoId} via API Gateway)
   */
  async obtenerIncidenciasPorPedido(pedidoId: string): Promise<Incidencia[]> {
    try {
      const response = await pedidoClient.get<Incidencia[]>(`/incidencias/pedido/${pedidoId}`);
      return response.data;
    } catch (error) {
      console.error(`[IncidenciaServiceClient] Error al obtener incidencias del pedido ${pedidoId}:`, error);
      return [];
    }
  }

  /**
   * Obtiene una incidencia específica
   * GET /incidencias/{incidenciaId} (se convierte a /api/incidencias/{incidenciaId} via API Gateway)
   */
  async obtenerIncidencia(incidenciaId: string): Promise<Incidencia | null> {
    try {
      const response = await pedidoClient.get<Incidencia>(`/incidencias/${incidenciaId}`);
      return response.data;
    } catch (error) {
      console.error(`[IncidenciaServiceClient] Error al obtener incidencia ${incidenciaId}:`, error);
      return null;
    }
  }
}