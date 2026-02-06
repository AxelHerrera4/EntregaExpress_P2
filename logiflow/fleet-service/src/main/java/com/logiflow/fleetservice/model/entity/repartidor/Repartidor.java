package com.logiflow.fleetservice.model.entity.repartidor;

import com.logiflow.fleetservice.model.entity.enums.EstadoRepartidor;
import com.logiflow.fleetservice.model.entity.enums.TipoDocumento;
import com.logiflow.fleetservice.model.entity.enums.TipoLicencia;
import com.logiflow.fleetservice.model.entity.vehiculo.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad Repartidor - Representa a un conductor de la flota
 * Según documentación Fleet Service
 */
@Entity
@Table(name = "repartidores", indexes = {
        @Index(name = "idx_repartidor_estado", columnList = "estado"),
        @Index(name = "idx_repartidor_zona", columnList = "zona_asignada"),
        @Index(name = "idx_repartidor_documento", columnList = "documento", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repartidor {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false, length = 100)
  private String apellido;

  @Column(nullable = false, unique = true, length = 20)
  private String documento;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_documento", nullable = false, length = 20)
  private TipoDocumento tipoDocumento;

  @Column(length = 20)
  private String telefono;

  @Column(unique = true, length = 100)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private EstadoRepartidor estado = EstadoRepartidor.DISPONIBLE;

  @Column(name = "zona_asignada", length = 50)
  private String zonaAsignada;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_licencia", nullable = false, length = 10)
  private TipoLicencia tipoLicencia;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehiculo_id")
  private VehiculoEntrega vehiculoAsignado;

  @Embedded
  @AttributeOverrides({
          @AttributeOverride(name = "latitud", column = @Column(name = "latitud")),
          @AttributeOverride(name = "longitud", column = @Column(name = "longitud"))
  })
  private Coordenada ubicacionActual;

  @Column(name = "ultima_actualizacion_ubicacion")
  private LocalDateTime ultimaActualizacionUbicacion;

  @Column(name = "fecha_contratacion", nullable = false)
  private LocalDate fechaContratacion;

  @Column(name = "activo")
  @Builder.Default
  private Boolean activo = true;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // ========== MÉTODOS DE NEGOCIO ==========

  /**
   * Verifica si el repartidor está disponible para asignación
   */
  public boolean estaDisponible() {
    return activo &&
            estado == EstadoRepartidor.DISPONIBLE &&
            vehiculoAsignado != null &&
            vehiculoAsignado.estaDisponible();
  }

  /**
   * Verifica si el repartidor puede manejar un tipo de vehículo
   */
  public boolean puedeConducirVehiculo(VehiculoEntrega vehiculo) {
    if (vehiculo instanceof Motorizado) {
      return tipoLicencia == TipoLicencia.TIPO_A ||
              tipoLicencia == TipoLicencia.TIPO_E;
    } else if (vehiculo instanceof VehiculoLiviano) {
      return tipoLicencia == TipoLicencia.TIPO_B ||
              tipoLicencia == TipoLicencia.TIPO_C ||
              tipoLicencia == TipoLicencia.TIPO_E;
    } else if (vehiculo instanceof Camion) {
      return tipoLicencia == TipoLicencia.TIPO_C ||
              tipoLicencia == TipoLicencia.TIPO_E;
    }
    return false;
  }

  /**
   * Asigna un vehículo al repartidor
   */
  public void asignarVehiculo(VehiculoEntrega vehiculo) {
    if (!puedeConducirVehiculo(vehiculo)) {
      throw new IllegalArgumentException(
              "El repartidor no tiene licencia para conducir este tipo de vehículo"
      );
    }
    this.vehiculoAsignado = vehiculo;
  }

  /**
   * Cambia el estado del repartidor
   */
  public void cambiarEstado(EstadoRepartidor nuevoEstado) {
    this.estado = nuevoEstado;
  }

  /**
   * Actualiza la ubicación actual del repartidor (caché)
   */
  public void actualizarUbicacion(Coordenada nuevaUbicacion, LocalDateTime timestamp) {
    if (nuevaUbicacion != null && nuevaUbicacion.esValida()) {
      this.ubicacionActual = nuevaUbicacion;
      this.ultimaActualizacionUbicacion = timestamp;
    }
  }

  /**
   * Obtiene el nombre completo del repartidor
   */
  public String getNombreCompleto() {
    return nombre + " " + apellido;
  }

  /**
   * Verifica si el repartidor puede trabajar en una zona
   */
  public boolean puedeTrabajarEnZona(String zona) {
    if (zonaAsignada == null || zonaAsignada.isEmpty()) {
      return true; // Sin restricción de zona
    }
    return zonaAsignada.equalsIgnoreCase(zona);
  }
}