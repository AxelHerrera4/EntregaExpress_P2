package com.logiflow.fleetservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ============================================
    // EXCHANGES
    // ============================================
    @Value("${rabbitmq.exchange.pedidos}")
    private String pedidosExchange;

    @Value("${rabbitmq.exchange.fleet}")
    private String fleetExchange;

    @Value("${rabbitmq.exchange.tracking}")
    private String trackingExchange;

    // ============================================
    // QUEUES
    // ============================================
    @Value("${rabbitmq.queue.pedido-creado}")
    private String pedidoCreadoQueue;

    @Value("${rabbitmq.queue.pedido-estado}")
    private String pedidoEstadoQueue;

    @Value("${rabbitmq.queue.tracking-ubicacion}")
    private String trackingUbicacionQueue;

    @Value("${rabbitmq.queue.pedido-reintento}")
    private String pedidoReintentoQueue;

    // ============================================
    // ROUTING KEYS
    // ============================================
    @Value("${rabbitmq.routing-key.pedido-creado}")
    private String pedidoCreadoRoutingKey;

    @Value("${rabbitmq.routing-key.pedido-estado}")
    private String pedidoEstadoRoutingKey;

    @Value("${rabbitmq.routing-key.tracking-ubicacion}")
    private String trackingUbicacionRoutingKey;

    @Value("${rabbitmq.routing-key.pedido-reintento}")
    private String pedidoReintentoRoutingKey;

    // ============================================
    // EXCHANGE DEFINITIONS
    // ============================================

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(pedidosExchange);
    }

    @Bean
    public TopicExchange fleetExchange() {
        return new TopicExchange(fleetExchange);
    }

    @Bean
    public TopicExchange trackingExchange() {
        return new TopicExchange(trackingExchange);
    }

    // ============================================
    // QUEUE DEFINITIONS
    // ============================================

    /** Cola para eventos pedido.creado desde PedidoService */
    @Bean
    public Queue pedidoCreadoQueue() {
        return new Queue(pedidoCreadoQueue, true);
    }

    /**
     * Cola para eventos pedido.estado.actualizado desde PedidoService.
     * NOTA: Las cancelaciones también llegan aquí (estadoNuevo = "CANCELADO"),
     * ya que PedidoService NO tiene routing key separado para cancelaciones.
     */
    @Bean
    public Queue pedidoEstadoActualizadoQueue() {
        return new Queue(pedidoEstadoQueue, true);
    }

    /** Cola para eventos de ubicación GPS desde TrackingService */
    @Bean
    public Queue trackingUbicacionQueue() {
        return new Queue(trackingUbicacionQueue, true);
    }

    /** Cola para eventos de reintento de asignación desde PedidoService */
    @Bean
    public Queue pedidoReintentoQueue() {
        return new Queue(pedidoReintentoQueue, true);
    }

    // ============================================
    // BINDINGS
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
    public Binding bindingTrackingUbicacion(Queue trackingUbicacionQueue, TopicExchange trackingExchange) {
        return BindingBuilder
                .bind(trackingUbicacionQueue)
                .to(trackingExchange)
                .with(trackingUbicacionRoutingKey);
    }

    @Bean
    public Binding bindingPedidoReintento(Queue pedidoReintentoQueue, TopicExchange pedidosExchange) {
        return BindingBuilder
                .bind(pedidoReintentoQueue)
                .to(pedidosExchange)
                .with(pedidoReintentoRoutingKey);
    }

    // ============================================
    // MESSAGE CONVERTER
    // ============================================

    /**
     * Configura Jackson con TypePrecedence.INFERRED para que al deserializar
     * use el tipo del parámetro del @RabbitListener en vez del header __TypeId__.
     * Esto es crítico porque PedidoService envía __TypeId__ con sus propios
     * FQCNs (e.g. com.logiflow.pedidoservice.rabbit.events.PedidoCreadoEvent)
     * que no existen en el classpath de FleetService.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(DefaultJackson2JavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.addTrustedPackages("com.logiflow.*");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
