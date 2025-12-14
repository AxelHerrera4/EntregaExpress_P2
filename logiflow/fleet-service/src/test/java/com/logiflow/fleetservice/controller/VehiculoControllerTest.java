package com.logiflow.fleetservice.controller;

import com.logiflow.fleetservice.dto.request.VehiculoCreateRequest;
import com.logiflow.fleetservice.dto.request.VehiculoUpdateRequest;
import com.logiflow.fleetservice.dto.response.VehiculoResponse;
import com.logiflow.fleetservice.model.entity.enums.TipoVehiculo;
import com.logiflow.fleetservice.service.VehiculoServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehiculoController Fase 1 Tests")
class VehiculoControllerTest {

  @Mock
  private VehiculoServiceImpl vehiculoService;

  @InjectMocks
  private VehiculoController vehiculoController;

  @Test
  @DisplayName("crearVehiculo debe retornar 201 y el body del servicio")
  void crearVehiculo_DeberiaRetornar201() {
    VehiculoCreateRequest request = VehiculoCreateRequest.builder()
        .tipo(TipoVehiculo.MOTORIZADO)
        .placa("ABC-123")
        .marca("Yamaha")
        .modelo("FZ")
        .anio(2024)
        .build();

    VehiculoResponse response = VehiculoResponse.builder()
        .id(1L)
        .placa("ABC-123")
        .build();

    when(vehiculoService.crearVehiculo(request)).thenReturn(response);

    ResponseEntity<VehiculoResponse> result = vehiculoController.crearVehiculo(request);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(result.getBody()).isEqualTo(response);
    verify(vehiculoService).crearVehiculo(request);
  }

  @Test
  @DisplayName("listarVehiculos debe retornar 200 con lista del servicio")
  void listarVehiculos_DeberiaRetornarLista() {
    VehiculoResponse v1 = VehiculoResponse.builder().id(1L).placa("AAA-111").build();
    VehiculoResponse v2 = VehiculoResponse.builder().id(2L).placa("BBB-222").build();

    when(vehiculoService.obtenerTodosLosVehiculos()).thenReturn(List.of(v1, v2));

    ResponseEntity<List<VehiculoResponse>> result = vehiculoController.listarVehiculos();

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).containsExactly(v1, v2);
    verify(vehiculoService).obtenerTodosLosVehiculos();
  }

  @Test
  @DisplayName("eliminarVehiculo debe retornar 204 y delegar en el servicio")
  void eliminarVehiculo_DeberiaRetornar204() {
    Long id = 10L;

    ResponseEntity<Void> result = vehiculoController.eliminarVehiculo(id);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(vehiculoService).eliminarVehiculo(id);
  }

  @Test
  @DisplayName("actualizarVehiculo debe retornar 200 con el DTO actualizado")
  void actualizarVehiculo_DeberiaRetornar200() {
    Long id = 5L;
    VehiculoUpdateRequest request = VehiculoUpdateRequest.builder()
        .marca("Toyota")
        .build();

    VehiculoResponse response = VehiculoResponse.builder()
        .id(5L)
        .marca("Toyota")
        .build();

    when(vehiculoService.actualizarVehiculo(id, request)).thenReturn(response);

    ResponseEntity<VehiculoResponse> result = vehiculoController.actualizarVehiculo(id, request);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
    verify(vehiculoService).actualizarVehiculo(id, request);
  }
}
