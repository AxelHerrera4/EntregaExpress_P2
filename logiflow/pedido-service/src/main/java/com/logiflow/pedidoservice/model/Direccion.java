package com.logiflow.pedidoservice.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.*;
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
    @Pattern(regexp = "^[A-Za-z0-9\\s]+$", message = "La calle solo puede contener letras, números y espacios")
    private String calle;

    @NotBlank(message = "El número es obligatorio y no puede estar vacío")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "El número solo puede contener letras y números (sin espacios ni caracteres especiales)")
    private String numero;

    @NotBlank(message = "La ciudad es obligatoria y no puede estar vacía")
    @Pattern(regexp = "^[A-Za-z\\s]+$", message = "La ciudad solo puede contener letras y espacios")
    private String ciudad;

    @NotBlank(message = "La provincia es obligatoria y no puede estar vacía")
    @Pattern(regexp = "^[A-Za-z\\s]+$", message = "La provincia solo puede contener letras y espacios")
    private String provincia;

    @NotNull(message = "La latitud es obligatoria")
    @Min(value = -90, message = "La latitud mínima es -90")
    @Max(value = 90, message = "La latitud máxima es 90")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    @Min(value = -180, message = "La longitud mínima es -180")
    @Max(value = 180, message = "La longitud máxima es 180")
    private Double longitud;

}

