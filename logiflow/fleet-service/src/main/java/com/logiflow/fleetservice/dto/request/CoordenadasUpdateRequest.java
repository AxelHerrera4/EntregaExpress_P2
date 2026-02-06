package com.logiflow.fleetservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para actualizar coordenadas GPS del repartidor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordenadasUpdateRequest {

  @NotNull(message = "La latitud es obligatoria")
  @Min(value = -90, message = "La latitud mínima es -90")
  @Max(value = 90, message = "La latitud máxima es 90")
  private Double latitud;

  @NotNull(message = "La longitud es obligatoria")
  @Min(value = -180, message = "La longitud mínima es -180")
  @Max(value = 180, message = "La longitud máxima es 180")
  private Double longitud;
}
