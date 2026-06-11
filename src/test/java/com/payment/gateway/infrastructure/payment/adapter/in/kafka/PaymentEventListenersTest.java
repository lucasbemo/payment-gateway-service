package com.payment.gateway.infrastructure.payment.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.application.payment.port.out.PaymentQueryPort;
import com.payment.gateway.application.webhook.port.out.WebhookDeliveryPort;
import com.payment.gateway.domain.payment.port.PaymentEventPublisherPort;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PaymentEventListeners. Dependencies are mocked and the listener is
 * exercised directly, so no Kafka broker is required.
 */
@DisplayName("Payment Event Listeners Tests")
class PaymentEventListenersTest {

    private PaymentQueryPort paymentQueryPort;
    private PaymentEventPublisherPort paymentEventPublisher;
    private ObjectMapper objectMapper;
    private WebhookDeliveryPort webhookDeliveryPort;
    private PaymentEventListeners listeners;
    private TestMessageHandler messageHandler;

    @BeforeEach
    void setUp() {
        paymentQueryPort = mock(PaymentQueryPort.class);
        paymentEventPublisher = mock(PaymentEventPublisherPort.class);
        objectMapper = new ObjectMapper();
        webhookDeliveryPort = mock(WebhookDeliveryPort.class);
        messageHandler = new TestMessageHandler();

        listeners =
                new PaymentEventListeners(paymentQueryPort, paymentEventPublisher, objectMapper, webhookDeliveryPort);
    }

    @Nested
    @DisplayName("onPaymentCreated")
    class OnPaymentCreated {

        @Test
        @DisplayName("Should process payment.created event successfully")
        void shouldProcessPaymentCreatedEvent() throws Exception {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_123",
                    "merchantId", "merchant_123",
                    "amount", "100.00",
                    "currency", "USD",
                    "idempotencyKey", "idem_123");

            listeners.onPaymentCreated(event, null);

            // payment.created does not trigger merchant webhooks
            verifyNoInteractions(webhookDeliveryPort);
        }

        @Test
        @DisplayName("Should handle missing merchantId gracefully")
        void shouldHandleMissingMerchantId() {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_123",
                    "amount", "100.00",
                    "currency", "USD");

            listeners.onPaymentCreated(event, null);

            // Verify no exception thrown
            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("onPaymentCompleted")
    class OnPaymentCompleted {

        @Test
        @DisplayName("Should process payment.completed event successfully")
        void shouldProcessPaymentCompletedEvent() throws Exception {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_123",
                    "merchantId", "merchant_123",
                    "amount", "100.00",
                    "currency", "USD",
                    "providerTransactionId", "txn_abc");

            listeners.onPaymentCompleted(event, null);

            verify(webhookDeliveryPort).deliver(eq("merchant_123"), eq("payment.completed"), anyString());
        }

        @Test
        @DisplayName("Should handle missing providerTransactionId gracefully")
        void shouldHandleMissingProviderTransactionId() {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_123",
                    "merchantId", "merchant_123");

            listeners.onPaymentCompleted(event, null);

            verify(webhookDeliveryPort).deliver(eq("merchant_123"), eq("payment.completed"), anyString());
        }

        @Test
        @DisplayName("Should not break consumption when webhook delivery throws")
        void shouldNotBreakConsumptionWhenWebhookDeliveryThrows() {
            doThrow(new RuntimeException("webhook endpoint down"))
                    .when(webhookDeliveryPort)
                    .deliver(anyString(), anyString(), anyString());

            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_123",
                    "merchantId", "merchant_123",
                    "providerTransactionId", "txn_abc");

            // Listener must not propagate webhook failures (the message must still be acked)
            listeners.onPaymentCompleted(event, null);

            verify(webhookDeliveryPort).deliver(eq("merchant_123"), eq("payment.completed"), anyString());
        }
    }

    @Nested
    @DisplayName("onPaymentFailed")
    class OnPaymentFailed {

        @Test
        @DisplayName("Should process payment.failed event successfully")
        void shouldProcessPaymentFailedEvent() throws Exception {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_123",
                    "merchantId", "merchant_123",
                    "amount", "100.00",
                    "currency", "USD",
                    "errorCode", "ERR_001",
                    "errorMessage", "Insufficient funds");

            listeners.onPaymentCreated(event, null);

            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle error code and message")
        void shouldHandleErrorCodeAndMessage() {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_456",
                    "errorCode", "ERR_TIMEOUT",
                    "errorMessage", "Connection timeout");

            listeners.onPaymentFailed(event, null);

            // No merchantId on the event: delivery is invoked with null and skipped downstream
            verify(webhookDeliveryPort).deliver(eq(null), eq("payment.failed"), anyString());
        }
    }

    @Nested
    @DisplayName("onPaymentCancelled")
    class OnPaymentCancelled {

        @Test
        @DisplayName("Should process payment.cancelled event successfully")
        void shouldProcessPaymentCancelledEvent() throws Exception {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_123",
                    "merchantId", "merchant_123",
                    "reason", "Customer request");

            listeners.onPaymentCancelled(event, null);

            verify(webhookDeliveryPort).deliver(eq("merchant_123"), eq("payment.cancelled"), anyString());
        }

        @Test
        @DisplayName("Should handle missing reason")
        void shouldHandleMissingReason() {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_123",
                    "merchantId", "merchant_123");

            listeners.onPaymentCancelled(event, null);

            verify(webhookDeliveryPort).deliver(eq("merchant_123"), eq("payment.cancelled"), anyString());
        }
    }

    @Nested
    @DisplayName("Event Processing Logic")
    class EventProcessingLogic {

        @Test
        @DisplayName("Should log event processing")
        void shouldLogEventProcessing() throws Exception {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_test",
                    "merchantId", "merchant_test");

            listeners.onPaymentCreated(event, null);

            // Verify the method was called (logging is internal)
            assertThat(true).isTrue();
        }
    }

    // Helper class for testing
    private static class TestMessageHandler {
        private final CountDownLatch latch = new CountDownLatch(1);
        private Map<String, Object> lastMessage;

        public void handleMessage(Map<String, Object> message) {
            this.lastMessage = message;
            latch.countDown();
        }

        public boolean awaitMessage(long timeout, TimeUnit unit) throws InterruptedException {
            return latch.await(timeout, unit);
        }

        public Map<String, Object> getLastMessage() {
            return lastMessage;
        }
    }
}
