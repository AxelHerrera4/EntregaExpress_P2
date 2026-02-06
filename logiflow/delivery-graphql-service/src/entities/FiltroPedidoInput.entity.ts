import { EstadoPedido, ModalidadServicio, TipoEntrega } from '../enums';

export interface FiltroPedidosInput {
  estado?: EstadoPedido;
  repartidorId?: string;
  ciudadOrigen?: string;
  ciudadDestino?: string;
  provinciaOrigen?: string;
  provinciaDestino?: string;
  cobertura?: string; // Reemplaza zonaId
  modalidadServicio?: ModalidadServicio;
  tipoEntrega?: TipoEntrega;
}

// Mantener también la interfaz antigua para compatibilidad con código existente
export interface FiltroPedidoInput {
  zonaId?: string;
  estado?: EstadoPedido;
  repartidorId?: string;
  ciudadOrigen?: string;
  ciudadDestino?: string;
  provinciaOrigen?: string;
  provinciaDestino?: string;
}
