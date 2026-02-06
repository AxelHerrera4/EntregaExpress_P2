export interface EstadisticasCobertura {
  cobertura: string;
  pedidosTotal: number;
  pedidosPendientes: number;
  pedidosEnRuta: number;
  pedidosEntregados: number;
  pedidosCancelados: number;
  tiempoPromedioEntrega?: number;
  repartidoresActivos: number;
  fecha?: string;
}

export interface EstadisticasCiudad {
  ciudad: string;
  provincia: string;
  pedidosOrigen: number;
  pedidosDestino: number;
  pedidosInternos: number; // origen y destino en la misma ciudad
  rutasPopulares: RutaPopular[];
}

export interface RutaPopular {
  origen: string;
  destino: string;
  cantidad: number;
}

export interface Coordenadas {
  latitud: number;
  longitud: number;
  ultimaActualizacion: string;
}