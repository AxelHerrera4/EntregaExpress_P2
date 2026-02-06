package com.logiflow.fleetservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento publicado por FleetService cuando cambia el estado de un vehículo
 * Puede ser consumido por PedidoService, TrackingService, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoEstadoActualizadoEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String vehiculoId;
    private String placa;
    private String tipoVehiculo;
    private String estadoAnterior;
    private String estadoNuevo;
    private Boolean disponible;
    private LocalDateTime fechaActualizacion;
}
