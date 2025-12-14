package com.logiflow.fleetservice.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
@ExtendWith(MockitoExtension.class)
@DisplayName("HealthController Tests")
class HealthControllerTest {

  @InjectMocks
  private HealthController healthController;

  @Test
  @DisplayName("Debe retornar estado UP del servicio")
  void debeRetornarHealthCheckCorrectamente() {
    ResponseEntity<Map<String, Object>> response = healthController.healthCheck();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("status")).isEqualTo("UP");
    assertThat(response.getBody().get("service")).isEqualTo("fleet-service");
    assertThat(response.getBody().get("version")).isEqualTo("1.0.0");
    assertThat(response.getBody().get("timestamp")).isNotNull();
  }

  @Test
  @DisplayName("Debe incluir timestamp en la respuesta")
  void debeIncluirTimestamp() {
    ResponseEntity<Map<String, Object>> response = healthController.healthCheck();

    assertThat(response.getBody()).containsKey("timestamp");
    assertThat(response.getBody().get("timestamp")).isNotNull();
  }

  @Test
  @DisplayName("Debe retornar todos los campos requeridos")
  void debeRetornarTodosLosCamposRequeridos() {
    ResponseEntity<Map<String, Object>> response = healthController.healthCheck();

    assertThat(response.getBody())
        .containsKeys("status", "service", "timestamp", "version");
  }

  @Test
  @DisplayName("Debe retornar HTTP 200 OK")
  void debeRetornarHttp200() {
    ResponseEntity<Map<String, Object>> response = healthController.healthCheck();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
