package com.payment.gateway.domain.outbox.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.gateway.domain.outbox.model.EventStatus;
import com.payment.gateway.domain.outbox.model.EventType;
import com.payment.gateway.domain.outbox.model.OutboxEvent;
import com.payment.gateway.domain.outbox.port.OutboxEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Outbox domain service.
 * Contains business logic for outbox event operations.
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class OutboxEventDomainService {

    private final OutboxEventRepositoryPort repository;
    private final ObjectMapper objectMapper;

    public <T> OutboxEvent publish(String aggregateId, String aggregateType, EventType eventType, T payload) {
        log.info("Creating outbox event {} for aggregate {} ({})", eventType, aggregateId, aggregateType);

        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            OutboxEvent event = OutboxEvent.create(aggregateId, aggregateType, eventType, payloadJson);
            return repository.save(event);
        } catch (Exception e) {
            log.error("Failed to serialize payload for event {}", eventType, e);
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }

    public OutboxEvent processEvent(String eventId) {
        log.info("Processing outbox event {}", eventId);

        OutboxEvent event = repository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        if (!event.isPending()) {
            throw new IllegalStateException("Event is not pending: " + event.getStatus());
        }

        event.markAsProcessing();
        return repository.save(event);
    }

    public OutboxEvent completeEvent(String eventId) {
        log.info("Completing outbox event {}", eventId);

        OutboxEvent event = repository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        event.markAsPublished();
        return repository.save(event);
    }

    public OutboxEvent failEvent(String eventId, String errorMessage) {
        log.error("Failing outbox event {}: {}", eventId, errorMessage);

        OutboxEvent event = repository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        event.markAsFailed(errorMessage);
        return repository.save(event);
    }

    public OutboxEvent retryEvent(String eventId) {
        log.info("Retrying outbox event {}", eventId);

        OutboxEvent event = repository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        if (!event.canRetry()) {
            throw new IllegalStateException("Event cannot be retried: " + eventId);
        }

        event.markAsRetrying();
        return repository.save(event);
    }

    public List<OutboxEvent> getPendingEvents() {
        return repository.findByStatus(EventStatus.PENDING);
    }

    public List<OutboxEvent> getPendingEventsBefore(Instant cutoffTime) {
        return repository.findPendingEventsBefore(cutoffTime);
    }

    public List<OutboxEvent> getEventsForRetry(int maxRetryCount) {
        return repository.findByRetryCountLessThanEqual(maxRetryCount);
    }

    public List<OutboxEvent> getEventsByAggregateId(String aggregateId) {
        return repository.findByAggregateId(aggregateId);
    }
}
