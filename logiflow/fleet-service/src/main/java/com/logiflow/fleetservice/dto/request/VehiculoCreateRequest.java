package com.logiflow.fleetservice.dto.request;

import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import jakarta.validation.constraints.*;
import lombok.*;

// ========== VEHÍCULO DTOs ==========

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoCreateRequest {

  @NotNull(message = "El tipo de vehículo es obligatorio")
  private TipoVehiculo tipo;

  @NotBlank(message = "La placa es obligatoria")
  @Size(max = 20, message = "La placa no puede tener más de 20 caracteres")
  private String placa;

  @NotBlank(message = "La marca es obligatoria")
  @Size(max = 50, message = "La marca no puede tener más de 50 caracteres")
  private String marca;

  @NotBlank(message = "El modelo es obligatorio")
  @Size(max = 50, message = "El modelo no puede tener más de 50 caracteres")
  private String modelo;

  @NotNull(message = "El año es obligatorio")
  @Min(value = 1990, message = "El año debe ser mayor a 1990")
  @Max(value = 2025, message = "El año no puede ser mayor a 2025")
  private Integer anio;

  @Positive(message = "La capacidad de carga debe ser positiva")
  private Double capacidadCargaKg;

  @Positive(message = "El consumo de combustible debe ser positivo")
  private Double consumoCombustibleKmPorLitro;

  // Campos específicos para Camion
  @Min(value = 2, message = "El número de ejes debe ser al menos 2")
  private Integer numeroEjes;

  private Boolean requiereRampa;
}
