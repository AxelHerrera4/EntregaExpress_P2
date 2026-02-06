import { PedidoService, PedidoResponse } from './pedido.service';
import { FleetServiceClient } from './fleet.client';
import { EstadoPedido } from '../enums';

/**
 * Interface de KPI para estadísticas
 */
export interface Kpi {
  cobertura?: string;
  fecha?: string;
  pedidosTotal: number;
  pedidosPendientes: number;
  pedidosEnRuta: number;
  pedidosEntregados: number;
  pedidosCancelados: number;
  tiempoPromedioEntrega: number | null;
  repartidoresActivos: number;
}

/**
 * KpiService - Calcula KPIs combinando datos de pedidos y flota
 */
export class KpiService {
  private pedidoService: PedidoService;
  private fleetClient: FleetServiceClient;

  constructor(pedidoService: PedidoService, fleetClient: FleetServiceClient) {
    this.pedidoService = pedidoService;
    this.fleetClient = fleetClient;
  }

  /**
   * Calcula KPIs por cobertura
   */
  async calcularKpisPorCobertura(cobertura: string): Promise<Kpi> {
    console.log(`[KpiService] Calculando KPIs para cobertura: ${cobertura}`);

    // Obtener todos los pedidos
    const todosPedidos = await this.pedidoService.obtenerTodosLosPedidos();
    
    // Filtrar por cobertura
    const pedidos = todosPedidos.filter((p: PedidoResponse) => p.cobertura === cobertura);

    return this.calcularKpisDesdeArray(pedidos, cobertura);
  }

  /**
   * Calcula KPIs generales (todos los pedidos)
   */
  async calcularKpisGenerales(): Promise<Kpi> {
    console.log('[KpiService] Calculando KPIs generales');
    
    const pedidos = await this.pedidoService.obtenerTodosLosPedidos();
    return this.calcularKpisDesdeArray(pedidos, 'GENERAL');
  }

  /**
   * Calcula estadísticas desde un array de pedidos
   */
  private async calcularKpisDesdeArray(pedidos: PedidoResponse[], cobertura: string): Promise<Kpi> {
    // Calcular métricas
    const pendientes = pedidos.filter((p: PedidoResponse) => p.estado === EstadoPedido.PENDIENTE).length;
    const enRuta = pedidos.filter((p: PedidoResponse) => p.estado === EstadoPedido.EN_RUTA).length;
    const entregados = pedidos.filter((p: PedidoResponse) => p.estado === EstadoPedido.ENTREGADO).length;
    const cancelados = pedidos.filter((p: PedidoResponse) => p.estado === EstadoPedido.CANCELADO).length;

    // Tiempo promedio de entrega (si hay campo de tiempo disponible)
    // Por ahora no está disponible en PedidoResponse, se puede calcular si se agrega
    const tiempoPromedio = null;

    // Obtener resumen de flota
    const resumenFlota = await this.fleetClient.obtenerFlotaResumen();
    const repartidoresActivos = resumenFlota.disponibles + resumenFlota.enRuta;

    return {
      cobertura,
      fecha: new Date().toISOString(),
      pedidosTotal: pedidos.length,
      pedidosPendientes: pendientes,
      pedidosEnRuta: enRuta,
      pedidosEntregados: entregados,
      pedidosCancelados: cancelados,
      tiempoPromedioEntrega: tiempoPromedio,
      repartidoresActivos,
    };
  }
}
