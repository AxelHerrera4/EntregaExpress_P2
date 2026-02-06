package com.logiflow.fleetservice.dto.mapper;

import com.logiflow.fleetservice.dto.request.RepartidorCreateRequest;
import com.logiflow.fleetservice.dto.response.RepartidorResponse;
import com.logiflow.fleetservice.model.entity.repartidor.Repartidor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Mapper de Repartidor según documentación Fleet Service
 */
@Component
public class RepartidorMapper {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  public Repartidor toEntity(RepartidorCreateRequest request) {
    return Repartidor.builder()
            .nombre(request.getNombre())
            .apellido(request.getApellido())
            .documento(request.getDocumento())
            .tipoDocumento(request.getTipoDocumento())
            .telefono(request.getTelefono())
            .email(request.getEmail())
            .zonaAsignada(request.getZonaAsignada())
            .tipoLicencia(request.getTipoLicencia())
            .fechaContratacion(LocalDate.now())
            .activo(true)
            .build();
  }

  public RepartidorResponse toResponse(Repartidor repartidor) {
    if (repartidor == null) {
      return null;
    }

    RepartidorResponse.RepartidorResponseBuilder builder = RepartidorResponse.builder()
            .id(repartidor.getId() != null ? repartidor.getId().toString() : null)
            .nombre(repartidor.getNombre())
            .apellido(repartidor.getApellido())
            .documento(repartidor.getDocumento())
            .tipoDocumento(repartidor.getTipoDocumento())
            .telefono(repartidor.getTelefono())
            .email(repartidor.getEmail())
            .estado(repartidor.getEstado())
            .zonaAsignada(repartidor.getZonaAsignada())
            .tipoLicencia(repartidor.getTipoLicencia())
            .activo(repartidor.getActivo());

    // Información del vehículo asignado
    if (repartidor.getVehiculoAsignado() != null) {
      builder.vehiculoId(repartidor.getVehiculoAsignado().getId() != null ? 
              repartidor.getVehiculoAsignado().getId().toString() : null);
      
      builder.vehiculo(RepartidorResponse.VehiculoInfo.builder()
              .placa(repartidor.getVehiculoAsignado().getPlaca())
              .tipo(repartidor.getVehiculoAsignado().getTipo().name())
              .estado(repartidor.getVehiculoAsignado().getEstado().name())
              .build());
    }

    // Ubicación actual (caché)
    if (repartidor.getUbicacionActual() != null) {
      builder.ubicacionActual(RepartidorResponse.UbicacionInfo.builder()
              .latitud(repartidor.getUbicacionActual().getLatitud())
              .longitud(repartidor.getUbicacionActual().getLongitud())
              .ultimaActualizacion(repartidor.getUltimaActualizacionUbicacion() != null ?
                      repartidor.getUltimaActualizacionUbicacion().format(DATETIME_FORMATTER) : null)
              .build());
    }

    // Fechas
    if (repartidor.getFechaContratacion() != null) {
      builder.fechaContratacion(repartidor.getFechaContratacion().format(DATE_FORMATTER));
    }
    if (repartidor.getCreatedAt() != null) {
      builder.createdAt(repartidor.getCreatedAt().format(DATETIME_FORMATTER));
    }
    if (repartidor.getUpdatedAt() != null) {
      builder.updatedAt(repartidor.getUpdatedAt().format(DATETIME_FORMATTER));
    }

    return builder.build();
  }
}
