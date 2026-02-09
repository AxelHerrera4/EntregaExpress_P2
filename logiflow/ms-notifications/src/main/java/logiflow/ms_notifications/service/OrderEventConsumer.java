package logiflow.ms_notifications.service;

import logiflow.ms_notifications.config.RabbitMQConfig;
import logiflow.ms_notifications.dto.OrderCreatedEventDto;
import logiflow.ms_notifications.dto.OrderStatusUpdatedEventDto;
import logiflow.ms_notifications.utils.IdempotencyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final IdempotencyManager idempotencyManager;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreatedEvent(OrderCreatedEventDto event) {
        log.info("Received order created event: messageId={}, orderId={}",
                event.getMessageId(), event.getPedidoId());

        try {
            // Build email content
            String subject = "Confirmación de Pedido - LogiFlow";
            String body = emailService.buildOrderCreatedEmailBody(
                    event.getClienteId(),
                    event.getPedidoId(),
                    event.getPeso()
            );

            // Create and send notification
            notificationService.createAndSendNotification(
                    event.getPedidoId(),
                    event.getClienteId() + "@example.com", // Assuming email can be derived from customer ID
                    subject,
                    body,
                    "ORDER_CREATED"
            );

            // Mark message as processed for idempotency
            idempotencyManager.markAsProcessed(event.getMessageId(), "ORDER_CREATED");

            log.info("Successfully processed order created event: {}", event.getPedidoId());
        } catch (Exception e) {
            log.error("Error processing order created event: messageId={}, orderId={}",
                    event.getMessageId(), event.getPedidoId(), e);
            throw e; // Trigger retry mechanism
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_STATUS_UPDATED_QUEUE)
    public void handleOrderStatusUpdatedEvent(OrderStatusUpdatedEventDto event) {
        log.info("Received order status updated event: messageId={}, orderId={}, newStatus={}",
                event.getMessageId(), event.getPedidoId(), event.getEstadoNuevo());

        // Check idempotency - skip if already processed
        if (idempotencyManager.isMessageProcessed(event.getMessageId())) {
            log.warn("Message already processed, skipping: {}", event.getMessageId());
            return;
        }

        try {
            // Build email content
            String subject = "Actualización de Estado de Pedido - LogiFlow";
            String body = emailService.buildOrderStatusUpdatedEmailBody(
                    event.getRepartidorId(),
                    event.getPedidoId().toString(),
                    event.getEstadoAnterior(),
                    event.getEstadoNuevo()
            );

            // Create and send notification
            notificationService.createAndSendNotification(
                    event.getPedidoId(),
                    event.getUsuarioModificador() + "@example.com", // Assuming email can be derived from user ID
                    subject,
                    body,
                    "ORDER_STATUS_UPDATED"
            );

            // Mark message as processed for idempotency
            idempotencyManager.markAsProcessed(event.getMessageId(), "ORDER_STATUS_UPDATED");

            log.info("Successfully processed order status updated event: {}", event.getPedidoId());
        } catch (Exception e) {
            log.error("Error processing order status updated event: messageId={}, orderId={}",
                    event.getMessageId(), event.getPedidoId(), e);
            throw e; // Trigger retry mechanism
        }
    }
}

