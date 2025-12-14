package com.logiflow.fleetservice.controller;

import com.logiflow.fleetservice.dto.request.RepartidorCreateRequest;
import com.logiflow.fleetservice.dto.request.RepartidorUpdateRequest;
import com.logiflow.fleetservice.dto.response.RepartidorResponse;
import com.logiflow.fleetservice.model.entity.enums.EstadoRepartidor;
import com.logiflow.fleetservice.model.entity.enums.TipoLicencia;
import com.logiflow.fleetservice.service.RepartidorServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RepartidorController Fase 1 Tests")
class RepartidorControllerTest {

  @Mock
  private RepartidorServiceImpl repartidorService;

  @InjectMocks
  private RepartidorController repartidorController;

  @Test
  @DisplayName("crearRepartidor debe retornar 201 y el body del servicio")
  void crearRepartidor_DeberiaRetornar201() {
    RepartidorCreateRequest request = RepartidorCreateRequest.builder()
        .cedula("1234567890")
        .nombre("Juan")
        .apellido("Pérez")
        .fechaContratacion(LocalDate.now())
        .tipoLicencia(TipoLicencia.TIPO_B)
        .numeroLicencia("LIC-123")
        .build();

    RepartidorResponse response = RepartidorResponse.builder()
        .id(1L)
        .cedula("1234567890")
        .nombre("Juan")
        .apellido("Pérez")
        .build();

    when(repartidorService.crearRepartidor(request)).thenReturn(response);

    ResponseEntity<RepartidorResponse> result = repartidorController.crearRepartidor(request);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(result.getBody()).isEqualTo(response);
    verify(repartidorService).crearRepartidor(request);
  }

  @Test
  @DisplayName("listarRepartidores debe retornar 200 con lista del servicio")
  void listarRepartidores_DeberiaRetornarLista() {
    RepartidorResponse r1 = RepartidorResponse.builder().id(1L).cedula("111").build();
    RepartidorResponse r2 = RepartidorResponse.builder().id(2L).cedula("222").build();

    when(repartidorService.obtenerTodosLosRepartidores()).thenReturn(List.of(r1, r2));

    ResponseEntity<List<RepartidorResponse>> result = repartidorController.listarRepartidores();

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).containsExactly(r1, r2);
    verify(repartidorService).obtenerTodosLosRepartidores();
  }

  @Test
  @DisplayName("eliminarRepartidor debe retornar 204 y delegar en el servicio")
  void eliminarRepartidor_DeberiaRetornar204() {
    Long id = 10L;

    ResponseEntity<Void> result = repartidorController.eliminarRepartidor(id);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(repartidorService).eliminarRepartidor(id);
  }

  @Test
  @DisplayName("cambiarEstado debe retornar 200 con el DTO del servicio")
  void cambiarEstado_DeberiaRetornar200() {
    Long id = 7L;
    EstadoRepartidor nuevoEstado = EstadoRepartidor.MANTENIMIENTO;

    RepartidorResponse response = RepartidorResponse.builder()
        .id(7L)
        .estado(nuevoEstado)
        .build();

    when(repartidorService.cambiarEstadoRepartidor(id, nuevoEstado)).thenReturn(response);

    ResponseEntity<RepartidorResponse> result = repartidorController.cambiarEstado(id, nuevoEstado);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
    verify(repartidorService).cambiarEstadoRepartidor(id, nuevoEstado);
  }

  @Test
  @DisplayName("actualizarRepartidor debe retornar 200 con el DTO actualizado")
  void actualizarRepartidor_DeberiaRetornar200() {
    Long id = 3L;
    RepartidorUpdateRequest request = RepartidorUpdateRequest.builder()
        .telefono("0999999999")
        .build();

    RepartidorResponse response = RepartidorResponse.builder()
        .id(3L)
        .telefono("0999999999")
        .build();

    when(repartidorService.actualizarRepartidor(id, request)).thenReturn(response);

    ResponseEntity<RepartidorResponse> result = repartidorController.actualizarRepartidor(id, request);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
    verify(repartidorService).actualizarRepartidor(id, request);
  }

  @Test
  @DisplayName("asignarVehiculo debe retornar 200 y delegar en el servicio")
  void asignarVehiculo_DeberiaRetornar200() {
    Long repartidorId = 1L;
    Long vehiculoId = 2L;

    ResponseEntity<Void> result = repartidorController.asignarVehiculo(repartidorId, vehiculoId);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(repartidorService).asignarVehiculo(repartidorId, vehiculoId);
  }

  @Test
  @DisplayName("removerVehiculo debe retornar 204 y delegar en el servicio")
  void removerVehiculo_DeberiaRetornar204() {
    Long repartidorId = 1L;

    ResponseEntity<Void> result = repartidorController.removerVehiculo(repartidorId);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(repartidorService).removerVehiculo(repartidorId);
  }
}
