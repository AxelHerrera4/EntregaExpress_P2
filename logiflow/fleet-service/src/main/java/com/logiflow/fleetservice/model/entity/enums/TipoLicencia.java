package com.logiflow.fleetservice.entity.enums;

public enum TipoLicencia {
  TIPO_A("Licencia para motocicletas"),
  TIPO_B("Licencia para vehículos livianos"),
  TIPO_C("Licencia para vehículos pesados"),
  TIPO_D("Licencia para transporte público"),
  TIPO_E("Licencia profesional");

  private final String descripcion;

  TipoLicencia(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}