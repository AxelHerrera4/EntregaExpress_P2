package logiflow.ms_notifications.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String ORDER_EXCHANGE = "pedidos.exchange";
    public static final String TRACKING_EXCHANGE = "exchange-tracking";

    // Queue names
    public static final String ORDER_CREATED_QUEUE = "pedido.creado";
    public static final String ORDER_STATUS_UPDATED_QUEUE = "pedido.estado.actualizado";
    public static final String TRACKING_QUEUE = "tracking.ubicacion";

    // Routing keys
    public static final String ORDER_CREATED_ROUTING_KEY = "pedido.creado";
    public static final String ORDER_STATUS_UPDATED_ROUTING_KEY = "pedido.estado.actualizado";
    public static final String TRACKING_ROUTING_KEY = "repartidor.ubicacion";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange trackingExchange() {
        return new TopicExchange(TRACKING_EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE)
                .build();
    }

    @Bean
    public Queue orderStatusUpdatedQueue() {
        return QueueBuilder.durable(ORDER_STATUS_UPDATED_QUEUE)
                .build();
    }

    @Bean
    public Queue trackingQueue() {
        return QueueBuilder.durable(TRACKING_QUEUE)
                .build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(orderCreatedQueue)
                .to(orderExchange)
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding orderStatusUpdatedBinding(Queue orderStatusUpdatedQueue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(orderStatusUpdatedQueue)
                .to(orderExchange)
                .with(ORDER_STATUS_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding trackingBinding(Queue trackingQueue, TopicExchange trackingExchange) {
        return BindingBuilder
                .bind(trackingQueue)
                .to(trackingExchange)
                .with(TRACKING_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}

