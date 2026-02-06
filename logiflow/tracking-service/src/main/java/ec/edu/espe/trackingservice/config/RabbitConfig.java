package ec.edu.espe.trackingservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "exchange-tracking";
    public static final String QUEUE_NAME = "tracking.ubicacion";
    public static final String ROUTING_KEY = "repartidor.ubicacion";

    @Bean
    public Queue trackingQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange trackingExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue trackingQueue, TopicExchange trackingExchange) {
        return BindingBuilder
                .bind(trackingQueue)
                .to(trackingExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

