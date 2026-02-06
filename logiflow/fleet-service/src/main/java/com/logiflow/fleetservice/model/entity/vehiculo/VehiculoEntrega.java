package com.logiflow.fleetservice.model.entity.vehiculo;

import com.logiflow.fleetservice.dto.InformacionRuta;
import com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo;
import com.logiflow.fleetservice.model.entity.enums.TipoEntrega;
import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import com.logiflow.fleetservice.model.interfaces.IRuteable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Clase abstracta que define el comportamiento común de todos los vehículos.
 * NO puede ser instanciada directamente.
 * Según documentación Fleet Service
 */
@Entity
@Table(name = "vehiculos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_vehiculo", discriminatorType = DiscriminatorType.STRING)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class VehiculoEntrega implements IRuteable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, unique = true, length = 20)
  protected String placa;

  @Column(nullable = false, length = 50)
  protected String marca;

  @Column(nullable = false, length = 50)
  protected String modelo;

  @Column(nullable = false)
  protected Integer anio;

  @Column(name = "capacidad_carga")
  protected Double capacidadCarga;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  protected EstadoVehiculo estado = EstadoVehiculo.ACTIVO;

  @Column(name = "activo")
  protected Boolean activo = true;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // ========== CONSTRUCTOR PROTEGIDO ==========

  /**
   * Constructor protegido para forzar uso de subclases
   */
  protected VehiculoEntrega() {
  }

  protected VehiculoEntrega(String placa, String marca, String modelo) {
    this.placa = placa;
    this.marca = marca;
    this.modelo = modelo;
  }

  // ========== MÉTODOS ABSTRACTOS QUE LAS SUBCLASES DEBEN IMPLEMENTAR ==========

  /**
   * Cada subclase define su tipo de vehículo
   */
  public abstract TipoVehiculo getTipo();

  /**
   * Cada subclase define su capacidad máxima
   */
  public abstract Double getCapacidadMaxima();

  /**
   * Verifica si el vehículo es apto para un tipo de entrega
   */
  public abstract boolean esAptoParaEntrega(TipoEntrega tipoEntrega);

  // ========== IMPLEMENTACIÓN DE IRuteable ==========

  /**
   * Provee información del vehículo para el Routing Service
   */
  @Override
  public abstract InformacionRuta getInformacionRuta();

  // ========== MÉTODOS CONCRETOS COMPARTIDOS ==========

  /**
   * Verifica si el vehículo está disponible
   */
  public boolean estaDisponible() {
    return this.estado == EstadoVehiculo.ACTIVO && this.activo;
  }

  /**
   * Cambia el estado del vehículo
   */
  public void cambiarEstado(EstadoVehiculo nuevoEstado) {
    this.estado = nuevoEstado;
  }
}