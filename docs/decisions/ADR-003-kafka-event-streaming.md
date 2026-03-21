# ADR-003: Apache Kafka for Event Streaming

## Status

Accepted

## Context

The Payment Gateway Service requires event streaming capabilities for:
- Asynchronous processing of payments
- Event-driven communication between services
- Real-time notifications to merchants (webhooks)
- Audit logging and compliance
- Event replay for recovery

We evaluated several messaging systems:
- Apache Kafka
- RabbitMQ
- AWS SQS/SNS
- Google Pub/Sub

## Decision

We selected **Apache Kafka** as our event streaming platform.

### Reasons

| Criteria | Kafka | RabbitMQ | SQS/SNS |
|----------|-------|----------|---------|
| Event Retention | ✅ Days/Weeks | ❌ Queue-based | ❌ No retention |
| Replay Events | ✅ Yes | ❌ No | ❌ No |
| Throughput | ✅ Millions/sec | ⚠️ Thousands/sec | ⚠️ Varies |
| Ordering | ✅ Partition-level | ⚠️ Queue-level | ❌ No ordering |
| Scalability | ✅ Horizontal | ⚠️ Vertical first | ✅ Managed |
| Event Schema | ✅ Schema Registry | ❌ No native | ❌ No native |

### Kafka Topics

| Topic | Purpose | Partitions |
|-------|---------|------------|
| `payment-events` | Payment lifecycle events | 6 |
| `refund-events` | Refund processing | 3 |
| `transaction-events` | Transaction state changes | 6 |
| `customer-events` | Customer management | 3 |
| `merchant-events` | Merchant management | 3 |
| `outbox-events` | Outbox pattern events | 3 |
| `reconciliation-events` | Reconciliation events | 3 |
| `notification-events` | Notification delivery | 3 |

## Consequences

### Positive
- High throughput and low latency
- Event replay capability for debugging and recovery
- Strong ordering guarantees within partitions
- Event retention for compliance
- Excellent monitoring via Kafka UI

### Negative
- More complex setup than RabbitMQ
- Requires Zookeeper (or KRaft mode)
- Need to manage partitions and consumer groups
- Learning curve for operations team

### Mitigation
- Use Docker Compose for development
- Use managed Kafka (Confluent, AWS MSK) for production
- Provide comprehensive documentation
- Monitor consumer lag and broker health

## Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:19092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: payment-gateway-group
      auto-offset-reset: earliest
```

## References

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/reference/)