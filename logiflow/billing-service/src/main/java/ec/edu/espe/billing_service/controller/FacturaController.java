package ec.edu.espe.billing_service.controller;

import ec.edu.espe.billing_service.model.dto.request.FacturaRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.FacturaResponseDTO;
import ec.edu.espe.billing_service.model.enums.EstadoFactura;
import ec.edu.espe.billing_service.service.FacturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@Tag(name = "Facturas", description = "Operaciones de facturación")

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @Operation(summary = "Crear factura",
            description = "Genera una factura en estado BORRADOR según el tipo de entrega")

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<FacturaResponseDTO> crearFactura(@RequestBody FacturaRequestDTO request) {
        FacturaResponseDTO factura = facturaService.crearFactura(request);
        return new ResponseEntity<>(factura, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener factura por ID")

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<FacturaResponseDTO> obtenerFacturaPorId(@PathVariable UUID id) {
        FacturaResponseDTO factura = facturaService.obtenerFacturaPorId(id);
        return ResponseEntity.ok(factura);
    }

    @Operation(summary = "Obtener factura por ID de pedido")
    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<FacturaResponseDTO> obtenerFacturaPorPedidoId(@PathVariable String pedidoId) {
        FacturaResponseDTO factura = facturaService.obtenerFacturaPorPedidoId(pedidoId);
        return ResponseEntity.ok(factura);
    }

    @Operation(summary = "Actualizar estado de factura")
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<FacturaResponseDTO> actualizarEstado(
            @PathVariable UUID id,
            @RequestParam EstadoFactura estado
    ) {
        FacturaResponseDTO factura = facturaService.actualizarEstado(id, estado);
        return ResponseEntity.ok(factura);
    }
}
