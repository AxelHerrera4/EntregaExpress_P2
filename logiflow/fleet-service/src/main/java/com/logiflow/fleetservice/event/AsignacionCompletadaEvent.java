package com.logiflow.fleetservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento publicado por FleetService cuando completa una asignación exitosa
 * Consumido por PedidoService para actualizar el estado del pedido
 * 
 * Flujo:
 * 1. FleetService asigna repartidor y vehículo
 * 2. Publica este evento a RabbitMQ
 * 3. PedidoService escucha y actualiza automáticamente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionCompletadaEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    // Control de idempotencia
    private String messageId;
    private LocalDateTime timestamp;
    
    // Identificadores de la asignación
    private String pedidoId;          // UUID del pedido asignado
    private String repartidorId;      // UUID del repartidor asignado
    private String vehiculoId;        // UUID del vehículo asignado
    
    // Información adicional
    private String repartidorNombre;  // Nombre completo del repartidor
    private String vehiculoPlaca;     // Placa del vehículo
    private String estadoPedido;      // Estado resultante: "ASIGNADO"
    
    // Contexto de la asignación
    private String servicioOrigen;    // "FLEET_SERVICE"
    private String motivoAsignacion;  // "ASIGNACION_AUTOMATICA" o "ASIGNACION_MANUAL"
}
