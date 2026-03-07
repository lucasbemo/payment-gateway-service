package com.payment.gateway.infrastructure.outbox.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "aggregate_id", nullable = false, length = 36)
    private String aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    private OutboxEventJpaEntity(Builder builder) {
        this.id = builder.id;
        this.aggregateId = builder.aggregateId;
        this.aggregateType = builder.aggregateType;
        this.eventType = builder.eventType;
        this.payload = builder.payload;
        this.status = builder.status;
        this.errorMessage = builder.errorMessage;
        this.retryCount = builder.retryCount;
        this.createdAt = builder.createdAt;
        this.publishedAt = builder.publishedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String aggregateId;
        private String aggregateType;
        private String eventType;
        private String payload;
        private String status;
        private String errorMessage;
        private Integer retryCount;
        private Instant createdAt;
        private Instant publishedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder aggregateId(String aggregateId) { this.aggregateId = aggregateId; return this; }
        public Builder aggregateType(String aggregateType) { this.aggregateType = aggregateType; return this; }
        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder payload(String payload) { this.payload = payload; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder retryCount(Integer retryCount) { this.retryCount = retryCount; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder publishedAt(Instant publishedAt) { this.publishedAt = publishedAt; return this; }

        public OutboxEventJpaEntity build() {
            return new OutboxEventJpaEntity(this);
        }
    }
}
