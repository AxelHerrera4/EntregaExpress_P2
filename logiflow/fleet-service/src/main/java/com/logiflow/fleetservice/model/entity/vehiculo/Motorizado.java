package com.logiflow.fleetservice.model.entity;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Vehículo tipo Motorizado - Para entregas urbanas rápidas (última milla)
 */
@Entity
@DiscriminatorValue("MOTORIZADO")
public class Motorizado extends VehiculoEntrega {

  private static final double VELOCIDAD_PROMEDIO = 35.0; // km/h en ciudad
  private static final double RANGO_MAXIMO = 100.0; // km
  private static final double COSTO_BASE_POR_KM = 0.50; // USD

  @Override
  protected double calcularCostoBase(double distanciaKm) {
    return distanciaKm * COSTO_BASE_POR_KM;
  }

  @Override
  protected double calcularCostoAdicional(double distanciaKm) {
    // Recargo por tráfico urbano
    return distanciaKm * 0.10;
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
    // Los motorizados operan principalmente en zonas urbanas
    return "URBANA".equalsIgnoreCase(tipoZona) ||
            "RESIDENCIAL".equalsIgnoreCase(tipoZona);
  }

  /**
   * Los motorizados tienen mayor agilidad en ciudad
   */
  public boolean puedeAccederACentroHistorico() {
    return true;
  }
}
