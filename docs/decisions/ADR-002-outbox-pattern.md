# ADR-002: Outbox Pattern for Event Reliability

## Status

Accepted

## Context

In an event-driven architecture, we need to ensure that:
- Domain events are never lost
- Events are published exactly once
- System state and events remain consistent
- Recovery from failures is possible

The traditional approach of publishing events directly after database transactions can lead to:
- Lost events if the message broker is unavailable
- Inconsistent state if the transaction succeeds but event publishing fails
- Duplicate events if retry mechanisms are not idempotent

## Decision

We implemented the **Outbox Pattern** for reliable event publishing:

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   Domain Entity  │────▶│   Outbox Table   │────▶│   Kafka Topic    │
│   (Transaction)  │     │   (Same TX)      │     │   (Publisher)    │
└──────────────────┘     └──────────────────┘     └──────────────────┘
```

### Implementation

1. **Domain Event**: Domain entities emit events when state changes
2. **Outbox Table**: Events are stored in the same database transaction
3. **Event Publisher**: Background process polls outbox and publishes to Kafka
4. **Idempotent Consumer**: Consumers handle duplicate events gracefully

### Outbox Entity
```java
@Entity
public class OutboxEvent {
    private String id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payload;
    private Instant createdAt;
    private boolean published;
}
```

### Event Flow
1. Domain service performs business operation
2. Event is created and saved to outbox table (same transaction)
3. Transaction commits (both entity and event persisted)
4. Event publisher picks up unpublished events
5. Event published to Kafka
6. Event marked as published in outbox

## Consequences

### Positive
- Guaranteed event delivery (event stored with entity)
- At-least-once delivery semantics
- Easy to replay events from outbox
- Transactional consistency between state and events
- Built-in audit trail

### Negative
- Additional database table and polling
- Slight delay in event publishing (polling interval)
- Need to handle event cleanup/archival

### Mitigation
- Use efficient polling with index on `published` flag
- Configure appropriate polling interval (configurable)
- Archive published events to cold storage
- Use change data capture (CDC) as alternative

## Alternatives Considered

1. **Direct Publishing**: Simpler but risks lost events
2. **Event Sourcing**: More complex, significant learning curve
3. **Change Data Capture (Debezium)**: Good alternative, requires additional infrastructure

## References

- [Transaction Log Trailing - Martin Kleppmann](https://martin.kleppmann.com/)
- [Outbox Pattern - Chris Richardson](https://microservices.io/patterns/data/transactional-outbox.html)