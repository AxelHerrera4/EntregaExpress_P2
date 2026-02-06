import { fleetClient } from '../utils';
import { RepartidorDetalle } from '../entities';

/**
 * FleetServiceClient - Comunicación con el microservicio Fleet (puerto 8083)
 */
export class FleetServiceClient {
  /**
   * Obtiene todos los repartidores de una zona
   * GET /api/repartidores/zona/{zonaId}
   */
  async obtenerRepartidoresPorZona(zonaId: string): Promise<RepartidorDetalle[]> {
    try {
      const response = await fleetClient.get<RepartidorDetalle[]>(
        `/api/repartidores/zona/${zonaId}`
      );
      return response.data;
    } catch (error) {
      console.error(`[FleetServiceClient] Error al obtener repartidores de zona ${zonaId}:`, error);
      return [];
    }
  }

  /**
   * Obtiene información de un repartidor específico (async para resolver N+1)
   * GET /api/repartidores/{repartidorId}
   */
  async obtenerRepartidor(repartidorId: string): Promise<RepartidorDetalle | null> {
    try {
      const response = await fleetClient.get<RepartidorDetalle>(
        `/api/repartidores/${repartidorId}`
      );
      return response.data;
    } catch (error) {
      console.error(`[FleetServiceClient] Error al obtener repartidor ${repartidorId}:`, error);
      return null;
    }
  }
}
