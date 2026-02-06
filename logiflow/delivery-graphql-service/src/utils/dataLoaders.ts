import DataLoader from 'dataloader';
import { FleetServiceClient, RepartidorResponse, VehiculoResponse } from '../services';

/**
 * DataLoader para repartidores - Evita el problema N+1
 * Agrupa múltiples solicitudes de repartidores en una sola llamada batch
 */
export const createRepartidorLoader = (
  fleetClient: FleetServiceClient
): DataLoader<string, RepartidorResponse | null> => {
  return new DataLoader<string, RepartidorResponse | null>(
    async (repartidorIds: readonly string[]) => {
      console.log(`[DataLoader] Cargando ${repartidorIds.length} repartidores en batch`);

      // Llamada batch simulada: en producción el backend debería tener un endpoint batch
      // Por ahora hacemos las llamadas en paralelo (mejor que secuencial)
      const results = await Promise.all(
        repartidorIds.map((id) => fleetClient.obtenerRepartidor(id))
      );

      return results;
    },
    {
      // Cache habilitado por defecto durante el request
      cache: true,
    }
  );
};

/**
 * DataLoader para vehículos - Evita el problema N+1 al resolver Repartidor.vehiculo
 */
export const createVehiculoLoader = (
  fleetClient: FleetServiceClient
): DataLoader<string, VehiculoResponse | null> => {
  return new DataLoader<string, VehiculoResponse | null>(
    async (vehiculoIds: readonly string[]) => {
      console.log(`[DataLoader] Cargando ${vehiculoIds.length} vehículos en batch`);

      // Llamada batch: obtener vehículos en paralelo
      const results = await Promise.all(
        vehiculoIds.map((id) => fleetClient.obtenerVehiculo(id))
      );

      return results;
    },
    {
      cache: true,
    }
  );
};
