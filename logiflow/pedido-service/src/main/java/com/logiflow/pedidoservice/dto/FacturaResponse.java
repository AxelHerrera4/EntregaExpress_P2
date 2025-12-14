package com.logiflow.pedidoservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuesta de factura desde Billing Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaResponse {

    private String id; // UUID de la factura
    private String pedidoId; // UUID del pedido
    private String tipoEntrega;
    private BigDecimal montoTotal; // Tarifa calculada
    private String estado; // BORRADOR, PAGADA, CANCELADA
    private LocalDateTime fechaCreacion;
    private Double distanciaKm;
}

