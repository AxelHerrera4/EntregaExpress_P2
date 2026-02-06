import { PedidoService } from './pedido.service';
import { FleetServiceClient } from './fleet.client';
import { Kpi, FiltroPedidoInput } from '../entities';
import { EstadoPedido } from '../enums';

/**
 * KpiService - Calcula KPIs de una zona combinando datos de pedidos y flota
 */
export class KpiService {
  private pedidoService: PedidoService;
  private fleetClient: FleetServiceClient;

  constructor(pedidoService: PedidoService, fleetClient: FleetServiceClient) {
    this.pedidoService = pedidoService;
    this.fleetClient = fleetClient;
  }

  /**
   * Calcula KPIs para una zona específica
   */
  async calcularKpis(zonaId: string): Promise<Kpi> {
    // Obtener todos los pedidos de la zona
    const filtro: FiltroPedidoInput = { zonaId };
    const pedidos = await this.pedidoService.obtenerPedidosFiltrados(filtro);

    // Calcular métricas
    const pendientes = pedidos.filter((p) => p.estado === EstadoPedido.PENDIENTE).length;
    const enRuta = pedidos.filter((p) => p.estado === EstadoPedido.EN_RUTA).length;
    const entregados = pedidos.filter((p) => p.estado === EstadoPedido.ENTREGADO).length;

    // Tiempo promedio de entrega
    const entregadosConTiempo = pedidos.filter(
      (p) => p.estado === EstadoPedido.ENTREGADO && p.tiempoTranscurrido != null
    );
    const tiempoPromedio =
      entregadosConTiempo.length > 0
        ? entregadosConTiempo.reduce((sum, p) => sum + (p.tiempoTranscurrido || 0), 0) /
          entregadosConTiempo.length
        : null;

    // Repartidores activos
    const repartidores = await this.fleetClient.obtenerRepartidoresPorZona(zonaId);

    return {
      zonaId,
      pedidosPendientes: pendientes,
      pedidosEnRuta: enRuta,
      pedidosEntregados: entregados,
      tiempoPromedioEntrega: tiempoPromedio,
      repartidoresActivos: repartidores.length,
    };
  }

  /**
   * Calcula KPIs diarios para una fecha y zona específica
   * Nota: En producción, el backend debería tener un endpoint específico para KPIs por fecha
   */
  async calcularKpisDiarios(fecha: string, zonaId?: string): Promise<Kpi> {
    console.log(`[KpiService] Calculando KPIs diarios para fecha: ${fecha}, zona: ${zonaId || 'todas'}`);

    // Por ahora, como no tenemos endpoint específico de fecha en el backend,
    // reutilizamos el cálculo normal y agregamos la fecha al resultado
    const kpis = zonaId
      ? await this.calcularKpis(zonaId)
      : {
          zonaId: 'ALL',
          pedidosPendientes: 0,
          pedidosEnRuta: 0,
          pedidosEntregados: 0,
          tiempoPromedioEntrega: null,
          repartidoresActivos: 0,
        };

    return {
      ...kpis,
      fecha,
    };
  }

  /**
   * Calcula KPIs agregados por ciudad
   * @param ciudad Ciudad para calcular las estadísticas
   * @param tipo Tipo de estadística ('origen' | 'destino' | 'general')
   */
  async calcularKpisPorCiudad(ciudad: string, tipo: string): Promise<Kpi[]> {
    console.log(`[KpiService] Calculando KPIs por ciudad: ${ciudad}, tipo: ${tipo}`);

    let pedidos: any[] = [];
    
    try {
      // Obtener pedidos según el tipo solicitado
      switch (tipo.toLowerCase()) {
        case 'origen':
          pedidos = await this.pedidoService.obtenerPedidosPorCiudadOrigen(ciudad);
          break;
        case 'destino':
          pedidos = await this.pedidoService.obtenerPedidosPorCiudadDestino(ciudad);
          break;
        case 'general':
        default:
          // Obtener ambos origen y destino
          const pedidosOrigen = await this.pedidoService.obtenerPedidosPorCiudadOrigen(ciudad);
          const pedidosDestino = await this.pedidoService.obtenerPedidosPorCiudadDestino(ciudad);
          
          // Combinar y eliminar duplicados
          const pedidosMap = new Map();
          [...pedidosOrigen, ...pedidosDestino].forEach(pedido => {
            pedidosMap.set(pedido.id, pedido);
          });
          pedidos = Array.from(pedidosMap.values());
          break;
      }

      // Calcular métricas
      const pendientes = pedidos.filter((p) => p.estado === EstadoPedido.PENDIENTE).length;
      const enRuta = pedidos.filter((p) => p.estado === EstadoPedido.EN_RUTA).length;
      const entregados = pedidos.filter((p) => p.estado === EstadoPedido.ENTREGADO).length;

      // Tiempo promedio de entrega
      const entregadosConTiempo = pedidos.filter(
        (p) => p.estado === EstadoPedido.ENTREGADO && p.tiempoTranscurrido != null
      );
      const tiempoPromedio =
        entregadosConTiempo.length > 0
          ? entregadosConTiempo.reduce((sum, p) => sum + (p.tiempoTranscurrido || 0), 0) /
            entregadosConTiempo.length
          : null;

      const kpi: Kpi = {
        zonaId: `CIUDAD_${ciudad.toUpperCase()}`,
        pedidosPendientes: pendientes,
        pedidosEnRuta: enRuta,
        pedidosEntregados: entregados,
        tiempoPromedioEntrega: tiempoPromedio,
        repartidoresActivos: 0, // No tenemos información de repartidores por ciudad
      };

      return [kpi];
    } catch (error) {
      console.error(`[KpiService] Error al calcular KPIs por ciudad ${ciudad}:`, error);
      return [];
    }
  }
}
