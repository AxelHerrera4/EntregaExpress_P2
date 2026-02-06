package com.logiflow.fleetservice.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepartidorUpdateRequest {

  @Email
  private String email;

  private String telefono;

  private String zonaAsignada;

  private UUID vehiculoId;

  private Boolean activo;
}

