package com.payment.gateway.domain.outbox.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.domain.outbox.model.EventStatus;
import com.payment.gateway.domain.outbox.model.EventType;
import com.payment.gateway.domain.outbox.model.OutboxEvent;
import com.payment.gateway.domain.outbox.port.OutboxEventRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxEventDomainService Tests")
class OutboxEventDomainServiceTest {

    @Mock
    private OutboxEventRepositoryPort repository;

    @Mock
    private ObjectMapper objectMapper;

    private OutboxEventDomainService outboxEventDomainService;

    private final String EVENT_ID = "evt_123";
    private final String AGGREGATE_ID = "pay_123";
    private final String AGGREGATE_TYPE = "Payment";
    private final EventType EVENT_TYPE = EventType.PAYMENT_CREATED;
    private final String PAYLOAD_JSON = "{\"paymentId\":\"pay_123\",\"amount\":1500.00}";

    @BeforeEach
    void setUp() {
        outboxEventDomainService = new OutboxEventDomainService(repository, objectMapper);
    }

    @Nested
    @DisplayName("Publish Event")
    class PublishEventTests {

        @Test
        @DisplayName("Should publish event successfully when payload serialization succeeds")
        void shouldPublishEventSuccessfully() throws Exception {
            // Given
            Object payload = new Object();
            OutboxEvent event = OutboxEvent.create(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD_JSON);
            given(objectMapper.writeValueAsString(payload)).willReturn(PAYLOAD_JSON);
            given(repository.save(any(OutboxEvent.class))).willReturn(event);

            // When
            OutboxEvent result = outboxEventDomainService.publish(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, payload);

            // Then
            assertThat(result).isNotNull();
            verify(objectMapper).writeValueAsString(payload);
            verify(repository).save(any(OutboxEvent.class));
        }

        @Test
        @DisplayName("Should throw exception when payload serialization fails")
        void shouldThrowExceptionWhenSerializationFails() throws Exception {
            // Given
            Object payload = new Object();
            given(objectMapper.writeValueAsString(payload)).willThrow(new RuntimeException("Serialization error"));

            // When & Then
            assertThatThrownBy(() -> outboxEventDomainService.publish(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, payload))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to serialize");

            verify(objectMapper).writeValueAsString(payload);
            verify(repository, never()).save(any(OutboxEvent.class));
        }
    }

    @Nested
    @DisplayName("Process Event")
    class ProcessEventTests {

