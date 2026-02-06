package com.logiflow.fleetservice.model.entity.enums;

public enum TipoEntrega {
  URBANA_RAPIDA("Entrega urbana - última milla"),
  INTERMUNICIPAL("Entrega dentro de provincia"),
  NACIONAL("Entrega entre provincias");

  private final String descripcion;

  TipoEntrega(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}
