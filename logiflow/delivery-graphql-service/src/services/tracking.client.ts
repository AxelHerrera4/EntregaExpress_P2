import { trackingClient } from '../utils';
import { Ubicacion } from '../entities';

/**
 * TrackingServiceClient - Comunicación con el microservicio Tracking (puerto 8090)
 */
export class TrackingServiceClient {
  /**
   * Obtiene la ubicación en tiempo real de un repartidor
   * GET /api/tracking/repartidor/{repartidorId}
   */
  async obtenerUbicacion(repartidorId: string): Promise<Ubicacion | null> {
    try {
      const response = await trackingClient.get<Ubicacion>(
        `/api/tracking/repartidor/${repartidorId}`
      );
      return response.data;
    } catch (error) {
      console.error(`[TrackingServiceClient] Error al obtener ubicación de ${repartidorId}:`, error);
      return null;
    }
  }

  /**
   * Obtiene ubicaciones de todos los repartidores en una zona
   * GET /api/tracking/zona/{zonaId}
   */
  async obtenerUbicacionesPorZona(zonaId: string): Promise<Ubicacion[]> {
    try {
      const response = await trackingClient.get<Ubicacion[]>(
        `/api/tracking/zona/${zonaId}`
      );
      return response.data;
    } catch (error) {
      console.error(`[TrackingServiceClient] Error al obtener ubicaciones de zona ${zonaId}:`, error);
      return [];
    }
  }
}
