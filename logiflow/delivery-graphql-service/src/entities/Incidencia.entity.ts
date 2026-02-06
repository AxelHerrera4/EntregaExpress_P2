import { TipoIncidencia } from '../enums/TipoIncidenciaEnum.enum';

export interface Incidencia {
  id: string;
  pedidoId: string;
  descripcion: string;
  tipo: TipoIncidencia;
  fechaCreacion: string;
  resuelto: boolean;
}

export { TipoIncidencia };

export interface RegistrarIncidenciaInput {
  pedidoId: string;
  descripcion: string;
  tipo: TipoIncidencia;
}