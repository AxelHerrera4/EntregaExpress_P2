import { Vehiculo } from './Vehiculo.entity';

export interface RepartidorDetalle {
  id: string;
  nombre: string;
  vehiculo: Vehiculo | null;
  disponible: boolean;
}
