package com.logiflow.pedidoservice.service;

import com.logiflow.pedidoservice.dto.PedidoPatchRequest;
import com.logiflow.pedidoservice.dto.PedidoRequest;
import com.logiflow.pedidoservice.dto.PedidoResponse;
import com.logiflow.pedidoservice.model.ModalidadServicio;

import java.util.List;

/**
 * Servicio de negocio para gestión de pedidos
 * Aplicando principio de Segregación de Interfaces (ISP)
 */
public interface PedidoService {

    /**
     * Crear un nuevo pedido
     * @param request datos del pedido
     * @return pedido creado
     */
    PedidoResponse createPedido(PedidoRequest request);

    /**
     * Obtener pedido por ID
     * @param id identificador del pedido
     * @return pedido encontrado
     */
    PedidoResponse getPedidoById(String id);

    /**
     * Obtener todos los pedidos
     * @return lista de pedidos
     */
    List<PedidoResponse> getAllPedidos();

    /**
     * Obtener pedidos por cliente
     * @param clienteId identificador del cliente
     * @return lista de pedidos del cliente
     */
    List<PedidoResponse> getPedidosByCliente(String clienteId);

    /**
     * Actualización parcial de un pedido (PATCH)
     * @param id identificador del pedido
     * @param patchRequest campos a actualizar
     * @return pedido actualizado
     */
    PedidoResponse patchPedido(String id, PedidoPatchRequest patchRequest);

    /**
     * Cancelar un pedido
     * @param id identificador del pedido
     * @return pedido cancelado
     */
    PedidoResponse cancelarPedido(String id);

    /**
     * Eliminar un pedido (hard delete)
     * @param id identificador del pedido
     */
    void deletePedido(String id);

    // Métodos para integración con FleetService

    /**
     * Asignar repartidor y vehículo a un pedido
     * @param pedidoId ID del pedido
     * @param repartidorId ID del repartidor
     * @param vehiculoId ID del vehículo
     * @return pedido actualizado
     */
    PedidoResponse asignarRepartidorYVehiculo(String pedidoId, String repartidorId, String vehiculoId);

    /**
     * Obtener pedidos pendientes de asignación
     * @return lista de pedidos sin asignar
     */
    List<PedidoResponse> getPedidosPendientesAsignacion();

    /**
     * Obtener pedidos por repartidor
     * @param repartidorId ID del repartidor
     * @return lista de pedidos del repartidor
     */
    List<PedidoResponse> getPedidosByRepartidor(String repartidorId);

    /**
     * Obtener pedidos por modalidad de servicio
     * @param modalidad modalidad del servicio
     * @return lista de pedidos
     */
    List<PedidoResponse> getPedidosByModalidad(ModalidadServicio modalidad);

    // Métodos para integración con BillingService

    /**
     * Asociar factura a un pedido
     * @param pedidoId ID del pedido
     * @param facturaId ID de la factura
     * @param tarifa tarifa calculada
     * @return pedido actualizado
     */
    PedidoResponse asociarFactura(String pedidoId, String facturaId, Double tarifa);

    /**
     * Obtener pedidos sin factura
     * @return lista de pedidos sin facturar
     */
    List<PedidoResponse> getPedidosSinFactura();

    /**
     * Obtener pedidos de alta prioridad pendientes
     * @return lista de pedidos urgentes
     */
    List<PedidoResponse> getPedidosAltaPrioridad();
}


