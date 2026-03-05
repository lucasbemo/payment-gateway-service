package com.payment.gateway.domain.outbox.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * OutboxEvent aggregate root.
 * Represents an event in the outbox pattern for reliable event publishing.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {
    private String id;
    private String aggregateId;
    private String aggregateType;
    private EventType eventType;
    private String payload;
    private EventStatus status;
    private String errorMessage;
    private Integer retryCount;
    private Instant createdAt;
    private Instant publishedAt;
    private Instant updatedAt;

    private OutboxEvent(Builder builder) {
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
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static OutboxEvent create(String aggregateId, String aggregateType, EventType eventType, String payload) {
        Instant now = Instant.now();
        return new Builder()
                .id(UUID.randomUUID().toString())
                .aggregateId(aggregateId)
                .aggregateType(aggregateType)
                .eventType(eventType)
                .payload(payload)
                .status(EventStatus.PENDING)
                .retryCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void markAsProcessing() {
        this.status = EventStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void markAsPublished() {
        this.status = EventStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.updatedAt = this.publishedAt;
        this.errorMessage = null;
    }

    public void markAsFailed(String errorMessage) {
        this.status = EventStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount = this.retryCount != null ? this.retryCount + 1 : 1;
        this.updatedAt = Instant.now();
    }

    public void markAsRetrying() {
        this.status = EventStatus.RETRYING;
        this.updatedAt = Instant.now();
    }

    public boolean canRetry() {
        return this.retryCount != null && this.retryCount < 3;
    }

    public boolean isPending() {
        return this.status == EventStatus.PENDING;
    }

    public boolean isPublished() {
        return this.status == EventStatus.PUBLISHED;
    }

    public boolean isFailed() {
        return this.status == EventStatus.FAILED;
    }

    public static class Builder {
        private String id;
        private String aggregateId;
        private String aggregateType;
        private EventType eventType;
        private String payload;
        private EventStatus status;
        private String errorMessage;
        private Integer retryCount;
        private Instant createdAt;
        private Instant publishedAt;
        private Instant updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        public Builder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder status(EventStatus status) {
            this.status = status;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder retryCount(Integer retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder publishedAt(Instant publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public OutboxEvent build() {
            return new OutboxEvent(this);
        }
    }
}
