import { TipoVehiculo, EstadoVehiculo } from '../enums';

/**
 * Vehiculo - Basado en VehiculoResponse.java del fleet-service
 */
export interface Vehiculo {
  id: string;
  placa: string;
  tipo: TipoVehiculo;
  marca?: string;
  modelo?: string;
  anio?: number;
  capacidadCarga?: number;
  estado: EstadoVehiculo;
  caracteristicasEspecificas?: {
    cilindraje?: number;
    tieneCajones?: boolean;
    numeroPuertas?: number;
    tipoCarroceria?: string;
    numeroEjes?: number;
    capacidadVolumen?: number;
  };
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string;
}
