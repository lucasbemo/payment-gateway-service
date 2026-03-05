package com.payment.gateway.commons.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events.
 */
public abstract class DomainEvent {

    private final String id;
    private final Instant occurredOn;
    private final String aggregateId;
    private final String aggregateType;

    protected DomainEvent(String aggregateId, String aggregateType) {
        this.id = UUID.randomUUID().toString();
        this.occurredOn = Instant.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }

    protected DomainEvent(String id, Instant occurredOn, String aggregateId, String aggregateType) {
        this.id = id;
        this.occurredOn = occurredOn;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }

    public String getId() {
        return id;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public abstract String getEventType();
}
