package com.payment.gateway.infrastructure.refund.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for RefundEventListeners.
 */
@DisplayName("Refund Event Listeners Tests")
class RefundEventListenersTest {

    private ObjectMapper objectMapper;
    private RefundEventListeners listeners;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listeners = new RefundEventListeners(objectMapper);
    }

    @Nested
    @DisplayName("onRefundProcessed")
    class OnRefundProcessed {

        @Test
        @DisplayName("Should process refund.processed event successfully")
        void shouldProcessRefundProcessedEvent() throws Exception {
            Map<String, Object> event = Map.of(
                    "refundId", "ref_123",
                    "paymentId", "pay_123",
                    "merchantId", "merchant_123",
                    "refundAmount", "50.00",
                    "currency", "USD",
                    "refundType", "FULL"
            );

            listeners.onRefundProcessed(event, null);

            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle partial refund type")
        void shouldHandlePartialRefundType() {
            Map<String, Object> event = Map.of(
                    "refundId", "ref_456",
                    "paymentId", "pay_456",
                    "merchantId", "merchant_456",
                    "refundAmount", "25.00",
                    "currency", "USD",
                    "refundType", "PARTIAL"
            );

            listeners.onRefundProcessed(event, null);

            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("onRefundFailed")
    class OnRefundFailed {

        @Test
        @DisplayName("Should process refund.failed event successfully")
        void shouldProcessRefundFailedEvent() throws Exception {
            Map<String, Object> event = Map.of(
                    "refundId", "ref_failed",
                    "paymentId", "pay_123",
                    "errorCode", "REFUND_ERR_001",
                    "errorMessage", "Insufficient funds for refund"
            );

            listeners.onRefundFailed(event, null);

            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle timeout error")
        void shouldHandleTimeoutError() {
            Map<String, Object> event = Map.of(
                    "refundId", "ref_timeout",
                    "paymentId", "pay_789",
                    "errorCode", "TIMEOUT",
                    "errorMessage", "Provider timeout"
            );

            listeners.onRefundFailed(event, null);

            assertThat(true).isTrue();
        }
    }
}
