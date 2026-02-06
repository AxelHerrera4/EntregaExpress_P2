import { FleetServiceClient } from './fleet.client';
import { TrackingServiceClient } from './tracking.client';
import { RepartidorEnMapa, FlotaResumen } from '../entities';
import { EstadoRepartidor } from '../enums';

/**
 * FlotaService - Combina datos de Fleet Service + Tracking Service
 * Funcionalidad clave para el mapa en tiempo real
 */
export class FlotaService {
  private fleetClient: FleetServiceClient;
  private trackingClient: TrackingServiceClient;

  constructor(fleetClient: FleetServiceClient, trackingClient: TrackingServiceClient) {
    this.fleetClient = fleetClient;
    this.trackingClient = trackingClient;
  }

  /**
   * QUERY 2: Combina datos estáticos (Fleet) + tiempo real (Tracking)
   * Para cada repartidor de la zona, obtiene su ubicación en tiempo real
   */
  async obtenerFlotaActivaConUbicacion(zonaId: string): Promise<RepartidorEnMapa[]> {
    console.log(`[FlotaService] Obteniendo flota activa para zona: ${zonaId}`);

    // 1. Obtener repartidores de la zona (Fleet Service)
    const repartidores = await this.fleetClient.obtenerRepartidoresPorZona(zonaId);

    // 2. Por cada repartidor, obtener ubicación en tiempo real (Tracking Service)
    const resultados = await Promise.all(
      repartidores.map(async (repartidor) => {
        const ubicacion = await this.trackingClient.obtenerUbicacion(repartidor.id);

        const enMapa: RepartidorEnMapa = {
          id: repartidor.id,
          nombre: repartidor.nombre,
          placa: repartidor.vehiculo ? repartidor.vehiculo.placa : 'N/A',
          latitud: ubicacion ? ubicacion.latitud : 0.0,
          longitud: ubicacion ? ubicacion.longitud : 0.0,
          velocidad: ubicacion ? ubicacion.velocidad : 0.0,
          estado: repartidor.disponible
            ? EstadoRepartidor.DISPONIBLE
            : EstadoRepartidor.EN_RUTA,
          ultimaActualizacion: ubicacion ? ubicacion.ultimaActualizacion : null,
        };

        return enMapa;
      })
    );

    return resultados;
  }

  /**
   * Resumen de flota: total, disponibles, en ruta
   */
  async obtenerResumenFlota(zonaId: string): Promise<FlotaResumen> {
    const repartidores = await this.fleetClient.obtenerRepartidoresPorZona(zonaId);

    const disponibles = repartidores.filter((r) => r.disponible).length;
    const enRuta = repartidores.length - disponibles;

    return {
      total: repartidores.length,
      disponibles,
      enRuta,
    };
  }
}
