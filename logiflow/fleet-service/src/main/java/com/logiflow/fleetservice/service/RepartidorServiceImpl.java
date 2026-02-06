package com.logiflow.fleetservice.service;


import com.logiflow.fleetservice.dto.mapper.RepartidorMapper;
import com.logiflow.fleetservice.dto.request.RepartidorCreateRequest;
import com.logiflow.fleetservice.dto.request.RepartidorUpdateRequest;
import com.logiflow.fleetservice.dto.response.RepartidorResponse;
import com.logiflow.fleetservice.event.RepartidorUbicacionActualizadaEvent;
import com.logiflow.fleetservice.exception.BusinessException;
import com.logiflow.fleetservice.exception.DuplicateResourceException;
import com.logiflow.fleetservice.exception.ResourceNotFoundException;
import com.logiflow.fleetservice.model.entity.enums.EstadoRepartidor;
import com.logiflow.fleetservice.model.entity.repartidor.Repartidor;
import com.logiflow.fleetservice.model.entity.vehiculo.Coordenada;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoEntrega;
import com.logiflow.fleetservice.repository.RepartidorRepository;
import com.logiflow.fleetservice.repository.VehiculoRepository;
import com.logiflow.fleetservice.service.messaging.FleetEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RepartidorServiceImpl {

  private final RepartidorRepository repartidorRepository;
  private final VehiculoRepository vehiculoRepository;
  private final RepartidorMapper repartidorMapper;
  private final FleetEventPublisher eventPublisher;

  @Transactional
  public RepartidorResponse crearRepartidor(RepartidorCreateRequest request) {
    log.info("Creando repartidor con documento: {}", request.getDocumento());

    if (repartidorRepository.existsByDocumento(request.getDocumento())) {
      throw new DuplicateResourceException("Ya existe un repartidor con documento: " + request.getDocumento());
    }

    if (request.getEmail() != null && repartidorRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Ya existe un repartidor con email: " + request.getEmail());
    }

    Repartidor repartidor = repartidorMapper.toEntity(request);

    if (request.getVehiculoId() != null) {
      VehiculoEntrega vehiculo = vehiculoRepository.findById(request.getVehiculoId())
              .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
      
      if (vehiculo.getEstado() != com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.ACTIVO) {
        throw new BusinessException("No se puede asignar un vehículo que no está activo");
      }
      
      repartidor.asignarVehiculo(vehiculo);
    }

    Repartidor saved = repartidorRepository.save(repartidor);
    log.info("Repartidor creado con ID: {}", saved.getId());

    return repartidorMapper.toResponse(saved);
  }

  public RepartidorResponse obtenerRepartidorPorId(UUID id) {
    Repartidor repartidor = buscarRepartidorPorId(id);
    return repartidorMapper.toResponse(repartidor);
  }

  public List<RepartidorResponse> obtenerTodosLosRepartidores() {
    return repartidorRepository.findAll()
            .stream()
            .map(repartidorMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Transactional
  public RepartidorResponse actualizarRepartidor(UUID id, RepartidorUpdateRequest request) {
    log.info("Actualizando repartidor ID: {}", id);

    Repartidor repartidor = buscarRepartidorPorId(id);

    if (request.getEmail() != null && !request.getEmail().equals(repartidor.getEmail())) {
      if (repartidorRepository.existsByEmail(request.getEmail())) {
        throw new DuplicateResourceException("Email ya registrado");
      }
      repartidor.setEmail(request.getEmail());
    }

    if (request.getTelefono() != null) repartidor.setTelefono(request.getTelefono());
    if (request.getZonaAsignada() != null) repartidor.setZonaAsignada(request.getZonaAsignada());
    if (request.getActivo() != null) repartidor.setActivo(request.getActivo());

    if (request.getVehiculoId() != null) {
      VehiculoEntrega vehiculo = vehiculoRepository.findById(request.getVehiculoId())
              .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
      
      if (vehiculo.getEstado() != com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.ACTIVO) {
        throw new BusinessException("No se puede asignar un vehículo que no está activo");
      }
      
      repartidor.asignarVehiculo(vehiculo);
    }

    Repartidor updated = repartidorRepository.save(repartidor);
    return repartidorMapper.toResponse(updated);
  }

  @Transactional
  public RepartidorResponse cambiarEstadoRepartidor(UUID id, EstadoRepartidor nuevoEstado) {
    log.info("Cambiando estado del repartidor {} a: {}", id, nuevoEstado);

    Repartidor repartidor = buscarRepartidorPorId(id);
    repartidor.cambiarEstado(nuevoEstado);

    Repartidor updated = repartidorRepository.save(repartidor);
    return repartidorMapper.toResponse(updated);
  }

  @Transactional
  public void eliminarRepartidor(UUID id) {
    log.info("Eliminando repartidor ID: {}", id);

    Repartidor repartidor = buscarRepartidorPorId(id);

    if (repartidor.getEstado() == EstadoRepartidor.EN_RUTA) {
      throw new BusinessException("No se puede eliminar un repartidor que está en ruta");
    }

    repartidor.setActivo(false);
    repartidor.cambiarEstado(EstadoRepartidor.MANTENIMIENTO);
    repartidorRepository.save(repartidor);
  }

  @Transactional
  public void asignarVehiculo(UUID repartidorId, UUID vehiculoId) {
    log.info("Asignando vehículo {} al repartidor {}", vehiculoId, repartidorId);

    Repartidor repartidor = buscarRepartidorPorId(repartidorId);
    VehiculoEntrega vehiculo = vehiculoRepository.findById(vehiculoId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));

    if (vehiculo.getEstado() != com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.ACTIVO) {
      throw new BusinessException("No se puede asignar un vehículo que no está activo");
    }

    repartidor.asignarVehiculo(vehiculo);
    repartidorRepository.save(repartidor);

    log.info("Vehículo asignado exitosamente");
  }

  @Transactional
  public void removerVehiculo(UUID repartidorId) {
    log.info("Removiendo vehículo del repartidor {}", repartidorId);

    Repartidor repartidor = buscarRepartidorPorId(repartidorId);

    if (repartidor.getEstado() == EstadoRepartidor.EN_RUTA) {
      throw new BusinessException("No se puede remover el vehículo de un repartidor en ruta");
    }

    repartidor.setVehiculoAsignado(null);
    repartidorRepository.save(repartidor);
  }

  @Transactional
  public void actualizarCoordenadas(UUID repartidorId, Double latitud, Double longitud) {
    log.info("Actualizando coordenadas del repartidor {} - lat: {}, lon: {}", repartidorId, latitud, longitud);

    Repartidor repartidor = buscarRepartidorPorId(repartidorId);

    // Actualizar coordenadas
    Coordenada nuevaUbicacion = new Coordenada(latitud, longitud);
    repartidor.setUbicacionActual(nuevaUbicacion);

    Repartidor updated = repartidorRepository.save(repartidor);
    
    // Publicar evento de actualización de ubicación
    RepartidorUbicacionActualizadaEvent event = RepartidorUbicacionActualizadaEvent.builder()
        .repartidorId(updated.getId().toString())
        .nombreCompleto(updated.getNombre() + " " + updated.getApellido())
        .latitud(latitud)
        .longitud(longitud)
        .zona(updated.getZonaAsignada())
        .estado(updated.getEstado().name())
        .fechaActualizacion(LocalDateTime.now())
        .build();
    
    eventPublisher.publishRepartidorUbicacionActualizada(event);
    
    log.info("Coordenadas actualizadas exitosamente para repartidor {}", repartidorId);
  }

  // ========== MÉTODOS PRIVADOS ==========

  private Repartidor buscarRepartidorPorId(UUID id) {
    return repartidorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Repartidor no encontrado con ID: " + id));
  }
}