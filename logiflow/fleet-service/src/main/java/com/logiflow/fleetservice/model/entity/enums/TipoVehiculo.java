package com.logiflow.fleetservice.entity.enums;

public enum TipoVehiculo {
  MOTORIZADO("Moto o motocicleta para entregas urbanas"),
  VEHICULO_LIVIANO("Auto o camioneta para entregas intermunicipales"),
  CAMION("Furgoneta o camión para entregas nacionales");

  private final String descripcion;

  TipoVehiculo(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}