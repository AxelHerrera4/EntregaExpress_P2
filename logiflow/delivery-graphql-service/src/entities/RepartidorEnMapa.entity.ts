import { EstadoRepartidor } from '../enums';

export interface RepartidorEnMapa {
  id: string;
  nombre: string;
  placa: string;
  latitud: number;
  longitud: number;
  estado: EstadoRepartidor;
  velocidad: number | null;
  ultimaActualizacion: string | null;
}
