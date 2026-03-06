package com.payment.gateway.infrastructure.outbox.adapter.out.kafka;

import com.payment.gateway.domain.outbox.model.EventStatus;
import com.payment.gateway.domain.outbox.model.EventType;
import com.payment.gateway.domain.outbox.model.OutboxEvent;
import com.payment.gateway.domain.outbox.port.OutboxEventRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for OutboxPollingScheduler.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox Polling Scheduler Tests")
class OutboxPollingSchedulerTest {

    @Mock
    private OutboxEventRepositoryPort outboxEventRepositoryPort;

    @Mock
    private KafkaOutboxEventPublisher kafkaOutboxEventPublisher;

    @InjectMocks
    private OutboxPollingScheduler scheduler;

    private OutboxEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = OutboxEvent.create(
                "pay_test_123",
                "Payment",
                EventType.PAYMENT_CREATED,
                "{\"paymentId\":\"pay_test\",\"amount\":100.00}"
        );
    }

    @Nested
    @DisplayName("pollAndPublishEvents")
    class PollAndPublishEvents {

        @Test
        @DisplayName("Should do nothing when no pending events exist")
        void shouldDoNothingWhenNoPendingEvents() {
            when(outboxEventRepositoryPort.findByStatus(EventStatus.PENDING))
                    .thenReturn(List.of());

            scheduler.pollAndPublishEvents();

            verify(outboxEventRepositoryPort, never()).save(any());
            verify(kafkaOutboxEventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("Should process pending events and mark as published")
        void shouldProcessPendingEvents() {
            when(outboxEventRepositoryPort.findByStatus(EventStatus.PENDING))
                    .thenReturn(List.of(testEvent));
            when(kafkaOutboxEventPublisher.publish(testEvent))
                    .thenReturn(true);

            scheduler.pollAndPublishEvents();

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepositoryPort, Mockito.atLeastOnce()).save(captor.capture());

            OutboxEvent savedEvent = captor.getAllValues().get(captor.getAllValues().size() - 1);
            assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        }

        @Test
        @DisplayName("Should mark event as failed when publishing fails")
        void shouldMarkEventAsFailedWhenPublishingFails() {
            when(outboxEventRepositoryPort.findByStatus(EventStatus.PENDING))
                    .thenReturn(List.of(testEvent));
            when(kafkaOutboxEventPublisher.publish(testEvent))
                    .thenReturn(false);

            scheduler.pollAndPublishEvents();

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepositoryPort, Mockito.atLeastOnce()).save(captor.capture());

            OutboxEvent savedEvent = captor.getAllValues().get(captor.getAllValues().size() - 1);
            assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.FAILED);
            assertThat(savedEvent.getErrorMessage()).isEqualTo("Failed to publish to Kafka");
        }

        @Test
        @DisplayName("Should handle exception during event processing")
        void shouldHandleExceptionDuringProcessing() {
            when(outboxEventRepositoryPort.findByStatus(EventStatus.PENDING))
                    .thenReturn(List.of(testEvent));
            when(kafkaOutboxEventPublisher.publish(testEvent))
                    .thenThrow(new RuntimeException("Kafka connection error"));

            scheduler.pollAndPublishEvents();

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepositoryPort, Mockito.atLeastOnce()).save(captor.capture());

            OutboxEvent savedEvent = captor.getAllValues().get(captor.getAllValues().size() - 1);
            assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.FAILED);
            assertThat(savedEvent.getErrorMessage()).contains("Kafka connection error");
        }
    }

    @Nested
    @DisplayName("retryFailedEvents")
    class RetryFailedEvents {

        @Test
        @DisplayName("Should retry failed events with retry count less than 3")
        void shouldRetryFailedEventsWithRetryCountLessThan3() {
            OutboxEvent failedEvent = OutboxEvent.builder()
                    .id("event_failed")
                    .aggregateId("pay_failed")
                    .aggregateType("Payment")
                    .eventType(EventType.PAYMENT_FAILED)
                    .payload("{}")
                    .status(EventStatus.FAILED)
                    .errorMessage("Previous error")
                    .retryCount(1)
                    .build();

            when(outboxEventRepositoryPort.findByStatus(EventStatus.FAILED))
                    .thenReturn(List.of(failedEvent));
            when(kafkaOutboxEventPublisher.publish(failedEvent))
                    .thenReturn(true);

            scheduler.retryFailedEvents();

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepositoryPort, Mockito.atLeastOnce()).save(captor.capture());

            OutboxEvent savedEvent = captor.getAllValues().get(captor.getAllValues().size() - 1);
            assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        }

        @Test
        @DisplayName("Should not retry events that exceeded max retries")
        void shouldNotRetryEventsExceedingMaxRetries() {
            OutboxEvent exhaustedEvent = OutboxEvent.builder()
                    .id("event_exhausted")
                    .aggregateId("pay_exhausted")
                    .aggregateType("Payment")
                    .eventType(EventType.PAYMENT_FAILED)
                    .payload("{}")
                    .status(EventStatus.FAILED)
                    .errorMessage("Max retries exceeded")
                    .retryCount(3)
                    .build();

            when(outboxEventRepositoryPort.findByStatus(EventStatus.FAILED))
                    .thenReturn(List.of(exhaustedEvent));

            scheduler.retryFailedEvents();

            verify(outboxEventRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should increment retry count on failed retry")
        void shouldIncrementRetryCountOnFailedRetry() {
            OutboxEvent failedEvent = OutboxEvent.builder()
                    .id("event_retry")
                    .aggregateId("pay_retry")
                    .aggregateType("Payment")
                    .eventType(EventType.PAYMENT_FAILED)
                    .payload("{}")
                    .status(EventStatus.FAILED)
                    .errorMessage("Previous error")
                    .retryCount(1)
                    .build();

            when(outboxEventRepositoryPort.findByStatus(EventStatus.FAILED))
                    .thenReturn(List.of(failedEvent));
            when(kafkaOutboxEventPublisher.publish(failedEvent))
                    .thenReturn(false);

            scheduler.retryFailedEvents();

            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(outboxEventRepositoryPort, Mockito.atLeastOnce()).save(captor.capture());

            OutboxEvent savedEvent = captor.getAllValues().get(captor.getAllValues().size() - 1);
            assertThat(savedEvent.getRetryCount()).isEqualTo(2);
        }
    }
}
