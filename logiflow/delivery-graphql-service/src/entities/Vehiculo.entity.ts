import { TipoVehiculo } from '../enums';

export interface Vehiculo {
  id: string;
  tipo: TipoVehiculo;
  placa: string;
  modelo: string | null;
  capacidad: number | null;
}
