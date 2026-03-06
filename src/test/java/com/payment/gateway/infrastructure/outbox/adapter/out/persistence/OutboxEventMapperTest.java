package com.payment.gateway.infrastructure.outbox.adapter.out.persistence;

import com.payment.gateway.domain.outbox.model.EventStatus;
import com.payment.gateway.domain.outbox.model.EventType;
import com.payment.gateway.domain.outbox.model.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventMapperTest {

    private OutboxEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OutboxEventMapper();
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map domain OutboxEvent to OutboxEventJpaEntity with all fields")
        void shouldMapDomainToEntity() {
            // given
            Instant now = Instant.now();
            Instant publishedAt = now.plusSeconds(10);
            OutboxEvent event = OutboxEvent.builder()
                    .id("event-001")
                    .aggregateId("agg-001")
                    .aggregateType("Payment")
                    .eventType(EventType.PAYMENT_CREATED)
                    .payload("{\"paymentId\":\"pay-001\"}")
                    .status(EventStatus.PENDING)
                    .errorMessage(null)
                    .retryCount(0)
                    .createdAt(now)
                    .publishedAt(publishedAt)
                    .updatedAt(now)
                    .build();

            // when
            OutboxEventJpaEntity entity = mapper.toEntity(event);

            // then
            assertThat(entity.getId()).isEqualTo("event-001");
            assertThat(entity.getAggregateId()).isEqualTo("agg-001");
            assertThat(entity.getAggregateType()).isEqualTo("Payment");
            assertThat(entity.getEventType()).isEqualTo("PAYMENT_CREATED");
            assertThat(entity.getPayload()).isEqualTo("{\"paymentId\":\"pay-001\"}");
            assertThat(entity.getStatus()).isEqualTo("PENDING");
            assertThat(entity.getErrorMessage()).isNull();
            assertThat(entity.getRetryCount()).isEqualTo(0);
            assertThat(entity.getCreatedAt()).isEqualTo(now);
            assertThat(entity.getPublishedAt()).isEqualTo(publishedAt);
        }

        @Test
        @DisplayName("should handle null eventType and status")
        void shouldHandleNullEventTypeAndStatus() {
            OutboxEvent event = OutboxEvent.builder()
                    .id("event-null")
                    .aggregateId("agg-null")
                    .aggregateType("Payment")
                    .eventType(null)
                    .payload("{}")
                    .status(null)
                    .createdAt(Instant.now())
                    .build();

            OutboxEventJpaEntity entity = mapper.toEntity(event);

            assertThat(entity.getEventType()).isNull();
            assertThat(entity.getStatus()).isNull();
        }

        @Test
        @DisplayName("should map FAILED event with error message")
        void shouldMapFailedEventWithError() {
            OutboxEvent event = OutboxEvent.builder()
                    .id("event-failed")
                    .aggregateId("agg-failed")
                    .aggregateType("Transaction")
                    .eventType(EventType.TRANSACTION_FAILED)
                    .payload("{\"error\":true}")
                    .status(EventStatus.FAILED)
                    .errorMessage("Connection timeout")
                    .retryCount(3)
                    .createdAt(Instant.now())
                    .build();

            OutboxEventJpaEntity entity = mapper.toEntity(event);

            assertThat(entity.getStatus()).isEqualTo("FAILED");
            assertThat(entity.getErrorMessage()).isEqualTo("Connection timeout");
            assertThat(entity.getRetryCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("should map OutboxEventJpaEntity to domain OutboxEvent with all fields")
        void shouldMapEntityToDomain() {
            // given
            Instant now = Instant.now();
            Instant publishedAt = now.plusSeconds(5);
            OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder()
                    .id("event-entity-001")
                    .aggregateId("agg-entity-001")
                    .aggregateType("Refund")
                    .eventType("REFUND_PROCESSED")
                    .payload("{\"refundId\":\"ref-001\"}")
                    .status("PUBLISHED")
                    .errorMessage(null)
                    .retryCount(0)
                    .createdAt(now)
                    .publishedAt(publishedAt)
                    .build();

            // when
            OutboxEvent event = mapper.toDomain(entity);

            // then
            assertThat(event.getId()).isEqualTo("event-entity-001");
            assertThat(event.getAggregateId()).isEqualTo("agg-entity-001");
            assertThat(event.getAggregateType()).isEqualTo("Refund");
            assertThat(event.getEventType()).isEqualTo(EventType.REFUND_PROCESSED);
            assertThat(event.getPayload()).isEqualTo("{\"refundId\":\"ref-001\"}");
            assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
            assertThat(event.getErrorMessage()).isNull();
            assertThat(event.getRetryCount()).isEqualTo(0);
            assertThat(event.getCreatedAt()).isEqualTo(now);
            assertThat(event.getPublishedAt()).isEqualTo(publishedAt);
            // updatedAt is set to entity.getCreatedAt() in the mapper
            assertThat(event.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should handle null eventType and status in entity")
        void shouldHandleNullEventTypeAndStatus() {
            OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder()
                    .id("event-null")
                    .aggregateId("agg-null")
                    .aggregateType("Payment")
                    .eventType(null)
                    .payload("{}")
                    .status(null)
                    .createdAt(Instant.now())
                    .build();

            OutboxEvent event = mapper.toDomain(entity);

            assertThat(event.getEventType()).isNull();
            assertThat(event.getStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("round-trip")
    class RoundTrip {

        @Test
        @DisplayName("should preserve key data through toEntity then toDomain")
        void shouldPreserveDataThroughRoundTrip() {
            OutboxEvent original = OutboxEvent.create(
                    "agg-rt", "Payment",
                    EventType.PAYMENT_COMPLETED,
                    "{\"status\":\"completed\"}"
            );

            OutboxEventJpaEntity entity = mapper.toEntity(original);
            OutboxEvent restored = mapper.toDomain(entity);

            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getAggregateId()).isEqualTo(original.getAggregateId());
            assertThat(restored.getAggregateType()).isEqualTo(original.getAggregateType());
            assertThat(restored.getEventType()).isEqualTo(original.getEventType());
            assertThat(restored.getPayload()).isEqualTo(original.getPayload());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getRetryCount()).isEqualTo(original.getRetryCount());
        }
    }
}
