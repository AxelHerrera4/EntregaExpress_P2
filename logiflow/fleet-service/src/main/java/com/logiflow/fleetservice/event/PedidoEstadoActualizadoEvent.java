package com.logiflow.fleetservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento publicado por PedidoService cuando se actualiza el estado de un pedido
 * Consumido por FleetService para actualizar asignaciones
 * 
 * IMPORTANTE: Estructura debe coincidir EXACTAMENTE con PedidoService
 * 
 * NOTA: repartidorId y vehiculoId son incluidos por PedidoService SOLO cuando
 * el cambio de estado implica asignación (aunque esta información proviene de FleetService originalmente)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEstadoActualizadoEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    // Control de idempotencia
    private String messageId;
    private LocalDateTime timestamp;
    
    // Información del cambio de estado
    private String pedidoId;
    private String estadoAnterior;
    private String estadoNuevo;
    private String usuarioModificador;
    private String repartidorId;
    private String vehiculoId;
}
