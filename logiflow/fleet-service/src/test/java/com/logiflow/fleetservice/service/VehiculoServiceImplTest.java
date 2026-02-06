// TESTS TEMPORALMENTE DESHABILITADOS - Necesitan actualización
package com.logiflow.fleetservice.service;

import com.logiflow.fleetservice.dto.mapper.VehiculoMapper;
import com.logiflow.fleetservice.dto.request.VehiculoCreateRequest;
import com.logiflow.fleetservice.dto.request.VehiculoUpdateRequest;
import com.logiflow.fleetservice.dto.response.VehiculoResponse;
import com.logiflow.fleetservice.exception.DuplicateResourceException;
import com.logiflow.fleetservice.factory.VehiculoFactory;
import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import com.logiflow.fleetservice.model.entity.vehiculo.Motorizado;
import com.logiflow.fleetservice.model.entity.vehiculo.VehiculoEntrega;
import com.logiflow.fleetservice.repository.VehiculoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehiculoServiceImpl Fase 1 Tests")
class VehiculoServiceImplTest {

  @Mock
  private VehiculoRepository vehiculoRepository;

  @Mock
  private VehiculoFactory vehiculoFactory;

  @Mock
  private VehiculoMapper vehiculoMapper;

  @InjectMocks
  private VehiculoServiceImpl vehiculoService;

  @Test
  @DisplayName("crearVehiculo debe crear y retornar DTO cuando la placa es única")
  void crearVehiculo_DeberiaCrearVehiculoCuandoPlacaUnica() {
    UUID testId = UUID.randomUUID();
    VehiculoCreateRequest request = VehiculoCreateRequest.builder()
        .tipo(TipoVehiculo.MOTORIZADO)
        .placa("ABC-123")
        .marca("Yamaha")
        .modelo("FZ")
        .anio(2024)
        .build();

    VehiculoEntrega motorizado = new Motorizado();
    motorizado.setPlaca("ABC-123");

    VehiculoResponse response = VehiculoResponse.builder()
        .id(testId.toString())
        .placa("ABC-123")
        .build();

    when(vehiculoRepository.existsByPlaca("ABC-123")).thenReturn(false);
    when(vehiculoFactory.crearVehiculo(request)).thenReturn(motorizado);
    when(vehiculoRepository.save(motorizado)).thenReturn(motorizado);
    when(vehiculoMapper.toResponse(motorizado)).thenReturn(response);

    VehiculoResponse result = vehiculoService.crearVehiculo(request);

    assertThat(result.getId()).isNotNull();
    assertThat(result.getPlaca()).isEqualTo("ABC-123");
    verify(vehiculoRepository).existsByPlaca("ABC-123");
    verify(vehiculoRepository).save(motorizado);
  }

  @Test
  @DisplayName("crearVehiculo debe lanzar excepción si la placa ya existe")
  void crearVehiculo_DeberiaLanzarExcepcionSiPlacaDuplicada() {
    VehiculoCreateRequest request = VehiculoCreateRequest.builder()
        .tipo(TipoVehiculo.MOTORIZADO)
        .placa("ABC-123")
        .marca("Yamaha")
        .modelo("FZ")
        .anio(2024)
        .build();

    when(vehiculoRepository.existsByPlaca("ABC-123")).thenReturn(true);

    assertThatThrownBy(() -> vehiculoService.crearVehiculo(request))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("placa");

    verify(vehiculoRepository, never()).save(any());
  }

  @Test
  @DisplayName("actualizarVehiculo debe actualizar campos básicos")
  void actualizarVehiculo_DeberiaActualizarCamposBasicos() {
    UUID testId = UUID.randomUUID();
    VehiculoEntrega motorizado = new Motorizado();
    motorizado.setId(testId);
    motorizado.setPlaca("XYZ-999");

    VehiculoUpdateRequest request = VehiculoUpdateRequest.builder()
        .estado(com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.MANTENIMIENTO)
        .capacidadCarga(35.0)
        .build();

    VehiculoResponse response = VehiculoResponse.builder()
        .id(testId.toString())
        .estado(com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.MANTENIMIENTO)
        .build();

    when(vehiculoRepository.findById(testId)).thenReturn(Optional.of(motorizado));
    when(vehiculoRepository.save(motorizado)).thenReturn(motorizado);
    when(vehiculoMapper.toResponse(motorizado)).thenReturn(response);

    VehiculoResponse result = vehiculoService.actualizarVehiculo(testId, request);

    assertThat(motorizado.getEstado()).isEqualTo(com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.MANTENIMIENTO);
    assertThat(motorizado.getCapacidadCarga()).isEqualTo(35.0);
    verify(vehiculoRepository).save(motorizado);
  }

  @Test
  @DisplayName("actualizarEstadoVehiculo debe cambiar el flag activo")
  void actualizarEstadoVehiculo_DeberiaCambiarActivo() {
    UUID testId = UUID.randomUUID();
    VehiculoEntrega motorizado = new Motorizado();
    motorizado.setId(testId);
    motorizado.setEstado(com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.ACTIVO);

    VehiculoResponse response = VehiculoResponse.builder()
        .id(testId.toString())
        .estado(com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.FUERA_DE_SERVICIO)
        .build();

    when(vehiculoRepository.findById(testId)).thenReturn(Optional.of(motorizado));
    when(vehiculoRepository.save(motorizado)).thenReturn(motorizado);
    when(vehiculoMapper.toResponse(motorizado)).thenReturn(response);

    VehiculoResponse result = vehiculoService.actualizarEstadoVehiculo(testId, com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.FUERA_DE_SERVICIO);

    assertThat(motorizado.getEstado()).isEqualTo(com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo.FUERA_DE_SERVICIO);
    verify(vehiculoRepository).save(motorizado);
  }

  @Test
  @DisplayName("eliminarVehiculo debe marcar como inactivo y guardar")
  void eliminarVehiculo_DeberiaMarcarComoInactivo() {
    UUID testId = UUID.randomUUID();
    VehiculoEntrega motorizado = new Motorizado();
    motorizado.setId(testId);
    motorizado.setActivo(true);

    when(vehiculoRepository.findById(testId)).thenReturn(Optional.of(motorizado));

    vehiculoService.eliminarVehiculo(testId);

    assertThat(motorizado.getActivo()).isFalse();
    verify(vehiculoRepository).save(motorizado);
  }
}
