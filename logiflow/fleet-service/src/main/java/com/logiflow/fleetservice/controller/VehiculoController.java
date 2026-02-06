package com.logiflow.fleetservice.controller;


import com.logiflow.fleetservice.dto.request.VehiculoCreateRequest;
import com.logiflow.fleetservice.dto.request.VehiculoUpdateRequest;
import com.logiflow.fleetservice.dto.response.VehiculoResponse;
import com.logiflow.fleetservice.service.VehiculoAsignacionService;
import com.logiflow.fleetservice.service.VehiculoServiceImpl;
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
import java.util.UUID;


@RestController
@RequestMapping("/vehiculos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehículos", description = "API para gestión de vehículos de la flota")
@SecurityRequirement(name = "Bearer Authentication")
public class VehiculoController {

    private final VehiculoServiceImpl vehiculoService;
    private final VehiculoAsignacionService asignacionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Crear un nuevo vehículo",
            description = "Crea un nuevo vehículo en la flota usando Factory Pattern")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehículo creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un vehículo con esa placa"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes")
    })
    public ResponseEntity<VehiculoResponse> crearVehiculo(
            @Valid @RequestBody VehiculoCreateRequest request
    ) {
        log.info("POST /vehiculos - Creando vehículo");
        VehiculoResponse response = vehiculoService.crearVehiculo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('REPARTIDOR_MOTORIZADO', 'REPARTIDOR_VEHICULO', 'REPARTIDOR_CAMION', 'SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Obtener vehículo por ID",
            description = "Consulta los detalles de un vehículo específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<VehiculoResponse> obtenerVehiculoPorId(
            @Parameter(description = "ID del vehículo", required = true)
            @PathVariable UUID id
    ) {
        log.info("GET /vehiculos/{} - Consultando vehículo", id);
        VehiculoResponse response = vehiculoService.obtenerVehiculoPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Listar todos los vehículos",
            description = "Obtiene la lista completa de vehículos de la flota")
    @ApiResponse(responseCode = "200", description = "Lista de vehículos obtenida")
    public ResponseEntity<List<VehiculoResponse>> listarVehiculos() {
        log.info("GET /vehiculos - Listando todos los vehículos");
        List<VehiculoResponse> vehiculos = vehiculoService.obtenerTodosLosVehiculos();
        return ResponseEntity.ok(vehiculos);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Actualizar vehículo parcialmente",
            description = "Actualiza campos específicos de un vehículo (PATCH)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículo actualizado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<VehiculoResponse> actualizarVehiculo(
            @Parameter(description = "ID del vehículo", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody VehiculoUpdateRequest request
    ) {
        log.info("PATCH /vehiculos/{} - Actualizando vehículo", id);
        VehiculoResponse response = vehiculoService.actualizarVehiculo(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Cambiar estado del vehículo",
            description = "Cambiar estado de un vehículo")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    public ResponseEntity<VehiculoResponse> cambiarEstado(
            @PathVariable UUID id,
            @RequestParam com.logiflow.fleetservice.model.entity.enums.EstadoVehiculo estado
    ) {
        log.info("PATCH /vehiculos/{}/estado - Cambiando a: {}", id, estado);
        VehiculoResponse response = vehiculoService.actualizarEstadoVehiculo(id, estado);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Eliminar vehículo",
            description = "Eliminación lógica del vehículo (marca como inactivo)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vehículo eliminado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<Void> eliminarVehiculo(
            @Parameter(description = "ID del vehículo", required = true)
            @PathVariable UUID id
    ) {
        log.info("DELETE /vehiculos/{} - Eliminando vehículo", id);
        vehiculoService.eliminarVehiculo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponibles-por-peso")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    @Operation(summary = "Obtener vehículos disponibles para un peso específico",
            description = "Retorna vehículos disponibles según capacidad de carga. " +
                    "Motorizado: hasta 30kg, VehículoLiviano: 31-1000kg, Camión: más de 1000kg")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de vehículos disponibles"),
            @ApiResponse(responseCode = "400", description = "Peso inválido")
    })
    public ResponseEntity<List<VehiculoResponse>> obtenerVehiculosDisponiblesPorPeso(
            @Parameter(description = "Peso de la carga en kilogramos", required = true)
            @RequestParam Double peso
    ) {
        log.info("GET /vehiculos/disponibles-por-peso?peso={}", peso);
        List<VehiculoResponse> vehiculos = asignacionService.obtenerVehiculosDisponiblesPorPeso(peso);
        return ResponseEntity.ok(vehiculos);
    }
}