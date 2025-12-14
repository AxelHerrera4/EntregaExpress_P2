package com.logiflow.fleetservice.dto.request;


import com.logiflow.fleetservice.model.entity.enums.TipoLicencia;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepartidorCreateRequest {

  @NotBlank(message = "La cédula es obligatoria")
  @Size(min = 10, max = 20, message = "La cédula debe tener entre 10 y 20 caracteres")
  private String cedula;

  @NotBlank(message = "El nombre es obligatorio")
  @Size(max = 100)
  private String nombre;

  @NotBlank(message = "El apellido es obligatorio")
  @Size(max = 100)
  private String apellido;

  @Email(message = "El email debe ser válido")
  private String email;

  @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
  private String telefono;

  @Size(max = 500)
  private String direccion;

  @Past(message = "La fecha de nacimiento debe ser en el pasado")
  private LocalDate fechaNacimiento;

  @NotNull(message = "La fecha de contratación es obligatoria")
  private LocalDate fechaContratacion;

  @NotNull(message = "El tipo de licencia es obligatorio")
  private TipoLicencia tipoLicencia;

  @NotBlank(message = "El número de licencia es obligatorio")
  @Size(max = 30)
  private String numeroLicencia;

  @Future(message = "La fecha de vencimiento debe ser futura")
  private LocalDate fechaVencimientoLicencia;

  @Size(max = 100)
  private String zonaAsignada;

  private Long vehiculoId;

  private Set<String> diasLaborales;

  @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Formato de hora inválido (HH:mm)")
  private String horaInicioTurno;

  @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Formato de hora inválido (HH:mm)")
  private String horaFinTurno;
}