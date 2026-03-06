package com.payment.gateway.infrastructure.merchant.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka listeners for merchant notification events.
 * Handles consumption of merchant-related events from Kafka topics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantNotificationListeners {

    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.merchant-notification:merchant.notification}")
    private String merchantNotificationTopic;

    @KafkaListener(
        topics = "${kafka.topics.merchant-notification:merchant.notification}",
        groupId = "${spring.kafka.consumer.group-id:payment-gateway-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMerchantNotification(@Payload Map<String, Object> event,
                                        @Header(value = "kafka_receivedMessageKey", required = false) String receivedKey) {
        log.info("Received merchant.notification event: {}", event);
        try {
            String notificationId = (String) event.get("notificationId");
            String merchantId = (String) event.get("merchantId");
            String notificationType = (String) event.get("notificationType");
            String payload = (String) event.get("payload");

            // Process the merchant notification event
            handleMerchantNotification(notificationId, merchantId, notificationType, payload);

            log.info("Successfully processed merchant.notification event: {}", notificationId);
        } catch (Exception e) {
            log.error("Error processing merchant.notification event: {}", event, e);
            throw e; // Re-throw to trigger retry
        }
    }

    private void handleMerchantNotification(String notificationId, String merchantId,
                                             String notificationType, String payload) {
        log.info("Handling merchant notification: notificationId={}, merchantId={}, notificationType={}",
                 notificationId, merchantId, notificationType);
        // Add business logic here (e.g., send webhook, email notification, update merchant dashboard, etc.)
    }
}
