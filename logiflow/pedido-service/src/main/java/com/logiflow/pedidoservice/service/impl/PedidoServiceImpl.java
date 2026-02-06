package com.logiflow.pedidoservice.service.impl;

import com.logiflow.pedidoservice.client.BillingClient;
import com.logiflow.pedidoservice.client.FleetClient;
import com.logiflow.pedidoservice.dto.*;
import com.logiflow.pedidoservice.event.PedidoCreadoEvent;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private String obtenerTokenActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() != null) {
            return auth.getCredentials().toString();
        }
        log.warn("⚠️ No se encontró token en el contexto de seguridad");
        return null;
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        log.warn("⚠️ No se encontró usuario autenticado");
        return "SYSTEM";
    }

    @Override
    @Transactional

    public PedidoResponse createPedido(PedidoRequest request) {
        String correlacionId = java.util.UUID.randomUUID().toString();
        log.info("🚀 [INICIO-TRANSACCION] Creando nuevo pedido para cliente: {} | CorrelacionID: {}", 
            request.getClienteId(), correlacionId);

        // 1. Extraer información de contexto de seguridad
        String token = obtenerTokenActual();
        String usuario = obtenerUsuarioActual();
        log.info("🔐 [AUTH-CONTEXT] Usuario: {} | Token presente: {} | CorrelacionID: {}", 
            usuario, token != null, correlacionId);

        Pedido pedido = pedidoMapper.toEntity(request);
        validateCobertura(pedido.getCobertura());
        validateTipoEntrega(request.getTipoEntrega(), pedido.getCobertura());

        Pedido savedPedido = pedidoRepository.save(pedido);
        log.info("💾 [DATABASE] Pedido guardado exitosamente - ID: {} | Usuario: {} | CorrelacionID: {}", 
            savedPedido.getId(), usuario, correlacionId);

        // 2. Calcular distancia para el evento
        Double distanciaEstimada = calcularDistanciaEstimada(
            savedPedido.getDireccionOrigen().getCiudad(), 
            savedPedido.getDireccionDestino().getCiudad(), 
            savedPedido.getModalidadServicio()
        );
        log.info("📏 [CALCULO] Distancia estimada: {} km | PedidoID: {} | CorrelacionID: {}", 
            distanciaEstimada, savedPedido.getId(), correlacionId);

        // 3. 🔥 PUBLICAR EVENTO PEDIDO.CREADO PRIMERO (para billing-service)
        log.info("📤 [EVENT-PREPARATION] Preparando evento pedido.creado | PedidoID: {} | CorrelacionID: {}", 
            savedPedido.getId(), correlacionId);
            
        PedidoCreadoEvent creadoEvent = new PedidoCreadoEvent(
            savedPedido.getId(),
            savedPedido.getClienteId(),
            usuario, // 🔑 Usuario que creó el pedido
            savedPedido.getEstado().name(),
            savedPedido.getTipoEntrega().name(),
            savedPedido.getModalidadServicio().name(),
            savedPedido.getPrioridad().name(),
            savedPedido.getPeso(),
            savedPedido.getDireccionOrigen().getCalle() + " " + savedPedido.getDireccionOrigen().getNumero(),
            savedPedido.getDireccionDestino().getCalle() + " " + savedPedido.getDireccionDestino().getNumero(),
            savedPedido.getDireccionOrigen().getCiudad(),
            savedPedido.getDireccionDestino().getCiudad(),
            distanciaEstimada,
            null // tarifaCalculada se calculará después por billing-service
        );
        
        log.info("🎯 [EVENT-PUBLISH] Publicando evento pedido.creado | MessageID: {} | PedidoID: {} | Usuario: {} | CorrelacionID: {}", 
            creadoEvent.getMessageId(), savedPedido.getId(), usuario, correlacionId);
        pedidoEventPublisher.publishPedidoCreadoEvent(creadoEvent);

        // 4. ============= BILLING SERVICE (SINCRONO) =============
        if (billingIntegrationEnabled) {
            try {
                log.info("💳 [BILLING-SYNC] Iniciando integración síncrona con Billing Service | PedidoID: {} | CorrelacionID: {}", 
                    savedPedido.getId(), correlacionId);
                    
                FacturaRequest facturaRequest = FacturaRequest.builder()
                        .pedidoId(savedPedido.getId())
                        .tipoEntrega(savedPedido.getTipoEntrega().name())
                        .distanciaKm(distanciaEstimada)
                        .build();

                log.info("🔗 [BILLING-CALL] Llamando a billing-service | PedidoID: {} | Token presente: {} | CorrelacionID: {}", 
                    savedPedido.getId(), token != null, correlacionId);
                    
                FacturaResponse facturaResponse = billingClient.crearFactura(facturaRequest, token);

                savedPedido.setFacturaId(facturaResponse.getId());
                savedPedido.setTarifaCalculada(facturaResponse.getMontoTotal().doubleValue());
                savedPedido = pedidoRepository.save(savedPedido);
                
                log.info("✅ [BILLING-SUCCESS] Factura creada exitosamente - FacturaID: {} | PedidoID: {} | Monto: {} | CorrelacionID: {}", 
                    facturaResponse.getId(), savedPedido.getId(), facturaResponse.getMontoTotal(), correlacionId);
            } catch (Exception e) {
                log.error("❌ [BILLING-ERROR] Error en integración con Billing Service | PedidoID: {} | Error: {} | CorrelacionID: {}", 
                    savedPedido.getId(), e.getMessage(), correlacionId, e);
            }
        } else {
            log.warn("⚠️ [BILLING-DISABLED] Integración con Billing deshabilitada | PedidoID: {} | CorrelacionID: {}", 
                savedPedido.getId(), correlacionId);
        }

        // 5. ============= FLEET SERVICE (SINCRONO) =============
        if (fleetIntegrationEnabled) {
            try {
                log.info("🚛 [FLEET-SYNC] Iniciando integración síncrona con Fleet Service | PedidoID: {} | CorrelacionID: {}", 
                    savedPedido.getId(), correlacionId);
                    
                AsignacionRequest asignacionRequest = AsignacionRequest.builder()
                        .pedidoId(savedPedido.getId())
                        .modalidadServicio(savedPedido.getModalidadServicio().name())
                        .tipoEntrega(savedPedido.getTipoEntrega().name())
                        .prioridad(savedPedido.getPrioridad().name())
                        .ciudadOrigen(savedPedido.getDireccionOrigen().getCiudad())
                        .ciudadDestino(savedPedido.getDireccionDestino().getCiudad())
                        .peso(savedPedido.getPeso())
                        .build();

                log.info("🔗 [FLEET-CALL] Llamando a fleet-service | PedidoID: {} | Token presente: {} | CorrelacionID: {}", 
                    savedPedido.getId(), token != null, correlacionId);
                    
                AsignacionResponse asignacionResponse = fleetClient.asignarRepartidor(asignacionRequest, token);

                if ("ASIGNADO".equals(asignacionResponse.getEstado())) {
                    String estadoAnterior = savedPedido.getEstado().name();
                    savedPedido.setRepartidorId(asignacionResponse.getRepartidorId());
                    savedPedido.setVehiculoId(asignacionResponse.getVehiculoId());
                    savedPedido.setEstado(EstadoPedido.ASIGNADO);
                    savedPedido = pedidoRepository.save(savedPedido);

                    log.info("✅ [FLEET-SUCCESS] Repartidor asignado exitosamente - RepartidorID: {} | VehiculoID: {} | PedidoID: {} | CorrelacionID: {}", 
                        asignacionResponse.getRepartidorId(), asignacionResponse.getVehiculoId(), savedPedido.getId(), correlacionId);

                    // 📤 PUBLICAR EVENTO ESTADO ACTUALIZADO: CREADO -> ASIGNADO
                    PedidoEstadoEvent asignadoEvent = new PedidoEstadoEvent(
                        savedPedido.getId(), 
                        estadoAnterior, 
                        savedPedido.getEstado().name(), 
                        usuario, // 🔑 Usuario que modificó (sistema en este caso)
                        savedPedido.getRepartidorId(), 
                        savedPedido.getVehiculoId()
                    );
                    
                    log.info("🎯 [EVENT-PUBLISH] Publicando evento pedido.estado.actualizado | MessageID: {} | {}→{} | PedidoID: {} | CorrelacionID: {}", 
                        asignadoEvent.getMessageId(), estadoAnterior, savedPedido.getEstado().name(), savedPedido.getId(), correlacionId);
                    pedidoEventPublisher.publishPedidoEstadoEvent(asignadoEvent);
                    
                } else {
                    log.warn("⚠️ [FLEET-WARNING] No se pudo asignar repartidor | Estado recibido: {} | PedidoID: {} | CorrelacionID: {}", 
                        asignacionResponse.getEstado(), savedPedido.getId(), correlacionId);
                }
            } catch (Exception e) {
                log.error("❌ [FLEET-ERROR] Error en integración con Fleet Service | PedidoID: {} | Error: {} | CorrelacionID: {}", 
                    savedPedido.getId(), e.getMessage(), correlacionId, e);
            }
        } else {
            log.warn("⚠️ [FLEET-DISABLED] Integración con Fleet deshabilitada | PedidoID: {} | CorrelacionID: {}", 
                savedPedido.getId(), correlacionId);
        }

        log.info("🏁 [COMPLETION] Pedido creado exitosamente | PedidoID: {} | Estado final: {} | CorrelacionID: {}", 
            savedPedido.getId(), savedPedido.getEstado(), correlacionId);
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
        String correlacionId = java.util.UUID.randomUUID().toString();
        String usuario = obtenerUsuarioActual();
        
        log.info("🚫 [CANCEL-START] Iniciando cancelación de pedido | PedidoID: {} | Usuario: {} | CorrelacionID: {}", 
            id, usuario, correlacionId);

        Pedido pedido = findPedidoOrThrow(id);

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            log.warn("⚠️ [CANCEL-WARNING] Pedido ya está cancelado | PedidoID: {} | CorrelacionID: {}", 
                id, correlacionId);
            throw new IllegalStateException("El pedido ya está cancelado");
        }

        String estadoAnterior = pedido.getEstado().name();
        log.info("📊 [CANCEL-INFO] Estado actual: {} | PedidoID: {} | CorrelacionID: {}", 
            estadoAnterior, id, correlacionId);

        pedido.setEstado(EstadoPedido.CANCELADO);
        Pedido canceledPedido = pedidoRepository.save(pedido);

        // 🔥 PUBLICAR EVENTO: X → CANCELADO
        PedidoEstadoEvent canceladoEvent = new PedidoEstadoEvent(
            canceledPedido.getId(),
            estadoAnterior,
            "CANCELADO",
            usuario, // 🔑 Usuario que canceló
            canceledPedido.getRepartidorId(),
            canceledPedido.getVehiculoId()
        );

        log.info("🎯 [EVENT-PUBLISH] Publicando evento cancelación | MessageID: {} | {}→CANCELADO | PedidoID: {} | Usuario: {} | CorrelacionID: {}", 
            canceladoEvent.getMessageId(), estadoAnterior, id, usuario, correlacionId);
        pedidoEventPublisher.publishPedidoEstadoEvent(canceladoEvent);

        log.info("✅ [CANCEL-SUCCESS] Pedido cancelado exitosamente | PedidoID: {} | Usuario: {} | CorrelacionID: {}", 
            id, usuario, correlacionId);

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