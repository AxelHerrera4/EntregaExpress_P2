package com.logiflow.fleetservice.model.entity.vehiculo;

import com.logiflow.fleetservice.dto.InformacionRuta;
import com.logiflow.fleetservice.model.entity.enums.TipoEntrega;
import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Camion - Para entregas nacionales
 * Según documentación Fleet Service
 */
@Entity
@DiscriminatorValue("CAMION")
public class Camion extends VehiculoEntrega {

  @Column(name = "numero_ejes")
  private Integer numeroEjes;

  @Column(name = "capacidad_volumen")
  private Double capacidadVolumen; // m³

  @Column(name = "requiere_licencia_especial")
  private boolean requiereLicenciaEspecial;

  /**
   * Constructor con parámetros básicos
   */
  public Camion(String placa, String marca, String modelo, Integer ejes, Double volumen) {
    super(placa, marca, modelo);
    this.numeroEjes = ejes;
    this.capacidadVolumen = volumen;
    this.capacidadCarga = ejes * 2000.0; // Aproximado por eje
    this.requiereLicenciaEspecial = true;
  }

  /**
   * Constructor vacío para JPA
   */
  public Camion() {
    super();
  }

  @Override
  public TipoVehiculo getTipo() {
    return TipoVehiculo.CAMION;
  }

  @Override
  public Double getCapacidadMaxima() {
    return this.capacidadCarga;
  }

  @Override
  public boolean esAptoParaEntrega(TipoEntrega tipoEntrega) {
    return tipoEntrega == TipoEntrega.NACIONAL;
  }

  @Override
  public InformacionRuta getInformacionRuta() {
    return InformacionRuta.builder()
            .vehiculoId(this.getId() != null ? this.getId().toString() : null)
            .tipo(TipoVehiculo.CAMION)
            .capacidadCarga(this.capacidadCarga)
            .puedeUsarCiclovias(false)
            .numeroEjes(this.numeroEjes)
            .capacidadVolumen(this.capacidadVolumen)
            .build();
  }

  // Getters y Setters específicos
  public Integer getNumeroEjes() {
    return numeroEjes;
  }

  public void setNumeroEjes(Integer numeroEjes) {
    this.numeroEjes = numeroEjes;
  }

  public Double getCapacidadVolumen() {
    return capacidadVolumen;
  }

  public void setCapacidadVolumen(Double capacidadVolumen) {
    this.capacidadVolumen = capacidadVolumen;
  }

  public boolean isRequiereLicenciaEspecial() {
    return requiereLicenciaEspecial;
  }

  public void setRequiereLicenciaEspecial(boolean requiereLicenciaEspecial) {
    this.requiereLicenciaEspecial = requiereLicenciaEspecial;
  }
}
