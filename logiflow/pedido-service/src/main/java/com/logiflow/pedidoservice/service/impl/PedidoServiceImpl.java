package com.logiflow.pedidoservice.service.impl;

import com.logiflow.pedidoservice.client.BillingClient;
import com.logiflow.pedidoservice.client.FleetClient;
import com.logiflow.pedidoservice.dto.*;
import com.logiflow.pedidoservice.event.PedidoEstadoEvent;
import com.logiflow.pedidoservice.model.*;
import com.logiflow.pedidoservice.rabbit.PedidoEventPublisher;
import com.logiflow.pedidoservice.repository.PedidoRepository;
import com.logiflow.pedidoservice.service.CoberturaValidationService;
import com.logiflow.pedidoservice.service.PedidoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final CoberturaValidationService coberturaValidationService;
    private final BillingClient billingClient;
    private final FleetClient fleetClient;
    private final PedidoEventPublisher pedidoEventPublisher; // ✅ ÚNICA INYECCIÓN (corregido)

    @Value("${integration.billing.enabled:true}")
    private boolean billingIntegrationEnabled;

    @Value("${integration.fleet.enabled:true}")
    private boolean fleetIntegrationEnabled;

    @Override
    @Transactional
    public PedidoResponse createPedido(PedidoRequest request) {
        log.info("➡️ Creando nuevo pedido para cliente: {}", request.getClienteId());

        Pedido pedido = pedidoMapper.toEntity(request);

        validateCobertura(pedido.getCobertura());
        validateTipoEntrega(request.getTipoEntrega(), pedido.getCobertura());

        Pedido savedPedido = pedidoRepository.save(pedido);
        log.info("💾 Pedido guardado con ID: {}", savedPedido.getId());

        // 🔥 PUBLICAMOS EVENTO: PENDIENTE
        PedidoEstadoEvent creadoEvent =
                new PedidoEstadoEvent(
                        savedPedido.getId(),
                        null,
                        savedPedido.getEstado().name()
                );

        log.info("📤 Publicando evento de creación de pedido...");
        pedidoEventPublisher.publishPedidoEstadoEvent(creadoEvent);

        // ============= BILLING SERVICE =============
        if (billingIntegrationEnabled) {
            try {
                log.info("💳 Integrando con Billing Service...");

                Double distanciaKm = calcularDistanciaEstimada(
                        savedPedido.getDireccionOrigen().getCiudad(),
                        savedPedido.getDireccionDestino().getCiudad(),
                        savedPedido.getModalidadServicio()
                );

                FacturaRequest facturaRequest = FacturaRequest.builder()
                        .pedidoId(savedPedido.getId())
                        .tipoEntrega(savedPedido.getTipoEntrega().name())
                        .distanciaKm(distanciaKm)
                        .build();

                FacturaResponse facturaResponse = billingClient.crearFactura(facturaRequest);

                savedPedido.setFacturaId(facturaResponse.getId());
                savedPedido.setTarifaCalculada(facturaResponse.getMontoTotal().doubleValue());

                savedPedido = pedidoRepository.save(savedPedido);

                log.info("✅ Factura asociada al pedido: {}", facturaResponse.getId());

            } catch (Exception e) {
                log.error("❌ Error con Billing: {}", e.getMessage());
            }
        }

        // ============= FLEET SERVICE =============
        if (fleetIntegrationEnabled) {
            try {
                log.info("🚚 Integrando con Fleet Service...");

                AsignacionRequest asignacionRequest = AsignacionRequest.builder()
                        .pedidoId(savedPedido.getId())
                        .modalidadServicio(savedPedido.getModalidadServicio().name())
                        .tipoEntrega(savedPedido.getTipoEntrega().name())
                        .prioridad(savedPedido.getPrioridad().name())
                        .ciudadOrigen(savedPedido.getDireccionOrigen().getCiudad())
                        .ciudadDestino(savedPedido.getDireccionDestino().getCiudad())
                        .peso(savedPedido.getPeso())
                        .build();

                AsignacionResponse asignacionResponse =
                        fleetClient.asignarRepartidor(asignacionRequest);

                if ("ASIGNADO".equals(asignacionResponse.getEstado())) {

                    String estadoAnterior = savedPedido.getEstado().name();

                    savedPedido.setRepartidorId(asignacionResponse.getRepartidorId());
                    savedPedido.setVehiculoId(asignacionResponse.getVehiculoId());
                    savedPedido.setEstado(EstadoPedido.ASIGNADO);

                    savedPedido = pedidoRepository.save(savedPedido);

                    // 🔥 PUBLICAMOS EVENTO: PENDIENTE → ASIGNADO
                    PedidoEstadoEvent asignadoEvent =
                            new PedidoEstadoEvent(
                                    savedPedido.getId(),
                                    estadoAnterior,
                                    savedPedido.getEstado().name()
                            );

                    log.info("📤 Publicando evento ASIGNADO...");
                    pedidoEventPublisher.publishPedidoEstadoEvent(asignadoEvent);

                    log.info("✅ Repartidor asignado correctamente");

                } else {
                    log.warn("⚠️ No se pudo asignar repartidor: {}",
                            asignacionResponse.getMensaje());
                }

            } catch (Exception e) {
                log.error("❌ Error con Fleet: {}", e.getMessage());
            }
        }

        log.info("🎯 Pedido creado correctamente - ID: {} | Estado: {}",
                savedPedido.getId(), savedPedido.getEstado());

        return pedidoMapper.toResponse(savedPedido);
    }

    @Override
    public PedidoResponse getPedidoById(String id) {
        return null;
    }

    @Override
    public List<PedidoResponse> getAllPedidos() {
        return List.of();
    }

    @Override
    public List<PedidoResponse> getPedidosByCliente(String clienteId) {
        return List.of();
    }

    @Override
    public PedidoResponse patchPedido(String id, PedidoPatchRequest patchRequest) {
        return null;
    }

    @Override
    @Transactional
    public PedidoResponse cancelarPedido(String id) {
        log.info("❌ Cancelando pedido {}", id);

        Pedido pedido = findPedidoOrThrow(id);

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("El pedido ya está cancelado");
        }

        String estadoAnterior = pedido.getEstado().name();

        pedido.setEstado(EstadoPedido.CANCELADO);
        Pedido canceledPedido = pedidoRepository.save(pedido);

        // 🔥 PUBLICAMOS EVENTO: X → CANCELADO
        PedidoEstadoEvent canceladoEvent =
                new PedidoEstadoEvent(
                        canceledPedido.getId(),
                        estadoAnterior,
                        "CANCELADO"
                );

        log.info("📤 Publicando evento CANCELADO...");
        pedidoEventPublisher.publishPedidoEstadoEvent(canceladoEvent);

        log.info("✅ Pedido cancelado con éxito");

        return pedidoMapper.toResponse(canceledPedido);
    }

    @Override
    public void deletePedido(String id) {

    }

    @Override
    public PedidoResponse asignarRepartidorYVehiculo(String pedidoId, String repartidorId, String vehiculoId) {
        return null;
    }

    @Override
    public List<PedidoResponse> getPedidosPendientesAsignacion() {
        return List.of();
    }

    @Override
    public List<PedidoResponse> getPedidosByRepartidor(String repartidorId) {
        return List.of();
    }

    @Override
    public List<PedidoResponse> getPedidosByModalidad(ModalidadServicio modalidad) {
        return List.of();
    }

    @Override
    public PedidoResponse asociarFactura(String pedidoId, String facturaId, Double tarifa) {
        return null;
    }

    @Override
    public List<PedidoResponse> getPedidosSinFactura() {
        return List.of();
    }

    @Override
    public List<PedidoResponse> getPedidosAltaPrioridad() {
        return List.of();
    }

    // ======= MÉTODOS AUXILIARES =======

    private Pedido findPedidoOrThrow(String id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Pedido no encontrado: " + id));
    }

    private void validateCobertura(String cobertura) {
        if (!coberturaValidationService.isValidCobertura(cobertura)) {
            throw new IllegalArgumentException("Cobertura no válida: " + cobertura);
        }
    }

    private void validateTipoEntrega(TipoEntrega tipoEntrega, String cobertura) {
        if (!coberturaValidationService.isTipoEntregaDisponible(tipoEntrega, cobertura)) {
            throw new IllegalArgumentException(
                    "Tipo de entrega no disponible para cobertura: " + cobertura);
        }
    }

    private Double calcularDistanciaEstimada(String origen, String destino, ModalidadServicio modalidad) {
        return switch (modalidad) {
            case URBANA_RAPIDA -> 10.0;
            case INTERMUNICIPAL -> 50.0;
            case NACIONAL -> 200.0;
        };
    }
}