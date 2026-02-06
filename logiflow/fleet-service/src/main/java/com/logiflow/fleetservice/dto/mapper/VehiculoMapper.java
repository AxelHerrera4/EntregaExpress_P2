package com.logiflow.fleetservice.dto.mapper;

import com.logiflow.fleetservice.dto.response.VehiculoResponse;
import com.logiflow.fleetservice.model.entity.vehiculo.Camion;
import com.logiflow.fleetservice.model.entity.vehiculo.Motorizado;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoEntrega;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoLiviano;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Mapper de Vehículo según documentación Fleet Service
 */
@Component
public class VehiculoMapper {

  private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  public VehiculoResponse toResponse(VehiculoEntrega vehiculo) {
    if (vehiculo == null) {
      return null;
    }

    VehiculoResponse.VehiculoResponseBuilder builder = VehiculoResponse.builder()
            .id(vehiculo.getId() != null ? vehiculo.getId().toString() : null)
            .placa(vehiculo.getPlaca())
            .tipo(vehiculo.getTipo())
            .marca(vehiculo.getMarca())
            .modelo(vehiculo.getModelo())
            .anio(vehiculo.getAnio())
            .capacidadCarga(vehiculo.getCapacidadCarga())
            .estado(vehiculo.getEstado())
            .activo(vehiculo.getActivo());

    // Características específicas según tipo
    VehiculoResponse.CaracteristicasEspecificas.CaracteristicasEspecificasBuilder caracBuilder = 
            VehiculoResponse.CaracteristicasEspecificas.builder();

    if (vehiculo instanceof Motorizado motorizado) {
      caracBuilder.cilindraje(motorizado.getCilindraje())
              .tieneCajones(motorizado.isTieneCajones());
    } else if (vehiculo instanceof VehiculoLiviano liviano) {
      caracBuilder.numeroPuertas(liviano.getNumeroPuertas())
              .tipoCarroceria(liviano.getTipoCarroceria() != null ? 
                      liviano.getTipoCarroceria().name() : null);
    } else if (vehiculo instanceof Camion camion) {
      caracBuilder.numeroEjes(camion.getNumeroEjes())
              .capacidadVolumen(camion.getCapacidadVolumen());
    }

    builder.caracteristicasEspecificas(caracBuilder.build());

    // Fechas
    if (vehiculo.getCreatedAt() != null) {
      builder.createdAt(vehiculo.getCreatedAt().format(DATETIME_FORMATTER));
    }
    if (vehiculo.getUpdatedAt() != null) {
      builder.updatedAt(vehiculo.getUpdatedAt().format(DATETIME_FORMATTER));
    }

    return builder.build();
  }
}
