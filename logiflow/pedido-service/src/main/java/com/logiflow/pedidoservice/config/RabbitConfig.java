package com.logiflow.pedidoservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_PEDIDOS = "pedidos.exchange";
    public static final String QUEUE_PEDIDOS = "pedido.estado.actualizado";
    public static final String ROUTING_KEY = "pedido.estado.actualizado";

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(EXCHANGE_PEDIDOS);
    }

    @Bean
    public Queue pedidoQueue() {
        return new Queue(QUEUE_PEDIDOS, true);
    }

    @Bean
    public Binding bindingPedido(Queue pedidoQueue, TopicExchange pedidosExchange) {
        return BindingBuilder
                .bind(pedidoQueue)
                .to(pedidosExchange)
                .with(ROUTING_KEY);
    }
}