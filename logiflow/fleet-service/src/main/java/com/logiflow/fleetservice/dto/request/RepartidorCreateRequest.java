package com.logiflow.fleetservice.dto.request;

import com.logiflow.fleetservice.model.entity.enums.TipoDocumento;
import com.logiflow.fleetservice.model.entity.enums.TipoLicencia;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepartidorCreateRequest {

  @NotBlank(message = "El nombre es obligatorio")
  @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
  private String nombre;

  @NotBlank(message = "El apellido es obligatorio")
  @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
  private String apellido;

  @NotBlank(message = "El documento es obligatorio")
  @Size(min = 5, max = 20, message = "El documento debe tener entre 5 y 20 caracteres")
  private String documento;

  @NotNull(message = "El tipo de documento es obligatorio")
  private TipoDocumento tipoDocumento;

  @Pattern(regexp = "^\\+593[0-9]{9}$", message = "Teléfono debe ser formato +593XXXXXXXXX")
  private String telefono;

  @Email(message = "El email debe ser válido")
  private String email;

  @NotNull(message = "La zona asignada es obligatoria")
  @Size(min = 2, max = 50, message = "La zona debe tener entre 2 y 50 caracteres")
  private String zonaAsignada;

  @NotNull(message = "El tipo de licencia es obligatorio")
  private TipoLicencia tipoLicencia;

  private UUID vehiculoId;
}
