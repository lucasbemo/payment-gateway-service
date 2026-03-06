package com.payment.gateway.infrastructure.outbox.adapter.out.persistence;

import com.payment.gateway.domain.outbox.model.EventStatus;
import com.payment.gateway.domain.outbox.model.EventType;
import com.payment.gateway.domain.outbox.model.OutboxEvent;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventMapper {

    public OutboxEventJpaEntity toEntity(OutboxEvent event) {
        return OutboxEventJpaEntity.builder()
                .id(event.getId())
                .aggregateId(event.getAggregateId())
                .aggregateType(event.getAggregateType())
                .eventType(event.getEventType() != null ? event.getEventType().name() : null)
                .payload(event.getPayload())
                .status(event.getStatus() != null ? event.getStatus().name() : null)
                .errorMessage(event.getErrorMessage())
                .retryCount(event.getRetryCount())
                .createdAt(event.getCreatedAt())
                .publishedAt(event.getPublishedAt())
                .build();
    }

    public OutboxEvent toDomain(OutboxEventJpaEntity entity) {
        return OutboxEvent.builder()
                .id(entity.getId())
                .aggregateId(entity.getAggregateId())
                .aggregateType(entity.getAggregateType())
                .eventType(entity.getEventType() != null ? EventType.valueOf(entity.getEventType()) : null)
                .payload(entity.getPayload())
                .status(entity.getStatus() != null ? EventStatus.valueOf(entity.getStatus()) : null)
                .errorMessage(entity.getErrorMessage())
                .retryCount(entity.getRetryCount())
                .createdAt(entity.getCreatedAt())
                .publishedAt(entity.getPublishedAt())
                .updatedAt(entity.getCreatedAt())
                .build();
    }
}
