package com.logiflow.fleetservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento publicado cuando se cancela un pedido
 * Consumido por FleetService para liberar recursos asignados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCanceladoEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String pedidoId;
    private String repartidorId;
    private String vehiculoId;
    private String motivo;
    private LocalDateTime fechaCancelacion;
}
