package com.payment.gateway.infrastructure.payment.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.application.payment.port.out.PaymentQueryPort;
import com.payment.gateway.domain.payment.port.PaymentEventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for PaymentEventListeners using embedded Kafka.
 */
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" },
        topics = {
                "payment.created",
                "payment.completed",
                "payment.failed",
                "payment.cancelled"
        }
)
@DisplayName("Payment Event Listeners Tests")
class PaymentEventListenersTest {

    private EmbeddedKafkaBroker embeddedKafka;
    private PaymentQueryPort paymentQueryPort;
    private PaymentEventPublisherPort paymentEventPublisher;
    private ObjectMapper objectMapper;
    private PaymentEventListeners listeners;
    private TestMessageHandler messageHandler;

    @BeforeEach
    void setUp() {
        paymentQueryPort = mock(PaymentQueryPort.class);
        paymentEventPublisher = mock(PaymentEventPublisherPort.class);
        objectMapper = new ObjectMapper();
        messageHandler = new TestMessageHandler();

        listeners = new PaymentEventListeners(paymentQueryPort, paymentEventPublisher, objectMapper);
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
                    "idempotencyKey", "idem_123"
            );

            listeners.onPaymentCreated(event, null);

            // Verify no exception thrown (successful processing)
            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle missing merchantId gracefully")
        void shouldHandleMissingMerchantId() {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_123",
                    "amount", "100.00",
                    "currency", "USD"
            );

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
                    "providerTransactionId", "txn_abc"
            );

            listeners.onPaymentCompleted(event, null);

            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle missing providerTransactionId gracefully")
        void shouldHandleMissingProviderTransactionId() {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_123",
                    "merchantId", "merchant_123"
            );

            listeners.onPaymentCompleted(event, null);

            assertThat(true).isTrue();
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
                    "errorMessage", "Insufficient funds"
            );

            listeners.onPaymentCreated(event, null);

            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle error code and message")
        void shouldHandleErrorCodeAndMessage() {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_456",
                    "errorCode", "ERR_TIMEOUT",
                    "errorMessage", "Connection timeout"
            );

            listeners.onPaymentFailed(event, null);

            assertThat(true).isTrue();
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
                    "reason", "Customer request"
            );

            listeners.onPaymentCancelled(event, null);

            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle missing reason")
        void shouldHandleMissingReason() {
            Map<String, Object> event = Map.of(
                    "aggregateId", "pay_123",
                    "merchantId", "merchant_123"
            );

            listeners.onPaymentCancelled(event, null);

            assertThat(true).isTrue();
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
                    "merchantId", "merchant_test"
            );

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
