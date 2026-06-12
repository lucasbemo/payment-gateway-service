# Payment Gateway Service

> Process payments, refunds and settlements with Stripe — a production-style
> payment gateway built on **Java 26 / Spring Boot 4.1**, using DDD + Hexagonal
> architecture, a transactional outbox to Kafka, and Redis-backed idempotency.

[![CI Pipeline](https://github.com/lucasbemo/payment-gateway-service/actions/workflows/ci.yml/badge.svg)](https://github.com/lucasbemo/payment-gateway-service/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-26-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen.svg)

> ⚠️ **Security notice** — this is a learning/demo project. Default credentials
> (`admin/admin`, `postgres/postgres`, …) are for local development **only**.
> Never deploy as-is — see the [Security Guide](docs/SECURITY_GUIDE.md) before
> running anywhere public.

---

## Overview

Payment Gateway Service is a backend that handles the full lifecycle of online
payments: it authorizes and captures payments through **Stripe**, issues refunds,
tracks transactions, manages merchants and customers, reconciles settlements, and
delivers webhooks to merchants. Domain events are published reliably via a
**transactional outbox** to Kafka, and every payment-like command is **idempotent**
so retries never double-charge. It exists as a reference implementation of a
clean, event-driven payment system using DDD and Hexagonal architecture.

## Features

- **Payments** — authorize, capture and process card payments via Stripe;
  idempotent per `X-Idempotency-Key`. `POST /api/v1/payments`
- **Refunds** — full and partial refunds against a payment. `…/api/v1/refunds`
- **Transactions** — query and track the ledger of payment/refund movements.
  `…/api/v1/transactions`
- **Merchants** — register, update, suspend merchants and manage their
  configuration. `…/api/v1/merchants`
- **Customers** — manage customers and their tokenized payment methods.
  `…/api/v1/customers`
- **Reconciliation** — match a merchant's transactions for a UTC day against the
  gateway; idempotent per merchant/gateway/date. `…/api/v1/reconciliation`
- **Webhooks** — receive Stripe events and fan them out to merchant endpoints.
  `…/api/v1/webhooks`
- **Idempotency** — Redis + Redisson distributed locks guarantee exactly-once
  processing of retryable commands.
- **Transactional outbox → Kafka** — aggregates and their events are persisted in
  one transaction, then published to Kafka by a poller (no lost or phantom events).
- **Resilience** — Resilience4j (circuit breaker, retry, bulkhead, rate limiter)
  and Bucket4j protect external calls and enforce per-merchant limits.
- **Observability** — Micrometer + Prometheus metrics and Brave/Zipkin tracing
  across business flows.

See the [API Reference](docs/API_REFERENCE.md) for full request/response details.

## Architecture

The service follows **DDD + Hexagonal (Ports & Adapters)** with a strict
dependency direction `infrastructure → application → domain`:

```
┌──────────────────────────────────────────────────────────────────┐
│ DOMAIN — business core: rich entities & domain services          │
│ payment · refund · transaction · merchant · customer ·           │
│ reconciliation · outbox · idempotency                            │
├──────────────────────────────────────────────────────────────────┤
│ APPLICATION — use cases, commands / responses, ports             │
├──────────────────────────────────────────────────────────────────┤
│ INFRASTRUCTURE — REST & Kafka adapters, JPA, Stripe              │
└──────────────────────────────────────────────────────────────────┘
```

The boundaries are enforced by an ArchUnit test (`HexagonalArchitectureTest`):
the domain may not depend on application or infrastructure, and no cycles are
allowed.

| Area | Technology |
|------|------------|
| Language / build | Java **26**, Maven (`./mvnw`) |
| Framework | Spring Boot **4.1.0** |
| Persistence | PostgreSQL + Spring Data JPA/Hibernate, Flyway migrations |
| Messaging | Spring Kafka (transactional outbox) |
| Cache / locks | Redis + Redisson |
| Resilience | Resilience4j + Bucket4j |
| Payments | stripe-java |
| API docs | springdoc-openapi (Swagger UI) |
| Observability | Micrometer + Prometheus + Brave/Zipkin |
| Testing | JUnit 5, Testcontainers, ArchUnit |

Deeper design notes live in
[Stripe Integration Architecture](docs/STRIPE_INTEGRATION_ARCHITECTURE.md),
[Webhooks](docs/WEBHOOKS.md) and the [ADRs](docs/decisions/).

## Quick Start

### Prerequisites

- **Java 26+** (Temurin 26 recommended)
- **Docker & Docker Compose**
- Maven is provided via the `./mvnw` wrapper

### 1. Clone

```bash
git clone git@github.com:lucasbemo/payment-gateway-service.git
cd payment-gateway-service
cp .env.example .env        # adjust values as needed
```

### 2. Start infrastructure

```bash
docker-compose up -d        # or: make docker-up
# Postgres, Kafka, Redis, Prometheus, Grafana, Zipkin, MinIO, pgAdmin, Kafka UI
```

### 3. Run the application

```bash
./mvnw spring-boot:run                 # or: make run  (profile: local)
# or build a jar and run it
./mvnw clean package && java -jar target/*.jar
```

### 4. Access

| Service | URL | Credentials |
|---------|-----|-------------|
| API | http://localhost:8080 | — |
| Swagger UI | http://localhost:8080/swagger-ui.html | — |
| Health | http://localhost:8080/actuator/health | — |
| pgAdmin | http://localhost:8083 | admin@admin.com / admin |
| Kafka UI | http://localhost:8082 | — |
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | — |
| Zipkin | http://localhost:9411 | — |

### Configuration

Configuration comes from environment variables (see `.env.example`). Key values
and their local docker-compose defaults:

```bash
# Database (host port 5433)
DATASOURCE_URL=jdbc:postgresql://localhost:5433/payment_gateway
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=your_secure_password_here

# Kafka (host port 19092)
KAFKA_BOOTSTRAP_SERVERS=localhost:19092

# Redis (host port 6380)
REDIS_HOST=localhost
REDIS_PORT=6380

# Active Spring profile
SPRING_PROFILES_ACTIVE=local
```

**Profiles:** `local` (default — host-port defaults, no extra config),
`dev` (used by the docker app container), `prod` (requires `DATASOURCE_URL` and
the other secrets to be supplied via environment/secrets manager).

## Documentation

Full documentation lives in [`docs/`](docs/README.md). Start here:

| Topic | Document |
|-------|----------|
| Getting started | [docs/GETTING_STARTED.md](docs/GETTING_STARTED.md) |
| API reference | [docs/API_REFERENCE.md](docs/API_REFERENCE.md) · [error codes](docs/ERROR_CODES.md) |
| Webhooks | [docs/WEBHOOKS.md](docs/WEBHOOKS.md) |
| Testing | [docs/TESTING_GUIDE.md](docs/TESTING_GUIDE.md) |
| Security | [docs/SECURITY_GUIDE.md](docs/SECURITY_GUIDE.md) |
| Deployment | [docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md) |
| Development | [docs/DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md) |
| Troubleshooting | [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) |
| Architecture decisions | [docs/decisions/](docs/decisions/) |

## Contributing

Contributions are welcome. This project is built with a **spec-driven,
human-in-the-loop AI workflow** ([Claude Code](https://claude.com/claude-code)):
every non-trivial change flows from a backlog item to an approved spec to an
implementation, with explicit human approval gates at each step — so the intent
is reviewed *before* any code is written.

```
GitHub Project item → /import-github-project-item <n>   (scaffold + enrich a spec)
                    → human review & approval           (answer blocking questions)
                    → /implement-feature <spec>          (plan → APPROVE → code in a worktree)
                    → /spring-boot-review → /create-pr   (review → draft PR)
                    → merge → /finish-feature <n>-<slug> (archive spec, clean up)
```

See **[GUIDE_WORKFLOW.md](GUIDE_WORKFLOW.md)** for the full walkthrough and
[CONTRIBUTING.md](CONTRIBUTING.md) for the branching model, Conventional Commits,
and code style. The workflow is optional for one-off fixes, but it's how the
backlog is delivered.

## License

Released under the [MIT License](LICENSE).
