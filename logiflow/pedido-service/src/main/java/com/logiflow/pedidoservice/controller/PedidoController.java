package com.logiflow.pedidoservice.controller;

import com.logiflow.pedidoservice.dto.PedidoPatchRequest;
import com.logiflow.pedidoservice.dto.PedidoRequest;
import com.logiflow.pedidoservice.dto.PedidoResponse;
import com.logiflow.pedidoservice.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de pedidos
 * Documentado con OpenAPI 3.0
 */
@Slf4j
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "API para gestión de pedidos de entrega")
public class PedidoController {

    private final PedidoService pedidoService;

    @Operation(
            summary = "Crear un nuevo pedido",
            description = "Crea un nuevo pedido con validación de cobertura y tipo de entrega"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<PedidoResponse> createPedido(
            @Valid @RequestBody PedidoRequest request) {
        log.info("POST /api/pedidos - Solicitud de creación de pedido recibida");
        log.debug("Datos del pedido: clienteId={}, modalidad={}, tipo={}, peso={}kg, origen={}, destino={}",
                request.getClienteId(),
                request.getModalidadServicio(),
                request.getTipoEntrega(),
                request.getPeso(),
                request.getDireccionOrigen().getCiudad(),
                request.getDireccionDestino().getCiudad());

        PedidoResponse response = pedidoService.createPedido(request);

        log.info("POST /api/pedidos - Pedido creado exitosamente con ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Obtener pedido por ID",
            description = "Consulta un pedido específico por su identificador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'REPARTIDOR_MOTORIZADO', 'REPARTIDOR_VEHICULO', 'REPARTIDOR_CAMION', 'SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<PedidoResponse> getPedidoById(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable String id) {
        log.info("GET /api/pedidos/{} - Consultando pedido", id);

        PedidoResponse response = pedidoService.getPedidoById(id);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener todos los pedidos",
            description = "Consulta todos los pedidos registrados en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pedidos obtenida exitosamente"
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<List<PedidoResponse>> getAllPedidos() {
        log.info("GET /api/pedidos - Consultando todos los pedidos");

        List<PedidoResponse> response = pedidoService.getAllPedidos();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener pedidos por cliente",
            description = "Consulta todos los pedidos de un cliente específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pedidos del cliente obtenida exitosamente"
            )
    })
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<List<PedidoResponse>> getPedidosByCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable String clienteId) {
        log.info("GET /api/pedidos/cliente/{} - Consultando pedidos del cliente", clienteId);

        List<PedidoResponse> response = pedidoService.getPedidosByCliente(clienteId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Actualizar parcialmente un pedido (PATCH)",
            description = "Actualiza uno o más campos de un pedido existente. Solo los campos enviados serán actualizados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<PedidoResponse> updatePedido(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable String id,
            @RequestBody PedidoPatchRequest patchRequest) {
        log.info("PATCH /api/pedidos/{} - Actualizando pedido parcialmente", id);

        PedidoResponse response = pedidoService.patchPedido(id, patchRequest);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cancelar un pedido",
            description = "Cambia el estado de un pedido a CANCELADO"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido cancelado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
            @ApiResponse(responseCode = "400", description = "El pedido no puede ser cancelado")
    })
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<PedidoResponse> cancelPedido(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable String id) {
        log.info("PATCH /api/pedidos/{}/cancelar - Cancelando pedido", id);

        PedidoResponse response = pedidoService.cancelarPedido(id);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Eliminar un pedido",
            description = "Elimina permanentemente un pedido del sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<Void> deletePedido(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable String id) {
        log.info("DELETE /api/pedidos/{} - Eliminando pedido", id);

        pedidoService.deletePedido(id);

        return ResponseEntity.noContent().build();
    }

    // ==================== ENDPOINTS PARA INTEGRACIÓN CON FLEETSERVICE ====================

