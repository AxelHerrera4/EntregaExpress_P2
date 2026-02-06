package com.logiflow.fleetservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // EXCHANGES
    @Value("${rabbitmq.exchange.pedidos}")
    private String pedidosExchange;

    @Value("${rabbitmq.exchange.fleet}")
    private String fleetExchange;

    // QUEUES - Fleet Service consume eventos de Pedidos
    @Value("${rabbitmq.queue.pedido-creado}")
    private String pedidoCreadoQueue;

    @Value("${rabbitmq.queue.pedido-estado}")
    private String pedidoEstadoQueue;

    @Value("${rabbitmq.queue.pedido-cancelado}")
    private String pedidoCanceladoQueue;

    // ROUTING KEYS
    @Value("${rabbitmq.routing-key.pedido-creado}")
    private String pedidoCreadoRoutingKey;

    @Value("${rabbitmq.routing-key.pedido-estado}")
    private String pedidoEstadoRoutingKey;

    @Value("${rabbitmq.routing-key.pedido-cancelado}")
    private String pedidoCanceladoRoutingKey;

    // ============================================
    // TRACKING SERVICE INTEGRATION
    // ============================================
    @Value("${rabbitmq.exchange.tracking}")
    private String trackingExchange;

    @Value("${rabbitmq.queue.tracking-ubicacion}")
    private String trackingUbicacionQueue;

    @Value("${rabbitmq.routing-key.tracking-ubicacion}")
    private String trackingUbicacionRoutingKey;

    // ============================================
    // EXCHANGE DEFINITIONS
    // ============================================
    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(pedidosExchange);
    }

    /**
     * Exchange para eventos de fleet (publicado por FleetService)
     */
    @Bean
    public TopicExchange fleetExchange() {
        return new TopicExchange(fleetExchange);
    }

    /**
     * Exchange para eventos de tracking (consumido por FleetService)
     * Publicado por TrackingService
     */
    @Bean
    public TopicExchange trackingExchange() {
        return new TopicExchange(trackingExchange);
    }

    // ============================================
    // QUEUE DEFINITIONS - FleetService consume
    // ============================================
    @Bean
    public Queue pedidoCreadoQueue() {
        return new Queue(pedidoCreadoQueue, true);
    }

    @Bean
    public Queue pedidoEstadoActualizadoQueue() {
        return new Queue(pedidoEstadoQueue, true);
    }

    @Bean
    public Queue pedidoCanceladoQueue() {
        return new Queue(pedidoCanceladoQueue, true);
    }

    /**
     * Queue para consumir eventos de ubicación desde TrackingService
     */
    @Bean
    public Queue trackingUbicacionQueue() {
        return new Queue(trackingUbicacionQueue, true);
    }

    // ============================================
    // BINDINGS - Conectar queues con exchanges
    // ============================================
    @Bean
    public Binding bindingPedidoCreado(Queue pedidoCreadoQueue, TopicExchange pedidosExchange) {
        return BindingBuilder
                .bind(pedidoCreadoQueue)
                .to(pedidosExchange)
                .with(pedidoCreadoRoutingKey);
    }

    @Bean
    public Binding bindingPedidoEstado(Queue pedidoEstadoActualizadoQueue, TopicExchange pedidosExchange) {
        return BindingBuilder
                .bind(pedidoEstadoActualizadoQueue)
                .to(pedidosExchange)
                .with(pedidoEstadoRoutingKey);
    }

    @Bean
    public Binding bindingPedidoCancelado(Queue pedidoCanceladoQueue, TopicExchange pedidosExchange) {
        return BindingBuilder
                .bind(pedidoCanceladoQueue)
                .to(pedidosExchange)
                .with(pedidoCanceladoRoutingKey);
    }

    /**
     * Binding para consumir eventos de ubicación de TrackingService
     */
    @Bean
    public Binding bindingTrackingUbicacion(Queue trackingUbicacionQueue, TopicExchange trackingExchange) {
        return BindingBuilder
                .bind(trackingUbicacionQueue)
                .to(trackingExchange)
                .with(trackingUbicacionRoutingKey);
    }

    // ============================================
    // MESSAGE CONVERTER
    // ============================================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
