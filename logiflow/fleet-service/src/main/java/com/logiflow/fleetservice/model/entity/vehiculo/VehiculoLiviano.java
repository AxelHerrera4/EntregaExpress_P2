package com.logiflow.fleetservice.model.entity.vehiculo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Vehículo tipo Vehículo Liviano - Para entregas intermunicipales y suburbanas
 */
@Entity
@DiscriminatorValue("VEHICULO_LIVIANO")
public class VehiculoLiviano extends VehiculoEntrega {

  private static final double VELOCIDAD_PROMEDIO = 60.0; // km/h en carretera
  private static final double RANGO_MAXIMO = 300.0; // km
  private static final double COSTO_BASE_POR_KM = 0.80; // USD

  @Override
  protected double calcularCostoBase(double distanciaKm) {
    return distanciaKm * COSTO_BASE_POR_KM;
  }

  @Override
  protected double calcularCostoAdicional(double distanciaKm) {
    // Recargo por peajes y vías interprovinciales
    double recargoDistancia = distanciaKm > 100 ? distanciaKm * 0.15 : distanciaKm * 0.08;
    return recargoDistancia + 2.0; // $2 fijo por peajes estimados
  }

  @Override
  protected double calcularCostoMantenimiento(double distanciaKm) {
    // Mayor costo de mantenimiento que motos
    return distanciaKm * 0.08;
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
    // Operan en zonas urbanas e intermunicipales
    return "URBANA".equalsIgnoreCase(tipoZona) ||
            "INTERMUNICIPAL".equalsIgnoreCase(tipoZona) ||
            "SUBURBANA".equalsIgnoreCase(tipoZona);
  }

  /**
   * Capacidad para transportar paquetes medianos
   */
  public int getCantidadMaximaPaquetes() {
    return 30;
  }
}