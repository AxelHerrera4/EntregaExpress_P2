package ec.edu.espe.billing_service.service.impl;

import ec.edu.espe.billing_service.factory.TarifaStrategyFactory;
import ec.edu.espe.billing_service.model.dto.request.FacturaRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.FacturaResponseDTO;
import ec.edu.espe.billing_service.model.entity.Factura;
import ec.edu.espe.billing_service.model.entity.TarifaBase;
import ec.edu.espe.billing_service.model.enums.EstadoFactura;
import ec.edu.espe.billing_service.repository.FacturaRepository;
import ec.edu.espe.billing_service.service.FacturaService;
import ec.edu.espe.billing_service.service.TarifaBaseService;
import ec.edu.espe.billing_service.strategy.TarifaStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final TarifaBaseService tarifaBaseService;
    private final TarifaStrategyFactory tarifaStrategyFactory;

    @Override
    public FacturaResponseDTO crearFactura(FacturaRequestDTO request) {

        log.info("Creando factura para pedidoId={}, tipoEntrega={}, distanciaKm={}",
                request.getPedidoId(),
                request.getTipoEntrega(),
                request.getDistanciaKm());


        if (facturaRepository.existsByPedidoId(request.getPedidoId())) {
            log.warn("Intento de crear factura duplicada para pedidoId={}", request.getPedidoId());
            throw new IllegalStateException(
                    "Ya existe una factura para el pedido " + request.getPedidoId()
            );
        }
        log.debug("Buscando tarifa base para tipoEntrega={}", request.getTipoEntrega());
        TarifaBase tarifaBase = tarifaBaseService.obtenerEntidadPorTipoEntrega(request.getTipoEntrega());

        log.debug("Seleccionando TarifaStrategy para tipoEntrega={}", request.getTipoEntrega());
        TarifaStrategy strategy =
                tarifaStrategyFactory.obtenerStrategy(request.getTipoEntrega());


        var montoFinal = strategy.calcularTarifa(tarifaBase, request.getDistanciaKm());
        log.info("Monto calculado | pedidoId={} | monto={}",
                request.getPedidoId(),
                montoFinal);

        Factura factura = Factura.builder()
                .pedidoId(request.getPedidoId())
                .tipoEntrega(request.getTipoEntrega())
                .montoTotal(montoFinal)
                .estado(EstadoFactura.BORRADOR)
                .fechaCreacion(LocalDateTime.now())
                .distanciaKm(request.getDistanciaKm())
                .build();

        Factura guardada = facturaRepository.save(factura);

        log.info("Factura guardada | facturaId={} | pedidoId={}",
                guardada.getId(),
                guardada.getPedidoId());


        return mapToResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponseDTO obtenerFacturaPorId(UUID facturaId) {
        log.debug("Consultando factura por id={}", facturaId);
        return facturaRepository.findById(facturaId)
                .map(this::mapToResponse)
                .orElseThrow(() -> {
                    log.error("Factura no encontrada id={}", facturaId);
                    return new RuntimeException("Factura no encontrada");
                });
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponseDTO obtenerFacturaPorPedidoId(String pedidoId) {
        log.debug("Consultando factura por pedidoId={}", pedidoId);
        return facturaRepository.findByPedidoId(pedidoId)
                .map(this::mapToResponse)
                .orElseThrow(() -> {
                    log.error("Factura no encontrada para pedidoId={}", pedidoId);
                    return new RuntimeException(
                            "Factura no encontrada para el pedido " + pedidoId);
                });
    }

    @Override
    public FacturaResponseDTO actualizarEstado(UUID facturaId, EstadoFactura estado) {
        log.info("Actualizando estado factura | facturaId={} | nuevoEstado={}",
                facturaId,
                estado);

        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> {
                    log.error("No existe factura para actualizar estado | facturaId={}", facturaId);
                    return new RuntimeException("Factura no encontrada");
                });

        factura.setEstado(estado);

        Factura actualizada = facturaRepository.save(factura);

        log.info("Estado actualizado correctamente | facturaId={} | estado={}",
                actualizada.getId(),
                actualizada.getEstado());

        return mapToResponse(actualizada);

    }

    private FacturaResponseDTO mapToResponse(Factura factura) {
        return FacturaResponseDTO.builder()
                .id(factura.getId())
                .pedidoId(factura.getPedidoId())
                .tipoEntrega(factura.getTipoEntrega())
                .montoTotal(factura.getMontoTotal())
                .estado(factura.getEstado())
                .fechaCreacion(factura.getFechaCreacion())
                .distanciaKm(factura.getDistanciaKm()) // ← incluir distancia
                .build();
    }
}
