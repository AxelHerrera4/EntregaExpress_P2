package com.logiflow.fleetservice.service;


import com.logiflow.fleetservice.dto.mapper.VehiculoMapper;
import com.logiflow.fleetservice.dto.request.VehiculoCreateRequest;
import com.logiflow.fleetservice.dto.request.VehiculoUpdateRequest;
import com.logiflow.fleetservice.dto.response.VehiculoResponse;
import com.logiflow.fleetservice.event.VehiculoEstadoActualizadoEvent;
import com.logiflow.fleetservice.exception.DuplicateResourceException;
import com.logiflow.fleetservice.exception.ResourceNotFoundException;
import com.logiflow.fleetservice.factory.VehiculoFactory;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoEntrega;
import com.logiflow.fleetservice.repository.VehiculoRepository;
import com.logiflow.fleetservice.service.messaging.FleetEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehiculoServiceImpl {

  private final VehiculoRepository vehiculoRepository;
  private final VehiculoFactory vehiculoFactory;
  private final VehiculoMapper vehiculoMapper;
  private final FleetEventPublisher eventPublisher;

  @Transactional
  public VehiculoResponse crearVehiculo(VehiculoCreateRequest request) {
    log.info("Creando vehículo con placa: {}", request.getPlaca());

    if (vehiculoRepository.existsByPlaca(request.getPlaca())) {
      throw new DuplicateResourceException("Ya existe un vehículo con la placa: " + request.getPlaca());
    }

    VehiculoEntrega vehiculo = vehiculoFactory.crearVehiculo(request);

    VehiculoEntrega saved = vehiculoRepository.save(vehiculo);
    log.info("Vehículo creado exitosamente con ID: {}", saved.getId());

    return vehiculoMapper.toResponse(saved);
  }

  public VehiculoResponse obtenerVehiculoPorId(UUID id) {
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
  public VehiculoResponse actualizarVehiculo(UUID id, VehiculoUpdateRequest request) {
    log.info("Actualizando vehículo con ID: {}", id);

    VehiculoEntrega vehiculo = buscarVehiculoPorId(id);

    if (request.getEstado() != null) {
      vehiculo.setEstado(request.getEstado());
    }
    if (request.getCapacidadCarga() != null) {
      vehiculo.setCapacidadCarga(request.getCapacidadCarga());
    }
    if (request.getActivo() != null) {
      vehiculo.setActivo(request.getActivo());
    }

    VehiculoEntrega updated = vehiculoRepository.save(vehiculo);
    log.info("Vehículo actualizado exitosamente");

    return vehiculoMapper.toResponse(updated);
  }

  @Transactional
  public VehiculoResponse actualizarEstadoVehiculo(UUID id, com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo estado) {
    log.info("Actualizando estado del vehículo {} a: {}", id, estado);

    VehiculoEntrega vehiculo = buscarVehiculoPorId(id);
    com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo estadoAnterior = vehiculo.getEstado();
    vehiculo.setEstado(estado);

    VehiculoEntrega updated = vehiculoRepository.save(vehiculo);
    
    // Publicar evento de cambio de estado
    VehiculoEstadoActualizadoEvent event = VehiculoEstadoActualizadoEvent.builder()
        .vehiculoId(updated.getId().toString())
        .placa(updated.getPlaca())
        .tipoVehiculo(updated.getClass().getSimpleName())
        .estadoAnterior(estadoAnterior.name())
        .estadoNuevo(estado.name())
        .disponible(estado == com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.ACTIVO)
        .fechaActualizacion(LocalDateTime.now())
        .build();
    
    eventPublisher.publishVehiculoEstadoActualizado(event);
    
    return vehiculoMapper.toResponse(updated);
  }

  @Transactional
  public void eliminarVehiculo(UUID id) {
    log.info("Eliminando vehículo con ID: {}", id);

    VehiculoEntrega vehiculo = buscarVehiculoPorId(id);
    vehiculo.setActivo(false);
    vehiculo.setEstado(com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.FUERA_DE_SERVICIO);
    vehiculoRepository.save(vehiculo);

    log.info("Vehículo eliminado lógicamente");
  }

  @Transactional
  public void registrarUbicacionGPS(UUID id, Double latitud, Double longitud) {
    throw new UnsupportedOperationException("Registro de ubicación GPS fuera de alcance para Fase 1");
  }

  private VehiculoEntrega buscarVehiculoPorId(UUID id) {
    return vehiculoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));
  }
}
