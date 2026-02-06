package com.logiflow.fleetservice.model.entity.enums;

public enum TipoDocumento {
  CEDULA("Cédula de identidad"),
  PASAPORTE("Pasaporte");

  private final String descripcion;

  TipoDocumento(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}
