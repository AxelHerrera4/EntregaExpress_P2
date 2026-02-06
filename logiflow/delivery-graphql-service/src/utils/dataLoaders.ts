import DataLoader from 'dataloader';
import { FleetServiceClient } from '../services';
import { RepartidorDetalle, Vehiculo } from '../entities';

/**
 * DataLoader para repartidores - Evita el problema N+1
 * Agrupa múltiples solicitudes de repartidores en una sola llamada batch
 */
export const createRepartidorLoader = (
  fleetClient: FleetServiceClient
): DataLoader<string, RepartidorDetalle | null> => {
  return new DataLoader<string, RepartidorDetalle | null>(
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
 * Nota: Asume que el vehiculo ya viene en RepartidorDetalle, este loader es para casos
 * donde se necesite cargar vehículos por separado
 */
export const createVehiculoLoader = (
  fleetClient: FleetServiceClient
): DataLoader<string, Vehiculo | null> => {
  return new DataLoader<string, Vehiculo | null>(
    async (vehiculoIds: readonly string[]) => {
      console.log(`[DataLoader] Cargando ${vehiculoIds.length} vehículos en batch`);

      // Placeholder: Si existiera un endpoint batch para vehículos
      // En este caso, el vehículo ya viene con el repartidor, así que este loader
      // es más una muestra de cómo implementar otro DataLoader
      const results = vehiculoIds.map(() => null);

      return results;
    },
    {
      cache: true,
    }
  );
};
