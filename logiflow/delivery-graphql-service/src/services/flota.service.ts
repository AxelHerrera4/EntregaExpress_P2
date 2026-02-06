import { FleetServiceClient, RepartidorResponse } from './fleet.client';
import { TrackingServiceClient } from './tracking.client';
import { EstadoRepartidor } from '../enums';

/**
 * Repartidor en mapa para visualización
 */
export interface RepartidorEnMapa {
  id: string;
  nombre: string;
  placa: string | null;
  latitud: number;
  longitud: number;
  velocidad: number | null;
  estado: EstadoRepartidor;
  ultimaActualizacion: string | null;
}

/**
 * Resumen de flota
 */
export interface FlotaResumen {
  total: number;
  disponibles: number;
  enRuta: number;
  mantenimiento: number;
  desconectados: number;
}

/**
 * FlotaService - Combina datos de Fleet Service + Tracking Service
 * Funcionalidad para el mapa en tiempo real
 */
export class FlotaService {
  private fleetClient: FleetServiceClient;
  private trackingClient: TrackingServiceClient;

  constructor(fleetClient: FleetServiceClient, trackingClient: TrackingServiceClient) {
    this.fleetClient = fleetClient;
    this.trackingClient = trackingClient;
  }

  /**
   * Obtiene flota activa con ubicación en tiempo real
   * Combina datos estáticos (Fleet) + tiempo real (Tracking)
   */
  async obtenerFlotaActivaConUbicacion(): Promise<RepartidorEnMapa[]> {
    console.log('[FlotaService] Obteniendo flota activa');

    // 1. Obtener todos los repartidores activos
    const repartidores = await this.fleetClient.obtenerFlotaActiva();

    // 2. Por cada repartidor, obtener ubicación en tiempo real si está disponible
    const resultados = await Promise.all(
      repartidores.map(async (repartidor: RepartidorResponse) => {
        // Intentar obtener ubicación del tracking service
        let ubicacion = null;
        try {
          ubicacion = await this.trackingClient.obtenerUbicacion(repartidor.id);
        } catch (e) {
          // Si falla, usar la ubicación del repartidor
        }

        const enMapa: RepartidorEnMapa = {
          id: repartidor.id,
          nombre: `${repartidor.nombre} ${repartidor.apellido || ''}`.trim(),
          placa: repartidor.vehiculo?.placa || null,
          latitud: ubicacion?.latitud || repartidor.ubicacionActual?.latitud || 0.0,
          longitud: ubicacion?.longitud || repartidor.ubicacionActual?.longitud || 0.0,
          velocidad: ubicacion?.velocidad || null,
          estado: repartidor.estado as EstadoRepartidor,
          ultimaActualizacion: ubicacion?.ultimaActualizacion || 
            repartidor.ubicacionActual?.ultimaActualizacion || null,
        };

        return enMapa;
      })
    );

    return resultados;
  }

  /**
   * Obtiene resumen de flota
   */
  async obtenerResumenFlota(): Promise<FlotaResumen> {
    return this.fleetClient.obtenerFlotaResumen();
  }
}
