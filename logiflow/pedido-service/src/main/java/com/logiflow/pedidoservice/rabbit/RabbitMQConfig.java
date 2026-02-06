package com.logiflow.pedidoservice.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    @Value("${rabbitmq.exchange.pedidos:pedidos.exchange}")
    private String pedidosExchange;

    @Value("${rabbitmq.queue.pedido-creado:pedido.creado}")
    private String pedidoCreadoQueue;

    @Value("${rabbitmq.queue.pedido-estado:pedido.estado.actualizado}")
    private String pedidoEstadoQueue;

    @Value("${rabbitmq.routing-key.pedido-creado:pedido.creado}")
    private String pedidoCreadoRoutingKey;

    @Value("${rabbitmq.routing-key.pedido-estado:pedido.estado.actualizado}")
    private String pedidoEstadoRoutingKey;

    // 1. Definición del Exchange (Topic para permitir ruteo flexible)
    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(pedidosExchange);
    }

    // 2. Definición de Colas (Durables para persistencia)
    @Bean
    public Queue pedidoCreadoQueue() {
        return new Queue(pedidoCreadoQueue, true);
    }

    @Bean
    public Queue pedidoEstadoQueue() {
        return new Queue(pedidoEstadoQueue, true);
    }

    // 3. Bindings (Relación entre Colas y Exchange)
    @Bean
    public Binding bindingPedidoCreado() {
        return BindingBuilder
                .bind(pedidoCreadoQueue())
                .to(pedidosExchange())
                .with(pedidoCreadoRoutingKey);
    }

    @Bean
    public Binding bindingPedidoEstado() {
        return BindingBuilder
                .bind(pedidoEstadoQueue())
                .to(pedidosExchange())
                .with(pedidoEstadoRoutingKey);
    }

    // 4. Conversor JSON (Corregido para evitar error de compilación y manejar LocalDateTime)
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 🛡️ Soporte para tipos de fecha de Java 8 (JSR310)
        objectMapper.registerModule(new JavaTimeModule());

        // 🛡️ Escribir fechas como ISO-8601 (texto) en lugar de timestamps numéricos
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ✅ Usamos el constructor para pasar el ObjectMapper y evitar el error "setObjectMapper"
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // 5. Configuración manual del Template (Asegura el uso del conversor JSON)
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}