package logiflow.ms_notifications.repository;

import logiflow.ms_notifications.model.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, UUID> {

    Optional<ProcessedMessage> findByMessageId(UUID messageId);

    boolean existsByMessageId(UUID messageId);
}

