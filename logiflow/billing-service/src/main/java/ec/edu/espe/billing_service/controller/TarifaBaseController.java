package ec.edu.espe.billing_service.controller;

import ec.edu.espe.billing_service.model.dto.request.TarifaBaseRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.TarifaBaseResponseDTO;
import ec.edu.espe.billing_service.service.TarifaBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tarifas", description = "Gestión de tarifas base")
@RestController
@RequestMapping("/api/tarifas")
@RequiredArgsConstructor
public class TarifaBaseController {

    private final TarifaBaseService tarifaBaseService;

    @Operation(summary = "Crear tarifa base")
    @PostMapping
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<TarifaBaseResponseDTO> crearTarifa(@RequestBody TarifaBaseRequestDTO request) {
        TarifaBaseResponseDTO tarifa = tarifaBaseService.crearTarifa(request);
        return new ResponseEntity<>(tarifa, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener tarifa por tipo de entrega")
    @GetMapping("/{tipoEntrega}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<TarifaBaseResponseDTO> obtenerPorTipoEntrega(@PathVariable String tipoEntrega) {
        TarifaBaseResponseDTO tarifa = tarifaBaseService.obtenerPorTipoEntrega(tipoEntrega);
        return ResponseEntity.ok(tarifa);
    }

    @Operation(summary = "Actualizar tarifa base")
    @PutMapping("/{tipoEntrega}")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public ResponseEntity<TarifaBaseResponseDTO> actualizarTarifa(
            @PathVariable String tipoEntrega,
            @RequestBody TarifaBaseRequestDTO request
    ) {
        TarifaBaseResponseDTO tarifa = tarifaBaseService.actualizarTarifa(tipoEntrega, request);
        return ResponseEntity.ok(tarifa);
    }

    @GetMapping("/tarifas")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'GERENTE', 'ADMINISTRADOR_SISTEMA')")
    public List<TarifaBaseResponseDTO> verTodasLasTarifas() {
        return tarifaBaseService.obtenerTodasLasTarifas();
    }

}
