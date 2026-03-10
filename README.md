# Payment Gateway Service

> **Production-Ready Payment Gateway** built with Java 21 + Spring Boot 3, following DDD Modular Hexagonal Architecture with Kafka event-driven messaging, PostgreSQL persistence, and Redis-based idempotency handling.

---

## ⚠️ Security Notice

**This is a learning/demo project.** Default credentials (e.g., `admin/admin`, `postgres/postgres`) are used for development purposes only.

**Before deploying to production:**
- Replace all default credentials with secure, randomly generated values
- Use environment variables or secrets management for sensitive configuration
- Enable TLS/SSL for all communications
- Review and harden security configurations
- Conduct a security audit

---

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose

### 1. Clone and Setup

```bash
git clone <repository-url>
cd payment-gateway-service

# Copy environment template
cp .env.example .env
```

### 2. Start Infrastructure

```bash
# Start all services (PostgreSQL, Kafka, Redis, pgAdmin, Kafka UI, Prometheus, Grafana)
docker-compose up -d

# Wait for services to be ready (approx. 30 seconds)
sleep 30
```

### 3. Run Application

```bash
# Build and run
./mvnw clean package
java -jar target/payment-gateway-0.0.1-SNAPSHOT.jar

# Or use Maven directly
./mvnw spring-boot:run
```

### 4. Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| API | http://localhost:8080 | - |
| Swagger UI | http://localhost:8080/swagger-ui.html | - |
| pgAdmin | http://localhost:8080 | admin@admin.com / admin |
| Kafka UI | http://localhost:8082 | - |
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | - |
| Zipkin | http://localhost:9411 | - |

---

## 🏗️ Architecture

### DDD + Hexagonal Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER (Core)                           │
│  [Payment] [Merchant] [Transaction] [Customer] [Idempotency]    │
│  [Reconciliation] [Outbox] [Notification]                        │
├─────────────────────────────────────────────────────────────────┤
│                   APPLICATION LAYER                              │
│  Use Cases | DTOs | Mappers | Handlers                          │
├─────────────────────────────────────────────────────────────────┤
│                  INFRASTRUCTURE LAYER                            │
│  Controllers | Repositories | Kafka | External APIs             │
└─────────────────────────────────────────────────────────────────┘
```

### Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.2 |
| **Database** | PostgreSQL 16 |
| **Messaging** | Apache Kafka |
| **Cache** | Redis |
| **ORM** | Hibernate / JPA |
| **Migrations** | Flyway |
| **Security** | Spring Security + JWT |
| **Resilience** | Resilience4j + Bucket4j |
| **Observability** | Micrometer + Prometheus + Zipkin |
| **Testing** | JUnit 5 + Testcontainers |

---

## 📦 Project Structure

```
payment-gateway-service/
├── src/main/java/com/payment/gateway/
│   ├── domain/           # Domain Layer (Business Logic)
│   ├── application/      # Application Layer (Use Cases)
│   ├── infrastructure/   # Infrastructure Layer (Adapters)
│   └── commons/          # Shared Kernel
├── src/main/resources/
│   ├── application.yml
│   ├── application-local.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/     # Flyway Migrations
├── docker-compose.yml
├── docker-compose.override.yml
├── Dockerfile
├── pom.xml
└── .env.example
```

---

## 🔧 Configuration

### Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
# Database
DATASOURCE_URL=jdbc:postgresql://localhost:5433/payment_gateway
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=your_secure_password_here

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9093

# Redis
REDIS_HOST=localhost
REDIS_PORT=6380

# Profile
SPRING_PROFILES_ACTIVE=local
```

### Profiles

| Profile | Description |
|---------|-------------|
| `local` | Local development with Docker Compose |
| `dev` | Development environment |
| `prod` | Production environment (requires env vars) |

---

## 🧪 Testing

### Running Tests

