package com.logiflow.fleetservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoUpdateRequest {

  private String marca;

  @Size(max = 50)
  private String modelo;

  @Min(value = 0, message = "El kilometraje no puede ser negativo")
  private Integer kilometraje;

  @Positive
  private Double capacidadCargaKg;

  @Positive
  private Double consumoCombustibleKmPorLitro;

  private Boolean activo;

  // Para actualización de ubicación
  private Double latitud;
  private Double longitud;
}

