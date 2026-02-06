package com.logiflow.fleetservice.model.entity.enums;

public enum TipoCarroceria {
  SEDAN("Sedán"),
  SUV("SUV o camioneta"),
  PICKUP("Pickup o camioneta de carga");

  private final String descripcion;

  TipoCarroceria(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}
