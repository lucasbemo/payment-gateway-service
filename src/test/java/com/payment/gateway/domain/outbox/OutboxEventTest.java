package com.payment.gateway.domain.outbox;

import com.payment.gateway.domain.outbox.model.EventStatus;
import com.payment.gateway.domain.outbox.model.EventType;
import com.payment.gateway.domain.outbox.model.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OutboxEvent aggregate.
 */
class OutboxEventTest {

    private static final String AGGREGATE_ID = "pay_123";
    private static final String AGGREGATE_TYPE = "Payment";
    private static final EventType EVENT_TYPE = EventType.PAYMENT_CREATED;
    private static final String PAYLOAD = "{\"paymentId\":\"pay_123\",\"amount\":100.00}";

    private OutboxEvent event;

    @BeforeEach
    void setUp() {
        event = OutboxEvent.create(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD);
    }

    @Nested
    @DisplayName("OutboxEvent Creation")
    class Creation {

        @Test
        @DisplayName("Should create event with PENDING status")
        void shouldCreateEventWithPendingStatus() {
            assertNotNull(event.getId());
            assertEquals(AGGREGATE_ID, event.getAggregateId());
            assertEquals(AGGREGATE_TYPE, event.getAggregateType());
            assertEquals(EVENT_TYPE, event.getEventType());
            assertEquals(PAYLOAD, event.getPayload());
            assertEquals(EventStatus.PENDING, event.getStatus());
            assertNotNull(event.getCreatedAt());
        }

        @Test
        @DisplayName("Should create event with zero retry count")
        void shouldCreateEventWithZeroRetryCount() {
            assertEquals(0, event.getRetryCount());
        }

        @Test
        @DisplayName("Should create event with null published at")
        void shouldCreateEventWithNullPublishedAt() {
            assertNull(event.getPublishedAt());
        }
    }

    @Nested
    @DisplayName("Event Processing")
    class Processing {

        @Test
        @DisplayName("Should mark event as processing")
        void shouldMarkEventAsProcessing() {
            event.markAsProcessing();

            assertEquals(EventStatus.PROCESSING, event.getStatus());
        }

        @Test
        @DisplayName("Should mark event as published")
        void shouldMarkEventAsPublished() {
            event.markAsPublished();

            assertEquals(EventStatus.PUBLISHED, event.getStatus());
            assertNotNull(event.getPublishedAt());
            assertNull(event.getErrorMessage());
        }

        @Test
        @DisplayName("Should mark event as failed with error message")
        void shouldMarkEventAsFailed() {
            event.markAsFailed("Connection timeout");

            assertEquals(EventStatus.FAILED, event.getStatus());
            assertEquals("Connection timeout", event.getErrorMessage());
            assertEquals(1, event.getRetryCount());
        }

        @Test
        @DisplayName("Should mark event as retrying")
        void shouldMarkEventAsRetrying() {
            event.markAsRetrying();

            assertEquals(EventStatus.RETRYING, event.getStatus());
        }
    }

    @Nested
    @DisplayName("Event Retry Logic")
    class RetryLogic {

        @Test
        @DisplayName("Should allow retry when retry count is less than 3")
        void shouldAllowRetryWhenRetryCountLessThan3() {
            event.markAsFailed("Error 1");

            assertTrue(event.canRetry());
        }

        @Test
        @DisplayName("Should not allow retry when retry count reaches 3")
        void shouldNotAllowRetryWhenRetryCountReaches3() {
            event.markAsFailed("Error 1");
            event.markAsRetrying();
            event.markAsFailed("Error 2");
            event.markAsRetrying();
            event.markAsFailed("Error 3");

            assertFalse(event.canRetry());
        }

        @Test
        @DisplayName("Should increment retry count on failure")
        void shouldIncrementRetryCountOnFailure() {
            event.markAsFailed("Error 1");
            event.markAsRetrying();
            event.markAsFailed("Error 2");

            assertEquals(2, event.getRetryCount());
        }
    }

    @Nested
    @DisplayName("Event Status Checks")
    class StatusChecks {

        @Test
        @DisplayName("Should return true for isPending when status is PENDING")
        void shouldBePendingWhenStatusIsPending() {
            assertTrue(event.isPending());
        }

        @Test
        @DisplayName("Should return false for isPending when status is PROCESSING")
        void shouldNotBePendingWhenStatusIsProcessing() {
            event.markAsProcessing();

            assertFalse(event.isPending());
        }

        @Test
        @DisplayName("Should return true for isPublished when status is PUBLISHED")
        void shouldBePublishedWhenStatusIsPublished() {
            event.markAsPublished();

            assertTrue(event.isPublished());
        }

        @Test
        @DisplayName("Should return true for isFailed when status is FAILED")
        void shouldBeFailedWhenStatusIsFailed() {
            event.markAsFailed("Error");

            assertTrue(event.isFailed());
        }
    }

    @Nested
    @DisplayName("Event Builder")
    class Builder {

        @Test
        @DisplayName("Should build event with all fields")
        void shouldBuildEventWithAllFields() {
            OutboxEvent built = OutboxEvent.builder()
                    .id("custom_id")
                    .aggregateId(AGGREGATE_ID)
                    .aggregateType(AGGREGATE_TYPE)
                    .eventType(EventType.PAYMENT_COMPLETED)
                    .payload("{\"status\":\"completed\"}")
                    .status(EventStatus.PROCESSING)
                    .retryCount(5)
                    .build();

            assertEquals("custom_id", built.getId());
            assertEquals(EventType.PAYMENT_COMPLETED, built.getEventType());
            assertEquals(EventStatus.PROCESSING, built.getStatus());
            assertEquals(5, built.getRetryCount());
        }
    }
}
