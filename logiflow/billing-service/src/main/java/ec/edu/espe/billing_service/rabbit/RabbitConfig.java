package ec.edu.espe.billing_service.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.exchange.pedidos}")
    private String pedidosExchange;

    @Value("${rabbitmq.queue.pedido-creado}")
    private String pedidoCreadoQueue;

    @Value("${rabbitmq.queue.pedido-estado}")
    private String pedidoEstadoQueue;

    @Value("${rabbitmq.routing-key.pedido-creado}")
    private String pedidoCreadoRoutingKey;

    @Value("${rabbitmq.routing-key.pedido-estado}")
    private String pedidoEstadoRoutingKey;

    // Exchange
    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(pedidosExchange);
    }

    // Colas
    @Bean
    public Queue pedidoCreadoQueue() {
        return new Queue(pedidoCreadoQueue, true);
    }

    @Bean
    public Queue pedidoEstadoQueue() {
        return new Queue(pedidoEstadoQueue, true);
    }

    // Bindings
    @Bean
    public Binding bindingPedidoCreado(Queue pedidoCreadoQueue, TopicExchange pedidosExchange) {
        return BindingBuilder
                .bind(pedidoCreadoQueue)
                .to(pedidosExchange)
                .with(pedidoCreadoRoutingKey);
    }

    @Bean
    public Binding bindingPedidoEstado(Queue pedidoEstadoQueue, TopicExchange pedidosExchange) {
        return BindingBuilder
                .bind(pedidoEstadoQueue)
                .to(pedidosExchange)
                .with(pedidoEstadoRoutingKey);
    }

    // Converter para JSON con soporte para LocalDateTime
    @Bean
    public MessageConverter jsonMessageConverter() {
        // Configurar ObjectMapper para manejar LocalDateTime
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Pasar ObjectMapper al constructor (setObjectMapper() fue removido en Spring AMQP 3.x)
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
