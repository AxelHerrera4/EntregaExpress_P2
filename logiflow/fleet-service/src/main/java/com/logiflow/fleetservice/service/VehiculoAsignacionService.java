package com.logiflow.fleetservice.service;

import com.logiflow.fleetservice.dto.response.VehiculoResponse;
import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import com.logiflow.fleetservice.model.entity.vehiculo.Camion;
import com.logiflow.fleetservice.model.entity.vehiculo.Motorizado;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoEntrega;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoLiviano;
import com.logiflow.fleetservice.repository.VehiculoRepository;
import com.logiflow.fleetservice.dto.mapper.VehiculoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para asignación inteligente de vehículos según peso de la carga
 * Según documentación:
 * - Motorizado: hasta 30 kg
 * - VehículoLiviano: 31-1000 kg (500-1000kg según carrocería)
 * - Camión: más de 1000 kg
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VehiculoAsignacionService {

  private final VehiculoRepository vehiculoRepository;
  private final VehiculoMapper vehiculoMapper;

  // Constantes de capacidad según documentación
  private static final Double CAPACIDAD_MAX_MOTORIZADO = 30.0; // kg
  private static final Double CAPACIDAD_MAX_VEHICULO_LIVIANO = 1000.0; // kg

  /**
   * Determina el tipo de vehículo adecuado según el peso
   */
  public TipoVehiculo determinarTipoVehiculoPorPeso(Double peso) {
    if (peso == null || peso <= 0) {
      throw new IllegalArgumentException("El peso debe ser mayor a 0");
    }

    if (peso <= CAPACIDAD_MAX_MOTORIZADO) {
      log.info("Peso {} kg - Tipo recomendado: MOTORIZADO", peso);
      return TipoVehiculo.MOTORIZADO;
    } else if (peso <= CAPACIDAD_MAX_VEHICULO_LIVIANO) {
      log.info("Peso {} kg - Tipo recomendado: VEHICULO_LIVIANO", peso);
      return TipoVehiculo.VEHICULO_LIVIANO;
    } else {
      log.info("Peso {} kg - Tipo recomendado: CAMION", peso);
      return TipoVehiculo.CAMION;
    }
  }

  /**
   * Obtiene vehículos disponibles para un peso específico
   */
  public List<VehiculoResponse> obtenerVehiculosDisponiblesPorPeso(Double peso) {
    TipoVehiculo tipoRecomendado = determinarTipoVehiculoPorPeso(peso);
    
    List<VehiculoEntrega> vehiculosDisponibles = vehiculoRepository.findVehiculosDisponibles()
            .stream()
            .filter(v -> v.getTipo() == tipoRecomendado)
            .filter(v -> v.getCapacidadCarga() >= peso)
            .collect(Collectors.toList());

    log.info("Encontrados {} vehículos {} disponibles para peso {} kg", 
             vehiculosDisponibles.size(), tipoRecomendado, peso);

    return vehiculosDisponibles.stream()
            .map(vehiculoMapper::toResponse)
            .collect(Collectors.toList());
  }

  /**
   * Obtiene vehículos por tipo específico
   */
  public List<VehiculoResponse> obtenerVehiculosPorTipo(TipoVehiculo tipo) {
    Class<? extends VehiculoEntrega> tipoClase = switch (tipo) {
      case MOTORIZADO -> Motorizado.class;
      case VEHICULO_LIVIANO -> VehiculoLiviano.class;
      case CAMION -> Camion.class;
    };

    List<VehiculoEntrega> vehiculos = vehiculoRepository.findVehiculosActivosPorTipo(tipoClase);
    
    return vehiculos.stream()
            .map(vehiculoMapper::toResponse)
            .collect(Collectors.toList());
  }

  /**
   * Valida si un vehículo es apto para transportar un peso
   */
  public boolean esAptoParaPeso(VehiculoEntrega vehiculo, Double peso) {
    if (vehiculo == null || peso == null || peso <= 0) {
      return false;
    }

    return vehiculo.getCapacidadMaxima() >= peso && 
           vehiculo.estaDisponible();
  }
}
