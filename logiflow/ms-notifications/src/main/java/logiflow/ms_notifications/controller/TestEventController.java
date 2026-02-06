package logiflow.ms_notifications.controller;

import logiflow.ms_notifications.dto.OrderCreatedEventDto;
import logiflow.ms_notifications.dto.OrderStatusUpdatedEventDto;
import logiflow.ms_notifications.service.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Test controller for development and testing purposes
 * Allows manual triggering of events to test the notification system
 */
@RestController
@RequestMapping("/api/test/events")
@RequiredArgsConstructor
public class TestEventController {

    private final OrderEventPublisher eventPublisher;

    @PostMapping("/order-created")
    public ResponseEntity<String> publishOrderCreatedEvent(@RequestBody OrderCreatedEventDto event) {
        // Ensure messageId is set
        if (event.getMessageId() == null) {
            event.setMessageId(UUID.randomUUID());
        }

        eventPublisher.publishOrderCreatedEvent(event);
        return ResponseEntity.ok("Order created event published: " + event.getMessageId());
    }

    @PostMapping("/order-status-updated")
    public ResponseEntity<String> publishOrderStatusUpdatedEvent(@RequestBody OrderStatusUpdatedEventDto event) {
        // Ensure messageId is set
        if (event.getMessageId() == null) {
            event.setMessageId(UUID.randomUUID());
        }

        eventPublisher.publishOrderStatusUpdatedEvent(event);
        return ResponseEntity.ok("Order status updated event published: " + event.getMessageId());
    }

    @PostMapping("/order-created/sample")
    public ResponseEntity<OrderCreatedEventDto> publishSampleOrderCreatedEvent() {
        OrderCreatedEventDto event = eventPublisher.createSampleOrderCreatedEvent();
        eventPublisher.publishOrderCreatedEvent(event);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/order-status-updated/sample")
    public ResponseEntity<OrderStatusUpdatedEventDto> publishSampleOrderStatusUpdatedEvent(
            @RequestParam(required = false) UUID orderId) {
        UUID targetOrderId = orderId != null ? orderId : UUID.randomUUID();
        OrderStatusUpdatedEventDto event = eventPublisher.createSampleOrderStatusUpdatedEvent(targetOrderId);
        eventPublisher.publishOrderStatusUpdatedEvent(event);
        return ResponseEntity.ok(event);
    }
}

