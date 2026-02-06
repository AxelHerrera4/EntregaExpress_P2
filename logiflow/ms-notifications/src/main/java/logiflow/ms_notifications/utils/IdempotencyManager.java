package logiflow.ms_notifications.utils;

import logiflow.ms_notifications.model.ProcessedMessage;
import logiflow.ms_notifications.repository.ProcessedMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
public class IdempotencyManager {

    private final ProcessedMessageRepository processedMessageRepository;
    private final Cache processedMessagesCache;

    public IdempotencyManager(ProcessedMessageRepository processedMessageRepository,
                             CacheManager cacheManager) {
        this.processedMessageRepository = processedMessageRepository;
        this.processedMessagesCache = cacheManager.getCache("processedMessages");
    }

    /**
     * Check if a message has already been processed (idempotency check)
     * @param messageId UUID of the message
     * @return true if already processed, false otherwise
     */
    public boolean isMessageProcessed(UUID messageId) {
        // Check cache first
        if (processedMessagesCache != null) {
            Boolean cachedResult = processedMessagesCache.get(messageId, Boolean.class);
            if (cachedResult != null && cachedResult) {
                log.info("Message {} already processed (found in cache)", messageId);
                return true;
            }
        }

        // Check database
        boolean exists = processedMessageRepository.existsByMessageId(messageId);

        if (exists) {
            log.info("Message {} already processed (found in database)", messageId);
            // Update cache
            if (processedMessagesCache != null) {
                processedMessagesCache.put(messageId, true);
            }
        }

        return exists;
    }

    /**
     * Mark a message as processed
     * @param messageId UUID of the message
     * @param eventType Type of event
     */
    @Transactional
    public void markAsProcessed(UUID messageId, String eventType) {
        ProcessedMessage processedMessage = new ProcessedMessage();
        processedMessage.setMessageId(messageId);
        processedMessage.setEventType(eventType);

        processedMessageRepository.save(processedMessage);

        // Update cache
        if (processedMessagesCache != null) {
            processedMessagesCache.put(messageId, true);
        }

        log.info("Message {} marked as processed", messageId);
    }
}

