package ec.edu.espe.billing_service.rabbit;

import ec.edu.espe.billing_service.event.PedidoEstadoEvent;
import ec.edu.espe.billing_service.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoEstadoListener {

    private final BillingService billingService;
    
    // Control de idempotencia simple en memoria (en producción usar Redis)
    private final Set<String> processedMessages = new HashSet<>();

    @RabbitListener(queues = "${rabbitmq.queue.pedido-estado}")
    public void escucharPedidoEstado(PedidoEstadoEvent event) {

        log.info("=====================================================");
        log.info("📥 [RABBIT-CONSUMER] Evento ESTADO ACTUALIZADO recibido");
        log.info("🆔  Message ID     : {}", event.getMessageId());
        log.info("⌚  Timestamp      : {}", event.getTimestamp());
        log.info("📦  Pedido ID      : {}", event.getPedidoId());
        log.info("🔄  Estado anterior: {}", event.getEstadoAnterior());
        log.info("🆕  Estado nuevo   : {}", event.getEstadoNuevo());
        log.info("🔑  Usuario        : {}", event.getUsuarioModificador());
        log.info("🚚  Repartidor ID  : {}", event.getRepartidorId());
        log.info("🚗  Vehículo ID    : {}", event.getVehiculoId());
        log.info("=====================================================");

        // Control de idempotencia
        if (processedMessages.contains(event.getMessageId())) {
            log.warn("⚠️ [IDEMPOTENCIA] Mensaje ya procesado, ignorando | MessageID: {} | PedidoID: {} | Cambio: {}\u2192{}", 
                event.getMessageId(), event.getPedidoId(), event.getEstadoAnterior(), event.getEstadoNuevo());
            return;
        }

        try {
            log.info("💳 [BILLING-PROCESSING] Iniciando procesamiento de estado actualizado | PedidoID: {} | {}\u2192{} | Usuario: {} | MessageID: {}", 
                event.getPedidoId(), event.getEstadoAnterior(), event.getEstadoNuevo(), event.getUsuarioModificador(), event.getMessageId());
                
            // Procesar la actualización de estado para facturación
            billingService.procesarEstadoActualizado(event);
            
            // Marcar como procesado
            processedMessages.add(event.getMessageId());
            
            log.info("✅ [BILLING-SUCCESS] Estado actualizado procesado exitosamente | PedidoID: {} | {}\u2192{} | Usuario: {} | MessageID: {}", 
                event.getPedidoId(), event.getEstadoAnterior(), event.getEstadoNuevo(), event.getUsuarioModificador(), event.getMessageId());
            log.info("🏁 [CORRELACION-BILLING] MessageID={} | PedidoID={} | Usuario={} | CambioEstado={}\u2192{}", 
                event.getMessageId(), event.getPedidoId(), event.getUsuarioModificador(), event.getEstadoAnterior(), event.getEstadoNuevo());
            
        } catch (Exception e) {
            log.error("❌ [BILLING-ERROR] Error procesando estado actualizado | PedidoID={} | {}\u2192{} | Usuario={} | MessageID={} | Error={}", 
                    event.getPedidoId(), event.getEstadoAnterior(), event.getEstadoNuevo(), event.getUsuarioModificador(), event.getMessageId(), e.getMessage(), e);
            throw e; // Relanzar para que RabbitMQ maneje el retry
        }
    }
}
