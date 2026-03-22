package com.payment.gateway.infrastructure.outbox.adapter.out.kafka;

import com.payment.gateway.domain.outbox.model.OutboxEvent;
import com.payment.gateway.infrastructure.commons.monitoring.KafkaMetricsBinder;
import io.micrometer.core.instrument.Timer;
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
    private final KafkaMetricsBinder kafkaMetricsBinder;

    @Value("${kafka.topics.outbox-events:outbox-events}")
    private String outboxEventsTopic;

    public boolean publish(OutboxEvent event) {
        Timer.Sample sample = kafkaMetricsBinder.startProducerTimer();
        try {
            String key = event.getAggregateId();
            String topic = resolveTopic(event);
            CompletableFuture<?> future = kafkaTemplate.send(topic, key, event.getPayload());
            future.get();
            log.debug("Published outbox event: {} type: {}", event.getId(), event.getEventType());
            kafkaMetricsBinder.recordMessageProduced(topic);
            kafkaMetricsBinder.recordProducerLatency(sample);
            return true;
        } catch (Exception ex) {
            log.error("Failed to publish outbox event: {} type: {}", event.getId(), event.getEventType(), ex);
            kafkaMetricsBinder.recordMessageProducedFailed(outboxEventsTopic, ex.getClass().getSimpleName());
            return false;
        }
    }

    private String resolveTopic(OutboxEvent event) {
        if (event.getEventType() == null) {
            return outboxEventsTopic;
        }
        return switch (event.getEventType()) {
            case PAYMENT_CREATED -> "payment.created";
            case PAYMENT_COMPLETED -> "payment.completed";
            case PAYMENT_FAILED -> "payment.failed";
            case PAYMENT_CANCELLED -> "payment.cancelled";
            case REFUND_PROCESSED -> "refund.processed";
            case TRANSACTION_CREATED -> "transaction.created";
            case TRANSACTION_COMPLETED -> "transaction.completed";
            case TRANSACTION_FAILED -> "transaction.failed";
            case CUSTOMER_CREATED -> "customer.created";
            case CUSTOMER_UPDATED -> "customer.updated";
            case MERCHANT_ACTIVATED -> "merchant.activated";
            case MERCHANT_SUSPENDED -> "merchant.suspended";
        };
    }
}
