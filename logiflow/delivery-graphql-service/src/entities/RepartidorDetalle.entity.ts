import { Vehiculo } from './Vehiculo.entity';
import { EstadoRepartidor } from '../enums';

/**
 * RepartidorDetalle - Basado en RepartidorResponse.java del fleet-service
 */
export interface RepartidorDetalle {
  id: string;
  nombre: string;
  apellido?: string;
  documento?: string;
  tipoDocumento?: string;
  telefono?: string;
  email?: string;
  estado: EstadoRepartidor;
  zonaAsignada?: string;
  tipoLicencia?: string;
  vehiculoId?: string;
  vehiculo?: {
    placa?: string;
    tipo?: string;
    estado?: string;
  } | null;
  ubicacionActual?: {
    latitud: number;
    longitud: number;
    ultimaActualizacion?: string;
  } | null;
  fechaContratacion?: string;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string;
}
