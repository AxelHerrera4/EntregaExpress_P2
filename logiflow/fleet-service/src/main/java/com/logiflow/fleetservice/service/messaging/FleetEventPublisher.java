package com.logiflow.fleetservice.service.messaging;

import com.logiflow.fleetservice.event.AsignacionCompletadaEvent;
import com.logiflow.fleetservice.event.RepartidorUbicacionActualizadaEvent;
import com.logiflow.fleetservice.event.VehiculoEstadoActualizadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio para publicar eventos de dominio del FleetService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FleetEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.fleet}")
    private String fleetExchange;

    @Value("${rabbitmq.routing-key.vehiculo-estado}")
    private String vehiculoEstadoRoutingKey;

    @Value("${rabbitmq.routing-key.repartidor-ubicacion}")
    private String repartidorUbicacionRoutingKey;

    @Value("${rabbitmq.routing-key.asignacion-completada}")
    private String asignacionCompletadaRoutingKey;

    /**
     * Publica evento cuando cambia el estado de un vehículo
     */
    public void publishVehiculoEstadoActualizado(VehiculoEstadoActualizadoEvent event) {
        try {
            log.info("Publicando evento: Vehículo {} cambió estado de {} a {}", 
                    event.getVehiculoId(), event.getEstadoAnterior(), event.getEstadoNuevo());
            
            rabbitTemplate.convertAndSend(
                    fleetExchange, 
                    vehiculoEstadoRoutingKey, 
                    event
            );
            
            log.debug("Evento publicado exitosamente en exchange: {} con routing key: {}", 
                    fleetExchange, vehiculoEstadoRoutingKey);
        } catch (Exception e) {
            log.error("Error al publicar evento VehiculoEstadoActualizado: {}", e.getMessage(), e);
        }
    }

    /**
     * Publica evento cuando se actualiza la ubicación de un repartidor
     */
    public void publishRepartidorUbicacionActualizada(RepartidorUbicacionActualizadaEvent event) {
        try {
            log.info("Publicando evento: Ubicación de repartidor {} actualizada a [{}, {}]", 
                    event.getRepartidorId(), event.getLatitud(), event.getLongitud());
            
            rabbitTemplate.convertAndSend(
                    fleetExchange, 
                    repartidorUbicacionRoutingKey, 
                    event
            );
            
            log.debug("Evento publicado exitosamente en exchange: {} con routing key: {}", 
                    fleetExchange, repartidorUbicacionRoutingKey);
        } catch (Exception e) {
            log.error("Error al publicar evento RepartidorUbicacionActualizada: {}", e.getMessage(), e);
        }
    }

    /**
     * Publica evento cuando FleetService completa una asignación
     * Este evento será consumido por PedidoService para actualizar el estado
     */
    public void publishAsignacionCompletada(AsignacionCompletadaEvent event) {
        try {
            log.info("=== PUBLICANDO EVENTO: asignacion.completada ===");
            log.info("MessageID: {} | Timestamp: {}", event.getMessageId(), event.getTimestamp());
            log.info("Pedido: {} | Repartidor: {} | Vehículo: {}", 
                    event.getPedidoId(), event.getRepartidorNombre(), event.getVehiculoPlaca());
            log.info("Estado: {} | Motivo: {}", event.getEstadoPedido(), event.getMotivoAsignacion());
            
            rabbitTemplate.convertAndSend(
                    fleetExchange, 
                    asignacionCompletadaRoutingKey, 
                    event
            );
            
            log.info("[RABBIT-PRODUCER] Evento publicado en exchange: {} con routing key: {}", 
                    fleetExchange, asignacionCompletadaRoutingKey);
        } catch (Exception e) {
            log.error("[ERROR-RABBIT] Error al publicar evento AsignacionCompletada: {}", e.getMessage(), e);
        }
    }
}
