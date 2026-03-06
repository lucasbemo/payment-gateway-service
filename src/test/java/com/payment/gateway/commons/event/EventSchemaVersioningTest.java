package com.payment.gateway.commons.event;

import com.payment.gateway.domain.payment.event.PaymentCreatedEvent;
import com.payment.gateway.domain.payment.event.PaymentCompletedEvent;
import com.payment.gateway.domain.payment.event.PaymentFailedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for event schema versioning and backward compatibility.
 */
@DisplayName("Event Schema Versioning Tests")
class EventSchemaVersioningTest {

    @Nested
    @DisplayName("IntegrationEvent Tests")
    class IntegrationEventTests {

        @Test
        @DisplayName("Should create event with current schema version")
        void shouldCreateEventWithCurrentSchemaVersion() {
            PaymentCreatedEvent event = new PaymentCreatedEvent(
                    "pay_123", "merchant_123", "100.00", "USD", "idem_123"
            );

            assertThat(event.getSchemaVersion()).isEqualTo("1.0.0");
            assertThat(event.getEventType()).isEqualTo("PAYMENT_CREATED");
        }

        @Test
        @DisplayName("Should check compatibility with same major version")
        void shouldCheckCompatibilityWithSameMajorVersion() {
            PaymentCreatedEvent event = new PaymentCreatedEvent(
                    "pay_123", "merchant_123", "100.00", "USD", "idem_123"
            );

            assertThat(event.isCompatibleWith("1.0.0")).isTrue();
            assertThat(event.isCompatibleWith("1.1.0")).isTrue();
            assertThat(event.isCompatibleWith("1.2.5")).isTrue();
        }

        @Test
        @DisplayName("Should not be compatible with different major version")
        void shouldNotBeCompatibleWithDifferentMajorVersion() {
            PaymentCreatedEvent event = new PaymentCreatedEvent(
                    "pay_123", "merchant_123", "100.00", "USD", "idem_123"
            );

            assertThat(event.isCompatibleWith("2.0.0")).isFalse();
            assertThat(event.isCompatibleWith("0.9.0")).isFalse();
        }
    }

    @Nested
    @DisplayName("PaymentCreatedEvent Schema Tests")
    class PaymentCreatedEventSchemaTests {

        @Test
        @DisplayName("Should serialize event to map with schema version")
        void shouldSerializeEventToMap() {
            PaymentCreatedEvent event = new PaymentCreatedEvent(
                    "pay_123", "merchant_123", "100.00", "USD", "idem_123"
            );

            Map<String, Object> map = event.toMap();

            assertThat(map.get("schemaVersion")).isEqualTo("1.0.0");
            assertThat(map.get("eventType")).isEqualTo("PAYMENT_CREATED");
            assertThat(map.get("aggregateId")).isEqualTo("pay_123");
            assertThat(map.get("merchantId")).isEqualTo("merchant_123");
            assertThat(map.get("amount")).isEqualTo("100.00");
            assertThat(map.get("currency")).isEqualTo("USD");
            assertThat(map.get("idempotencyKey")).isEqualTo("idem_123");
        }

        @Test
        @DisplayName("Should deserialize event from map with schema version")
        void shouldDeserializeEventFromMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", "event-123");
            map.put("occurredOn", Instant.now());
            map.put("aggregateId", "pay_123");
            map.put("schemaVersion", "1.0.0");
            map.put("eventType", "PAYMENT_CREATED");
            map.put("merchantId", "merchant_123");
            map.put("amount", "100.00");
            map.put("currency", "USD");
            map.put("idempotencyKey", "idem_123");

            PaymentCreatedEvent event = PaymentCreatedEvent.fromMap(map);

            assertThat(event.getId()).isEqualTo("event-123");
            assertThat(event.getAggregateId()).isEqualTo("pay_123");
            assertThat(event.getMerchantId()).isEqualTo("merchant_123");
            assertThat(event.getAmount()).isEqualTo("100.00");
            assertThat(event.getSchemaVersion()).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("Should handle missing schema version in map")
        void shouldHandleMissingSchemaVersionInMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", "event-123");
            map.put("occurredOn", Instant.now());
            map.put("aggregateId", "pay_123");
            map.put("eventType", "PAYMENT_CREATED");
            map.put("merchantId", "merchant_123");
            map.put("amount", "100.00");
            map.put("currency", "USD");
            map.put("idempotencyKey", "idem_123");

            PaymentCreatedEvent event = PaymentCreatedEvent.fromMap(map);

            assertThat(event.getSchemaVersion()).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("Should migrate from compatible 1.x version")
        void shouldMigrateFromCompatibleVersion() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", "event-123");
            map.put("occurredOn", Instant.now());
            map.put("aggregateId", "pay_123");
            map.put("schemaVersion", "1.0.0");
            map.put("eventType", "PAYMENT_CREATED");
            map.put("merchantId", "merchant_123");
            map.put("amount", "100.00");
            map.put("currency", "USD");
            map.put("idempotencyKey", "idem_123");

