import { EstadoPedido } from '../enums';
import { Cliente } from './Cliente.entity';
import { RepartidorDetalle } from './RepartidorDetalle.entity';

export interface Pedido {
  id: string;
  clienteId: string;
  cliente: Cliente | null;
  destino: string;
  estado: EstadoPedido;
  repartidorId: string | null;
  repartidor?: RepartidorDetalle | null;
  tiempoTranscurrido: number | null;
  retrasoMin: number | null;
  fechaCreacion: string | null;
  fechaActualizacion: string | null;
}
