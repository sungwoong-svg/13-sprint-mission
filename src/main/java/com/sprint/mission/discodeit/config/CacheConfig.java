package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager(
      RedisConnectionFactory connectionFactory,
      ObjectMapper objectMapper
  ) {

    RedisCacheConfiguration cacheConfiguration =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(600))
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(GenericJackson2JsonRedisSerializer.builder()
                        .typeHintPropertyName("@class")
                        .defaultTyping(true)
                        .build())
            );

    JavaType userListType = objectMapper.getTypeFactory()
        .constructCollectionType(List.class, UserDto.class);

    Jackson2JsonRedisSerializer<Object> usersSerializer =
        new Jackson2JsonRedisSerializer<>(objectMapper, userListType);

    RedisCacheConfiguration usersCacheConfiguration =
        cacheConfiguration.serializeValuesWith(
            RedisSerializationContext.SerializationPair
                .fromSerializer(usersSerializer)
        );

    JavaType channelListType = objectMapper.getTypeFactory()
        .constructCollectionType(List.class, ChannelDto.class);

    Jackson2JsonRedisSerializer<Object> channelsSerializer =
        new Jackson2JsonRedisSerializer<>(objectMapper, channelListType);

    RedisCacheConfiguration channelsCacheConfiguration =
        cacheConfiguration.serializeValuesWith(
            RedisSerializationContext.SerializationPair
                .fromSerializer(channelsSerializer)
        );

    JavaType notificationListType = objectMapper.getTypeFactory()
        .constructCollectionType(List.class, NotificationDto.class);

    Jackson2JsonRedisSerializer<Object> notificationsSerializer =
        new Jackson2JsonRedisSerializer<>(objectMapper, notificationListType);

    RedisCacheConfiguration notificationsCacheConfiguration =
        cacheConfiguration.serializeValuesWith(
            RedisSerializationContext.SerializationPair
                .fromSerializer(notificationsSerializer)
        );

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(cacheConfiguration)
        .withInitialCacheConfigurations(
            Map.of(
                "channels", channelsCacheConfiguration,
                "notifications", notificationsCacheConfiguration,
                "users", usersCacheConfiguration
            )
        )
        .build();
  }
}