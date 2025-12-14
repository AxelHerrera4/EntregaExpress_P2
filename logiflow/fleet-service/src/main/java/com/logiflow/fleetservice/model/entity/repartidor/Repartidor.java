package com.logiflow.fleetservice.model.entity.repartidor;


import com.logiflow.fleetservice.model.entity.enums.EstadoRepartidor;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Repartidor - Representa a un conductor de la flota
 */
@Entity
@Table(name = "repartidores", indexes = {
        @Index(name = "idx_repartidor_estado", columnList = "estado"),
        @Index(name = "idx_repartidor_zona", columnList = "zona_asignada"),
        @Index(name = "idx_repartidor_cedula", columnList = "cedula", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repartidor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 20)
  private String cedula;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false, length = 100)
  private String apellido;

  @Column(unique = true, length = 100)
  private String email;

  @Column(length = 20)
  private String telefono;

  @Column(length = 500)
  private String direccion;

  @Column(name = "fecha_nacimiento")
  private LocalDate fechaNacimiento;

  @Column(name = "fecha_contratacion", nullable = false)
  private LocalDate fechaContratacion;

  // ========== LICENCIA ==========

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_licencia", nullable = false)
  private TipoLicencia tipoLicencia;

  @Column(name = "numero_licencia", length = 30)
  private String numeroLicencia;

  @Column(name = "fecha_vencimiento_licencia")
  private LocalDate fechaVencimientoLicencia;

  // ========== ESTADO Y DISPONIBILIDAD ==========

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoRepartidor estado = EstadoRepartidor.DISPONIBLE;

  @Column(name = "zona_asignada", length = 100)
  private String zonaAsignada;

  @Embedded
  @AttributeOverrides({
          @AttributeOverride(name = "latitud", column = @Column(name = "ubicacion_actual_lat")),
          @AttributeOverride(name = "longitud", column = @Column(name = "ubicacion_actual_lon"))
  })
  private Coordenada ubicacionActual;

  // ========== VEHÍCULO ASIGNADO ==========

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehiculo_id")
  private VehiculoEntrega vehiculoAsignado;

  // ========== MÉTRICAS ==========

  @Column(name = "entregas_completadas")
  private Integer entregasCompletadas = 0;

  @Column(name = "entregas_fallidas")
  private Integer entregasFallidas = 0;

  @Column(name = "calificacion_promedio")
  private Double calificacionPromedio = 0.0;

  @Column(name = "total_calificaciones")
  private Integer totalCalificaciones = 0;

  @Column(name = "kilometros_recorridos")
  private Double kilometrosRecorridos = 0.0;

  // ========== HORARIOS ==========

  @ElementCollection
  @CollectionTable(
          name = "repartidor_horarios",
          joinColumns = @JoinColumn(name = "repartidor_id")
  )
  @Column(name = "dia_semana")
  private Set<String> diasLaborales = new HashSet<>();

  @Column(name = "hora_inicio_turno")
  private String horaInicioTurno;

  @Column(name = "hora_fin_turno")
  private String horaFinTurno;

  // ========== AUDITORÍA ==========

  @Column(name = "activo")
  private Boolean activo = true;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "observaciones", length = 1000)
  private String observaciones;

  // ========== MÉTODOS DE NEGOCIO ==========

  /**
   * Verifica si el repartidor está disponible para asignación
   */
  public boolean estaDisponible() {
    return activo &&
            estado == EstadoRepartidor.DISPONIBLE &&
            vehiculoAsignado != null &&
            vehiculoAsignado.estaOperativo() &&
            licenciaVigente();
  }

  /**
   * Verifica si la licencia está vigente
   */
  public boolean licenciaVigente() {
    if (fechaVencimientoLicencia == null) {
      return false;
    }
    return fechaVencimientoLicencia.isAfter(LocalDate.now());
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
   * Actualiza la ubicación actual del repartidor
   */
  public void actualizarUbicacion(Coordenada nuevaUbicacion) {
    if (nuevaUbicacion != null && nuevaUbicacion.esValida()) {
      this.ubicacionActual = nuevaUbicacion;

      // Actualizar también la ubicación del vehículo
      if (vehiculoAsignado != null) {
        vehiculoAsignado.registrarUbicacion(nuevaUbicacion, LocalDateTime.now());
      }
    }
  }

  /**
   * Registra una entrega completada
   */
  public void registrarEntregaCompletada(double distanciaKm) {
    this.entregasCompletadas++;
    this.kilometrosRecorridos += distanciaKm;

    if (vehiculoAsignado != null) {
      vehiculoAsignado.registrarKilometrajeRecorrido(distanciaKm);
    }
  }

  /**
   * Registra una entrega fallida
   */
  public void registrarEntregaFallida() {
    this.entregasFallidas++;
  }

  /**
   * Registra una calificación del cliente
   */
  public void registrarCalificacion(double calificacion) {
    if (calificacion < 1.0 || calificacion > 5.0) {
      throw new IllegalArgumentException("La calificación debe estar entre 1 y 5");
    }

    double sumaTotal = (calificacionPromedio * totalCalificaciones) + calificacion;
    totalCalificaciones++;
    calificacionPromedio = sumaTotal / totalCalificaciones;
  }

  /**
   * Calcula la tasa de éxito del repartidor
   */
  public double getTasaExito() {
    int totalEntregas = entregasCompletadas + entregasFallidas;
    if (totalEntregas == 0) {
      return 0.0;
    }
    return (entregasCompletadas * 100.0) / totalEntregas;
  }

  /**
   * Verifica si el repartidor está en su horario laboral
   */
  public boolean estaEnHorarioLaboral() {
    // Implementación simplificada
    return activo && !diasLaborales.isEmpty();
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