package com.payment.gateway.infrastructure.outbox.adapter.out.kafka;

import com.payment.gateway.domain.outbox.model.EventStatus;
import com.payment.gateway.domain.outbox.model.OutboxEvent;
import com.payment.gateway.domain.outbox.port.OutboxEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPollingScheduler {

    private final OutboxEventRepositoryPort outboxEventRepositoryPort;
    private final KafkaOutboxEventPublisher kafkaOutboxEventPublisher;

    @Scheduled(fixedDelayString = "${outbox.polling.interval-ms:5000}")
    public void pollAndPublishEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepositoryPort.findByStatus(EventStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                event.markAsProcessing();
                outboxEventRepositoryPort.save(event);

                boolean published = kafkaOutboxEventPublisher.publish(event);

                if (published) {
                    event.markAsPublished();
                } else {
                    event.markAsFailed("Failed to publish to Kafka");
                }
                outboxEventRepositoryPort.save(event);
            } catch (Exception ex) {
                log.error("Error processing outbox event: {}", event.getId(), ex);
                event.markAsFailed(ex.getMessage());
                outboxEventRepositoryPort.save(event);
            }
        }
    }

    @Scheduled(fixedDelayString = "${outbox.retry.interval-ms:30000}")
    public void retryFailedEvents() {
        List<OutboxEvent> failedEvents = outboxEventRepositoryPort.findByStatus(EventStatus.FAILED);

        for (OutboxEvent event : failedEvents) {
            if (!event.canRetry()) {
                log.warn("Outbox event {} has exceeded max retries", event.getId());
                continue;
            }

            try {
                event.markAsRetrying();
                outboxEventRepositoryPort.save(event);

                boolean published = kafkaOutboxEventPublisher.publish(event);

                if (published) {
                    event.markAsPublished();
                } else {
                    event.markAsFailed("Retry failed");
                }
                outboxEventRepositoryPort.save(event);
            } catch (Exception ex) {
                log.error("Error retrying outbox event: {}", event.getId(), ex);
                event.markAsFailed(ex.getMessage());
                outboxEventRepositoryPort.save(event);
            }
        }
    }
}
