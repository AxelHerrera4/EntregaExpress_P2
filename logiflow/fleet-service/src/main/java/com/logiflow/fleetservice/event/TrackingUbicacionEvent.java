package com.logiflow.fleetservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Evento de ubicación publicado por TrackingService
 * Consumido por FleetService para actualizar ubicación de repartidores
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingUbicacionEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long repartidorId;
    private Double latitud;
    private Double longitud;
    private String timestamp;
}