    @Operation(
            summary = "Asignar repartidor y vehículo a un pedido",
            description = "Asigna un repartidor y vehículo a un pedido pendiente y cambia su estado a ASIGNADO"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Asignación exitosa",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
            @ApiResponse(responseCode = "400", description = "El pedido no puede ser asignado")
    })
    @PatchMapping("/{pedidoId}/asignar")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<PedidoResponse> asignarRepartidorYVehiculo(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable String pedidoId,
            @Parameter(description = "ID del repartidor", required = true)
            @RequestParam String repartidorId,
            @Parameter(description = "ID del vehículo", required = true)
            @RequestParam String vehiculoId) {
        log.info("PATCH /api/pedidos/{}/asignar - Asignando repartidor {} y vehículo {}",
                pedidoId, repartidorId, vehiculoId);

        PedidoResponse response = pedidoService.asignarRepartidorYVehiculo(pedidoId, repartidorId, vehiculoId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener pedidos pendientes de asignación",
            description = "Consulta todos los pedidos que están en estado PENDIENTE y no tienen repartidor ni vehículo asignado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pedidos pendientes obtenida exitosamente"
            )
    })
    @GetMapping("/pendientes-asignacion")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<List<PedidoResponse>> getPedidosPendientesAsignacion() {
        log.info("GET /api/pedidos/pendientes-asignacion - Consultando pedidos pendientes");

        List<PedidoResponse> response = pedidoService.getPedidosPendientesAsignacion();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener pedidos por repartidor",
            description = "Consulta todos los pedidos asignados a un repartidor específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pedidos del repartidor obtenida exitosamente"
            )
    })
    @GetMapping("/repartidor/{repartidorId}")
    @PreAuthorize("hasAnyRole('REPARTIDOR_MOTORIZADO', 'REPARTIDOR_VEHICULO', 'REPARTIDOR_CAMION', 'SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<List<PedidoResponse>> getPedidosByRepartidor(
            @Parameter(description = "ID del repartidor", required = true)
            @PathVariable String repartidorId) {
        log.info("GET /api/pedidos/repartidor/{} - Consultando pedidos del repartidor", repartidorId);

        List<PedidoResponse> response = pedidoService.getPedidosByRepartidor(repartidorId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener pedidos por modalidad de servicio",
            description = "Consulta todos los pedidos de una modalidad específica (URBANA_RAPIDA, INTERMUNICIPAL, NACIONAL)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pedidos por modalidad obtenida exitosamente"
            )
    })
    @GetMapping("/modalidad/{modalidad}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<List<PedidoResponse>> getPedidosByModalidad(
            @Parameter(description = "Modalidad de servicio", required = true)
            @PathVariable com.logiflow.pedidoservice.model.ModalidadServicio modalidad) {
        log.info("GET /api/pedidos/modalidad/{} - Consultando pedidos por modalidad", modalidad);

        List<PedidoResponse> response = pedidoService.getPedidosByModalidad(modalidad);

        return ResponseEntity.ok(response);
    }

    // ==================== ENDPOINTS PARA INTEGRACIÓN CON BILLINGSERVICE ====================

    @Operation(
            summary = "Asociar factura a un pedido",
            description = "Asocia una factura generada por BillingService a un pedido y almacena la tarifa calculada"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Factura asociada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
            @ApiResponse(responseCode = "400", description = "El pedido ya tiene una factura asociada")
    })
    @PatchMapping("/{pedidoId}/factura")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<PedidoResponse> asociarFactura(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable String pedidoId,
            @Parameter(description = "ID de la factura", required = true)
            @RequestParam String facturaId,
            @Parameter(description = "Tarifa calculada", required = true)
            @RequestParam Double tarifa) {
        log.info("PATCH /api/pedidos/{}/factura - Asociando factura {} con tarifa {}",
                pedidoId, facturaId, tarifa);

        PedidoResponse response = pedidoService.asociarFactura(pedidoId, facturaId, tarifa);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener pedidos sin factura",
            description = "Consulta todos los pedidos que aún no tienen factura asociada"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pedidos sin factura obtenida exitosamente"
            )
    })
    @GetMapping("/sin-factura")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<List<PedidoResponse>> getPedidosSinFactura() {
        log.info("GET /api/pedidos/sin-factura - Consultando pedidos sin factura");

        List<PedidoResponse> response = pedidoService.getPedidosSinFactura();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener pedidos de alta prioridad",
            description = "Consulta todos los pedidos con prioridad ALTA o URGENTE que están pendientes o asignados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pedidos de alta prioridad obtenida exitosamente"
            )
    })
    @GetMapping("/alta-prioridad")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<List<PedidoResponse>> getPedidosAltaPrioridad() {
        log.info("GET /api/pedidos/alta-prioridad - Consultando pedidos de alta prioridad");

        List<PedidoResponse> response = pedidoService.getPedidosAltaPrioridad();

        return ResponseEntity.ok(response);
    }
}
