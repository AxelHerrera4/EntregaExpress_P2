package com.logiflow.fleetservice.dto.response;
import com.logiflow.fleetservice.dto.CoordenadaDTO;
import com.logiflow.fleetservice.model.entity.enums.EstadoRepartidor;
import com.logiflow.fleetservice.model.entity.enums.TipoLicencia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepartidorResponse {
  private Long id;
  private String cedula;
  private String nombre;
  private String apellido;
  private String nombreCompleto;
  private String email;
  private String telefono;
  private String direccion;
  private String fechaNacimiento;
  private String fechaContratacion;
  private TipoLicencia tipoLicencia;
  private String numeroLicencia;
  private String fechaVencimientoLicencia;
  private Boolean licenciaVigente;
  private EstadoRepartidor estado;
  private String zonaAsignada;
  private CoordenadaDTO ubicacionActual;
  private VehiculoResponse vehiculoAsignado;
  private Integer entregasCompletadas;
  private Integer entregasFallidas;
  private Double calificacionPromedio;
  private Double tasaExito;
  private Double kilometrosRecorridos;
  private Set<String> diasLaborales;
  private String horaInicioTurno;
  private String horaFinTurno;
  private Boolean activo;
  private Boolean disponible;
}
