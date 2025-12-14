package com.logiflow.fleetservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para solicitar asignación de repartidor y vehículo a un pedido
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionRequest {

    @NotBlank(message = "El ID del pedido es obligatorio")
    private String pedidoId;

    @NotBlank(message = "La modalidad de servicio es obligatoria")
    private String modalidadServicio; // URBANA_RAPIDA, INTERMUNICIPAL, NACIONAL

    @NotBlank(message = "El tipo de entrega es obligatorio")
    private String tipoEntrega; // EXPRESS, STANDARD, ECONOMICA

    @NotBlank(message = "La prioridad es obligatoria")
    private String prioridad; // ALTA, NORMAL, BAJA

    @NotBlank(message = "La ciudad de origen es obligatoria")
    private String ciudadOrigen;

    @NotBlank(message = "La ciudad de destino es obligatoria")
    private String ciudadDestino;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser mayor a 0")
    private Double peso; // en kilogramos
}

