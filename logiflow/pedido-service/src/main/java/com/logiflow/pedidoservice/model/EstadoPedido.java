package com.logiflow.pedidoservice.model;

/**
 * Estados del pedido en su ciclo de vida
 */
public enum EstadoPedido {
    PENDIENTE,           // Creado, esperando asignación
    ASIGNADO,            // Repartidor y vehículo asignados
    EN_PREPARACION,      // En proceso de preparación
    EN_TRANSITO,         // En camino al destino
    EN_DISTRIBUCION,     // En punto de distribución
    ENTREGADO,           // Entregado exitosamente
    FALLIDO,             // Intento de entrega fallido
    CANCELADO,           // Cancelado por cliente o sistema
    DEVUELTO             // Devuelto al remitente
}

