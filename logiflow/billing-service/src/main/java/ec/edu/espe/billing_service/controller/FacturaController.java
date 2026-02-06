package ec.edu.espe.billing_service.controller;

import ec.edu.espe.billing_service.model.dto.request.FacturaRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.EstadisticasFacturasDTO;
import ec.edu.espe.billing_service.model.dto.response.FacturaResponseDTO;
import ec.edu.espe.billing_service.model.enums.EstadoFactura;
import ec.edu.espe.billing_service.service.FacturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @Operation(
        summary = "Obtener todas las facturas",
        description = "Devuelve un listado paginado de todas las facturas. Soporta filtrado por estado y rango de fechas."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<Page<FacturaResponseDTO>> obtenerTodasLasFacturas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) EstadoFactura estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<FacturaResponseDTO> facturas;
        
        if (estado != null && fechaDesde != null && fechaHasta != null) {
            facturas = facturaService.obtenerFacturasPorEstadoYFechas(estado, fechaDesde, fechaHasta, pageable);
        } else if (estado != null) {
            facturas = facturaService.obtenerFacturasPorEstado(estado, pageable);
        } else if (fechaDesde != null && fechaHasta != null) {
            facturas = facturaService.obtenerFacturasPorFechas(fechaDesde, fechaHasta, pageable);
        } else {
            facturas = facturaService.obtenerTodasLasFacturas(pageable);
        }
        
        return ResponseEntity.ok(facturas);
    }

    @Operation(
        summary = "Obtener estadísticas de facturas",
        description = "Devuelve un resumen con estadísticas financieras: total de facturas por estado, montos facturados, promedios."
    )
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<EstadisticasFacturasDTO> obtenerEstadisticas() {
        EstadisticasFacturasDTO estadisticas = facturaService.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }
}
