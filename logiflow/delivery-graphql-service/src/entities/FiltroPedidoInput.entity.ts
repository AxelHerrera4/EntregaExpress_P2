import { EstadoPedido } from '../enums';

export interface FiltroPedidoInput {
  zonaId?: string;
  estado?: EstadoPedido;
  repartidorId?: string;
}
