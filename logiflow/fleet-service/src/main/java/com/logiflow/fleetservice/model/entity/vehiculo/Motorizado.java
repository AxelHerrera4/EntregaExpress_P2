package com.logiflow.fleetservice.model.entity.vehiculo;

import com.logiflow.fleetservice.dto.InformacionRuta;
import com.logiflow.fleetservice.model.entity.enums.TipoEntrega;
import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Motorizado - Para entregas urbanas rápidas (última milla)
 * Según documentación Fleet Service
 */
@Entity
@DiscriminatorValue("MOTORIZADO")
public class Motorizado extends VehiculoEntrega {

  @Column(name = "cilindraje")
  private Integer cilindraje;

  @Column(name = "tiene_cajones")
  private boolean tieneCajones; // Para delivery

  /**
   * Constructor con parámetros básicos
   */
  public Motorizado(String placa, String marca, String modelo, Integer cilindraje) {
    super(placa, marca, modelo);
    this.cilindraje = cilindraje;
    this.capacidadCarga = 30.0; // kg máximo
  }

  /**
   * Constructor vacío para JPA
   */
  public Motorizado() {
    super();
  }

  @Override
  public TipoVehiculo getTipo() {
    return TipoVehiculo.MOTORIZADO;
  }

  @Override
  public Double getCapacidadMaxima() {
    return this.capacidadCarga;
  }

  @Override
  public boolean esAptoParaEntrega(TipoEntrega tipoEntrega) {
    return tipoEntrega == TipoEntrega.URBANA_RAPIDA;
  }

  @Override
  public InformacionRuta getInformacionRuta() {
    return new InformacionRuta(
            this.getId() != null ? this.getId().toString() : null,
            TipoVehiculo.MOTORIZADO,
            this.capacidadCarga,
            true // puede usar ciclovías
    );
  }

  // Getters y Setters específicos
  public Integer getCilindraje() {
    return cilindraje;
  }

  public void setCilindraje(Integer cilindraje) {
    this.cilindraje = cilindraje;
  }

  public boolean isTieneCajones() {
    return tieneCajones;
  }

  public void setTieneCajones(boolean tieneCajones) {
    this.tieneCajones = tieneCajones;
  }
}
