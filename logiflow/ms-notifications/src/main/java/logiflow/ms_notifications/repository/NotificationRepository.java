package logiflow.ms_notifications.repository;

import logiflow.ms_notifications.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByOrderId(UUID orderId);

    List<Notification> findByRecipient(String recipient);

    List<Notification> findByStatus(String status);

    List<Notification> findByType(String type);
}

