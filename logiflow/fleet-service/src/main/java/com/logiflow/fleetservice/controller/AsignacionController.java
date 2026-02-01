package com.logiflow.fleetservice.controller;

import com.logiflow.fleetservice.dto.request.AsignacionRequest;
import com.logiflow.fleetservice.dto.response.AsignacionResponse;
import com.logiflow.fleetservice.service.AsignacionService;
import io.swagger.v3.oas.annotations.Operation;
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

/**
 * Controlador REST para gestión de asignaciones de pedidos
 */
@RestController
@RequestMapping("/api/asignaciones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Asignaciones", description = "API para asignar repartidores y vehículos a pedidos")
@SecurityRequirement(name = "Bearer Authentication")
public class AsignacionController {

    private final AsignacionService asignacionService;

    @PostMapping

    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    @Operation(
            summary = "Asignar repartidor y vehículo a un pedido",
            description = "Busca y asigna el mejor repartidor disponible con su vehículo para un pedido"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asignación realizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos"),
            @ApiResponse(responseCode = "404", description = "No hay repartidores disponibles")
    })
    public ResponseEntity<AsignacionResponse> asignarRepartidor(
            @Valid @RequestBody AsignacionRequest request
    ) {
        log.info("POST /api/asignaciones - Asignando repartidor para pedido: {}", request.getPedidoId());

        AsignacionResponse response = asignacionService.asignarRepartidorYVehiculo(request);

        // Si no se pudo asignar, retornar 404
        if ("RECHAZADO".equals(response.getEstado())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/pedido/{pedidoId}/liberar")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    @Operation(
            summary = "Liberar asignación de un pedido",
            description = "Libera el repartidor asignado cuando un pedido es cancelado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Asignación liberada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<Void> liberarAsignacion(
            @PathVariable String pedidoId
    ) {
        log.info("DELETE /api/asignaciones/pedido/{}/liberar", pedidoId);

        asignacionService.liberarAsignacion(pedidoId);

        return ResponseEntity.noContent().build();
    }
}

