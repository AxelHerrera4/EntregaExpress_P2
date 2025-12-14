package com.logiflow.pedidoservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de asignación desde Fleet Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionResponse {

    private String pedidoId; // ID del pedido
    private String repartidorId; // ID del repartidor asignado
    private String vehiculoId; // ID del vehículo asignado
    private String repartidorNombre; // Nombre completo del repartidor
    private String vehiculoPlaca; // Placa del vehículo
    private String estado; // ASIGNADO, RECHAZADO
    private String mensaje; // Mensaje adicional (ej: "No hay repartidores disponibles")
}

