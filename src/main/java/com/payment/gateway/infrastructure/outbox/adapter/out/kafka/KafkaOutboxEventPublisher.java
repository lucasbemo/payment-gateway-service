package com.payment.gateway.infrastructure.outbox.adapter.out.kafka;

import com.payment.gateway.domain.outbox.model.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOutboxEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.outbox-events:outbox-events}")
    private String outboxEventsTopic;

    public boolean publish(OutboxEvent event) {
        try {
            String key = event.getAggregateId();
            CompletableFuture<?> future = kafkaTemplate.send(outboxEventsTopic, key, event.getPayload());
            future.get();
            log.debug("Published outbox event: {} type: {}", event.getId(), event.getEventType());
            return true;
        } catch (Exception ex) {
            log.error("Failed to publish outbox event: {} type: {}", event.getId(), event.getEventType(), ex);
            return false;
        }
    }
}
