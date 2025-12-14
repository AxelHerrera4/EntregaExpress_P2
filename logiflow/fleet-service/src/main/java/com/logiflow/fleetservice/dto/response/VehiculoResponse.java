package com.logiflow.fleetservice.dto.response;

import com.logiflow.fleetservice.dto.CoordenadaDTO;
import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoResponse {
  private Long id;
  private TipoVehiculo tipo;
  private String placa;
  private String marca;
  private String modelo;
  private Integer anio;
  private Integer kilometraje;
  private Double capacidadCargaKg;
  private Double consumoCombustibleKmPorLitro;
  private Boolean activo;
  private CoordenadaDTO ultimaUbicacion;
  private String ultimaActualizacionGPS;

  // Campos específicos para Camion
  private Integer numeroEjes;
  private Boolean requiereRampa;
}