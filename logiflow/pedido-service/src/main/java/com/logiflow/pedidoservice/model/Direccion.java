package com.logiflow.pedidoservice.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo embebido minimalista para direcciones
 * Patrón: Value Object (DDD)
 * No tiene identidad propia, solo valor
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Direccion {

    @NotBlank(message = "La calle es obligatoria y no puede estar vacía")
    private String calle;

    @NotBlank(message = "El número es obligatorio y no puede estar vacío")
    private String numero;

    @NotBlank(message = "La ciudad es obligatoria y no puede estar vacía")
    private String ciudad;

    @NotBlank(message = "La provincia es obligatoria y no puede estar vacía")
    private String provincia;
}

