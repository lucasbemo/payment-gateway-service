# Changelog

All notable changes to the Payment Gateway Service project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Phase 11: Production Readiness (in progress)
- CI/CD Pipeline with GitHub Actions
- AI Code Review using Alibaba Cloud Coding Plan

## [1.0.0] - 2026-03-20

### Phase 10: API Documentation (100%)
#### Added
- OpenAPI/Swagger configuration with security schemes and servers
- 6 API documentation interfaces (PaymentApi, MerchantApi, CustomerApi, RefundApi, TransactionApi, ReconciliationApi)
- @Schema annotations on all DTOs (16 files)
- `docs/API_DOCUMENTATION.md` - Full API reference with examples
- `docs/ERROR_CODES.md` - All 20 error codes documented
- `docs/GETTING_STARTED.md` - Quick start guide with SDK examples
- `docs/WEBHOOKS.md` - Webhook events, payloads, signature verification
- Postman collection with 21 endpoints and 3 environments

### Phase 9: Observability (100%)
#### Added
- Health checks with Spring Boot Actuator
- Prometheus metrics integration
- Distributed tracing with Zipkin
- Structured logging with MDC
- Grafana dashboards for monitoring
- Alert rules for critical metrics

### Phase 9.5: E2E Tests (100%)
#### Added
- 12 E2E test classes covering all major flows
- Payment processing E2E tests
- Refund processing E2E tests
- Transaction management E2E tests
- Kafka event E2E tests
- Outbox pattern E2E tests
- Merchant/Customer management E2E tests
- Security E2E tests
- Resilience E2E tests
- Reconciliation E2E tests
- Observability E2E tests

### Phase 8: Testing (100%)
#### Added
- 1,161 unit tests (all passing)
- Integration tests for repositories
- Controller tests with MockMvc
- Architecture tests (Hexagonal pattern verification)
- Commons tests for exceptions and utils

### Phase 7: Event-Driven Architecture (100%)
#### Added
- Kafka topics configuration (8 topics)
- Outbox pattern for reliable event publishing
- Event listeners for domain events
- Schema versioning for events
- Dead letter queue handling

### Phase 6: Resilience Patterns (100%)
#### Added
- Circuit breaker with Resilience4j
- Retry with exponential backoff
- Rate limiter configuration
- Bulkhead isolation
- Time limiter for external calls

### Phase 5: Security (100%)
#### Added
- Spring Security configuration
- JWT authentication
- API Key authentication
- Card tokenization service
- AES encryption for sensitive data
- PCI DSS compliance considerations

### Phase 4: Infrastructure Layer (100%)
#### Added
- REST controllers for all domains
- JPA repositories
- Kafka publishers and consumers
- Redis cache integration
- External gateway adapters (Stripe stub)
- Global exception handling
- Request/response logging

### Phase 3: Application Layer (100%)
#### Added
- 20 use case interfaces and implementations
- DTOs for all operations
- Mappers (MapStruct)
- Application event handlers
- Transaction management

### Phase 2: Domain Layer (100%)
#### Added
- Payment aggregate and domain services
- Merchant aggregate and domain services
- Customer aggregate and domain services
- Transaction aggregate and domain services
- Refund aggregate and domain services
- Reconciliation aggregate and domain services
- Outbox entity for event reliability
- Domain events and exceptions
- Value objects and enums

### Phase 1: Project Initialization (100%)
#### Added
- Maven project structure with Java 21
- Spring Boot 3.2+ configuration
- Package structure (DDD Hexagonal)
- Docker Compose with PostgreSQL, Kafka, Redis, pgAdmin, Kafka UI, Prometheus, Grafana, Zipkin, MinIO
- Application configuration files
- Initial Git repository

## Architecture

### Technology Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3.2
- **Database:** PostgreSQL 16
- **Message Broker:** Apache Kafka
- **Cache:** Redis
- **Containerization:** Docker

### Project Statistics
- **Domain Models:** 8 bounded contexts
- **Use Cases:** 20 use cases
- **REST Endpoints:** 21 endpoints across 6 API groups
- **Database Tables:** 12 tables
- **Kafka Topics:** 8 topics
- **Unit Tests:** 1,161 tests
- **E2E Tests:** ~120 test scenarios
- **Code Coverage:** >80%

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 1.0.0 | 2026-03-20 | Production-ready release with all 10 phases complete |
| 0.1.0 | 2026-02-15 | Initial project setup |

---

[Unreleased]: https://github.com/lucasbemo/payment-gateway-service/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/lucasbemo/payment-gateway-service/releases/tag/v1.0.0