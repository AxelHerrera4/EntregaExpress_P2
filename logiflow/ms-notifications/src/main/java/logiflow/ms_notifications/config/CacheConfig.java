package logiflow.ms_notifications.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String NOTIFICATION_CACHE = "notifications";
    public static final String PROCESSED_MESSAGES_CACHE = "processedMessages";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(NOTIFICATION_CACHE, PROCESSED_MESSAGES_CACHE);
    }
}

