# ADR-001: Hexagonal Architecture (Ports and Adapters)

## Status

Accepted

## Context

We needed to design the architecture for the Payment Gateway Service to ensure:
- Testability of business logic independent of external dependencies
- Flexibility to swap infrastructure components (databases, messaging, external APIs)
- Clear separation of concerns between business logic and technical details
- Maintainability as the system grows

## Decision

We adopted **Hexagonal Architecture** (also known as Ports and Adapters architecture) with the following layer structure:

```
┌─────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                      │
│  Controllers | Repositories | Kafka | External APIs          │
├─────────────────────────────────────────────────────────────┤
│                    APPLICATION LAYER                         │
│  Use Cases | DTOs | Ports | Mappers                         │
├─────────────────────────────────────────────────────────────┤
│                       DOMAIN LAYER                           │
│  Entities | Value Objects | Domain Services | Domain Events │
└─────────────────────────────────────────────────────────────┘
```

### Domain Layer
- Contains business logic and rules
- No dependencies on frameworks or infrastructure
- Defines domain entities, value objects, and domain events
- Pure Java with no external annotations

### Application Layer
- Orchestrates use cases
- Defines ports (interfaces) for infrastructure
- Contains DTOs and mappers
- Application services coordinate domain objects

### Infrastructure Layer
- Implements adapters for ports defined in application layer
- Contains REST controllers, JPA repositories, Kafka producers/consumers
- Handles external integrations (Stripe, etc.)
- Manages persistence and messaging

## Consequences

### Positive
- Business logic is independent and highly testable
- Easy to swap database or messaging system
- Clear boundaries between layers
- Domain experts can understand the core logic
- Can test business logic without infrastructure

### Negative
- More boilerplate code (interfaces for everything)
- Steeper learning curve for new developers
- Need to maintain strict discipline to prevent layer violations

### Mitigation
- Use architecture tests (ArchUnit) to enforce layer boundaries
- Provide clear documentation and examples
- Code reviews focus on maintaining architecture integrity

## Example

### Port (Application Layer)
```java
public interface ProcessPaymentUseCase {
    PaymentResponse processPayment(ProcessPaymentCommand command);
}
```

### Domain Service (Domain Layer)
```java
public class PaymentDomainService {
    public Payment authorizePayment(Money amount, PaymentMethod method) {
        // Pure business logic
    }
}
```

### Adapter (Infrastructure Layer)
```java
@RestController
public class PaymentController implements PaymentApi {
    private final ProcessPaymentUseCase processPaymentUseCase;
    
    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(...) {
        // Delegates to use case
    }
}
```

## References

- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)