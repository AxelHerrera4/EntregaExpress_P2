import { fleetClient } from '../utils';

/**
 * Respuesta del FleetService para Repartidor
 * Basado en RepartidorResponse.java
 */
export interface RepartidorResponse {
  id: string;
  nombre: string;
  apellido: string;
  documento: string;
  tipoDocumento: string;
  telefono: string;
  email: string;
  estado: string;
  zonaAsignada: string;
  tipoLicencia: string;
  vehiculoId: string;
  vehiculo?: {
    placa: string;
    tipo: string;
    estado: string;
  };
  ubicacionActual?: {
    latitud: number;
    longitud: number;
    ultimaActualizacion: string;
  };
  fechaContratacion: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Respuesta del FleetService para Vehículo
 * Basado en VehiculoResponse.java
 */
export interface VehiculoResponse {
  id: string;
  placa: string;
  tipo: string;
  marca: string;
  modelo: string;
  anio: number;
  capacidadCarga: number;
  estado: string;
  caracteristicasEspecificas?: {
    cilindraje?: number;
    tieneCajones?: boolean;
    numeroPuertas?: number;
    tipoCarroceria?: string;
    numeroEjes?: number;
    capacidadVolumen?: number;
  };
  activo: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * FleetServiceClient - Comunicación con el microservicio Fleet a través del API Gateway
 * Rutas base: /fleet-service -> se convierte en /api
 * Los endpoints del fleet-service son:
 * - GET /repartidores - Listar todos los repartidores
 * - GET /repartidores/{id} - Obtener repartidor por ID
 * - PATCH /repartidores/{id}/estado?estado={estado} - Cambiar estado
 * - GET /vehiculos - Listar todos los vehículos
 * - GET /vehiculos/{id} - Obtener vehículo por ID
 */
export class FleetServiceClient {
  
  /**
   * Obtiene todos los repartidores
   * GET /repartidores
   */
  async obtenerTodosLosRepartidores(): Promise<RepartidorResponse[]> {
    try {
      console.log('[FleetServiceClient] GET /repartidores');
      const response = await fleetClient.get<RepartidorResponse[]>('/repartidores');
      console.log(`[FleetServiceClient] Obtenidos ${response.data.length} repartidores`);
      return response.data;
    } catch (error: any) {
      console.error('[FleetServiceClient] Error al obtener repartidores:', error.message);
      if (error.response) {
        console.error('[FleetServiceClient] Status:', error.response.status);
        console.error('[FleetServiceClient] Data:', error.response.data);
      }
      return [];
    }
  }

