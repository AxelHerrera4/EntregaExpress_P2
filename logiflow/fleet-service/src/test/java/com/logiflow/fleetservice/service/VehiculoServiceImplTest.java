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
        .id(1L)
        .placa("ABC-123")
        .build();

    when(vehiculoRepository.existsByPlaca("ABC-123")).thenReturn(false);
    when(vehiculoFactory.crearVehiculoPersonalizado(
        request.getTipo(),
        request.getPlaca(),
        request.getMarca(),
        request.getModelo(),
        request.getAnio(),
        request.getCapacidadCargaKg(),
        request.getConsumoCombustibleKmPorLitro()
    )).thenReturn(motorizado);
    when(vehiculoRepository.save(motorizado)).thenReturn(motorizado);
    when(vehiculoMapper.toResponse(motorizado)).thenReturn(response);

    VehiculoResponse result = vehiculoService.crearVehiculo(request);

    assertThat(result.getId()).isEqualTo(1L);
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
    VehiculoEntrega motorizado = new Motorizado();
    motorizado.setId(10L);
    motorizado.setPlaca("XYZ-999");
    motorizado.setMarca("Honda");

    VehiculoUpdateRequest request = VehiculoUpdateRequest.builder()
        .marca("Toyota")
        .kilometraje(1000)
        .build();

    VehiculoResponse response = VehiculoResponse.builder()
        .id(10L)
        .marca("Toyota")
        .build();

    when(vehiculoRepository.findById(10L)).thenReturn(Optional.of(motorizado));
    when(vehiculoRepository.save(motorizado)).thenReturn(motorizado);
    when(vehiculoMapper.toResponse(motorizado)).thenReturn(response);

    VehiculoResponse result = vehiculoService.actualizarVehiculo(10L, request);

    assertThat(motorizado.getMarca()).isEqualTo("Toyota");
    assertThat(motorizado.getKilometraje()).isEqualTo(1000);
    assertThat(result.getMarca()).isEqualTo("Toyota");
    verify(vehiculoRepository).save(motorizado);
  }

  @Test
  @DisplayName("actualizarEstadoVehiculo debe cambiar el flag activo")
  void actualizarEstadoVehiculo_DeberiaCambiarActivo() {
    VehiculoEntrega motorizado = new Motorizado();
    motorizado.setId(20L);
    motorizado.setActivo(true);

    VehiculoResponse response = VehiculoResponse.builder()
        .id(20L)
        .activo(false)
        .build();

    when(vehiculoRepository.findById(20L)).thenReturn(Optional.of(motorizado));
    when(vehiculoRepository.save(motorizado)).thenReturn(motorizado);
    when(vehiculoMapper.toResponse(motorizado)).thenReturn(response);

    VehiculoResponse result = vehiculoService.actualizarEstadoVehiculo(20L, false);

    assertThat(motorizado.getActivo()).isFalse();
    assertThat(result.getActivo()).isFalse();
    verify(vehiculoRepository).save(motorizado);
  }

  @Test
  @DisplayName("eliminarVehiculo debe marcar como inactivo y guardar")
  void eliminarVehiculo_DeberiaMarcarComoInactivo() {
    VehiculoEntrega motorizado = new Motorizado();
    motorizado.setId(30L);
    motorizado.setActivo(true);

    when(vehiculoRepository.findById(30L)).thenReturn(Optional.of(motorizado));

    vehiculoService.eliminarVehiculo(30L);

    assertThat(motorizado.getActivo()).isFalse();
    verify(vehiculoRepository).save(motorizado);
  }
}