        @Test
        @DisplayName("Should process pending event successfully")
        void shouldProcessEventSuccessfully() {
            // Given
            OutboxEvent event = OutboxEvent.create(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD_JSON);
            given(repository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(repository.save(any(OutboxEvent.class))).willReturn(event);

            // When
            OutboxEvent result = outboxEventDomainService.processEvent(EVENT_ID);

            // Then
            assertThat(result).isEqualTo(event);
            verify(repository).findById(EVENT_ID);
            verify(repository).save(event);
        }

        @Test
        @DisplayName("Should throw exception when event not found")
        void shouldThrowExceptionWhenEventNotFound() {
            // Given
            given(repository.findById(EVENT_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> outboxEventDomainService.processEvent(EVENT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event not found");

            verify(repository).findById(EVENT_ID);
        }

        @Test
        @DisplayName("Should throw exception when event is not pending")
        void shouldThrowExceptionWhenEventIsNotPending() {
            // Given
            OutboxEvent event = OutboxEvent.create(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD_JSON);
            event.markAsPublished();
            given(repository.findById(EVENT_ID)).willReturn(Optional.of(event));

            // When & Then
            assertThatThrownBy(() -> outboxEventDomainService.processEvent(EVENT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Event is not pending");

            verify(repository).findById(EVENT_ID);
        }
    }

    @Nested
    @DisplayName("Complete Event")
    class CompleteEventTests {

        @Test
        @DisplayName("Should complete event successfully")
        void shouldCompleteEventSuccessfully() {
            // Given
            OutboxEvent event = OutboxEvent.create(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD_JSON);
            given(repository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(repository.save(any(OutboxEvent.class))).willReturn(event);

            // When
            OutboxEvent result = outboxEventDomainService.completeEvent(EVENT_ID);

            // Then
            assertThat(result).isEqualTo(event);
            verify(repository).findById(EVENT_ID);
            verify(repository).save(event);
        }

        @Test
        @DisplayName("Should throw exception when event not found")
        void shouldThrowExceptionWhenEventNotFoundForComplete() {
            // Given
            given(repository.findById(EVENT_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> outboxEventDomainService.completeEvent(EVENT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event not found");
        }
    }

    @Nested
    @DisplayName("Fail Event")
    class FailEventTests {

        @Test
        @DisplayName("Should fail event with error message")
        void shouldFailEventWithErrorMessage() {
            // Given
            OutboxEvent event = OutboxEvent.create(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD_JSON);
            String errorMessage = "Processing failed";
            given(repository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(repository.save(any(OutboxEvent.class))).willReturn(event);

            // When
            OutboxEvent result = outboxEventDomainService.failEvent(EVENT_ID, errorMessage);

            // Then
            assertThat(result).isEqualTo(event);
            verify(repository).findById(EVENT_ID);
            verify(repository).save(event);
        }
    }

    @Nested
    @DisplayName("Retry Event")
    class RetryEventTests {

        @Test
        @DisplayName("Should retry event when retry is available")
        void shouldRetryEventWhenRetryAvailable() {
            // Given
            OutboxEvent event = OutboxEvent.create(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD_JSON);
            event.markAsFailed("Temporary error");
            given(repository.findById(EVENT_ID)).willReturn(Optional.of(event));
            given(repository.save(any(OutboxEvent.class))).willReturn(event);

            // When
            OutboxEvent result = outboxEventDomainService.retryEvent(EVENT_ID);

            // Then
            assertThat(result).isEqualTo(event);
            verify(repository).findById(EVENT_ID);
            verify(repository).save(event);
        }

        @Test
        @DisplayName("Should throw exception when event cannot retry")
        void shouldThrowExceptionWhenEventCannotRetry() {
            // Given
            OutboxEvent event = OutboxEvent.create(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD_JSON);
            event.markAsPublished(); // PUBLISHED events cannot retry
            event.markAsFailed("Error");
            event.markAsFailed("Error");
            event.markAsFailed("Error"); // 3 failures - cannot retry anymore
            given(repository.findById(EVENT_ID)).willReturn(Optional.of(event));

            // When & Then
            assertThatThrownBy(() -> outboxEventDomainService.retryEvent(EVENT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be retried");
        }

        @Test
        @DisplayName("Should throw exception when event not found for retry")
        void shouldThrowExceptionWhenEventNotFoundForRetry() {
            // Given
            given(repository.findById(EVENT_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> outboxEventDomainService.retryEvent(EVENT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event not found");
        }
    }

    @Nested
    @DisplayName("Get Pending Events")
    class GetPendingEventsTests {

        @Test
        @DisplayName("Should return list of pending events")
        void shouldReturnListOfPendingEvents() {
            // Given
            OutboxEvent event1 = OutboxEvent.create(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD_JSON);
            OutboxEvent event2 = OutboxEvent.create("agg_456", AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD_JSON);
            given(repository.findByStatus(EventStatus.PENDING)).willReturn(List.of(event1, event2));

            // When
            List<OutboxEvent> result = outboxEventDomainService.getPendingEvents();

            // Then
            assertThat(result).hasSize(2);
            verify(repository).findByStatus(EventStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Get Pending Events Before")
    class GetPendingEventsBeforeTests {

        @Test
        @DisplayName("Should return pending events before cutoff time")
        void shouldReturnPendingEventsBeforeCutoffTime() {
            // Given
            Instant cutoffTime = Instant.now().minusSeconds(3600);
            OutboxEvent event = OutboxEvent.create(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD_JSON);
            given(repository.findPendingEventsBefore(cutoffTime)).willReturn(List.of(event));

            // When
            List<OutboxEvent> result = outboxEventDomainService.getPendingEventsBefore(cutoffTime);

            // Then
            assertThat(result).hasSize(1);
            verify(repository).findPendingEventsBefore(cutoffTime);
        }
    }

    @Nested
    @DisplayName("Get Events For Retry")
    class GetEventsForRetryTests {

        @Test
        @DisplayName("Should return events with retry count less than max")
        void shouldReturnEventsWithRetryCountLessThanMax() {
            // Given
            int maxRetryCount = 3;
            OutboxEvent event = OutboxEvent.create(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD_JSON);
            given(repository.findByRetryCountLessThanEqual(maxRetryCount)).willReturn(List.of(event));

            // When
            List<OutboxEvent> result = outboxEventDomainService.getEventsForRetry(maxRetryCount);

            // Then
            assertThat(result).hasSize(1);
            verify(repository).findByRetryCountLessThanEqual(maxRetryCount);
        }
    }

    @Nested
    @DisplayName("Get Events By Aggregate ID")
    class GetEventsByAggregateIdTests {

        @Test
        @DisplayName("Should return events by aggregate ID")
        void shouldReturnEventsByAggregateId() {
            // Given
            OutboxEvent event = OutboxEvent.create(AGGREGATE_ID, AGGREGATE_TYPE, EVENT_TYPE, PAYLOAD_JSON);
            given(repository.findByAggregateId(AGGREGATE_ID)).willReturn(List.of(event));

            // When
            List<OutboxEvent> result = outboxEventDomainService.getEventsByAggregateId(AGGREGATE_ID);

            // Then
            assertThat(result).hasSize(1);
            verify(repository).findByAggregateId(AGGREGATE_ID);
        }
    }
}
