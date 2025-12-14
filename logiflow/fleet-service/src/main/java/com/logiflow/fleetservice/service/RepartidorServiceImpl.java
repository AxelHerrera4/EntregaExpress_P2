package com.logiflow.fleetservice.service;


import com.logiflow.fleetservice.dto.mapper.RepartidorMapper;
import com.logiflow.fleetservice.dto.request.RepartidorCreateRequest;
import com.logiflow.fleetservice.dto.request.RepartidorUpdateRequest;
import com.logiflow.fleetservice.dto.response.RepartidorResponse;
import com.logiflow.fleetservice.exception.BusinessException;
import com.logiflow.fleetservice.exception.DuplicateResourceException;
import com.logiflow.fleetservice.exception.ResourceNotFoundException;
import com.logiflow.fleetservice.model.entity.enums.EstadoRepartidor;
import com.logiflow.fleetservice.model.entity.repartidor.Repartidor;
import com.logiflow.fleetservice.model.entity.vehiculo.Coordenada;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoEntrega;
import com.logiflow.fleetservice.repository.RepartidorRepository;
import com.logiflow.fleetservice.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RepartidorServiceImpl {

  private final RepartidorRepository repartidorRepository;
  private final VehiculoRepository vehiculoRepository;
  private final RepartidorMapper repartidorMapper;

  @Transactional
  public RepartidorResponse crearRepartidor(RepartidorCreateRequest request) {
    log.info("Creando repartidor con cédula: {}", request.getCedula());

    if (repartidorRepository.existsByCedula(request.getCedula())) {
      throw new DuplicateResourceException("Ya existe un repartidor con cédula: " + request.getCedula());
    }

    if (request.getEmail() != null && repartidorRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Ya existe un repartidor con email: " + request.getEmail());
    }

    Repartidor repartidor = repartidorMapper.toEntity(request);

    if (request.getVehiculoId() != null) {
      VehiculoEntrega vehiculo = vehiculoRepository.findById(request.getVehiculoId())
              .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
      repartidor.asignarVehiculo(vehiculo);
    }

    Repartidor saved = repartidorRepository.save(repartidor);
    log.info("Repartidor creado con ID: {}", saved.getId());

    return repartidorMapper.toResponse(saved);
  }

  public RepartidorResponse obtenerRepartidorPorId(Long id) {
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
  public RepartidorResponse actualizarRepartidor(Long id, RepartidorUpdateRequest request) {
    log.info("Actualizando repartidor ID: {}", id);

    Repartidor repartidor = buscarRepartidorPorId(id);

    if (request.getEmail() != null && !request.getEmail().equals(repartidor.getEmail())) {
      if (repartidorRepository.existsByEmail(request.getEmail())) {
        throw new DuplicateResourceException("Email ya registrado");
      }
      repartidor.setEmail(request.getEmail());
    }

    if (request.getTelefono() != null) repartidor.setTelefono(request.getTelefono());
    if (request.getDireccion() != null) repartidor.setDireccion(request.getDireccion());
    if (request.getZonaAsignada() != null) repartidor.setZonaAsignada(request.getZonaAsignada());
    if (request.getActivo() != null) repartidor.setActivo(request.getActivo());
    if (request.getObservaciones() != null) repartidor.setObservaciones(request.getObservaciones());

    if (request.getEstado() != null) {
      repartidor.cambiarEstado(request.getEstado());
    }

    if (request.getVehiculoId() != null) {
      VehiculoEntrega vehiculo = vehiculoRepository.findById(request.getVehiculoId())
              .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
      repartidor.asignarVehiculo(vehiculo);
    }

    if (request.getLatitud() != null && request.getLongitud() != null) {
      Coordenada coordenada = new Coordenada(request.getLatitud(), request.getLongitud());
      repartidor.actualizarUbicacion(coordenada);
    }

    if (request.getDiasLaborales() != null) repartidor.setDiasLaborales(request.getDiasLaborales());
    if (request.getHoraInicioTurno() != null) repartidor.setHoraInicioTurno(request.getHoraInicioTurno());
    if (request.getHoraFinTurno() != null) repartidor.setHoraFinTurno(request.getHoraFinTurno());

    Repartidor updated = repartidorRepository.save(repartidor);
    return repartidorMapper.toResponse(updated);
  }

  @Transactional
  public RepartidorResponse cambiarEstadoRepartidor(Long id, EstadoRepartidor nuevoEstado) {
    log.info("Cambiando estado del repartidor {} a: {}", id, nuevoEstado);

    Repartidor repartidor = buscarRepartidorPorId(id);
    repartidor.cambiarEstado(nuevoEstado);

    Repartidor updated = repartidorRepository.save(repartidor);
    return repartidorMapper.toResponse(updated);
  }

  @Transactional
  public void eliminarRepartidor(Long id) {
    log.info("Eliminando repartidor ID: {}", id);

    Repartidor repartidor = buscarRepartidorPorId(id);

    if (repartidor.getEstado() == EstadoRepartidor.EN_RUTA) {
      throw new BusinessException("No se puede eliminar un repartidor que está en ruta");
    }

    repartidor.setActivo(false);
    repartidor.cambiarEstado(EstadoRepartidor.INACTIVO);
    repartidorRepository.save(repartidor);
  }

  @Transactional
  public void asignarVehiculo(Long repartidorId, Long vehiculoId) {
    log.info("Asignando vehículo {} al repartidor {}", vehiculoId, repartidorId);

    Repartidor repartidor = buscarRepartidorPorId(repartidorId);
    VehiculoEntrega vehiculo = vehiculoRepository.findById(vehiculoId)
            .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));

    if (!vehiculo.getActivo()) {
      throw new BusinessException("No se puede asignar un vehículo inactivo");
    }

    repartidor.asignarVehiculo(vehiculo);
    repartidorRepository.save(repartidor);

    log.info("Vehículo asignado exitosamente");
  }

  @Transactional
  public void removerVehiculo(Long repartidorId) {
    log.info("Removiendo vehículo del repartidor {}", repartidorId);

    Repartidor repartidor = buscarRepartidorPorId(repartidorId);

    if (repartidor.getEstado() == EstadoRepartidor.EN_RUTA) {
      throw new BusinessException("No se puede remover el vehículo de un repartidor en ruta");
    }

    repartidor.setVehiculoAsignado(null);
    repartidorRepository.save(repartidor);
  }

  private Repartidor buscarRepartidorPorId(Long id) {
    return repartidorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Repartidor no encontrado con ID: " + id));
  }
}