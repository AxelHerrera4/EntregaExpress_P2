package com.logiflow.fleetservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordenadaDTO {

  @NotNull(message = "La latitud es obligatoria")
  @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
  @DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
  private Double latitud;

  @NotNull(message = "La longitud es obligatoria")
  @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
  @DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
  private Double longitud;
}
