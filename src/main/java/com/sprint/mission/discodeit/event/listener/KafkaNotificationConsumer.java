package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationConsumer {

  private final ObjectMapper objectMapper;
  private final NotificationRequiredEventListener notificationListener;

  @Transactional
  @KafkaListener(
      topics = "discodeit.message.created",
      groupId = "discodeit-group"
  )
  public void consumeMessageCreated(String payload) throws JsonProcessingException {

    MessageCreatedEvent event = objectMapper.readValue(payload, MessageCreatedEvent.class);

    notificationListener.handle(event);

    log.info("Kafka 메시지 생성 이벤트 처리완료: messageId={}", event.messageId());
  }

  @Transactional
  @KafkaListener(
      topics = "discodeit.role.updated",
      groupId = "discodeit-group"
  )
  public void consumeRoleUpdated(String payload) throws JsonProcessingException {

    RoleUpdatedEvent event = objectMapper.readValue(payload, RoleUpdatedEvent.class);

    notificationListener.handler(event);

    log.info("Kafka 권한 변경 이벤트 처리 완료: userId={}", event.userId());
  }
}
