package com.logiflow.fleetservice.service.messaging;

import com.logiflow.fleetservice.event.PedidoCanceladoEvent;
import com.logiflow.fleetservice.event.PedidoCreadoEvent;
import com.logiflow.fleetservice.event.PedidoEstadoActualizadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Listener para eventos de dominio de PedidoService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PedidoEventListener {

    /**
     * Consume evento cuando se crea un nuevo pedido
     * FleetService puede iniciar proceso de asignación de vehículo/repartidor
     */
    @RabbitListener(queues = "${rabbitmq.queue.pedido-creado}")
    public void handlePedidoCreado(PedidoCreadoEvent event) {
        try {
            log.info("Evento recibido: Pedido creado - ID: {}, Peso: {}kg, Origen: {}, Destino: {}", 
                    event.getPedidoId(), event.getPeso(), event.getOrigen(), event.getDestino());
            
            // TODO: Implementar lógica de negocio
            // 1. Determinar tipo de vehículo según peso
            // 2. Buscar vehículo disponible
            // 3. Buscar repartidor disponible en zona
            // 4. Crear pre-asignación (reserva de recursos)
            
            log.info("Pedido {} procesado por FleetService", event.getPedidoId());
            
        } catch (Exception e) {
            log.error("Error al procesar evento PedidoCreado: {}", e.getMessage(), e);
            // TODO: Implementar estrategia de retry o DLQ (Dead Letter Queue)
        }
    }

    /**
     * Consume evento cuando se actualiza el estado de un pedido
     * FleetService puede confirmar asignación, liberar recursos, etc.
     */
    @RabbitListener(queues = "${rabbitmq.queue.pedido-estado}")
    public void handlePedidoEstadoActualizado(PedidoEstadoActualizadoEvent event) {
        try {
            log.info("Evento recibido: Estado de pedido actualizado - ID: {}, Estado anterior: {}, Estado nuevo: {}", 
                    event.getPedidoId(), event.getEstadoAnterior(), event.getEstadoNuevo());
            
            // TODO: Implementar lógica según estado
            // - Si pasa a EN_CAMINO: confirmar asignación de repartidor
            // - Si pasa a ENTREGADO: liberar repartidor y vehículo
            // - Si pasa a CANCELADO: liberar recursos
            
            if (event.getRepartidorId() != null) {
                log.info("Repartidor {} asignado al pedido {}", 
                        event.getRepartidorId(), event.getPedidoId());
            }
            
            if (event.getVehiculoId() != null) {
                log.info("Vehículo {} asignado al pedido {}", 
                        event.getVehiculoId(), event.getPedidoId());
            }
            
        } catch (Exception e) {
            log.error("Error al procesar evento PedidoEstadoActualizado: {}", e.getMessage(), e);
        }
    }

    /**
     * Consume evento cuando se cancela un pedido
     * FleetService debe liberar recursos asignados
     */
    @RabbitListener(queues = "${rabbitmq.queue.pedido-cancelado}")
    public void handlePedidoCancelado(PedidoCanceladoEvent event) {
        try {
            log.info("Evento recibido: Pedido cancelado - ID: {}, Motivo: {}", 
                    event.getPedidoId(), event.getMotivo());
            
            // TODO: Implementar lógica de liberación de recursos
            if (event.getRepartidorId() != null) {
                log.info("Liberando repartidor {} del pedido cancelado {}", 
                        event.getRepartidorId(), event.getPedidoId());
                // Cambiar estado del repartidor a DISPONIBLE
            }
            
            if (event.getVehiculoId() != null) {
                log.info("Liberando vehículo {} del pedido cancelado {}", 
                        event.getVehiculoId(), event.getPedidoId());
                // Cambiar estado del vehículo a ACTIVO
            }
            
        } catch (Exception e) {
            log.error("Error al procesar evento PedidoCancelado: {}", e.getMessage(), e);
        }
    }
}
