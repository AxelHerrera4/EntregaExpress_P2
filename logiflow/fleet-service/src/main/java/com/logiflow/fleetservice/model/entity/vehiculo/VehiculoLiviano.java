package com.logiflow.fleetservice.model.entity.vehiculo;

import com.logiflow.fleetservice.dto.InformacionRuta;
import com.logiflow.fleetservice.model.entity.enums.TipoCarroceria;
import com.logiflow.fleetservice.model.entity.enums.TipoEntrega;
import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

/**
 * VehiculoLiviano - Para entregas intermunicipales
 * Según documentación Fleet Service
 */
@Entity
@DiscriminatorValue("VEHICULO_LIVIANO")
public class VehiculoLiviano extends VehiculoEntrega {

  @Column(name = "numero_puertas")
  private Integer numeroPuertas;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_carroceria", length = 20)
  private TipoCarroceria tipoCarroceria; // SEDAN, SUV, PICKUP

  /**
   * Constructor con parámetros básicos
   */
  public VehiculoLiviano(String placa, String marca, String modelo, TipoCarroceria tipo) {
    super(placa, marca, modelo);
    this.tipoCarroceria = tipo;
    this.capacidadCarga = tipo == TipoCarroceria.PICKUP ? 1000.0 : 500.0;
  }

  /**
   * Constructor vacío para JPA
   */
  public VehiculoLiviano() {
    super();
  }

  @Override
  public TipoVehiculo getTipo() {
    return TipoVehiculo.VEHICULO_LIVIANO;
  }

  @Override
  public Double getCapacidadMaxima() {
    return this.capacidadCarga;
  }

  @Override
  public boolean esAptoParaEntrega(TipoEntrega tipoEntrega) {
    return tipoEntrega == TipoEntrega.INTERMUNICIPAL;
  }

  @Override
  public InformacionRuta getInformacionRuta() {
    return new InformacionRuta(
            this.getId() != null ? this.getId().toString() : null,
            TipoVehiculo.VEHICULO_LIVIANO,
            this.capacidadCarga,
            false // no puede usar ciclovías
    );
  }

  // Getters y Setters específicos
  public Integer getNumeroPuertas() {
    return numeroPuertas;
  }

  public void setNumeroPuertas(Integer numeroPuertas) {
    this.numeroPuertas = numeroPuertas;
  }

  public TipoCarroceria getTipoCarroceria() {
    return tipoCarroceria;
  }

  public void setTipoCarroceria(TipoCarroceria tipoCarroceria) {
    this.tipoCarroceria = tipoCarroceria;
  }
}
