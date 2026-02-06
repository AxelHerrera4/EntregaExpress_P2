package com.logiflow.fleetservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento publicado cuando se crea un nuevo pedido
 * Consumido por FleetService para iniciar proceso de asignación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCreadoEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String pedidoId;
    private String clienteId;
    private Double peso;
    private String origen;
    private String destino;
    private String prioridad;
    private LocalDateTime fechaCreacion;
}
