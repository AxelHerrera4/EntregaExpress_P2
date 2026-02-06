package com.logiflow.fleetservice.dto;

import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import lombok.*;

/**
 * DTO que encapsula información de ruta para el Routing Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InformacionRuta {
  
  private String vehiculoId;
  private TipoVehiculo tipo;
  private Double capacidadCarga;
  private boolean puedeUsarCiclovias;
  private Integer numeroEjes;
  private Double capacidadVolumen;
  
  // Constructor simplificado sin campos opcionales
  public InformacionRuta(String vehiculoId, TipoVehiculo tipo, Double capacidadCarga, boolean puedeUsarCiclovias) {
    this.vehiculoId = vehiculoId;
    this.tipo = tipo;
    this.capacidadCarga = capacidadCarga;
    this.puedeUsarCiclovias = puedeUsarCiclovias;
  }
}
