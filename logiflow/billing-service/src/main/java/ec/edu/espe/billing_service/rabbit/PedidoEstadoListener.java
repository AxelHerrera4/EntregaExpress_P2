package ec.edu.espe.billing_service.rabbit;


import ec.edu.espe.billing_service.event.PedidoEstadoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PedidoEstadoListener {

    @RabbitListener(queues = "${rabbitmq.queue.pedido-estado}")
    public void escucharPedidoEstado(PedidoEstadoEvent event) {

        log.info("=====================================================");
        log.info("📥 [RABBIT - BILLING] Evento recibido");
        log.info("➡️ Pedido ID      : {}", event.getPedidoId());
        log.info("➡️ Estado anterior: {}", event.getEstadoAnterior());
        log.info("➡️ Estado nuevo   : {}", event.getEstadoNuevo());
        log.info("=====================================================");

        // Aquí luego integraremos con facturación real
        log.info("💳 (Billing) Procesando evento de cambio de estado...");
    }
}
