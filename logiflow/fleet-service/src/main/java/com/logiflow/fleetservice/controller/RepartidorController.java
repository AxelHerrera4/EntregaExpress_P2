package com.logiflow.fleetservice.controller;


import com.logiflow.fleetservice.dto.request.RepartidorCreateRequest;
import com.logiflow.fleetservice.dto.request.RepartidorUpdateRequest;
import com.logiflow.fleetservice.dto.response.RepartidorResponse;
import com.logiflow.fleetservice.model.entity.enums.EstadoRepartidor;
import com.logiflow.fleetservice.service.RepartidorServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repartidores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Repartidores", description = "API para gestión de repartidores de la flota")
@SecurityRequirement(name = "Bearer Authentication")
public class RepartidorController {

  private final RepartidorServiceImpl repartidorService;

  @PostMapping
  @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
  @Operation(summary = "Crear nuevo repartidor",
          description = "Registra un nuevo repartidor en el sistema")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Repartidor creado"),
          @ApiResponse(responseCode = "400", description = "Datos inválidos"),
          @ApiResponse(responseCode = "409", description = "Cédula o email duplicado")
  })
  public ResponseEntity<RepartidorResponse> crearRepartidor(
          @Valid @RequestBody RepartidorCreateRequest request
  ) {
    log.info("POST /repartidores - Creando repartidor");
    RepartidorResponse response = repartidorService.crearRepartidor(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('REPARTIDOR_MOTORIZADO', 'REPARTIDOR_VEHICULO', 'REPARTIDOR_CAMION', 'SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
  @Operation(summary = "Obtener repartidor por ID")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Repartidor encontrado"),
          @ApiResponse(responseCode = "404", description = "Repartidor no encontrado")
  })
  public ResponseEntity<RepartidorResponse> obtenerRepartidorPorId(
          @Parameter(description = "ID del repartidor", required = true)
          @PathVariable Long id
  ) {
    log.info("GET /repartidores/{}", id);
    RepartidorResponse response = repartidorService.obtenerRepartidorPorId(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
  @Operation(summary = "Listar todos los repartidores")
  @ApiResponse(responseCode = "200", description = "Lista obtenida")
  public ResponseEntity<List<RepartidorResponse>> listarRepartidores() {
    log.info("GET /repartidores");
    List<RepartidorResponse> repartidores = repartidorService.obtenerTodosLosRepartidores();
    return ResponseEntity.ok(repartidores);
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAnyRole('REPARTIDOR_MOTORIZADO', 'REPARTIDOR_VEHICULO', 'REPARTIDOR_CAMION', 'SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
  @Operation(summary = "Actualizar repartidor",
          description = "Actualización parcial de datos del repartidor")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Actualizado"),
          @ApiResponse(responseCode = "404", description = "No encontrado")
  })
  public ResponseEntity<RepartidorResponse> actualizarRepartidor(
          @PathVariable Long id,
          @Valid @RequestBody RepartidorUpdateRequest request
  ) {
    log.info("PATCH /repartidores/{}", id);
    RepartidorResponse response = repartidorService.actualizarRepartidor(id, request);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
  @Operation(summary = "Cambiar estado del repartidor",
          description = "Cambia entre DISPONIBLE, EN_RUTA, MANTENIMIENTO, etc.")
  @ApiResponse(responseCode = "200", description = "Estado actualizado")
  public ResponseEntity<RepartidorResponse> cambiarEstado(
          @PathVariable Long id,
          @RequestParam EstadoRepartidor estado
  ) {
    log.info("PATCH /repartidores/{}/estado -> {}", id, estado);
    RepartidorResponse response = repartidorService.cambiarEstadoRepartidor(id, estado);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('GERENTE', 'ADMINISTRADOR_SISTEMA')")
  @Operation(summary = "Eliminar repartidor",
          description = "Eliminación lógica - marca como inactivo")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Eliminado"),
          @ApiResponse(responseCode = "404", description = "No encontrado"),
          @ApiResponse(responseCode = "400", description = "No se puede eliminar (está en ruta)")
  })
  public ResponseEntity<Void> eliminarRepartidor(
          @PathVariable Long id
  ) {
    log.info("DELETE /repartidores/{}", id);
    repartidorService.eliminarRepartidor(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/asignar-vehiculo")
  @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
  @Operation(summary = "Asignar vehículo a repartidor",
          description = "Valida licencia y tipo de vehículo antes de asignar")
  @ApiResponse(responseCode = "200", description = "Vehículo asignado")
  public ResponseEntity<Void> asignarVehiculo(
          @PathVariable Long id,
          @RequestParam Long vehiculoId
  ) {
    log.info("POST /repartidores/{}/asignar-vehiculo/{}", id, vehiculoId);
    repartidorService.asignarVehiculo(id, vehiculoId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}/vehiculo")
  @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
  @Operation(summary = "Remover vehículo del repartidor")
  @ApiResponse(responseCode = "204", description = "Vehículo removido")
  public ResponseEntity<Void> removerVehiculo(
          @PathVariable Long id
  ) {
    log.info("DELETE /repartidores/{}/vehiculo", id);
    repartidorService.removerVehiculo(id);
    return ResponseEntity.noContent().build();
  }
}