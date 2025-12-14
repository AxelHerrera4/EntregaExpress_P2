package com.logiflow.fleetservice.model.entity;

import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import com.logiflow.fleetservice.model.interfaces.IRegistrableGPS;
import com.logiflow.fleetservice.model.interfaces.IRuteable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "vehiculos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_vehiculo", discriminatorType = DiscriminatorType.STRING)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class VehiculoEntrega implements IRuteable, IRegistrableGPS {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 20)
  private String placa;

  @Column(nullable = false, length = 50)
  private String marca;

  @Column(nullable = false, length = 50)
  private String modelo;

  @Column(nullable = false)
  private Integer anio;

  @Column(name = "kilometraje")
  private Integer kilometraje;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo", nullable = false, insertable = false, updatable = false)
  private TipoVehiculo tipo;

  @Embedded
  @AttributeOverrides({
          @AttributeOverride(name = "latitud", column = @Column(name = "ultima_latitud")),
          @AttributeOverride(name = "longitud", column = @Column(name = "ultima_longitud"))
  })
  private Coordenada ultimaUbicacion;

  @Column(name = "ultima_actualizacion_gps")
  private LocalDateTime ultimaActualizacionGPS;

  @Column(name = "capacidad_carga_kg")
  private Double capacidadCargaKg;

  @Column(name = "consumo_combustible_km_litro")
  private Double consumoCombustibleKmPorLitro;

  @Column(name = "activo")
  private Boolean activo = true;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // ========== TEMPLATE METHOD PATTERN ==========

  /**
   * Template Method: Define el algoritmo general para calcular el costo de una ruta
   * Las subclases implementan los pasos específicos
   */
  public final double calcularCostoRuta(double distanciaKm) {
    double costoBase = calcularCostoBase(distanciaKm);
    double costoAdicional = calcularCostoAdicional(distanciaKm);
    double costoMantenimiento = calcularCostoMantenimiento(distanciaKm);

    return costoBase + costoAdicional + costoMantenimiento;
  }

  /**
   * Hook method: Cada tipo de vehículo calcula su costo base
   */
  protected abstract double calcularCostoBase(double distanciaKm);

  /**
   * Hook method: Costos adicionales específicos del vehículo
   */
  protected abstract double calcularCostoAdicional(double distanciaKm);

  /**
   * Hook method: Costo de mantenimiento por kilómetro
   */
  protected double calcularCostoMantenimiento(double distanciaKm) {
    return distanciaKm * 0.05; // $0.05 por km por defecto
  }

  // ========== MÉTODOS ABSTRACTOS ==========

  /**
   * Cada tipo de vehículo define su velocidad promedio
   */
  public abstract double getVelocidadPromedioKmH();

  /**
   * Cada tipo de vehículo define su rango máximo de operación
   */
  public abstract double getRangoMaximoKm();

  /**
   * Determina si el vehículo puede operar en una zona específica
   */
  public abstract boolean puedeOperarEnZona(String tipoZona);

  // ========== IMPLEMENTACIÓN DE IRuteable ==========

  @Override
  public List<Coordenada> generarRuta(Coordenada origen, Coordenada destino) {
    // Implementación simplificada - En producción integraría con API de mapas
    List<Coordenada> ruta = new ArrayList<>();
    ruta.add(origen);

    // Punto intermedio (simulación de ruta)
    double latMedia = (origen.getLatitud() + destino.getLatitud()) / 2;
    double lonMedia = (origen.getLongitud() + destino.getLongitud()) / 2;
    ruta.add(new Coordenada(latMedia, lonMedia));

    ruta.add(destino);
    return ruta;
  }

  @Override
  public double calcularDistancia(Coordenada origen, Coordenada destino) {
    return origen.distanciaHasta(destino);
  }

  @Override
  public int estimarTiempoViaje(Coordenada origen, Coordenada destino) {
    double distanciaKm = calcularDistancia(origen, destino);
    double tiempoHoras = distanciaKm / getVelocidadPromedioKmH();
    return (int) Math.ceil(tiempoHoras * 60); // Convertir a minutos
  }

  @Override
  public boolean puedeRealizarRuta(double distanciaKm) {
    return distanciaKm <= getRangoMaximoKm() && activo;
  }

  // ========== IMPLEMENTACIÓN DE IRegistrableGPS ==========

  @Override
  public void registrarUbicacion(Coordenada coordenada, LocalDateTime timestamp) {
    if (coordenada != null && coordenada.esValida()) {
      this.ultimaUbicacion = coordenada;
      this.ultimaActualizacionGPS = timestamp;
    }
  }

  @Override
  public Coordenada obtenerUltimaUbicacion() {
    return this.ultimaUbicacion;
  }

  @Override
  public LocalDateTime obtenerUltimaActualizacion() {
    return this.ultimaActualizacionGPS;
  }

  @Override
  public boolean ubicacionActualizada(int minutosMaximos) {
    if (ultimaActualizacionGPS == null) {
      return false;
    }
    LocalDateTime limite = LocalDateTime.now().minusMinutes(minutosMaximos);
    return ultimaActualizacionGPS.isAfter(limite);
  }

  // ========== MÉTODOS DE NEGOCIO ==========

  /**
   * Valida si el vehículo está en condiciones de operar
   */
  public boolean estaOperativo() {
    return activo && ubicacionActualizada(30);
  }

  /**
   * Incrementa el kilometraje del vehículo
   */
  public void registrarKilometrajeRecorrido(double km) {
    if (this.kilometraje == null) {
      this.kilometraje = 0;
    }
    this.kilometraje += (int) Math.ceil(km);
  }
}