package com.logiflow.fleetservice.model.entity.enums;

public enum EstadoRepartidor {
  DISPONIBLE("Repartidor listo para recibir asignaciones"),
  EN_RUTA("Repartidor actualmente realizando una entrega"),
  MANTENIMIENTO("Repartidor no disponible temporalmente");

  private final String descripcion;

  EstadoRepartidor(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}