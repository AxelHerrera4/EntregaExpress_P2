package com.logiflow.fleetservice.service;


import com.logiflow.fleetservice.dto.mapper.VehiculoMapper;
import com.logiflow.fleetservice.dto.request.VehiculoCreateRequest;
import com.logiflow.fleetservice.dto.request.VehiculoUpdateRequest;
import com.logiflow.fleetservice.dto.response.VehiculoResponse;
import com.logiflow.fleetservice.exception.DuplicateResourceException;
import com.logiflow.fleetservice.exception.ResourceNotFoundException;
import com.logiflow.fleetservice.factory.VehiculoFactory;
import com.logiflow.fleetservice.model.entity.vehiculo.Camion;
import com.logiflow.fleetservice.model.entity.vehiculo.Coordenada;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoEntrega;
import com.logiflow.fleetservice.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehiculoServiceImpl {

  private final VehiculoRepository vehiculoRepository;
  private final VehiculoFactory vehiculoFactory;
  private final VehiculoMapper vehiculoMapper;

  @Transactional
  public VehiculoResponse crearVehiculo(VehiculoCreateRequest request) {
    log.info("Creando vehículo con placa: {}", request.getPlaca());

    if (vehiculoRepository.existsByPlaca(request.getPlaca())) {
      throw new DuplicateResourceException("Ya existe un vehículo con la placa: " + request.getPlaca());
    }

    VehiculoEntrega vehiculo = vehiculoFactory.crearVehiculoPersonalizado(
            request.getTipo(),
            request.getPlaca(),
            request.getMarca(),
            request.getModelo(),
            request.getAnio(),
            request.getCapacidadCargaKg(),
            request.getConsumoCombustibleKmPorLitro()
    );

    if (vehiculo instanceof Camion camion && request.getNumeroEjes() != null) {
      camion.setNumeroEjes(request.getNumeroEjes());
      camion.setRequiereRampa(request.getRequiereRampa() != null ? request.getRequiereRampa() : false);
    }

    VehiculoEntrega saved = vehiculoRepository.save(vehiculo);
    log.info("Vehículo creado exitosamente con ID: {}", saved.getId());

    return vehiculoMapper.toResponse(saved);
  }

  public VehiculoResponse obtenerVehiculoPorId(Long id) {
    log.debug("Buscando vehículo con ID: {}", id);
    VehiculoEntrega vehiculo = buscarVehiculoPorId(id);
    return vehiculoMapper.toResponse(vehiculo);
  }

  public List<VehiculoResponse> obtenerTodosLosVehiculos() {
    log.debug("Obteniendo todos los vehículos");
    return vehiculoRepository.findAll()
            .stream()
            .map(vehiculoMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Transactional
  public VehiculoResponse actualizarVehiculo(Long id, VehiculoUpdateRequest request) {
    log.info("Actualizando vehículo con ID: {}", id);

    VehiculoEntrega vehiculo = buscarVehiculoPorId(id);

    if (request.getMarca() != null) {
      vehiculo.setMarca(request.getMarca());
    }
    if (request.getModelo() != null) {
      vehiculo.setModelo(request.getModelo());
    }
    if (request.getKilometraje() != null) {
      vehiculo.setKilometraje(request.getKilometraje());
    }
    if (request.getCapacidadCargaKg() != null) {
      vehiculo.setCapacidadCargaKg(request.getCapacidadCargaKg());
    }
    if (request.getConsumoCombustibleKmPorLitro() != null) {
      vehiculo.setConsumoCombustibleKmPorLitro(request.getConsumoCombustibleKmPorLitro());
    }
    if (request.getActivo() != null) {
      vehiculo.setActivo(request.getActivo());
    }

    if (request.getLatitud() != null && request.getLongitud() != null) {
      Coordenada nuevaUbicacion = new Coordenada(request.getLatitud(), request.getLongitud());
      if (nuevaUbicacion.esValida()) {
        vehiculo.registrarUbicacion(nuevaUbicacion, LocalDateTime.now());
      }
    }

    VehiculoEntrega updated = vehiculoRepository.save(vehiculo);
    log.info("Vehículo actualizado exitosamente");

    return vehiculoMapper.toResponse(updated);
  }

  @Transactional
  public VehiculoResponse actualizarEstadoVehiculo(Long id, Boolean activo) {
    log.info("Actualizando estado del vehículo {} a: {}", id, activo);

    VehiculoEntrega vehiculo = buscarVehiculoPorId(id);
    vehiculo.setActivo(activo);

    VehiculoEntrega updated = vehiculoRepository.save(vehiculo);
    return vehiculoMapper.toResponse(updated);
  }

  @Transactional
  public void eliminarVehiculo(Long id) {
    log.info("Eliminando vehículo con ID: {}", id);

    VehiculoEntrega vehiculo = buscarVehiculoPorId(id);
    vehiculo.setActivo(false);
    vehiculoRepository.save(vehiculo);

    log.info("Vehículo eliminado lógicamente");
  }

  @Transactional
  public void registrarUbicacionGPS(Long id, Double latitud, Double longitud) {
    throw new UnsupportedOperationException("Registro de ubicación GPS fuera de alcance para Fase 1");
  }

  private VehiculoEntrega buscarVehiculoPorId(Long id) {
    return vehiculoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));
  }
}
