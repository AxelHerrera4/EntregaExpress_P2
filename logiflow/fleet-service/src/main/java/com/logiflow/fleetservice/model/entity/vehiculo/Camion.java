package com.logiflow.fleetservice.model.entity;


import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Camión - Para entregas nacionales (furgonetas o camiones medianos/grandes)
 */
@Entity
@DiscriminatorValue("CAMION")
public class Camion extends VehiculoEntrega {

  private static final double VELOCIDAD_PROMEDIO = 50.0; // km/h en carretera
  private static final double RANGO_MAXIMO = 800.0; // km
  private static final double COSTO_BASE_POR_KM = 1.50; // USD

  @Column(name = "numero_ejes")
  private Integer numeroEjes;

  @Column(name = "requiere_rampa")
  private Boolean requiereRampa = false;

  @Override
  protected double calcularCostoBase(double distanciaKm) {
    double costoBase = distanciaKm * COSTO_BASE_POR_KM;

    // Recargo por número de ejes
    if (numeroEjes != null && numeroEjes > 2) {
      costoBase += distanciaKm * 0.20 * (numeroEjes - 2);
    }

    return costoBase;
  }

  @Override
  protected double calcularCostoAdicional(double distanciaKm) {
    // Recargos por peajes, peso y distancia
    double recargoPeajes = distanciaKm > 200 ? 15.0 : 8.0;
    double recargoLargaDistancia = distanciaKm > 500 ? distanciaKm * 0.10 : 0;

    return recargoPeajes + recargoLargaDistancia;
  }

  @Override
  protected double calcularCostoMantenimiento(double distanciaKm) {
    // Camiones tienen mayor costo de mantenimiento
    return distanciaKm * 0.15;
  }

  @Override
  public double getVelocidadPromedioKmH() {
    return VELOCIDAD_PROMEDIO;
  }

  @Override
  public double getRangoMaximoKm() {
    return RANGO_MAXIMO;
  }

  @Override
  public boolean puedeOperarEnZona(String tipoZona) {
    // Camiones operan en rutas nacionales e intermunicipales
    return "NACIONAL".equalsIgnoreCase(tipoZona) ||
            "INTERMUNICIPAL".equalsIgnoreCase(tipoZona) ||
            "INTERURBANA".equalsIgnoreCase(tipoZona);
  }

  /**
   * Verifica si el camión puede entrar a zonas con restricción de peso
   */
  public boolean puedeAccederZonaRestringida() {
    Double capacidad = getCapacidadCargaKg();
    return capacidad != null && capacidad < 5000; // Menos de 5 toneladas
  }

  /**
   * Capacidad para transportar grandes volúmenes
   */
  public int getCantidadMaximaPaquetes() {
    return 200;
  }

  // Getters y Setters específicos
  public Integer getNumeroEjes() {
    return numeroEjes;
  }

  public void setNumeroEjes(Integer numeroEjes) {
    this.numeroEjes = numeroEjes;
  }

  public Boolean getRequiereRampa() {
    return requiereRampa;
  }

  public void setRequiereRampa(Boolean requiereRampa) {
    this.requiereRampa = requiereRampa;
  }
}