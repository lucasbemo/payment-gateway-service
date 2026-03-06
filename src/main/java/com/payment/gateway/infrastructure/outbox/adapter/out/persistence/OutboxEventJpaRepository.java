package com.payment.gateway.infrastructure.outbox.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, String> {

    List<OutboxEventJpaEntity> findByStatus(String status);

    List<OutboxEventJpaEntity> findByAggregateId(String aggregateId);

    List<OutboxEventJpaEntity> findByEventType(String eventType);

    @Query("SELECT e FROM OutboxEventJpaEntity e WHERE e.status = 'PENDING' AND e.createdAt < :cutoffTime")
    List<OutboxEventJpaEntity> findPendingEventsBefore(@Param("cutoffTime") Instant cutoffTime);

    List<OutboxEventJpaEntity> findByRetryCountLessThanEqual(int maxRetryCount);
}
