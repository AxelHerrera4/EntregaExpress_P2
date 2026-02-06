package com.logiflow.fleetservice.factory;

import com.logiflow.fleetservice.dto.request.VehiculoCreateRequest;
import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import com.logiflow.fleetservice.model.entity.vehiculo.Camion;
import com.logiflow.fleetservice.model.entity.vehiculo.Motorizado;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoEntrega;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoLiviano;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern para creación de vehículos
 * Según documentación Fleet Service
 */
@Component
public class VehiculoFactory {

  /**
   * Crea un vehículo desde un DTO de request
   */
  public VehiculoEntrega crearVehiculo(VehiculoCreateRequest dto) {
    switch (dto.getTipo()) {
      case MOTORIZADO:
        Motorizado motorizado = new Motorizado(
                dto.getPlaca(),
                dto.getMarca(),
                dto.getModelo(),
                dto.getCilindraje()
        );
        if (dto.getTieneCajones() != null) {
          motorizado.setTieneCajones(dto.getTieneCajones());
        }
        motorizado.setAnio(dto.getAnio());
        if (dto.getCapacidadCarga() != null) {
          motorizado.setCapacidadCarga(dto.getCapacidadCarga());
        }
        return motorizado;

      case VEHICULO_LIVIANO:
        VehiculoLiviano liviano = new VehiculoLiviano(
                dto.getPlaca(),
                dto.getMarca(),
                dto.getModelo(),
                dto.getTipoCarroceria()
        );
        if (dto.getNumeroPuertas() != null) {
          liviano.setNumeroPuertas(dto.getNumeroPuertas());
        }
        liviano.setAnio(dto.getAnio());
        if (dto.getCapacidadCarga() != null) {
          liviano.setCapacidadCarga(dto.getCapacidadCarga());
        }
        return liviano;

      case CAMION:
        Camion camion = new Camion(
                dto.getPlaca(),
                dto.getMarca(),
                dto.getModelo(),
                dto.getNumeroEjes(),
                dto.getCapacidadVolumen()
        );
        camion.setAnio(dto.getAnio());
        if (dto.getCapacidadCarga() != null) {
          camion.setCapacidadCarga(dto.getCapacidadCarga());
        }
        return camion;

      default:
        throw new IllegalArgumentException("Tipo de vehículo no válido: " + dto.getTipo());
    }
  }
}