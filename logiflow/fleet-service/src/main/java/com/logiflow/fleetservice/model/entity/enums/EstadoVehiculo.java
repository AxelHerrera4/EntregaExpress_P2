package com.logiflow.fleetservice.model.entity.enums;

public enum EstadoVehiculo {
  ACTIVO("Vehículo operativo y disponible"),
  MANTENIMIENTO("Vehículo en reparación o revisión"),
  FUERA_DE_SERVICIO("Vehículo dado de baja o no operativo");

  private final String descripcion;

  EstadoVehiculo(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}
