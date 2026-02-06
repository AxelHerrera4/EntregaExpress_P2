package ec.edu.espe.billing_service.service;

import ec.edu.espe.billing_service.event.PedidoCreadoEvent;
import ec.edu.espe.billing_service.event.PedidoEstadoEvent;

/**
 * Servicio para manejar eventos de pedidos y procesamiento de facturación
 */
public interface BillingService {

    /**
     * Procesa el evento de pedido creado y genera la factura correspondiente
     * @param event Evento con información del pedido creado
     */
    void procesarPedidoCreado(PedidoCreadoEvent event);

    /**
     * Procesa la actualización de estado de un pedido para ajustes de facturación
     * @param event Evento con información del cambio de estado
     */
    void procesarEstadoActualizado(PedidoEstadoEvent event);
}