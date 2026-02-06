package com.logiflow.fleetservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento para reintentar asignación automática de repartidor y vehículo
 * Publicado por PedidoService cuando se solicita reintentar asignación de un pedido PENDIENTE
 * Consumido por FleetService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReintentarAsignacionEvent {
    
    private String messageId;
    private LocalDateTime timestamp;
    
    // Información del pedido
    private String pedidoId;
    private String clienteId;
    private String usuarioSolicitante;
    
    // Características del pedido
    private String modalidadServicio;  // URBANA_RAPIDA, INTERMUNICIPAL, NACIONAL
    private String tipoEntrega;        // NORMAL, EXPRESS
    private String prioridad;          // BAJA, MEDIA, ALTA
    private Double peso;
    
    // Ubicaciones
    private String ciudadOrigen;
    private String ciudadDestino;
    
    // Control de reintentos
    private Integer numeroReintento;
    private String motivoReintento;    // RECURSOS_DISPONIBLES, SOLICITUD_MANUAL, ERROR_PREVIO
}
