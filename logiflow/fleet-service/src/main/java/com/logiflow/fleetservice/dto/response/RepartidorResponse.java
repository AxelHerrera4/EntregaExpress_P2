package com.logiflow.fleetservice.dto.response;

import com.logiflow.fleetservice.model.entity.enums.EstadoRepartidor;
import com.logiflow.fleetservice.model.entity.enums.TipoDocumento;
import com.logiflow.fleetservice.model.entity.enums.TipoLicencia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response de Repartidor según documentación Fleet Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepartidorResponse {
  
  private String id;
  private String nombre;
  private String apellido;
  private String documento;
  private TipoDocumento tipoDocumento;
  private String telefono;
  private String email;
  private EstadoRepartidor estado;
  private String zonaAsignada;
  private TipoLicencia tipoLicencia;
  private String vehiculoId;
  
  // Objeto anidado con información del vehículo
  private VehiculoInfo vehiculo;
  
  // Ubicación actual (caché)
  private UbicacionInfo ubicacionActual;
  
  private String fechaContratacion;
  private Boolean activo;
  private String createdAt;
  private String updatedAt;

  /**
   * Información básica del vehículo asignado
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class VehiculoInfo {
    private String placa;
    private String tipo;
    private String estado;
  }

  /**
   * Información de ubicación del repartidor (caché)
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UbicacionInfo {
    private Double latitud;
    private Double longitud;
    private String ultimaActualizacion;
  }
}
