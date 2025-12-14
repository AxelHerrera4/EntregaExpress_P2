package com.logiflow.fleetservice.dto.mapper;


import com.logiflow.fleetservice.dto.CoordenadaDTO;
import com.logiflow.fleetservice.dto.response.VehiculoResponse;
import com.logiflow.fleetservice.model.entity.vehiculo.Camion;
import com.logiflow.fleetservice.model.entity.vehiculo.Coordenada;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoEntrega;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class VehiculoMapper {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public VehiculoResponse toResponse(VehiculoEntrega vehiculo) {
    if (vehiculo == null) {
      return null;
    }

    VehiculoResponse.VehiculoResponseBuilder builder = VehiculoResponse.builder()
            .id(vehiculo.getId())
            .tipo(vehiculo.getTipo())
            .placa(vehiculo.getPlaca())
            .marca(vehiculo.getMarca())
            .modelo(vehiculo.getModelo())
            .anio(vehiculo.getAnio())
            .kilometraje(vehiculo.getKilometraje())
            .capacidadCargaKg(vehiculo.getCapacidadCargaKg())
            .consumoCombustibleKmPorLitro(vehiculo.getConsumoCombustibleKmPorLitro())
            .activo(vehiculo.getActivo())
            .ultimaUbicacion(toCoordenadaDTO(vehiculo.getUltimaUbicacion()));

    if (vehiculo.getUltimaActualizacionGPS() != null) {
      builder.ultimaActualizacionGPS(vehiculo.getUltimaActualizacionGPS().format(FORMATTER));
    }

    // Campos específicos de Camion
    if (vehiculo instanceof Camion camion) {
      builder.numeroEjes(camion.getNumeroEjes())
              .requiereRampa(camion.getRequiereRampa());
    }

    return builder.build();
  }

  public CoordenadaDTO toCoordenadaDTO(Coordenada coordenada) {
    if (coordenada == null) {
      return null;
    }
    return CoordenadaDTO.builder()
            .latitud(coordenada.getLatitud())
            .longitud(coordenada.getLongitud())
            .build();
  }
}