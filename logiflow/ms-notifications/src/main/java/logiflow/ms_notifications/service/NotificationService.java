package logiflow.ms_notifications.service;

import logiflow.ms_notifications.dto.NotificationDto;
import logiflow.ms_notifications.model.Notification;
import logiflow.ms_notifications.repository.NotificationRepository;
import logiflow.ms_notifications.utils.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationDto createNotification(UUID orderId, String recipient, String subject,
                                             String message, String type) {
        Notification notification = new Notification();
        notification.setOrderId(orderId);
        notification.setRecipient(recipient);
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setType(type);
        notification.setStatus("PENDING");

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created: {}", saved.getId());

        return NotificationMapper.toDto(saved);
    }

    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public void sendNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        try {
            emailService.sendEmail(
                    notification.getRecipient(),
                    notification.getSubject(),
                    notification.getMessage()
            );

            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Notification sent successfully: {}", notificationId);
        } catch (Exception e) {
            notification.setStatus("FAILED");
            notificationRepository.save(notification);

            log.error("Failed to send notification: {}", notificationId, e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationDto createAndSendNotification(UUID orderId, String recipient,
                                                     String subject, String message, String type) {
        NotificationDto notificationDto = createNotification(orderId, recipient, subject, message, type);

        try {
            sendNotification(notificationDto.getId());
        } catch (Exception e) {
            log.error("Failed to send notification immediately: {}", notificationDto.getId(), e);
        }

        return notificationDto;
    }

    @Cacheable(value = "notifications", key = "#orderId")
    public List<NotificationDto> getNotificationsByOrderId(UUID orderId) {
        return notificationRepository.findByOrderId(orderId).stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> getNotificationsByRecipient(String recipient) {
        return notificationRepository.findByRecipient(recipient).stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public NotificationDto getNotificationById(UUID id) {
        return notificationRepository.findById(id)
                .map(NotificationMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
    }
}

