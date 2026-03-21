# Development Guide

This guide provides detailed instructions for developing and contributing to the Payment Gateway Service.

---

## Table of Contents

- [Development Environment](#development-environment)
- [Project Structure](#project-structure)
- [Building the Project](#building-the-project)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [Debugging](#debugging)
- [IDE Configuration](#ide-configuration)
- [Database Management](#database-management)
- [Kafka Management](#kafka-management)
- [Code Style](#code-style)
- [Troubleshooting](#troubleshooting)

---

## Development Environment

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| Java | 21+ | Runtime |
| Maven | 3.8+ | Build tool |
| Docker | Latest | Containerization |
| Docker Compose | Latest | Multi-container setup |
| Git | Latest | Version control |

### Recommended IDE

- **IntelliJ IDEA Ultimate** (recommended)
- VS Code with Java extensions
- Eclipse with Spring Tools

---

## Project Structure

```
payment-gateway-service/
├── src/main/java/com/payment/gateway/
│   ├── domain/              # Domain layer (core business logic)
│   │   ├── payment/         # Payment bounded context
│   │   ├── merchant/        # Merchant bounded context
│   │   ├── customer/        # Customer bounded context
│   │   ├── transaction/     # Transaction bounded context
│   │   ├── refund/          # Refund bounded context
│   │   ├── reconciliation/  # Reconciliation bounded context
│   │   └── outbox/          # Outbox pattern
│   ├── application/         # Application layer (use cases)
│   ├── infrastructure/      # Infrastructure layer (adapters)
│   └── commons/             # Shared utilities
├── src/main/resources/
│   ├── application.yml      # Main configuration
│   ├── application-local.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/        # Flyway migrations
├── src/test/                # Test sources
├── docker/                  # Docker configurations
├── postman/                 # Postman collection
├── docs/                    # Documentation
├── scripts/                 # Utility scripts
└── .github/                 # GitHub workflows
```

---

## Building the Project

### Clean Build

```bash
./mvnw clean install
```

### Skip Tests

```bash
./mvnw clean install -DskipTests
```

### Build Docker Image

```bash
docker build -t payment-gateway-service:latest .
```

---

## Running the Application

### With Maven

```bash
# Start infrastructure first
docker-compose up -d

# Run application
./mvnw spring-boot:run
```

### With Docker

```bash
docker-compose up -d
```

### With Specific Profile

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Running Tests

### Unit Tests

```bash
# All unit tests
./mvnw test -Dtest='!com.payment.gateway.e2e.**'

# Specific class
./mvnw test -Dtest=PaymentServiceTest

# Specific method
./mvnw test -Dtest=PaymentServiceTest#shouldProcessPayment
```

### E2E Tests

```bash
# All E2E tests (requires Docker)
./mvnw test -Dtest='com.payment.gateway.e2e.**'
```

### All Tests

```bash
./mvnw test
```

### Test Reports

Reports are generated in `target/surefire-reports/`

---

## Debugging

### Remote Debugging

The Dockerfile exposes port 5005 for debugging:

```bash
# Build development image
docker build --target development -t payment-gateway:dev .

# Run with debug port
docker run -p 8080:8080 -p 5005:5005 payment-gateway:dev
```

Then connect your IDE to `localhost:5005`.

### IntelliJ IDEA Setup

1. Run → Edit Configurations
2. Add "Remote JVM Debug"
3. Host: localhost, Port: 5005
4. Run in debug mode

---

## IDE Configuration

### IntelliJ IDEA

#### Import Project

1. File → Open → Select project root
2. Import as Maven project
3. Enable Lombok annotation processing:
   - Settings → Build → Compiler → Annotation Processors
   - Enable annotation processing

#### Recommended Plugins

- Lombok
- MapStruct Support
- Spring Boot Assistant
- JPA Buddy

#### Code Style

Import `intellij-code-style.xml` from `.idea/` if provided.

---

## Database Management

### Connect to PostgreSQL

```bash
# Via psql
docker exec -it payment-gateway-service-postgres psql -U admin -d payment_gateway

# Via pgAdmin
open http://localhost:8083
# Email: admin@admin.com
# Password: admin
```

### Run Migrations

Migrations run automatically on startup via Flyway.

```bash
# Check migration status
./mvnw flyway:info

# Manual migration
./mvnw flyway:migrate
```

### Reset Database

```bash
# Drop and recreate
docker-compose down -v
docker-compose up -d postgres
```

---

## Kafka Management

### Access Kafka UI

```bash
open http://localhost:8082
```

### Topics

| Topic | Purpose |
|-------|---------|
| payment-events | Payment lifecycle events |
| refund-events | Refund processing events |
| transaction-events | Transaction state changes |
| customer-events | Customer management events |
| merchant-events | Merchant management events |
| outbox-events | Outbox pattern events |
| reconciliation-events | Reconciliation events |
| notification-events | Notification events |

### Console Consumer

```bash
docker exec -it payment-gateway-service-kafka \
  kafka-console-consumer --bootstrap-server localhost:29092 --topic payment-events
```

---

## Code Style

### Java Conventions

- **Indentation:** 4 spaces
- **Line Length:** 120 characters
- **Braces:** K&R style
- **Imports:** Organized, no wildcards

### Architecture Rules

1. Domain layer has **no dependencies** on infrastructure
2. Use interfaces (ports) in application layer
3. Implement adapters in infrastructure layer
4. Keep domain logic in domain entities

### Package by Feature

```
domain/payment/
├── model/           # Entities, value objects
├── service/         # Domain services
├── port/            # Interfaces (if needed)
├── event/           # Domain events
└── exception/       # Domain exceptions
```

---

## Troubleshooting

### Port Already in Use

```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>
```

### Docker Issues

```bash
# Reset everything
docker-compose down -v
docker system prune -a

# Restart
docker-compose up -d
```

### Database Connection Failed

1. Check PostgreSQL is running: `docker ps`
2. Check logs: `docker logs payment-gateway-service-postgres`
3. Verify credentials in `application.yml`

### Kafka Connection Failed

1. Check Kafka is running: `docker ps`
2. Check logs: `docker logs payment-gateway-service-kafka`
3. Verify bootstrap servers: `localhost:19092`

---

## Useful Commands

```bash
# Check application health
curl http://localhost:8080/actuator/health

# View all metrics
curl http://localhost:8080/actuator/metrics

# View Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Check Kafka topics
docker exec payment-gateway-service-kafka kafka-topics --list --bootstrap-server localhost:29092

# View Redis keys
docker exec payment-gateway-service-redis redis-cli KEYS "*"
```