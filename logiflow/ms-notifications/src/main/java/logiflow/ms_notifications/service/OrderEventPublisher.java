package logiflow.ms_notifications.service;

import logiflow.ms_notifications.config.RabbitMQConfig;
import logiflow.ms_notifications.dto.OrderCreatedEventDto;
import logiflow.ms_notifications.dto.OrderStatusUpdatedEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test publisher for development and testing purposes
 * In production, these events would be published by other microservices
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreatedEvent(OrderCreatedEventDto event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                    event
            );
            log.info("Published order created event: messageId={}, orderId={}",
                    event.getMessageId(), event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish order created event", e);
            throw e;
        }
    }

    public void publishOrderStatusUpdatedEvent(OrderStatusUpdatedEventDto event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_STATUS_UPDATED_ROUTING_KEY,
                    event
            );
            log.info("Published order status updated event: messageId={}, orderId={}",
                    event.getMessageId(), event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish order status updated event", e);
            throw e;
        }
    }

    // Helper method to create a sample order created event
    public OrderCreatedEventDto createSampleOrderCreatedEvent() {
        return new OrderCreatedEventDto(
                UUID.randomUUID(), // messageId
                UUID.randomUUID(), // orderId
                UUID.randomUUID(), // customerId
                "customer@example.com",
                "Juan Pérez",
                150.50,
                LocalDateTime.now()
        );
    }

    // Helper method to create a sample order status updated event
    public OrderStatusUpdatedEventDto createSampleOrderStatusUpdatedEvent(UUID orderId) {
        return new OrderStatusUpdatedEventDto(
                UUID.randomUUID(), // messageId
                orderId,
                UUID.randomUUID(), // customerId
                "customer@example.com",
                "Juan Pérez",
                "PENDING",
                "IN_TRANSIT",
                LocalDateTime.now()
        );
    }
}

