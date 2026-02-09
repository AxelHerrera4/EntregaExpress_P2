package logiflow.ms_notifications.utils;

import logiflow.ms_notifications.dto.NotificationDto;
import logiflow.ms_notifications.model.Notification;

public class NotificationMapper {

    public static NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        return new NotificationDto(
                notification.getId(),
                notification.getOrderId(),
                notification.getRecipient(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getType(),
                notification.getCreatedAt()
        );
    }

    public static Notification toEntity(NotificationDto dto) {
        if (dto == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setOrderId(dto.getOrderId());
        notification.setRecipient(dto.getRecipient());
        notification.setSubject(dto.getSubject());
        notification.setMessage(dto.getMessage());
        notification.setType(dto.getType());
        notification.setCreatedAt(dto.getCreatedAt());

        return notification;
    }
}

