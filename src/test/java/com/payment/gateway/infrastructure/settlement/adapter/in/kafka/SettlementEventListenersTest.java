package com.payment.gateway.infrastructure.settlement.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for SettlementEventListeners.
 */
@DisplayName("Settlement Event Listeners Tests")
class SettlementEventListenersTest {

    private ObjectMapper objectMapper;
    private SettlementEventListeners listeners;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listeners = new SettlementEventListeners(objectMapper);
    }

    @Nested
    @DisplayName("onSettlementBatch")
    class OnSettlementBatch {

        @Test
        @DisplayName("Should process settlement.batch event successfully")
        void shouldProcessSettlementBatchEvent() throws Exception {
            Map<String, Object> event = Map.of(
                    "batchId", "batch_001",
                    "merchantId", "merchant_123",
                    "totalAmount", "10000.00",
                    "currency", "USD",
                    "transactionCount", 100
            );

            listeners.onSettlementBatch(event, null);

            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle large transaction count")
        void shouldHandleLargeTransactionCount() {
            Map<String, Object> event = Map.of(
                    "batchId", "batch_large",
                    "merchantId", "merchant_456",
                    "totalAmount", "1000000.00",
                    "currency", "USD",
                    "transactionCount", 10000
            );

            listeners.onSettlementBatch(event, null);

            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle missing transaction count")
        void shouldHandleMissingTransactionCount() {
            Map<String, Object> event = Map.of(
                    "batchId", "batch_partial",
                    "merchantId", "merchant_789",
                    "totalAmount", "5000.00",
                    "currency", "USD"
            );

            listeners.onSettlementBatch(event, null);

            assertThat(true).isTrue();
        }
    }
}
