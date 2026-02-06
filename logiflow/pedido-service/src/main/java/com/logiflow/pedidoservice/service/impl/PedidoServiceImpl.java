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
    private final PedidoEventPublisher pedidoEventPublisher; 

    @Value("${integration.billing.enabled:true}")
    private boolean billingIntegrationEnabled;

    @Value("${integration.fleet.enabled:true}")
    private boolean fleetIntegrationEnabled;

    private String obtenerTokenActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() != null) {
            return auth.getCredentials().toString();
        }
        log.warn("No se encontró token en el contexto de seguridad");
        return null;
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        log.warn("No se encontró usuario autenticado");
        return "SYSTEM";
    }

    @Override
    @Transactional
    public PedidoResponse createPedido(PedidoRequest request) {
        String correlacionId = java.util.UUID.randomUUID().toString();
        log.info("[INICIO-TRANSACCION] Creando nuevo pedido para cliente: {} | CorrelacionID: {}", 
            request.getClienteId(), correlacionId);

        // 1. Extraer información de contexto de seguridad
        String token = obtenerTokenActual();
        String usuario = obtenerUsuarioActual();
        log.info("[AUTH-CONTEXT] Usuario: {} | Token presente: {} | CorrelacionID: {}", 
            usuario, token != null, correlacionId);

        Pedido pedido = pedidoMapper.toEntity(request);
        validateCobertura(pedido.getCobertura());
        validateTipoEntrega(request.getTipoEntrega(), pedido.getCobertura());

        Pedido savedPedido = pedidoRepository.save(pedido);
        log.info(" Pedido guardado exitosamente - ID: {} | Usuario: {} | CorrelacionID: {}", 
            savedPedido.getId(), usuario, correlacionId);

        // 2. Calcular distancia para el evento
        Double distanciaEstimada = calcularDistanciaEstimada(
            savedPedido.getDireccionOrigen().getCiudad(), 
            savedPedido.getDireccionDestino().getCiudad(), 
            savedPedido.getModalidadServicio()
        );
        log.info(" Distancia estimada: {} km | PedidoID: {} | CorrelacionID: {}", 
            distanciaEstimada, savedPedido.getId(), correlacionId);

        // 3. PUBLICAR EVENTO PEDIDO.CREADO PRIMERO (para billing-service)
        log.info("📤 [EVENT-PREPARATION] Preparando evento pedido.creado | PedidoID: {} | CorrelacionID: {}", 
            savedPedido.getId(), correlacionId);
            
        PedidoCreadoEvent creadoEvent = new PedidoCreadoEvent(
            savedPedido.getId(),
            savedPedido.getClienteId(),
            usuario, // Usuario que creó el pedido
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
        
        log.info("[EVENT-PUBLISH] Publicando evento pedido.creado | MessageID: {} | PedidoID: {} | Usuario: {} | CorrelacionID: {}", 
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
                
                log.info("[BILLING-SUCCESS] Factura creada exitosamente - FacturaID: {} | PedidoID: {} | Monto: {} | CorrelacionID: {}", 
                    facturaResponse.getId(), savedPedido.getId(), facturaResponse.getMontoTotal(), correlacionId);
            } catch (Exception e) {
                log.error("[BILLING-ERROR] Error en integración con Billing Service | PedidoID: {} | Error: {} | CorrelacionID: {}", 
                    savedPedido.getId(), e.getMessage(), correlacionId, e);
            }
        } else {
            log.warn("[BILLING-DISABLED] Integración con Billing deshabilitada | PedidoID: {} | CorrelacionID: {}", 
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

                    log.info("[FLEET-SUCCESS] Repartidor asignado exitosamente - RepartidorID: {} | VehiculoID: {} | PedidoID: {} | CorrelacionID: {}", 
                        asignacionResponse.getRepartidorId(), asignacionResponse.getVehiculoId(), savedPedido.getId(), correlacionId);

                    // PUBLICAR EVENTO ESTADO ACTUALIZADO: CREADO -> ASIGNADO
                    PedidoEstadoEvent asignadoEvent = new PedidoEstadoEvent(
                        savedPedido.getId(), 
                        estadoAnterior, 
                        savedPedido.getEstado().name(), 
                        usuario, // Usuario que modificó (sistema en este caso)
                        savedPedido.getRepartidorId(), 
                        savedPedido.getVehiculoId()
                    );
                    
                    log.info("[EVENT-PUBLISH] Publicando evento pedido.estado.actualizado | MessageID: {} | {}\u2192{} | PedidoID: {} | CorrelacionID: {}", 
                        asignadoEvent.getMessageId(), estadoAnterior, savedPedido.getEstado().name(), savedPedido.getId(), correlacionId);
                    pedidoEventPublisher.publishPedidoEstadoEvent(asignadoEvent);
                    
                } else {
                    log.warn("[FLEET-WARNING] No se pudo asignar repartidor | Estado recibido: {} | PedidoID: {} | CorrelacionID: {}", 
                        asignacionResponse.getEstado(), savedPedido.getId(), correlacionId);
                }
            } catch (Exception e) {
                log.error("[FLEET-ERROR] Error en integración con Fleet Service | PedidoID: {} | Error: {} | CorrelacionID: {}", 
                    savedPedido.getId(), e.getMessage(), correlacionId, e);
            }
        } else {
            log.warn("[FLEET-DISABLED] Integración con Fleet deshabilitada | PedidoID: {} | CorrelacionID: {}", 
                savedPedido.getId(), correlacionId);
        }

        log.info("[COMPLETION] Pedido creado exitosamente | PedidoID: {} | Estado final: {} | CorrelacionID: {}",
            savedPedido.getId(), savedPedido.getEstado(), correlacionId);
        return pedidoMapper.toResponse(savedPedido);
    }

    @Override
    public PedidoResponse getPedidoById(String id) {
        return null;
    }

    @Override
    public List<PedidoResponse> getAllPedidos() {
        log.info("Consultando todos los pedidos en la base de datos");
        List<Pedido> pedidos = pedidoRepository.findAll();
        log.info("Encontrados {} pedidos", pedidos.size());
        return pedidos.stream()
                .map(pedidoMapper::toResponse)
                .toList();
    }

    @Override
    public List<PedidoResponse> getPedidosByCliente(String clienteId) {
        log.info("Consultando pedidos del cliente: {}", clienteId);
        List<Pedido> pedidos = pedidoRepository.findByClienteId(clienteId);
        log.info("Encontrados {} pedidos para cliente {}", pedidos.size(), clienteId);
        return pedidos.stream()
                .map(pedidoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PedidoResponse patchPedido(String id, PedidoPatchRequest patchRequest) {
        String correlacionId = java.util.UUID.randomUUID().toString();
        String usuario = obtenerUsuarioActual();
        log.info("[INICIO-PATCH] Actualizando pedido {} | Usuario: {} | CorrelacionID: {}", id, usuario, correlacionId);

        // 1. Buscar pedido
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));

        String estadoAnterior = pedido.getEstado().name();
        boolean estadoCambio = false;

        // 2. Aplicar cambios parciales
        if (patchRequest.getEstado() != null) {
            EstadoPedido nuevoEstado = patchRequest.getEstado();
            log.info("[PATCH-ESTADO] Cambiando estado: {} \u2192 {} | PedidoID: {} | CorrelacionID: {}",
                estadoAnterior, nuevoEstado, id, correlacionId);
            pedido.setEstado(nuevoEstado);
            estadoCambio = true;
        }

        // 3. Guardar cambios
        Pedido updatedPedido = pedidoRepository.save(pedido);
        log.info("[DATABASE] Pedido actualizado | PedidoID: {} | CorrelacionID: {}", id, correlacionId);

        // 4. Si cambió el estado, publicar evento
        if (estadoCambio) {
            PedidoEstadoEvent estadoEvent = new PedidoEstadoEvent(
                updatedPedido.getId(),
                estadoAnterior,
                updatedPedido.getEstado().name(),
                usuario,
                updatedPedido.getRepartidorId(),
                updatedPedido.getVehiculoId()
            );

            log.info("[EVENT-PUBLISH] Publicando evento cambio estado | MessageID: {} | {}\u2192{} | PedidoID: {} | Usuario: {} | CorrelacionID: {}",
                estadoEvent.getMessageId(), estadoAnterior, updatedPedido.getEstado(), id, usuario, correlacionId);
            pedidoEventPublisher.publishPedidoEstadoEvent(estadoEvent);
        }

        log.info("[PATCH-SUCCESS] Pedido actualizado exitosamente | PedidoID: {} | Usuario: {} | CorrelacionID: {}",
            id, usuario, correlacionId);

        return pedidoMapper.toResponse(updatedPedido);
    }

    @Override
    @Transactional
    public PedidoResponse cancelarPedido(String id) {
        String correlacionId = java.util.UUID.randomUUID().toString();
        String usuario = obtenerUsuarioActual();
        
        log.info(" Iniciando cancelación de pedido | PedidoID: {} | Usuario: {} | CorrelacionID: {}", 
            id, usuario, correlacionId);

        Pedido pedido = findPedidoOrThrow(id);

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            log.warn("Pedido ya está cancelado | PedidoID: {} | CorrelacionID: {}", 
                id, correlacionId);
            throw new IllegalStateException("El pedido ya está cancelado");
        }

        String estadoAnterior = pedido.getEstado().name();
        log.info("Estado actual: {} | PedidoID: {} | CorrelacionID: {}", 
            estadoAnterior, id, correlacionId);

        pedido.setEstado(EstadoPedido.CANCELADO);
        Pedido canceledPedido = pedidoRepository.save(pedido);

        // PUBLICAR EVENTO: X \u2192 CANCELADO
        PedidoEstadoEvent canceladoEvent = new PedidoEstadoEvent(
            canceledPedido.getId(),
            estadoAnterior,
            "CANCELADO",
            usuario,
            canceledPedido.getRepartidorId(),
            canceledPedido.getVehiculoId()
        );

        log.info("Publicando evento cancelación | MessageID: {} | {}→CANCELADO | PedidoID: {} | Usuario: {} | CorrelacionID: {}", 
            canceladoEvent.getMessageId(), estadoAnterior, id, usuario, correlacionId);
        pedidoEventPublisher.publishPedidoEstadoEvent(canceladoEvent);

        log.info("Pedido cancelado exitosamente | PedidoID: {} | Usuario: {} | CorrelacionID: {}", 
            id, usuario, correlacionId);

        return pedidoMapper.toResponse(canceledPedido);
    }

    @Override
    public void deletePedido(String id) {

    }

    @Override
    @Transactional
    public PedidoResponse asignarRepartidorYVehiculo(String pedidoId, String repartidorId, String vehiculoId) {
        String correlacionId = java.util.UUID.randomUUID().toString();
        String usuario = obtenerUsuarioActual();
        log.info("[INICIO-ASIGNACION] Asignando repartidor {} y vehículo {} al pedido {} | Usuario: {} | CorrelacionID: {}",
            repartidorId, vehiculoId, pedidoId, usuario, correlacionId);

        // 1. Buscar pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        String estadoAnterior = pedido.getEstado().name();
        log.info("[ASSIGN-INFO] Estado actual: {} | PedidoID: {} | CorrelacionID: {}",
            estadoAnterior, pedidoId, correlacionId);

        // 2. Actualizar pedido
        pedido.setRepartidorId(repartidorId);
        pedido.setVehiculoId(vehiculoId);
        pedido.setEstado(EstadoPedido.ASIGNADO);
        Pedido updatedPedido = pedidoRepository.save(pedido);
        log.info("[DATABASE] Pedido actualizado - Estado: {} \u2192 ASIGNADO | RepartidorID: {} | VehiculoID: {} | CorrelacionID: {}",
            estadoAnterior, repartidorId, vehiculoId, correlacionId);

        // 3. PUBLICAR EVENTO: X \u2192 ASIGNADO
        PedidoEstadoEvent asignadoEvent = new PedidoEstadoEvent(
            updatedPedido.getId(),
            estadoAnterior,
            "ASIGNADO",
            usuario,
            updatedPedido.getRepartidorId(),
            updatedPedido.getVehiculoId()
        );

        log.info("[EVENT-PUBLISH] Publicando evento asignación | MessageID: {} | {}\u2192ASIGNADO | PedidoID: {} | Repartidor: {} | Vehiculo: {} | Usuario: {} | CorrelacionID: {}",
            asignadoEvent.getMessageId(), estadoAnterior, pedidoId, repartidorId, vehiculoId, usuario, correlacionId);
        pedidoEventPublisher.publishPedidoEstadoEvent(asignadoEvent);

        log.info("[ASSIGN-SUCCESS] Pedido asignado exitosamente | PedidoID: {} | Repartidor: {} | Vehiculo: {} | Usuario: {} | CorrelacionID: {}",
            pedidoId, repartidorId, vehiculoId, usuario, correlacionId);

        return pedidoMapper.toResponse(updatedPedido);
    }

    @Override
    public List<PedidoResponse> getPedidosPendientesAsignacion() {
        log.info("[QUERY] Consultando pedidos pendientes de asignación (PENDIENTE sin repartidor/vehículo)");
        List<Pedido> pedidos = pedidoRepository.findPedidosPendientesAsignacion();
        log.info("[RESULT] Encontrados {} pedidos pendientes de asignación", pedidos.size());
        return pedidos.stream()
                .map(pedidoMapper::toResponse)
                .toList();
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

    @Override
    @Transactional
    public PedidoResponse reintentarAsignacionAutomatica(String pedidoId, String usuarioSolicitante) {
        String correlacionId = java.util.UUID.randomUUID().toString();
        log.info("[REINTENTO-ASIGNACION] Iniciando reintento para pedido={} | Usuario={} | CorrelacionID={}", 
            pedidoId, usuarioSolicitante, correlacionId);

        // 1. Validar que el pedido existe
        Pedido pedido = findPedidoOrThrow(pedidoId);
        
        // 2. Validar que el pedido está en estado PENDIENTE
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            log.warn("[REINTENTO-ASIGNACION] Pedido {} no está en estado PENDIENTE. Estado actual: {}", 
                pedidoId, pedido.getEstado());
            throw new IllegalStateException(
                String.format("El pedido debe estar en estado PENDIENTE para reintentar asignación. Estado actual: %s", 
                    pedido.getEstado()));
        }

        // 3. Construir evento de reintento
        com.logiflow.pedidoservice.event.ReintentarAsignacionEvent evento = 
            com.logiflow.pedidoservice.event.ReintentarAsignacionEvent.builder()
                .messageId(java.util.UUID.randomUUID().toString())
                .timestamp(java.time.LocalDateTime.now())
                .pedidoId(pedido.getId())
                .clienteId(pedido.getClienteId())
                .usuarioSolicitante(usuarioSolicitante)
                .modalidadServicio(pedido.getModalidadServicio().name())
                .tipoEntrega(pedido.getTipoEntrega().name())
                .prioridad(pedido.getPrioridad().name())
                .peso(pedido.getPeso())
                .ciudadOrigen(pedido.getDireccionOrigen().getCiudad())
                .ciudadDestino(pedido.getDireccionDestino().getCiudad())
                .numeroReintento(1) // Aquí podrías implementar un contador de reintentos
                .motivoReintento("SOLICITUD_MANUAL")
                .build();

        // 4. Publicar evento a RabbitMQ
        log.info("[REINTENTO-ASIGNACION] Publicando evento pedido.reintento.asignacion para pedido={}", pedidoId);
        pedidoEventPublisher.publishReintentarAsignacionEvent(evento);

        log.info("[REINTENTO-ASIGNACION] Evento publicado exitosamente. PedidoID={} | MessageID={} | CorrelacionID={}", 
            pedidoId, evento.getMessageId(), correlacionId);

        // 5. Retornar estado actual del pedido
        return pedidoMapper.toResponse(pedido);
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