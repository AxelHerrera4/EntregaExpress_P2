package com.logiflow.pedidoservice.rabbit;

import com.logiflow.pedidoservice.event.AsignacionCompletadaEvent;
import com.logiflow.pedidoservice.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Listener para eventos de asignación desde FleetService
 * 
 * Flujo:
 * 1. FleetService completa una asignación (repartidor + vehículo)
 * 2. FleetService publica evento "asignacion.completada" a RabbitMQ
 * 3. Este listener consume el evento
 * 4. PedidoService actualiza el pedido con los IDs asignados y cambia estado a ASIGNADO
 * 
 * Ventajas vs REST:
 * - Sin problemas de autenticación (no requiere JWT)
 * - Desacoplamiento total entre servicios
 * - Resiliente a caídas (mensajes quedan en cola)
 * - Auditoría automática (logs de eventos)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AsignacionEventListener {

    private final PedidoService pedidoService;

    /**
     * Consume eventos de asignación completada desde FleetService
     * Queue: pedido.asignacion.completada
     * Exchange: fleet.exchange
     * Routing Key: asignacion.completada
     */
    @RabbitListener(queues = "${rabbitmq.queue.asignacion-completada}")
    public void handleAsignacionCompletada(AsignacionCompletadaEvent event) {
        try {
            log.info("=== EVENTO RECIBIDO: asignacion.completada ===");
            log.info("MessageID: {} | Timestamp: {}", event.getMessageId(), event.getTimestamp());
            log.info("Pedido: {} | Estado: {}", event.getPedidoId(), event.getEstadoPedido());
            log.info("Repartidor: {} ({})", event.getRepartidorNombre(), event.getRepartidorId());
            log.info("Vehículo: {} ({})", event.getVehiculoPlaca(), event.getVehiculoId());
            log.info("Origen: {} | Motivo: {}", event.getServicioOrigen(), event.getMotivoAsignacion());
            
            log.info("[RABBIT-CONSUMER] Procesando asignación para pedido: {}", event.getPedidoId());
            
            // Convertir String UUIDs a UUID
            UUID pedidoId = UUID.fromString(event.getPedidoId());
            UUID repartidorId = UUID.fromString(event.getRepartidorId());
            UUID vehiculoId = UUID.fromString(event.getVehiculoId());
            
            // Actualizar pedido con la asignación
            pedidoService.asignarRepartidorYVehiculo(
                    pedidoId.toString(),
                    repartidorId.toString(),
                    vehiculoId.toString()
            );
            
            log.info("[CONFIRMACION] Asignación procesada exitosamente - Pedido: {} actualizado a ASIGNADO", 
                    event.getPedidoId());
            log.info("=== EVENTO PROCESADO EXITOSAMENTE ===");
            
        } catch (IllegalArgumentException e) {
            log.error("[ERROR-UUID] Error en formato de UUID: {}", e.getMessage());
            log.error("Pedido: {} | Repartidor: {} | Vehículo: {}", 
                    event.getPedidoId(), event.getRepartidorId(), event.getVehiculoId());
        } catch (Exception e) {
            log.error("[ERROR-PROCESAMIENTO] Error procesando evento de asignación [MessageID: {}]: {}", 
                    event.getMessageId(), e.getMessage(), e);
            log.error("Stack trace completo:", e);
        }
    }
}
