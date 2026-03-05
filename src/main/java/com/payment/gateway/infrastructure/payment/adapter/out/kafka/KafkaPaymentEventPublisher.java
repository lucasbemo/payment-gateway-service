package com.payment.gateway.infrastructure.payment.adapter.out.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.domain.payment.port.PaymentEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka adapter for publishing payment events.
 * Implements the PaymentEventPublisherPort interface.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher implements PaymentEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.payment-events:payment-events}")
    private String paymentEventsTopic;

    @Override
    public void publishPaymentCreated(String paymentId, String merchantId, String amount,
                                      String currency, String idempotencyKey) {
        Map<String, Object> event = createBaseEvent("PAYMENT_CREATED", paymentId, merchantId);
        event.put("amount", amount);
        event.put("currency", currency);
        event.put("idempotencyKey", idempotencyKey);
        publishEvent(event);
    }

    @Override
    public void publishPaymentCompleted(String paymentId, String merchantId, String amount,
                                        String currency, String providerTransactionId) {
        Map<String, Object> event = createBaseEvent("PAYMENT_COMPLETED", paymentId, merchantId);
        event.put("amount", amount);
        event.put("currency", currency);
        event.put("providerTransactionId", providerTransactionId);
        publishEvent(event);
    }

    @Override
    public void publishPaymentFailed(String paymentId, String merchantId, String amount,
                                     String currency, String errorCode, String errorMessage) {
        Map<String, Object> event = createBaseEvent("PAYMENT_FAILED", paymentId, merchantId);
        event.put("amount", amount);
        event.put("currency", currency);
        event.put("errorCode", errorCode);
        event.put("errorMessage", errorMessage);
        publishEvent(event);
    }

    @Override
    public void publishPaymentCancelled(String paymentId, String merchantId, String reason) {
        Map<String, Object> event = createBaseEvent("PAYMENT_CANCELLED", paymentId, merchantId);
        event.put("reason", reason);
        publishEvent(event);
    }

    @Override
    public void publishRefundProcessed(String refundId, String paymentId, String merchantId,
                                       String refundAmount, String currency, String refundType) {
        Map<String, Object> event = createBaseEvent("REFUND_PROCESSED", refundId, merchantId);
        event.put("paymentId", paymentId);
        event.put("refundAmount", refundAmount);
        event.put("currency", currency);
        event.put("refundType", refundType);
        publishEvent(event);
    }

    private Map<String, Object> createBaseEvent(String eventType, String aggregateId, String merchantId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("aggregateId", aggregateId);
        event.put("aggregateType", "Payment");
        event.put("merchantId", merchantId);
        event.put("timestamp", Instant.now().toString());
        event.put("occurredAt", Instant.now().toString());
        return event;
    }

    private void publishEvent(Map<String, Object> event) {
        String key = event.get("aggregateId").toString();
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(paymentEventsTopic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish payment event: {}", event.get("eventType"), ex);
            } else {
                log.debug("Successfully published payment event: {} to topic {} partition {} offset {}",
                        event.get("eventType"),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
