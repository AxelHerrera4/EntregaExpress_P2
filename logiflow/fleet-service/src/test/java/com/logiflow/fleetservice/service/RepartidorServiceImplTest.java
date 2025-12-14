package com.logiflow.fleetservice.service;

import com.logiflow.fleetservice.dto.mapper.RepartidorMapper;
import com.logiflow.fleetservice.dto.request.RepartidorCreateRequest;
import com.logiflow.fleetservice.dto.response.RepartidorResponse;
import com.logiflow.fleetservice.exception.BusinessException;
import com.logiflow.fleetservice.exception.DuplicateResourceException;
import com.logiflow.fleetservice.model.entity.enums.EstadoRepartidor;
import com.logiflow.fleetservice.model.entity.enums.TipoLicencia;
import com.logiflow.fleetservice.model.entity.repartidor.Repartidor;
import com.logiflow.fleetservice.repository.RepartidorRepository;
import com.logiflow.fleetservice.repository.VehiculoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RepartidorServiceImpl Fase 1 Tests")
class RepartidorServiceImplTest {

  @Mock
  private RepartidorRepository repartidorRepository;

  @Mock
  private VehiculoRepository vehiculoRepository;

  @Mock
  private RepartidorMapper repartidorMapper;

  @InjectMocks
  private RepartidorServiceImpl repartidorService;

  @Test
  @DisplayName("crearRepartidor debe crear y retornar DTO cuando no hay duplicados")
  void crearRepartidor_DeberiaCrearRepartidorCuandoNoHayDuplicados() {
    RepartidorCreateRequest request = RepartidorCreateRequest.builder()
        .cedula("1234567890")
        .nombre("Juan")
        .apellido("Pérez")
        .email("juan@example.com")
        .fechaContratacion(LocalDate.now())
        .tipoLicencia(TipoLicencia.TIPO_B)
        .numeroLicencia("LIC-123")
        .build();

    Repartidor entity = Repartidor.builder()
        .id(1L)
        .cedula("1234567890")
        .nombre("Juan")
        .apellido("Pérez")
        .fechaContratacion(request.getFechaContratacion())
        .tipoLicencia(TipoLicencia.TIPO_B)
        .estado(EstadoRepartidor.DISPONIBLE)
        .build();

    RepartidorResponse response = RepartidorResponse.builder()
        .id(1L)
        .cedula("1234567890")
        .nombre("Juan")
        .apellido("Pérez")
        .build();

    when(repartidorRepository.existsByCedula("1234567890")).thenReturn(false);
    when(repartidorRepository.existsByEmail("juan@example.com")).thenReturn(false);
    when(repartidorMapper.toEntity(request)).thenReturn(entity);
    when(repartidorRepository.save(entity)).thenReturn(entity);
    when(repartidorMapper.toResponse(entity)).thenReturn(response);

    RepartidorResponse result = repartidorService.crearRepartidor(request);

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getCedula()).isEqualTo("1234567890");
    verify(repartidorRepository).existsByCedula("1234567890");
    verify(repartidorRepository).existsByEmail("juan@example.com");
    verify(repartidorRepository).save(entity);
  }

  @Test
  @DisplayName("crearRepartidor debe lanzar excepción si la cédula ya existe")
  void crearRepartidor_DeberiaLanzarExcepcionSiCedulaDuplicada() {
    RepartidorCreateRequest request = RepartidorCreateRequest.builder()
        .cedula("1234567890")
        .nombre("Juan")
        .apellido("Pérez")
        .build();

    when(repartidorRepository.existsByCedula("1234567890")).thenReturn(true);

    assertThatThrownBy(() -> repartidorService.crearRepartidor(request))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("cédula");

    verify(repartidorRepository, never()).save(any());
  }

  @Test
  @DisplayName("eliminarRepartidor debe lanzar BusinessException cuando está EN_RUTA")
  void eliminarRepartidor_DeberiaLanzarExcepcionSiEstaEnRuta() {
    Repartidor repartidor = Repartidor.builder()
        .id(5L)
        .cedula("1234567890")
        .nombre("Juan")
        .apellido("Pérez")
        .fechaContratacion(LocalDate.now())
        .tipoLicencia(TipoLicencia.TIPO_B)
        .estado(EstadoRepartidor.EN_RUTA)
        .build();

    when(repartidorRepository.findById(5L)).thenReturn(Optional.of(repartidor));

    assertThatThrownBy(() -> repartidorService.eliminarRepartidor(5L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("en ruta");

    verify(repartidorRepository, never()).save(any());
  }

  @Test
  @DisplayName("cambiarEstadoRepartidor debe actualizar el estado y guardar")
  void cambiarEstadoRepartidor_DeberiaActualizarEstado() {
    Repartidor repartidor = Repartidor.builder()
        .id(10L)
        .cedula("9999999999")
        .nombre("Ana")
        .apellido("López")
        .fechaContratacion(LocalDate.now())
        .tipoLicencia(TipoLicencia.TIPO_B)
        .estado(EstadoRepartidor.DISPONIBLE)
        .build();

    RepartidorResponse response = RepartidorResponse.builder()
        .id(10L)
        .estado(EstadoRepartidor.MANTENIMIENTO)
        .build();

    when(repartidorRepository.findById(10L)).thenReturn(Optional.of(repartidor));
    when(repartidorRepository.save(repartidor)).thenReturn(repartidor);
    when(repartidorMapper.toResponse(repartidor)).thenReturn(response);

    RepartidorResponse result = repartidorService.cambiarEstadoRepartidor(10L, EstadoRepartidor.MANTENIMIENTO);

    assertThat(result.getEstado()).isEqualTo(EstadoRepartidor.MANTENIMIENTO);
    verify(repartidorRepository).save(repartidor);
  }
}
