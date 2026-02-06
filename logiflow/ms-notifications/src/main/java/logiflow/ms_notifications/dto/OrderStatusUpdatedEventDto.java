package logiflow.ms_notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdatedEventDto {
    private UUID messageId;
    private UUID orderId;
    private UUID customerId;
    private String customerEmail;
    private String customerName;
    private String previousStatus;
    private String newStatus;
    private LocalDateTime updatedAt;
}

