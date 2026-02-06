package ec.edu.espe.billing_service.service.impl;

import ec.edu.espe.billing_service.factory.TarifaStrategyFactory;
import ec.edu.espe.billing_service.model.dto.request.FacturaRequestDTO;
import ec.edu.espe.billing_service.model.dto.response.EstadisticasFacturasDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

        log.info("💰 [FACTURA-CREADA] Factura guardada exitosamente | FacturaID: {} | PedidoID: {} | Monto: {} | Estado: {}",
                guardada.getId(),
                guardada.getPedidoId(),
                guardada.getMontoTotal(),
                guardada.getEstado());
        
        log.info("🔍 [FACTURA-DETAILS] FacturaID: {} - Creada para Pedido: {} con monto total de ${}",
                guardada.getId(), guardada.getPedidoId(), guardada.getMontoTotal());

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

    @Override
    @Transactional(readOnly = true)
    public Page<FacturaResponseDTO> obtenerTodasLasFacturas(Pageable pageable) {
        log.info("Consultando todas las facturas con paginación | page={} | size={}",
                pageable.getPageNumber(),
                pageable.getPageSize());
        
        Page<Factura> facturas = facturaRepository.findAll(pageable);
        
        log.info("Facturas encontradas | total={} | paginas={}",
                facturas.getTotalElements(),
                facturas.getTotalPages());
        
        return facturas.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FacturaResponseDTO> obtenerFacturasPorEstado(EstadoFactura estado, Pageable pageable) {
        log.info("Consultando facturas por estado={} | page={} | size={}",
                estado,
                pageable.getPageNumber(),
                pageable.getPageSize());
        
        Page<Factura> facturas = facturaRepository.findAllByEstado(estado, pageable);
        
        log.info("Facturas encontradas con estado={} | total={}",
                estado,
                facturas.getTotalElements());
        
        return facturas.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FacturaResponseDTO> obtenerFacturasPorFechas(
            LocalDateTime fechaDesde, 
            LocalDateTime fechaHasta, 
            Pageable pageable) {
        
        log.info("Consultando facturas por rango de fechas | desde={} | hasta={} | page={} | size={}",
                fechaDesde,
                fechaHasta,
                pageable.getPageNumber(),
                pageable.getPageSize());
        
        Page<Factura> facturas = facturaRepository.findAllByFechaCreacionBetween(
                fechaDesde, 
                fechaHasta, 
                pageable
        );
        
        log.info("Facturas encontradas en rango | total={}",
                facturas.getTotalElements());
        
        return facturas.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FacturaResponseDTO> obtenerFacturasPorEstadoYFechas(
            EstadoFactura estado,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Pageable pageable) {
        
        log.info("Consultando facturas por estado y fechas | estado={} | desde={} | hasta={}",
                estado,
                fechaDesde,
                fechaHasta);
        
        Page<Factura> facturas = facturaRepository.findAllByEstadoAndFechaCreacionBetween(
                estado,
                fechaDesde,
                fechaHasta,
                pageable
        );
        
        log.info("Facturas encontradas | total={}",
                facturas.getTotalElements());
        
        return facturas.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasFacturasDTO obtenerEstadisticas() {
        log.info("Calculando estadísticas de facturas");
        
        Long totalFacturas = facturaRepository.count();
        Long totalPagadas = facturaRepository.countByEstado(EstadoFactura.PAGADA);
        Long totalPendientes = facturaRepository.countByEstado(EstadoFactura.EMITIDA);
        Long totalBorrador = facturaRepository.countByEstado(EstadoFactura.BORRADOR);
        Long totalCanceladas = facturaRepository.countByEstado(EstadoFactura.ANULADA);
        
        BigDecimal montoTotalFacturado = facturaRepository.sumMontoTotal();
        BigDecimal montoTotalPagado = facturaRepository.sumMontoByEstado(EstadoFactura.PAGADA);
        BigDecimal montoTotalPendiente = facturaRepository.sumMontoByEstado(EstadoFactura.EMITIDA);
        
        Double promedioMontoPorFactura = facturaRepository.avgMontoTotal();
        
        log.info("Estadísticas calculadas | total={} | pagadas={} | pendientes={} | montoTotal={}",
                totalFacturas,
                totalPagadas,
                totalPendientes,
                montoTotalFacturado);
        
        return EstadisticasFacturasDTO.builder()
                .totalFacturas(totalFacturas)
                .totalPagadas(totalPagadas)
                .totalPendientes(totalPendientes)
                .totalBorrador(totalBorrador)
                .totalCanceladas(totalCanceladas)
                .montoTotalFacturado(montoTotalFacturado)
                .montoTotalPagado(montoTotalPagado)
                .montoTotalPendiente(montoTotalPendiente)
                .promedioMontoPorFactura(promedioMontoPorFactura)
                .build();
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
