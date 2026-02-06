package com.logiflow.fleetservice.service.messaging;

import com.logiflow.fleetservice.dto.request.AsignacionRequest;
import com.logiflow.fleetservice.dto.response.AsignacionResponse;
import com.logiflow.fleetservice.event.AsignacionCompletadaEvent;
import com.logiflow.fleetservice.event.PedidoCreadoEvent;
import com.logiflow.fleetservice.event.PedidoEstadoActualizadoEvent;
import com.logiflow.fleetservice.service.AsignacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Listener para eventos de dominio de PedidoService.
 * 
 * NOTA IMPORTANTE SOBRE CANCELACIONES:
 * PedidoService NO tiene un routing key separado para cancelaciones.
 * Todas las cancelaciones llegan como un cambio de estado (estadoNuevo = "CANCELADO")
 * a través del routing key "pedido.estado.actualizado".
 * Por eso FleetService NO tiene una queue "fleet.pedido.cancelado" separada.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PedidoEventListener {

    private final AsignacionService asignacionService;
    private final FleetEventPublisher fleetEventPublisher;

    /**
     * Consume evento cuando se crea un nuevo pedido.
     * FleetService inicia el proceso de asignación de vehículo/repartidor.
     */
    @RabbitListener(queues = "${rabbitmq.queue.pedido-creado}")
    public void handlePedidoCreado(PedidoCreadoEvent event) {
        try {
            log.info("=== EVENTO RECIBIDO: pedido.creado ===");
            log.info("MessageID: {} | Timestamp: {}", event.getMessageId(), event.getTimestamp());
            log.info("Pedido: {} | Cliente: {} | Usuario: {}", 
                    event.getPedidoId(), event.getClienteId(), event.getUsuarioCreador());
            log.info("Estado: {} | Tipo: {} | Modalidad: {}", 
                    event.getEstado(), event.getTipoEntrega(), event.getModalidadServicio());
            log.info("Peso: {}kg | Prioridad: {} | Origen: {}", 
                    event.getPeso(), event.getPrioridad(), event.getCiudadOrigen());
            
            // ASIGNACIÓN AUTOMÁTICA
            log.info("[ASIGNACION-AUTO] Iniciando asignación automática para pedido: {}", event.getPedidoId());
            
            AsignacionRequest request = AsignacionRequest.builder()
                    .pedidoId(event.getPedidoId())
                    .modalidadServicio(event.getModalidadServicio())
                    .tipoEntrega(event.getTipoEntrega())
                    .prioridad(event.getPrioridad())
                    .ciudadOrigen(event.getCiudadOrigen())
                    .ciudadDestino(event.getCiudadDestino())
                    .peso(event.getPeso())
                    .build();
            
            AsignacionResponse asignacion = asignacionService.asignarRepartidorYVehiculo(request);
            
            if ("ASIGNADO".equals(asignacion.getEstado())) {
                log.info("[ASIGNACION-AUTO] Recursos encontrados - Repartidor: {} | Vehículo: {}", 
                        asignacion.getRepartidorNombre(), asignacion.getVehiculoPlaca());
                
                // PUBLICAR EVENTO EN LUGAR DE LLAMAR REST
                log.info("[EVENT-PUBLISH] Publicando evento de asignación completada a RabbitMQ");
                
                AsignacionCompletadaEvent asignacionEvent = AsignacionCompletadaEvent.builder()
                        .messageId(UUID.randomUUID().toString())
                        .timestamp(LocalDateTime.now())
                        .pedidoId(event.getPedidoId())
                        .repartidorId(asignacion.getRepartidorId().toString())
                        .vehiculoId(asignacion.getVehiculoId().toString())
                        .repartidorNombre(asignacion.getRepartidorNombre())
                        .vehiculoPlaca(asignacion.getVehiculoPlaca())
                        .estadoPedido("ASIGNADO")
                        .servicioOrigen("FLEET_SERVICE")
                        .motivoAsignacion("ASIGNACION_AUTOMATICA")
                        .build();
                
                try {
                    fleetEventPublisher.publishAsignacionCompletada(asignacionEvent);
                    log.info("[CONFIRMACION] Evento de asignación publicado exitosamente - Pedido: {}", event.getPedidoId());
                } catch (Exception publishEx) {
                    log.error("[EVENT-ERROR] Error publicando evento de asignación: {}", publishEx.getMessage());
                    // Revertir asignación en FleetService
                    asignacionService.liberarAsignacion(event.getPedidoId());
                }
            } else {
                log.warn("[ASIGNACION-AUTO] No se encontraron recursos disponibles - Pedido: {} | Motivo: {}",
                        event.getPedidoId(), asignacion.getMensaje());
            }
            
        } catch (Exception e) {
            log.error("[ERROR] Error procesando pedido.creado [MessageID: {}]: {}", 
                    event.getMessageId(), e.getMessage(), e);
        }
    }

    /**
     * Consume evento cuando se actualiza el estado de un pedido.
     * Esto incluye TODOS los cambios de estado, incluyendo cancelaciones.
     * 
     * PedidoService publica PedidoEstadoEvent con:
     *   - estadoNuevo = "ASIGNADO" → confirmar asignación
     *   - estadoNuevo = "EN_CAMINO" → repartidor en ruta
     *   - estadoNuevo = "ENTREGADO" → liberar recursos
     *   - estadoNuevo = "CANCELADO" → liberar recursos (este es el caso de cancelación)
     */
    @RabbitListener(queues = "${rabbitmq.queue.pedido-estado}")
    public void handlePedidoEstadoActualizado(PedidoEstadoActualizadoEvent event) {
        try {
            log.info("EVENTO RECIBIDO: pedido.estado.actualizado");
            log.info("MessageID: {} | Timestamp: {}", event.getMessageId(), event.getTimestamp());
            log.info("Pedido: {} | Usuario: {}", event.getPedidoId(), event.getUsuarioModificador());
            log.info("Estado: {} → {}", event.getEstadoAnterior(), event.getEstadoNuevo());

            String nuevoEstado = event.getEstadoNuevo();

            // Despachar lógica según el nuevo estado
            if ("CANCELADO".equalsIgnoreCase(nuevoEstado)) {
                handleCancelacion(event);
            } else if ("ENTREGADO".equalsIgnoreCase(nuevoEstado)) {
                handleEntrega(event);
            } else if ("ASIGNADO".equalsIgnoreCase(nuevoEstado)) {
                handleAsignacion(event);
            } else {
                log.info("Estado {} registrado para pedido {} (sin acción de fleet requerida)",
                        nuevoEstado, event.getPedidoId());
            }

        } catch (Exception e) {
            log.error("Error procesando pedido.estado.actualizado [MessageID: {}]: {}", 
                    event.getMessageId(), e.getMessage(), e);
        }
    }

    /**
     * Lógica cuando un pedido es CANCELADO.
     * Liberar repartidor y vehículo asignados para que queden disponibles.
     */
    private void handleCancelacion(PedidoEstadoActualizadoEvent event) {
        log.info("Pedido {} CANCELADO (antes: {})", event.getPedidoId(), event.getEstadoAnterior());

        if (event.getRepartidorId() != null) {
            log.info("Liberando repartidor {} del pedido cancelado {}",
                    event.getRepartidorId(), event.getPedidoId());
            // TODO: repartidorService.liberarRepartidor(event.getRepartidorId())
        }

        if (event.getVehiculoId() != null) {
            log.info("Liberando vehículo {} del pedido cancelado {}",
                    event.getVehiculoId(), event.getPedidoId());
            // TODO: vehiculoService.liberarVehiculo(event.getVehiculoId())
        }
    }

    /**
     * Lógica cuando un pedido es ENTREGADO.
     * Liberar repartidor y vehículo para que puedan tomar nuevos pedidos.
     */
    private void handleEntrega(PedidoEstadoActualizadoEvent event) {
        log.info("Pedido {} ENTREGADO", event.getPedidoId());

        if (event.getRepartidorId() != null) {
            log.info("Liberando repartidor {} tras entrega de pedido {}",
                    event.getRepartidorId(), event.getPedidoId());
            // TODO: repartidorService.liberarRepartidor(event.getRepartidorId())
        }

        if (event.getVehiculoId() != null) {
            log.info("Liberando vehículo {} tras entrega de pedido {}",
                    event.getVehiculoId(), event.getPedidoId());
            // TODO: vehiculoService.liberarVehiculo(event.getVehiculoId())
        }
    }

    /**
     * Lógica cuando un pedido es ASIGNADO.
     * Confirmar que FleetService reconoce la asignación que él mismo propuso.
     */
    private void handleAsignacion(PedidoEstadoActualizadoEvent event) {
        log.info("Pedido {} ASIGNADO", event.getPedidoId());

        if (event.getRepartidorId() != null) {
            log.info("Repartidor {} confirmado en pedido {}",
                    event.getRepartidorId(), event.getPedidoId());
        }
        if (event.getVehiculoId() != null) {
            log.info("Vehículo {} confirmado en pedido {}",
                    event.getVehiculoId(), event.getPedidoId());
        }
    }

    /**
     * Maneja eventos de REINTENTO DE ASIGNACIÓN desde PedidoService.
     * Se publica cuando un pedido PENDIENTE necesita reintentar asignación automática
     * (ej: después de crear nuevos repartidores/vehículos).
     * 
     * Flujo:
     * 1. PedidoService recibe POST /api/pedidos/{id}/reintentar-asignacion
     * 2. Publica pedido.reintento.asignacion a RabbitMQ
     * 3. FleetService escucha en cola fleet.pedido.reintento
     * 4. Intenta asignar repartidor/vehículo como lo haría con pedido.creado
     * 5. Si exitoso, publica asignacion.completada
     * 6. PedidoService actualiza pedido a ASIGNADO
     */
    @RabbitListener(queues = "${rabbitmq.queue.pedido-reintento}")
    public void handleReintentoAsignacion(com.logiflow.fleetservice.event.ReintentarAsignacionEvent event) {
        try {
            log.info("=============================================================");
            log.info("=== EVENTO RECIBIDO: pedido.reintento.asignacion ===");
            log.info("=============================================================");
            log.info("MessageID          : {}", event.getMessageId());
            log.info("Timestamp          : {}", event.getTimestamp());
            log.info("Pedido ID          : {}", event.getPedidoId());
            log.info("Cliente            : {}", event.getClienteId());
            log.info("Usuario Solicitante: {}", event.getUsuarioSolicitante());
            log.info("Modalidad          : {}", event.getModalidadServicio());
            log.info("Tipo Entrega       : {}", event.getTipoEntrega());
            log.info("Prioridad          : {}", event.getPrioridad());
            log.info("Peso               : {} kg", event.getPeso());
            log.info("Origen             : {}", event.getCiudadOrigen());
            log.info("Destino            : {}", event.getCiudadDestino());
            log.info("Reintento #        : {}", event.getNumeroReintento());
            log.info("Motivo             : {}", event.getMotivoReintento());
            log.info("=============================================================");

            // Construir request de asignación igual que con pedido.creado
            AsignacionRequest asignacionRequest = AsignacionRequest.builder()
                    .pedidoId(event.getPedidoId())
                    .modalidadServicio(event.getModalidadServicio())
                    .tipoEntrega(event.getTipoEntrega())
                    .prioridad(event.getPrioridad())
                    .peso(event.getPeso())
                    .ciudadOrigen(event.getCiudadOrigen())
                    .ciudadDestino(event.getCiudadDestino())
                    .build();

            log.info("[REINTENTO-ASIGNACION] Iniciando proceso de asignación automática para pedido: {}", 
                event.getPedidoId());

            // Llamar al servicio de asignación
            AsignacionResponse asignacion = asignacionService.asignarRepartidorYVehiculo(asignacionRequest);

            // Si se asignó exitosamente, publicar evento de asignación completada
            if ("ASIGNADO".equalsIgnoreCase(asignacion.getEstado())) {
                log.info("[REINTENTO-ASIGNACION] Asignación EXITOSA - Pedido: {} | Repartidor: {} | Vehiculo: {}",
                        event.getPedidoId(), 
                        asignacion.getRepartidorId(),
                        asignacion.getVehiculoId());

                // Construir evento de asignación completada
                AsignacionCompletadaEvent asignacionEvent = AsignacionCompletadaEvent.builder()
                        .messageId(java.util.UUID.randomUUID().toString())
                        .timestamp(java.time.LocalDateTime.now())
                        .pedidoId(event.getPedidoId())
                        .repartidorId(asignacion.getRepartidorId())
                        .vehiculoId(asignacion.getVehiculoId())
                        .repartidorNombre(asignacion.getRepartidorNombre())
                        .vehiculoPlaca(asignacion.getVehiculoPlaca())
                        .estadoPedido("ASIGNADO")
                        .servicioOrigen("FLEET_SERVICE")
                        .motivoAsignacion("REINTENTO_MANUAL")
                        .build();

                // Publicar evento a RabbitMQ
                fleetEventPublisher.publishAsignacionCompletada(asignacionEvent);

                log.info("[REINTENTO-ASIGNACION] Evento asignacion.completada publicado exitosamente");
                
            } else {
                log.warn("[REINTENTO-ASIGNACION] No se pudo asignar - Pedido: {} | Motivo: {}",
                        event.getPedidoId(), asignacion.getMensaje());
                log.info("[REINTENTO-ASIGNACION] El pedido {} permanece en estado PENDIENTE", 
                    event.getPedidoId());
            }

        } catch (Exception e) {
            log.error("[REINTENTO-ASIGNACION] ❌ Error procesando reintento para pedido {} [MessageID: {}]: {}", 
                    event.getPedidoId(), event.getMessageId(), e.getMessage(), e);
            log.error("[ERROR-DETAILS] Usuario: {} | Reintento#: {} | Motivo: {}", 
                event.getUsuarioSolicitante(), event.getNumeroReintento(), event.getMotivoReintento());
        }
    }
}
