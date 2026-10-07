package com.sprint.mission.discodeit.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager(
        "userChannels",
        "userNotifications",
        "users"
    );

    cacheManager.setCaffeine(
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(Duration.ofSeconds(600))
            .recordStats()
    );

    return cacheManager;
  }

}
