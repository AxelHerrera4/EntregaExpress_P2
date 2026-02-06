package com.logiflow.pedidoservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento publicado cuando se solicita reintentar la asignación de un pedido PENDIENTE
 * FleetService consume este evento e intenta nuevamente asignar repartidor y vehículo
 * 
 * Casos de uso:
 * - Pedido creado sin repartidores disponibles
 * - Asignación fallida por falta de recursos
 * - Re-asignación manual por administrador
 * - Job automático que reintenta pedidos pendientes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReintentarAsignacionEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    // Control de idempotencia
    private String messageId;
    private LocalDateTime timestamp;
    
    // Información del pedido a reasignar
    private String pedidoId;
    private String clienteId;
    private String usuarioSolicitante;  // Quien solicitó el reintento
    
    // Contexto para priorización
    private String modalidadServicio;   // URBANA, NACIONAL, INTERNACIONAL
    private String tipoEntrega;         // NORMAL, EXPRESS, SAME_DAY
    private String prioridad;           // ALTA, MEDIA, BAJA
    private Double peso;
    private String ciudadOrigen;
    private String ciudadDestino;
    
    // Metadata del reintento
    private Integer numeroReintento;    // Para limitar intentos
    private String motivoReintento;     // "RECURSOS_DISPONIBLES", "SOLICITUD_MANUAL", etc.
}
