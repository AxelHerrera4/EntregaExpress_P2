package com.logiflow.pedidoservice.rabbit;

import com.logiflow.pedidoservice.event.PedidoEstadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.pedidos:pedidos.exchange}")
    private String pedidosExchange;

    @Value("${rabbitmq.routing-key.pedido-estado:pedido.estado.actualizado}")
    private String routingKey;

    public void publishPedidoEstadoEvent(PedidoEstadoEvent event) {

        log.info("=====================================================");
        log.info("📤 [RABBIT] Publicando evento de Pedido");
        log.info("➡️  Pedido ID      : {}", event.getPedidoId());
        log.info("➡️  Estado anterior: {}", event.getEstadoAnterior());
        log.info("➡️  Estado nuevo   : {}", event.getEstadoNuevo());
        log.info("➡️  Exchange       : {}", pedidosExchange);
        log.info("➡️  RoutingKey     : {}", routingKey);
        log.info("=====================================================");

        try {
            rabbitTemplate.convertAndSend(
                    pedidosExchange,
                    routingKey,
                    event
            );

            log.info("✅ [RABBIT] Evento enviado con ÉXITO a RabbitMQ");

        } catch (Exception e) {
            log.error("❌ [RABBIT] ERROR enviando evento a RabbitMQ para pedidoId={}",
                    event.getPedidoId(), e);
            throw new RuntimeException("Fallo al publicar evento en RabbitMQ", e);
        }
    }
}