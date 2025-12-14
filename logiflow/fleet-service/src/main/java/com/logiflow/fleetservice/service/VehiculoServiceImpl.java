package com.logiflow.fleetservice.service;


import com.logiflow.fleetservice.dto.request.VehiculoCreateRequest;
import com.logiflow.fleetservice.dto.response.VehiculoResponse;
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

/**
 * Servicio para gestión de vehículos de la flota
 * Implementa transacciones ACID según requisitos
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehiculoService {

  private final VehiculoRepository vehiculoRepository;
  private final VehiculoFactory vehiculoFactory;
  private final VehiculoMapper vehiculoMapper;

  /**
   * Crea un nuevo vehículo - Transacción ACID
   */
  @Transactional
  public VehiculoResponse crearVehiculo(VehiculoCreateRequest request) {
    log.info("Creando vehículo con placa: {}", request.getPlaca());

    // Validar que no exista la placa
    if (vehiculoRepository.existsByPlaca(request.getPlaca())) {
      throw new DuplicateResourceException("Ya existe un vehículo con la placa: " + request.getPlaca());
    }

    // Usar Factory Pattern para crear el vehículo
    VehiculoEntrega vehiculo = vehiculoFactory.crearVehiculoPersonalizado(
            request.getTipo(),
            request.getPlaca(),
            request.getMarca(),
            request.getModelo(),
            request.getAnio(),
            request.getCapacidadCargaKg(),
            request.getConsumoCombustibleKmPorLitro()
    );

    // Configurar campos específicos de Camion
    if (vehiculo instanceof Camion camion && request.getNumeroEjes() != null) {
      camion.setNumeroEjes(request.getNumeroEjes());
      camion.setRequiereRampa(request.getRequiereRampa() != null ? request.getRequiereRampa() : false);
    }

    VehiculoEntrega saved = vehiculoRepository.save(vehiculo);
    log.info("Vehículo creado exitosamente con ID: {}", saved.getId());

    return vehiculoMapper.toResponse(saved);
  }

  /**
   * Obtiene un vehículo por ID
   */
  public VehiculoResponse obtenerVehiculoPorId(Long id) {
    log.debug("Buscando vehículo con ID: {}", id);
    VehiculoEntrega vehiculo = buscarVehiculoPorId(id);
    return vehiculoMapper.toResponse(vehiculo);
  }

  /**
   * Obtiene todos los vehículos
   */
  public List<VehiculoResponse> obtenerTodosLosVehiculos() {
    log.debug("Obteniendo todos los vehículos");
    return vehiculoRepository.findAll()
            .stream()
            .map(vehiculoMapper::toResponse)
            .collect(Collectors.toList());
  }

  /**
   * Obtiene vehículos por tipo
   */
  public List<VehiculoResponse> obtenerVehiculosPorTipo(TipoVehiculo tipo) {
    log.debug("Obteniendo vehículos de tipo: {}", tipo);
    return vehiculoRepository.findByTipo(tipo)
            .stream()
            .map(vehiculoMapper::toResponse)
            .collect(Collectors.toList());
  }

  /**
   * Obtiene vehículos activos
   */
  public List<VehiculoResponse> obtenerVehiculosActivos() {
    log.debug("Obteniendo vehículos activos");
    return vehiculoRepository.findByActivoTrue()
            .stream()
            .map(vehiculoMapper::toResponse)
            .collect(Collectors.toList());
  }

  /**
   * Obtiene vehículos disponibles (sin asignar)
   */
  public List<VehiculoResponse> obtenerVehiculosDisponibles() {
    log.debug("Obteniendo vehículos disponibles");
    return vehiculoRepository.findVehiculosDisponibles()
            .stream()
            .map(vehiculoMapper::toResponse)
            .collect(Collectors.toList());
  }

  /**
   * Actualiza un vehículo - Transacción ACID
   */
  @Transactional
  public VehiculoResponse actualizarVehiculo(Long id, VehiculoUpdateRequest request) {
    log.info("Actualizando vehículo con ID: {}", id);

    VehiculoEntrega vehiculo = buscarVehiculoPorId(id);

    // Actualizar campos si están presentes
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

    // Actualizar ubicación si se proporciona
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

  /**
   * Actualiza el estado de un vehículo
   */
  @Transactional
  public VehiculoResponse actualizarEstadoVehiculo(Long id, Boolean activo) {
    log.info("Actualizando estado del vehículo {} a: {}", id, activo);

    VehiculoEntrega vehiculo = buscarVehiculoPorId(id);
    vehiculo.setActivo(activo);

    VehiculoEntrega updated = vehiculoRepository.save(vehiculo);
    return vehiculoMapper.toResponse(updated);
  }

  /**
   * Elimina lógicamente un vehículo - Transacción ACID
   */
  @Transactional
  public void eliminarVehiculo(Long id) {
    log.info("Eliminando vehículo con ID: {}", id);

    VehiculoEntrega vehiculo = buscarVehiculoPorId(id);
    vehiculo.setActivo(false);
    vehiculoRepository.save(vehiculo);

    log.info("Vehículo eliminado lógicamente");
  }

  /**
   * Registra la ubicación GPS de un vehículo
   */
  @Transactional
  public void registrarUbicacionGPS(Long id, Double latitud, Double longitud) {
    log.debug("Registrando ubicación GPS del vehículo: {}", id);

    VehiculoEntrega vehiculo = buscarVehiculoPorId(id);
    Coordenada coordenada = new Coordenada(latitud, longitud);

    if (!coordenada.esValida()) {
      throw new IllegalArgumentException("Coordenadas GPS inválidas");
    }

    vehiculo.registrarUbicacion(coordenada, LocalDateTime.now());
    vehiculoRepository.save(vehiculo);
  }

  /**
   * Obtiene estadísticas de la flota
   */
  public FlotaEstadisticas obtenerEstadisticasFlota() {
    long totalVehiculos = vehiculoRepository.count();
    long vehiculosActivos = vehiculoRepository.countVehiculosActivos();
    long vehiculosDisponibles = vehiculoRepository.findVehiculosDisponibles().size();

    return FlotaEstadisticas.builder()
            .totalVehiculos(totalVehiculos)
            .vehiculosActivos(vehiculosActivos)
            .vehiculosDisponibles(vehiculosDisponibles)
            .build();
  }

  // ========== MÉTODOS PRIVADOS ==========

  private VehiculoEntrega buscarVehiculoPorId(Long id) {
    return vehiculoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));
  }
}
