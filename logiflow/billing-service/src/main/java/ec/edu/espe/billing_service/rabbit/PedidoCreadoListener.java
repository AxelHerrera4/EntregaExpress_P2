package ec.edu.espe.billing_service.rabbit;

import ec.edu.espe.billing_service.event.PedidoCreadoEvent;
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
public class PedidoCreadoListener {

    private final BillingService billingService;
    
    // Control de idempotencia simple en memoria (en producción usar Redis)
    private final Set<String> processedMessages = new HashSet<>();

    @RabbitListener(queues = "${rabbitmq.queue.pedido-creado}")
    public void escucharPedidoCreado(PedidoCreadoEvent event) {
        
        log.info("=====================================================");
        log.info("📥 [RABBIT-CONSUMER] Evento PEDIDO CREADO recibido");
        log.info("🆔  Message ID     : {}", event.getMessageId());
        log.info("⌚  Timestamp      : {}", event.getTimestamp());
        log.info("📦  Pedido ID      : {}", event.getPedidoId());
        log.info("👤  Cliente ID     : {}", event.getClienteId());
        log.info("🔑  Usuario Creador: {}", event.getUsuarioCreador());
        log.info("📈  Estado         : {}", event.getEstado());
        log.info("🚚  Tipo Entrega   : {}", event.getTipoEntrega());
        log.info("🌍  Modalidad      : {}", event.getModalidadServicio());
        log.info("🏁  Prioridad      : {}", event.getPrioridad());
        log.info("⚖️   Peso           : {} kg", event.getPeso());
        log.info("📍  Origen         : {} ({})", event.getDireccionOrigen(), event.getCiudadOrigen());
        log.info("📍  Destino        : {} ({})", event.getDireccionDestino(), event.getCiudadDestino());
        log.info("📏  Distancia      : {} km", event.getDistanciaEstimadaKm());
        log.info("💰  Tarifa         : {}", event.getTarifaCalculada());
        log.info("=====================================================");

        // Control de idempotencia
        if (processedMessages.contains(event.getMessageId())) {
            log.warn("⚠️ [IDEMPOTENCIA] Mensaje ya procesado, ignorando | MessageID: {} | PedidoID: {}", 
                event.getMessageId(), event.getPedidoId());
            return;
        }

        try {
            log.info("💳 [BILLING-PROCESSING] Iniciando procesamiento de factura | PedidoID: {} | Usuario: {} | MessageID: {}", 
                event.getPedidoId(), event.getUsuarioCreador(), event.getMessageId());
                
            // Procesar la creación de factura para el pedido
            billingService.procesarPedidoCreado(event);
            
            // Marcar como procesado
            processedMessages.add(event.getMessageId());
            
            log.info("✅ [BILLING-SUCCESS] Pedido creado procesado exitosamente | PedidoID: {} | Usuario: {} | MessageID: {}", 
                event.getPedidoId(), event.getUsuarioCreador(), event.getMessageId());
            log.info("🏁 [CORRELACION-BILLING] MessageID={} | PedidoID={} | Usuario={} | Tipo={}", 
                event.getMessageId(), event.getPedidoId(), event.getUsuarioCreador(), event.getTipoEntrega());
            
        } catch (Exception e) {
            log.error("❌ [BILLING-ERROR] Error procesando pedido creado | PedidoID={} | Usuario={} | MessageID={} | Error={}", 
                    event.getPedidoId(), event.getUsuarioCreador(), event.getMessageId(), e.getMessage(), e);
            // En un entorno real, aquí podrías enviar a una cola de errores (DLQ)
            throw e; // Relanzar para que RabbitMQ maneje el retry
        }
    }
}