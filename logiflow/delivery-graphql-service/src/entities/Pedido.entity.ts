import { EstadoPedido, ModalidadServicio, TipoEntrega } from '../enums';
import { Cliente } from './Cliente.entity';
import { RepartidorDetalle } from './RepartidorDetalle.entity';
import { Vehiculo } from './Vehiculo.entity';

export interface Direccion {
  calle: string;
  numero: string;
  ciudad: string;
  provincia: string;
  latitud: number;
  longitud: number;
}

export interface Pedido {
  id: string;
  clienteId: string;
  cliente?: Cliente | null;
  direccionOrigen: Direccion;
  direccionDestino: Direccion;
  modalidadServicio: ModalidadServicio;
  tipoEntrega: TipoEntrega;
  estado: EstadoPedido;
  peso: number;
  telefonoContacto: string;
  nombreDestinatario: string | null;
  fechaCreacion: string;
  fechaActualizacion: string;
  cobertura: string; // Campo real de los datos: URBANA-QUITO, INTERMUNICIPAL-QUITO-GYE, etc.
  repartidorId?: string | null;
  repartidor?: RepartidorDetalle | null;
  vehiculoId?: string | null;
  vehiculo?: Vehiculo | null;
  facturaId?: string | null;
  tarifa?: number | null;
  tiempoTranscurrido?: number | null;
  retrasoMin?: number | null;
}
