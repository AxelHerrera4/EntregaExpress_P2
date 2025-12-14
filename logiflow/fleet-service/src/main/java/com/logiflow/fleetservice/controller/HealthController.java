package com.logiflow.fleetservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Endpoints de salud del microservicio")
public class HealthController {

  @GetMapping
  @Operation(
      summary = "Health Check",
      description = "Verifica que el microservicio esté funcionando correctamente"
  )
  @ApiResponse(responseCode = "200", description = "Servicio operativo")
  public ResponseEntity<Map<String, Object>> healthCheck() {
    Map<String, Object> health = new HashMap<>();
    health.put("status", "UP");
    health.put("service", "fleet-service");
    health.put("timestamp", LocalDateTime.now());
    health.put("version", "1.0.0");
    return ResponseEntity.ok(health);
  }
}
