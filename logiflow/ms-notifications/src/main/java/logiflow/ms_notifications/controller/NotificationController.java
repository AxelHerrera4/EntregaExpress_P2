package logiflow.ms_notifications.controller;

import logiflow.ms_notifications.dto.NotificationDto;
import logiflow.ms_notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(notificationService.getNotificationsByOrderId(orderId));
    }

    @GetMapping("/recipient/{recipient}")
    public ResponseEntity<List<NotificationDto>> getNotificationsByRecipient(@PathVariable String recipient) {
        return ResponseEntity.ok(notificationService.getNotificationsByRecipient(recipient));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<Void> sendNotification(@PathVariable UUID id) {
        notificationService.sendNotification(id);
        return ResponseEntity.ok().build();
    }
}

