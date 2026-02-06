export interface Ubicacion {
  repartidorId: string;
  latitud: number;
  longitud: number;
  velocidad: number | null;
  ultimaActualizacion: string | null;
}

// Nueva interfaz para ubicaciones geográficas (ciudades, destinos)
export interface UbicacionGeografica {
  latitud: number;
  longitud: number;
  ciudad: string;
  provincia: string;
  direccion?: string;
}
