package com.logiflow.fleetservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento publicado cuando se actualiza el estado de un pedido
 * Consumido por FleetService para actualizar asignaciones
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEstadoActualizadoEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String pedidoId;
    private String estadoAnterior;
    private String estadoNuevo;
    private String repartidorId;
    private String vehiculoId;
    private LocalDateTime fechaActualizacion;
    private String motivo;
}
