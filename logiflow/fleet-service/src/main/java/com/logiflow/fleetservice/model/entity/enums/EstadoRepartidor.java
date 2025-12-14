package com.logiflow.fleetservice.model.entity.enums;

public enum EstadoRepartidor {
  DISPONIBLE("Disponible para asignaciones"),
  EN_RUTA("En ruta realizando una entrega"),
  MANTENIMIENTO("Vehículo en mantenimiento"),
  INACTIVO("Repartidor inactivo temporalmente"),
  DESCANSO("En período de descanso");

  private final String descripcion;

  EstadoRepartidor(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}