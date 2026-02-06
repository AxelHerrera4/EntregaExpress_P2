package com.logiflow.fleetservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo;
import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response de Vehículo según documentación Fleet Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehiculoResponse {
  
  private String id;
  private String placa;
  private TipoVehiculo tipo;
  private String marca;
  private String modelo;
  private Integer anio;
  private Double capacidadCarga;
  private EstadoVehiculo estado;
  
  // Objeto anidado con características específicas
  private CaracteristicasEspecificas caracteristicasEspecificas;
  
  private Boolean activo;
  private String createdAt;
  private String updatedAt;

  /**
   * Características específicas según tipo de vehículo
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CaracteristicasEspecificas {
    // Para MOTORIZADO
    private Integer cilindraje;
    private Boolean tieneCajones;
    
    // Para VEHICULO_LIVIANO
    private Integer numeroPuertas;
    private String tipoCarroceria;
    
    // Para CAMION
    private Integer numeroEjes;
    private Double capacidadVolumen;
  }
}
