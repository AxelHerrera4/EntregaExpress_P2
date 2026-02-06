package com.logiflow.fleetservice.dto.request;

import com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para actualizar vehículo según documentación Fleet Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoUpdateRequest {

  private EstadoVehiculo estado;

  @Positive
  private Double capacidadCarga;

  private Boolean activo;
}

