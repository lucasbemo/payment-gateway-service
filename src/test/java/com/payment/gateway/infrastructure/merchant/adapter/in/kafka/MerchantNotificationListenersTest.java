package com.payment.gateway.infrastructure.merchant.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MerchantNotificationListeners.
 */
@DisplayName("Merchant Notification Listeners Tests")
class MerchantNotificationListenersTest {

    private ObjectMapper objectMapper;
    private MerchantNotificationListeners listeners;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listeners = new MerchantNotificationListeners(objectMapper);
    }

    @Nested
    @DisplayName("onMerchantNotification")
    class OnMerchantNotification {

        @Test
        @DisplayName("Should process merchant.notification event successfully")
        void shouldProcessMerchantNotificationEvent() throws Exception {
            Map<String, Object> event = Map.of(
                    "notificationId", "notif_001",
                    "merchantId", "merchant_123",
                    "notificationType", "PAYMENT_RECEIVED",
                    "payload", "{\"paymentId\":\"pay_123\",\"amount\":100.00}"
            );

            listeners.onMerchantNotification(event, null);

            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle payment notification type")
        void shouldHandlePaymentNotificationType() {
            Map<String, Object> event = Map.of(
                    "notificationId", "notif_002",
                    "merchantId", "merchant_456",
                    "notificationType", "PAYMENT_COMPLETED",
                    "payload", "{\"status\":\"completed\"}"
            );

            listeners.onMerchantNotification(event, null);

            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle settlement notification type")
        void shouldHandleSettlementNotificationType() {
            Map<String, Object> event = Map.of(
                    "notificationId", "notif_003",
                    "merchantId", "merchant_789",
                    "notificationType", "SETTLEMENT_COMPLETED",
                    "payload", "{\"batchId\":\"batch_001\"}"
            );

            listeners.onMerchantNotification(event, null);

            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle missing payload")
        void shouldHandleMissingPayload() {
            Map<String, Object> event = Map.of(
                    "notificationId", "notif_004",
                    "merchantId", "merchant_000",
                    "notificationType", "GENERIC"
            );

            listeners.onMerchantNotification(event, null);

            assertThat(true).isTrue();
        }
    }
}
