export interface Kpi {
  zonaId: string;
  pedidosPendientes: number;
  pedidosEnRuta: number;
  pedidosEntregados: number;
  tiempoPromedioEntrega: number | null;
  repartidoresActivos: number;
  fecha?: string; // Agregado para KPIs diarios
}
