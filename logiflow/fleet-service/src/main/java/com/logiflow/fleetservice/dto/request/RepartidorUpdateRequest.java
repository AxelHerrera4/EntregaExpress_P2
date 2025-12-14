package com.logiflow.fleetservice.dto.request;


import com.logiflow.fleetservice.model.entity.enums.EstadoRepartidor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepartidorUpdateRequest {

  @Email
  private String email;

  private String telefono;

  @Size(max = 500)
  private String direccion;

  private EstadoRepartidor estado;

  @Size(max = 100)
  private String zonaAsignada;

  private Long vehiculoId;

  private Boolean activo;

  private Double latitud;
  private Double longitud;

  private Set<String> diasLaborales;
  private String horaInicioTurno;
  private String horaFinTurno;

  @Size(max = 1000)
  private String observaciones;
}
