package ec.edu.espe.billing_service.rabbit;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.exchange.pedidos}")
    private String pedidosExchange;

    @Value("${rabbitmq.queue.pedido-estado}")
    private String pedidoEstadoQueue;

    @Value("${rabbitmq.routing-key.pedido-estado}")
    private String routingKey;

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(pedidosExchange);
    }

    @Bean
    public Queue pedidoEstadoQueue() {
        return new Queue(pedidoEstadoQueue, true);
    }

    @Bean
    public Binding bindingPedidoEstado(
            Queue pedidoEstadoQueue,
            TopicExchange pedidosExchange) {

        return BindingBuilder
                .bind(pedidoEstadoQueue)
                .to(pedidosExchange)
                .with(routingKey);
    }
}
