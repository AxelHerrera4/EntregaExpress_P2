package com.logiflow.pedidoservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar creación de factura al Billing Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaRequest {

    private String pedidoId; // UUID del pedido como String
    private String tipoEntrega; // EXPRESS, NORMAL, PROGRAMADA
    private Double distanciaKm; // Distancia estimada en kilómetros
}

