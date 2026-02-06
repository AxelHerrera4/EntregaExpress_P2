package com.logiflow.fleetservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento publicado por PedidoService cuando se crea un nuevo pedido
 * Consumido por FleetService para iniciar proceso de asignación
 * 
 * IMPORTANTE: Estructura debe coincidir EXACTAMENTE con PedidoService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCreadoEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    // Control de idempotencia
    private String messageId;
    private LocalDateTime timestamp;
    
    // Información del pedido (tal como la envía PedidoService)
    private String pedidoId;
    private String clienteId;
    private String usuarioCreador;
    private String estado;
    private String tipoEntrega;
    private String modalidadServicio;
    private String prioridad;
    private Double peso;
    private String direccionOrigen;
    private String direccionDestino;
    private String ciudadOrigen;
    private String ciudadDestino;
    private Double distanciaEstimadaKm;
    private BigDecimal tarifaCalculada;
}
