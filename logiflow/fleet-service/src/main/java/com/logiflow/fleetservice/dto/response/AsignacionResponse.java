package com.logiflow.fleetservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta de asignación de repartidor y vehículo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionResponse {

    private String pedidoId;
    private String repartidorId; // ID del repartidor asignado (Long convertido a String)
    private String vehiculoId;   // ID del vehículo asignado (Long convertido a String)
    private String repartidorNombre;
    private String vehiculoPlaca;
    private String estado; // ASIGNADO, RECHAZADO
    private String mensaje;
}