            PaymentCreatedEvent event = PaymentCreatedEvent.fromMap(map);

            assertThat(event.getSchemaVersion()).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("Should throw exception for unsupported major version")
        void shouldThrowExceptionForUnsupportedMajorVersion() {
            PaymentCreatedEvent event = new PaymentCreatedEvent(
                    "pay_123", "merchant_123", "100.00", "USD", "idem_123"
            );

            assertThatThrownBy(() -> event.migrateFrom("2.0.0"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported schema version");
        }
    }

    @Nested
    @DisplayName("PaymentCompletedEvent Schema Tests")
    class PaymentCompletedEventSchemaTests {

        @Test
        @DisplayName("Should serialize payment completed event to map")
        void shouldSerializePaymentCompletedEvent() {
            PaymentCompletedEvent event = new PaymentCompletedEvent(
                    "pay_123", "merchant_123", "100.00", "USD", "txn_abc"
            );

            Map<String, Object> map = event.toMap();

            assertThat(map.get("schemaVersion")).isEqualTo("1.0.0");
            assertThat(map.get("eventType")).isEqualTo("PAYMENT_COMPLETED");
            assertThat(map.get("providerTransactionId")).isEqualTo("txn_abc");
        }

        @Test
        @DisplayName("Should deserialize payment completed event from map")
        void shouldDeserializePaymentCompletedEvent() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", "event-456");
            map.put("occurredOn", Instant.now());
            map.put("aggregateId", "pay_123");
            map.put("schemaVersion", "1.0.0");
            map.put("eventType", "PAYMENT_COMPLETED");
            map.put("merchantId", "merchant_123");
            map.put("amount", "100.00");
            map.put("currency", "USD");
            map.put("providerTransactionId", "txn_abc");

            PaymentCompletedEvent event = PaymentCompletedEvent.fromMap(map);

            assertThat(event.getId()).isEqualTo("event-456");
            assertThat(event.getProviderTransactionId()).isEqualTo("txn_abc");
        }
    }

    @Nested
    @DisplayName("PaymentFailedEvent Schema Tests")
    class PaymentFailedEventSchemaTests {

        @Test
        @DisplayName("Should serialize payment failed event to map")
        void shouldSerializePaymentFailedEvent() {
            PaymentFailedEvent event = new PaymentFailedEvent(
                    "pay_123", "merchant_123", "100.00", "USD", "ERR_001", "Insufficient funds"
            );

            Map<String, Object> map = event.toMap();

            assertThat(map.get("schemaVersion")).isEqualTo("1.0.0");
            assertThat(map.get("eventType")).isEqualTo("PAYMENT_FAILED");
            assertThat(map.get("errorCode")).isEqualTo("ERR_001");
            assertThat(map.get("errorMessage")).isEqualTo("Insufficient funds");
        }

        @Test
        @DisplayName("Should deserialize payment failed event from map")
        void shouldDeserializePaymentFailedEvent() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", "event-789");
            map.put("occurredOn", Instant.now());
            map.put("aggregateId", "pay_123");
            map.put("schemaVersion", "1.0.0");
            map.put("eventType", "PAYMENT_FAILED");
            map.put("merchantId", "merchant_123");
            map.put("amount", "100.00");
            map.put("currency", "USD");
            map.put("errorCode", "ERR_001");
            map.put("errorMessage", "Insufficient funds");

            PaymentFailedEvent event = PaymentFailedEvent.fromMap(map);

            assertThat(event.getId()).isEqualTo("event-789");
            assertThat(event.getErrorCode()).isEqualTo("ERR_001");
            assertThat(event.getErrorMessage()).isEqualTo("Insufficient funds");
        }
    }

    @Nested
    @DisplayName("Schema Evolution Tests")
    class SchemaEvolutionTests {

        @Test
        @DisplayName("Should handle additional fields gracefully")
        void shouldHandleAdditionalFieldsGracefully() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", "event-123");
            map.put("occurredOn", Instant.now());
            map.put("aggregateId", "pay_123");
            map.put("schemaVersion", "1.0.0");
            map.put("eventType", "PAYMENT_CREATED");
            map.put("merchantId", "merchant_123");
            map.put("amount", "100.00");
            map.put("currency", "USD");
            map.put("idempotencyKey", "idem_123");
            // Additional field that might be added in future versions
            map.put("newField", "future_value");

            PaymentCreatedEvent event = PaymentCreatedEvent.fromMap(map);

            assertThat(event.getMerchantId()).isEqualTo("merchant_123");
        }
    }
}
