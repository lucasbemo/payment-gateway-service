package com.payment.gateway.commons.event;

import java.time.Instant;

/**
 * Base class for integration events published to Kafka.
 */
public abstract class IntegrationEvent {

    private final String id;
    private final Instant occurredOn;
    private final String aggregateId;

    protected IntegrationEvent(String aggregateId) {
        this.id = java.util.UUID.randomUUID().toString();
        this.occurredOn = Instant.now();
        this.aggregateId = aggregateId;
    }

    protected IntegrationEvent(String id, Instant occurredOn, String aggregateId) {
        this.id = id;
        this.occurredOn = occurredOn;
        this.aggregateId = aggregateId;
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

    public abstract String getEventType();
}