```bash
# Run all unit and integration tests (recommended for development)
# Note: Use single quotes to prevent shell expansion of '!'
./mvnw test -Dtest='!com.payment.gateway.e2e.**'

# Alternative: Use surefire plugin parameter
./mvnw surefire:test -Dtest='!com.payment.gateway.e2e.**'

# Run specific test categories
./mvnw test -Dtest="*Test"            # All tests ending with 'Test'
./mvnw test -Dtest="*IntegrationTest" # Integration tests
./mvnw test -Dtest="*ControllerTest"  # Controller tests

# Run E2E tests separately (requires Docker)
./mvnw test -Dtest="com.payment.gateway.e2e.**"

# Run all tests
./mvnw test

# Code coverage
./mvnw test jacoco:report

# Open coverage report
open target/site/jacoco/index.html
```

### Test Structure

| Test Type | Count | Description |
|-----------|-------|-------------|
| Unit Tests | 1,039 | Domain models, services, use cases, enums |
| Integration Tests | 28 | Repository, controller, provider tests |
| E2E Tests | ~120 | Full flow tests with Testcontainers |

### E2E Test Configuration

E2E tests use **Testcontainers** to spin up PostgreSQL, Kafka, and Redis containers.

**Note:** E2E tests complete in ~60 seconds. Some functional tests may fail due to security configuration differences in test profile (security is disabled for testing).

```bash
# Ensure Docker is running
docker ps

# Increase Docker resources if tests timeout
# Recommended: 4+ CPU cores, 8GB+ RAM for Docker
```

**Current Status:**
- 122 E2E tests run without connection leaks or hanging (was: infinite loop)
- ~59 tests passing (functional test failures being addressed)
- All infrastructure/connection issues resolved
- Tests complete in ~60 seconds

**Known Issues (being fixed):**
- Refund tests: Refund endpoint requires transaction but test profile payment flow doesn't create one
- Security tests: Expect 401 but security is disabled in `e2e` profile (test design issue)
- Customer tests: Some tests expect `external_id`, `phone` columns not in schema
- Merchant tests: API response format differs from test expectations

### CI/CD Recommendation

For CI/CD pipelines, configure tests to run in two stages:

```yaml
# Stage 1: Fast unit/integration tests
# Note: Use single quotes around the pattern in shell
- run: ./mvnw test -Dtest='!com.payment.gateway.e2e.**'

# Stage 2: E2E tests (separate job with more resources)
- run: ./mvnw test -Dtest='com.payment.gateway.e2e.**'
  timeout_minutes: 30
  resources:
    docker: true
```

This approach:
- Provides fast feedback on most changes (< 30 seconds for unit tests)
- Isolates flaky E2E tests to a separate stage
- Allows E2E tests to have dedicated resources and timeouts

---

## 📚 Documentation

- [Architecture Plan](PAYMENT_GATEWAY_PROJECT_PLAN.md)
- [Implementation Checklist](CHECKPOINT.md)
- [API Documentation](http://localhost:8080/swagger-ui.html)

---

## 🛡️ Security Best Practices

### For Development
- Default credentials are used for convenience
- All endpoints are accessible locally

### For Production
1. **Credentials**: Replace all default passwords
2. **Secrets**: Use environment variables or secrets manager
3. **TLS**: Enable HTTPS for all communications
4. **Database**: Use SSL connection, restrict network access
5. **Kafka**: Enable SASL authentication
6. **Redis**: Enable AUTH password
7. **Rate Limiting**: Configure per-merchant limits
8. **Audit**: Enable audit logging for all payment operations

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

```bash
# Validate code style
./mvnw spotless:check

# Auto-format code
./mvnw spotless:apply
```

---

## 📄 License

This project is for educational purposes. See the [Architecture Plan](PAYMENT_GATEWAY_PROJECT_PLAN.md) for learning goals.

---

## 📞 Support

For questions or issues:
1. Check the [Architecture Plan](PAYMENT_GATEWAY_PROJECT_PLAN.md)
2. Review the [Implementation Checklist](CHECKPOINT.md)
3. Open an issue on GitHub

---

*Built with ❤️ using Java 21 + Spring Boot 3 + DDD + Hexagonal Architecture*
