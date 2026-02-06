package com.logiflow.fleetservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento publicado por FleetService cuando se actualiza la ubicación de un repartidor
 * Puede ser consumido por PedidoService para tracking, TrackingService, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepartidorUbicacionActualizadaEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String repartidorId;
    private String nombreCompleto;
    private Double latitud;
    private Double longitud;
    private String zona;
    private String estado;
    private LocalDateTime fechaActualizacion;
}
