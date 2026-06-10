package com.payment.gateway.infrastructure.refund.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.application.webhook.port.out.WebhookDeliveryPort;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka listeners for refund events.
 * Handles consumption of refund-related events from Kafka topics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundEventListeners {

    private final ObjectMapper objectMapper;
    private final WebhookDeliveryPort webhookDeliveryPort;

    @Value("${kafka.topics.refund-processed:refund.processed}")
    private String refundProcessedTopic;

    @Value("${kafka.topics.refund-failed:refund.failed}")
    private String refundFailedTopic;

    @KafkaListener(
            topics = "${kafka.topics.refund-processed:refund.processed}",
            groupId = "${spring.kafka.consumer.group-id:payment-gateway-group}",
            containerFactory = "kafkaListenerContainerFactory")
    public void onRefundProcessed(
            @Payload Map<String, Object> event,
            @Header(value = "kafka_receivedMessageKey", required = false) String receivedKey) {
        log.info("Received refund.processed event: {}", event);
        try {
            String refundId = (String) event.get("refundId");
            String paymentId = (String) event.get("paymentId");
            String merchantId = (String) event.get("merchantId");
            String refundAmount = (String) event.get("refundAmount");
            String currency = (String) event.get("currency");
            String refundType = (String) event.get("refundType");

            // Process the refund processed event
            handleRefundProcessed(refundId, paymentId, merchantId, refundAmount, currency, refundType, event);

            log.info("Successfully processed refund.processed event for refund: {}", refundId);
        } catch (Exception e) {
            log.error("Error processing refund.processed event: {}", event, e);
            throw e; // Re-throw to trigger retry
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.refund-failed:refund.failed}",
            groupId = "${spring.kafka.consumer.group-id:payment-gateway-group}",
            containerFactory = "kafkaListenerContainerFactory")
    public void onRefundFailed(
            @Payload Map<String, Object> event,
            @Header(value = "kafka_receivedMessageKey", required = false) String receivedKey) {
        log.info("Received refund.failed event: {}", event);
        try {
            String refundId = (String) event.get("refundId");
            String paymentId = (String) event.get("paymentId");
            String errorCode = (String) event.get("errorCode");
            String errorMessage = (String) event.get("errorMessage");

            // Process the refund failed event
            handleRefundFailed(refundId, paymentId, errorCode, errorMessage);

            log.info("Successfully processed refund.failed event for refund: {}", refundId);
        } catch (Exception e) {
            log.error("Error processing refund.failed event: {}", event, e);
            throw e; // Re-throw to trigger retry
        }
    }

    private void handleRefundProcessed(
            String refundId,
            String paymentId,
            String merchantId,
            String refundAmount,
            String currency,
            String refundType,
            Map<String, Object> event) {
        log.info(
                "Handling refund processed: refundId={}, paymentId={}, merchantId={}, amount={} {}",
                refundId,
                paymentId,
                merchantId,
                refundAmount,
                currency);
        deliverMerchantWebhook("refund.processed", merchantId, event);
    }

    /**
     * Deliver the event to the merchant's webhook. Never throws: webhook delivery
     * problems must not break Kafka consumption (the listener must still ack).
     */
    private void deliverMerchantWebhook(String fallbackEventType, String merchantId, Map<String, Object> event) {
        try {
            String eventType =
                    event.get("eventType") instanceof String type && !type.isBlank() ? type : fallbackEventType;
            String payloadJson = objectMapper.writeValueAsString(event);
            webhookDeliveryPort.deliver(merchantId, eventType, payloadJson);
        } catch (Exception e) {
            log.error("Webhook delivery failed for {} event: {}", fallbackEventType, event, e);
        }
    }

    private void handleRefundFailed(String refundId, String paymentId, String errorCode, String errorMessage) {
        log.info(
                "Handling refund failed: refundId={}, paymentId={}, errorCode={}, errorMessage={}",
                refundId,
                paymentId,
                errorCode,
                errorMessage);
        // Add business logic here (e.g., notify customer, trigger retry, escalate, etc.)
    }
}
