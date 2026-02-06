import { EstadoPedido } from '../enums';

export interface FiltroPedidoInput {
  zonaId?: string;
  estado?: EstadoPedido;
  repartidorId?: string;
  ciudadOrigen?: string;
  ciudadDestino?: string;
  provinciaOrigen?: string;
  provinciaDestino?: string;
}
