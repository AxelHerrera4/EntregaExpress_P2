package logiflow.ms_notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private UUID id;
    private String orderId;
    private String recipient;
    private String subject;
    private String message;
    private String type;
    private LocalDateTime createdAt;
}

