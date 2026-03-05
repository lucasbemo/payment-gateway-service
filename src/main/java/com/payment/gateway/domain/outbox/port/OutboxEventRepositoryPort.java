package com.payment.gateway.domain.outbox.port;

import com.payment.gateway.domain.outbox.model.EventStatus;
import com.payment.gateway.domain.outbox.model.OutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Outbox event repository port interface.
 */
public interface OutboxEventRepositoryPort {
    OutboxEvent save(OutboxEvent event);
    Optional<OutboxEvent> findById(String id);
    List<OutboxEvent> findByStatus(EventStatus status);
    List<OutboxEvent> findByAggregateId(String aggregateId);
    List<OutboxEvent> findByEventType(String eventType);
    List<OutboxEvent> findPendingEventsBefore(Instant cutoffTime);
    List<OutboxEvent> findByRetryCountLessThanEqual(int maxRetryCount);
    void deleteById(String id);
}
