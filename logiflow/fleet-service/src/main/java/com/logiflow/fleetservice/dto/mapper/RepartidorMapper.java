package com.logiflow.fleetservice.dto.mapper;


import com.logiflow.fleetservice.dto.CoordenadaDTO;
import com.logiflow.fleetservice.dto.request.RepartidorCreateRequest;
import com.logiflow.fleetservice.dto.response.RepartidorResponse;
import com.logiflow.fleetservice.model.entity.repartidor.Repartidor;
import com.logiflow.fleetservice.model.entity.vehiculo.Coordenada;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class RepartidorMapper {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final VehiculoMapper vehiculoMapper;

  public Repartidor toEntity(RepartidorCreateRequest request) {
    return Repartidor.builder()
            .cedula(request.getCedula())
            .nombre(request.getNombre())
            .apellido(request.getApellido())
            .email(request.getEmail())
            .telefono(request.getTelefono())
            .direccion(request.getDireccion())
            .fechaNacimiento(request.getFechaNacimiento())
            .fechaContratacion(request.getFechaContratacion())
            .tipoLicencia(request.getTipoLicencia())
            .numeroLicencia(request.getNumeroLicencia())
            .fechaVencimientoLicencia(request.getFechaVencimientoLicencia())
            .zonaAsignada(request.getZonaAsignada())
            .diasLaborales(request.getDiasLaborales())
            .horaInicioTurno(request.getHoraInicioTurno())
            .horaFinTurno(request.getHoraFinTurno())
            .activo(true)
            .build();
  }

  public RepartidorResponse toResponse(Repartidor repartidor) {
    if (repartidor == null) {
      return null;
    }

    RepartidorResponse.RepartidorResponseBuilder builder = RepartidorResponse.builder()
            .id(repartidor.getId())
            .cedula(repartidor.getCedula())
            .nombre(repartidor.getNombre())
            .apellido(repartidor.getApellido())
            .nombreCompleto(repartidor.getNombreCompleto())
            .email(repartidor.getEmail())
            .telefono(repartidor.getTelefono())
            .direccion(repartidor.getDireccion())
            .tipoLicencia(repartidor.getTipoLicencia())
            .numeroLicencia(repartidor.getNumeroLicencia())
            .licenciaVigente(repartidor.licenciaVigente())
            .estado(repartidor.getEstado())
            .zonaAsignada(repartidor.getZonaAsignada())
            .ubicacionActual(toCoordenadaDTO(repartidor.getUbicacionActual()))
            .vehiculoAsignado(vehiculoMapper.toResponse(repartidor.getVehiculoAsignado()))
            .entregasCompletadas(repartidor.getEntregasCompletadas())
            .entregasFallidas(repartidor.getEntregasFallidas())
            .calificacionPromedio(repartidor.getCalificacionPromedio())
            .tasaExito(repartidor.getTasaExito())
            .kilometrosRecorridos(repartidor.getKilometrosRecorridos())
            .diasLaborales(repartidor.getDiasLaborales())
            .horaInicioTurno(repartidor.getHoraInicioTurno())
            .horaFinTurno(repartidor.getHoraFinTurno())
            .activo(repartidor.getActivo())
            .disponible(repartidor.estaDisponible());

    if (repartidor.getFechaNacimiento() != null) {
      builder.fechaNacimiento(repartidor.getFechaNacimiento().format(DATE_FORMATTER));
    }
    if (repartidor.getFechaContratacion() != null) {
      builder.fechaContratacion(repartidor.getFechaContratacion().format(DATE_FORMATTER));
    }
    if (repartidor.getFechaVencimientoLicencia() != null) {
      builder.fechaVencimientoLicencia(repartidor.getFechaVencimientoLicencia().format(DATE_FORMATTER));
    }

    return builder.build();
  }

  private CoordenadaDTO toCoordenadaDTO(Coordenada coordenada) {
    return vehiculoMapper.toCoordenadaDTO(coordenada);
  }
}