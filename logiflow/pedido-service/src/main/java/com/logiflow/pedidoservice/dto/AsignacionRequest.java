package com.logiflow.pedidoservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar asignación de repartidor y vehículo al Fleet Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionRequest {

    private String pedidoId; // UUID del pedido
    private String modalidadServicio; // URBANA_RAPIDA, INTERMUNICIPAL, NACIONAL
    private String tipoEntrega; // EXPRESS, NORMAL, PROGRAMADA
    private String prioridad; // BAJA, NORMAL, ALTA, URGENTE
    private String ciudadOrigen;
    private String ciudadDestino;
    private Double peso; // Para determinar tipo de vehículo requerido
}

