export interface Ubicacion {
  repartidorId: string;
  latitud: number;
  longitud: number;
  velocidad: number | null;
  ultimaActualizacion: string | null;
}
