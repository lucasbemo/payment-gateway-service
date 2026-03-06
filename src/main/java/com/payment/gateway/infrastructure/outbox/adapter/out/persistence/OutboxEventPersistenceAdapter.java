package com.payment.gateway.infrastructure.outbox.adapter.out.persistence;

import com.payment.gateway.domain.outbox.model.EventStatus;
import com.payment.gateway.domain.outbox.model.OutboxEvent;
import com.payment.gateway.domain.outbox.port.OutboxEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OutboxEventPersistenceAdapter implements OutboxEventRepositoryPort {

    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final OutboxEventMapper outboxEventMapper;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        var entity = outboxEventMapper.toEntity(event);
        var saved = outboxEventJpaRepository.save(entity);
        return outboxEventMapper.toDomain(saved);
    }

    @Override
    public Optional<OutboxEvent> findById(String id) {
        return outboxEventJpaRepository.findById(id).map(outboxEventMapper::toDomain);
    }

    @Override
    public List<OutboxEvent> findByStatus(EventStatus status) {
        return outboxEventJpaRepository.findByStatus(status.name()).stream()
                .map(outboxEventMapper::toDomain)
                .toList();
    }

    @Override
    public List<OutboxEvent> findByAggregateId(String aggregateId) {
        return outboxEventJpaRepository.findByAggregateId(aggregateId).stream()
                .map(outboxEventMapper::toDomain)
                .toList();
    }

    @Override
    public List<OutboxEvent> findByEventType(String eventType) {
        return outboxEventJpaRepository.findByEventType(eventType).stream()
                .map(outboxEventMapper::toDomain)
                .toList();
    }

    @Override
    public List<OutboxEvent> findPendingEventsBefore(Instant cutoffTime) {
        return outboxEventJpaRepository.findPendingEventsBefore(cutoffTime).stream()
                .map(outboxEventMapper::toDomain)
                .toList();
    }

    @Override
    public List<OutboxEvent> findByRetryCountLessThanEqual(int maxRetryCount) {
        return outboxEventJpaRepository.findByRetryCountLessThanEqual(maxRetryCount).stream()
                .map(outboxEventMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        outboxEventJpaRepository.deleteById(id);
    }
}