  /**
   * Obtiene información de un repartidor específico
   * GET /repartidores/{repartidorId}
   */
  async obtenerRepartidor(repartidorId: string): Promise<RepartidorResponse | null> {
    try {
      console.log(`[FleetServiceClient] GET /repartidores/${repartidorId}`);
      const response = await fleetClient.get<RepartidorResponse>(`/repartidores/${repartidorId}`);
      return response.data;
    } catch (error: any) {
      console.error(`[FleetServiceClient] Error al obtener repartidor ${repartidorId}:`, error.message);
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  }

  /**
   * Actualiza el estado de un repartidor
   * PATCH /repartidores/{repartidorId}/estado?estado={estado}
   */
  async actualizarEstadoRepartidor(
    repartidorId: string, 
    estado: string, 
    motivo?: string
  ): Promise<RepartidorResponse> {
    try {
      console.log(`[FleetServiceClient] PATCH /repartidores/${repartidorId}/estado?estado=${estado}`);
      const response = await fleetClient.patch<RepartidorResponse>(
        `/repartidores/${repartidorId}/estado`,
        null, // No body, solo query params
        { params: { estado } }
      );
      
      console.log(`[FleetServiceClient] Estado de repartidor ${repartidorId} actualizado a ${estado}`);
      return response.data;
    } catch (error: any) {
      console.error(`[FleetServiceClient] Error al actualizar estado de repartidor ${repartidorId}:`, error.message);
      if (error.response) {
        console.error('[FleetServiceClient] Status:', error.response.status);
        console.error('[FleetServiceClient] Data:', error.response.data);
      }
      throw new Error(`Error al actualizar estado del repartidor: ${error.message}`);
    }
  }

  /**
   * Obtiene todos los vehículos
   * GET /vehiculos
   */
  async obtenerTodosLosVehiculos(): Promise<VehiculoResponse[]> {
    try {
      console.log('[FleetServiceClient] GET /vehiculos');
      const response = await fleetClient.get<VehiculoResponse[]>('/vehiculos');
      console.log(`[FleetServiceClient] Obtenidos ${response.data.length} vehículos`);
      return response.data;
    } catch (error: any) {
      console.error('[FleetServiceClient] Error al obtener vehículos:', error.message);
      if (error.response) {
        console.error('[FleetServiceClient] Status:', error.response.status);
        console.error('[FleetServiceClient] Data:', error.response.data);
      }
      return [];
    }
  }

  /**
   * Obtiene un vehículo por ID
   * GET /vehiculos/{vehiculoId}
   */
  async obtenerVehiculo(vehiculoId: string): Promise<VehiculoResponse | null> {
    try {
      console.log(`[FleetServiceClient] GET /vehiculos/${vehiculoId}`);
      const response = await fleetClient.get<VehiculoResponse>(`/vehiculos/${vehiculoId}`);
      return response.data;
    } catch (error: any) {
      console.error(`[FleetServiceClient] Error al obtener vehículo ${vehiculoId}:`, error.message);
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  }

  /**
   * Asigna un vehículo a un repartidor
   * POST /repartidores/{id}/asignar-vehiculo?vehiculoId={vehiculoId}
   */
  async asignarVehiculo(repartidorId: string, vehiculoId: string): Promise<void> {
    try {
      console.log(`[FleetServiceClient] POST /repartidores/${repartidorId}/asignar-vehiculo?vehiculoId=${vehiculoId}`);
      await fleetClient.post(
        `/repartidores/${repartidorId}/asignar-vehiculo`,
        null,
        { params: { vehiculoId } }
      );
      console.log(`[FleetServiceClient] Vehículo ${vehiculoId} asignado a repartidor ${repartidorId}`);
    } catch (error: any) {
      console.error(`[FleetServiceClient] Error al asignar vehículo:`, error.message);
      throw new Error(`Error al asignar vehículo: ${error.message}`);
    }
  }

  /**
   * Obtiene repartidores activos con ubicación (para mapa)
   * Filtra repartidores con estado EN_RUTA o DISPONIBLE que tengan ubicación
   */
  async obtenerFlotaActiva(): Promise<RepartidorResponse[]> {
    try {
      const repartidores = await this.obtenerTodosLosRepartidores();
      // Filtrar solo los que tienen ubicación y están activos
      return repartidores.filter(r => 
        r.activo && 
        r.ubicacionActual && 
        (r.estado === 'DISPONIBLE' || r.estado === 'EN_RUTA')
      );
    } catch (error: any) {
      console.error('[FleetServiceClient] Error al obtener flota activa:', error.message);
      return [];
    }
  }

  /**
   * Obtiene resumen de flota
   */
  async obtenerFlotaResumen(): Promise<{
    total: number;
    disponibles: number;
    enRuta: number;
    mantenimiento: number;
    desconectados: number;
  }> {
    try {
      const repartidores = await this.obtenerTodosLosRepartidores();
      const activos = repartidores.filter(r => r.activo);
      
      return {
        total: activos.length,
        disponibles: activos.filter(r => r.estado === 'DISPONIBLE').length,
        enRuta: activos.filter(r => r.estado === 'EN_RUTA').length,
        mantenimiento: activos.filter(r => r.estado === 'MANTENIMIENTO').length,
        desconectados: activos.filter(r => r.estado === 'INACTIVO' || r.estado === 'DESCONECTADO').length
      };
    } catch (error: any) {
      console.error('[FleetServiceClient] Error al obtener resumen de flota:', error.message);
      return {
        total: 0,
        disponibles: 0,
        enRuta: 0,
        mantenimiento: 0,
        desconectados: 0
      };
    }
  }
}
