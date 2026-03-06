# 📋 Implementation Checklist - Payment Gateway Service

> Track your progress through each implementation phase. Check off items as you complete them.

---

## 📍 How to Use This Checklist

- [ ] Empty checkbox = Not started
- [x] Checked checkbox = Completed
- [~] Partially completed (add notes)

Update this file as you progress. Commit after each major milestone.

---

## 🚀 PHASE 1: PROJECT INITIALIZATION

### 1.1 Project Setup
- [x] Create Maven project structure with `pom.xml`
- [x] Configure Java 21 in `pom.xml`
- [x] Configure Spring Boot 3.2+ dependencies
- [x] Add Spring Web dependency
- [x] Add Spring Data JPA dependency
- [x] Add PostgreSQL driver dependency
- [x] Add Flyway dependency
- [x] Add Lombok dependency (optional)
- [x] Add MapStruct dependency (for DTO mapping)
- [x] Configure Maven compiler plugin for Java 21
- [x] Create `.gitignore` file
- [x] Create `.gitattributes` file
- [x] Initialize Git repository
- [x] Create initial commit

### 1.2 Package Structure Creation
- [x] Create base package: `com.payment.gateway`
- [x] Create `domain/` package structure
  - [x] `domain/payment/` with subpackages (model, service, port, event, exception)
  - [x] `domain/transaction/` with subpackages
  - [x] `domain/refund/` with subpackages
  - [x] `domain/merchant/` with subpackages (model, service, port, exception)
  - [x] `domain/customer/` with subpackages
  - [x] `domain/outbox/` with subpackages
- [x] Create `application/` package structure
  - [x] `application/payment/` with subpackages (port/in, port/out, service)
  - [x] `application/refund/` with subpackages
  - [x] `application/transaction/` with subpackages
  - [x] `application/merchant/` with subpackages
  - [x] `application/customer/` with subpackages
  - [x] `application/reconciliation/` with subpackages
- [x] Create `infrastructure/` package structure
  - [x] `infrastructure/payment/` with subpackages (adapter/in, adapter/out, config)
  - [x] `infrastructure/refund/` with subpackages
  - [x] `infrastructure/transaction/` with subpackages
  - [x] `infrastructure/merchant/` with subpackages
  - [x] `infrastructure/customer/` with subpackages
  - [x] `infrastructure/outbox/` with subpackages
  - [x] `infrastructure/commons/` with subpackages (rest, kafka, persistence, security, resilience, logging, monitoring, async)
  - [x] `infrastructure/config/` for global configuration
- [x] Create `commons/` package structure
  - [x] `commons/exception/`
  - [x] `commons/validation/` with constraints
  - [x] `commons/utils/`
  - [x] `commons/constants/`

### 1.3 Docker Compose Setup
- [x] Create `docker-compose.yml` in project root
- [x] Configure PostgreSQL service
  - [x] Set image to `postgres:16-alpine`
  - [x] Configure environment variables (DB name, user, password)
  - [x] Configure ports (5433:5432)
  - [x] Configure volumes for data persistence
  - [x] Add healthcheck configuration
- [x] Configure pgAdmin service
  - [x] Set image to `dpage/pgadmin4:latest`
  - [x] Configure admin email and password
  - [x] Configure ports (8080:80)
  - [x] Add dependency on PostgreSQL
- [x] Configure Zookeeper service
  - [x] Set image to `confluentinc/cp-zookeeper:7.5.0`
  - [x] Configure environment variables
- [x] Configure Kafka service
  - [x] Set image to `confluentinc/cp-kafka:7.5.0`
  - [x] Configure listeners and advertised listeners
  - [x] Configure ports (9092, 29092)
  - [x] Add dependency on Zookeeper
- [x] Configure Kafka UI service
  - [x] Set image to `provectuslabs/kafka-ui:latest`
  - [x] Configure cluster connection
  - [x] Configure ports (8081:8080)
- [x] Configure Redis service (optional)
  - [x] Set image to `redis:7-alpine`
  - [x] Configure ports (6379:6379)
- [x] Configure payment-gateway application service
  - [x] Configure build context and Dockerfile
  - [x] Configure environment variables
  - [x] Configure dependencies on postgres and kafka
  - [x] Configure ports (8080:8080)
- [x] Define volumes
- [x] Define networks
- [x] Create `docker-compose.override.yml` for local development
- [x] Create `docker/` directory structure
  - [x] `Dockerfile` (multi-stage build)
  - [x] `docker/kafka/init-topics.sh`
  - [x] `docker/postgres/init.sql`
- [x] Test Docker Compose startup
  - [x] Run `docker-compose up -d`
  - [x] Verify all containers are running
  - [x] Test PostgreSQL connection
  - [x] Test pgAdmin access
  - [x] Test Kafka connection
  - [x] Test Kafka UI access

### 1.4 Application Configuration
- [x] Create `src/main/resources/application.yml`
  - [x] Configure application name
  - [x] Configure profiles (local, dev, staging, prod)
  - [x] Configure server port
- [x] Create `src/main/resources/application-local.yml`
  - [x] Configure datasource for local PostgreSQL
  - [x] Configure Kafka bootstrap servers
  - [x] Configure Redis host (if using)
  - [x] Enable H2 console for debugging (optional)
  - [x] Configure logging level
- [x] Create `src/main/resources/application-dev.yml`
- [x] Create `src/main/resources/application-prod.yml`
- [x] Create `src/main/resources/logback-spring.xml`
- [x] Create `PaymentGatewayApplication.java` main class
- [x] Test application startup with Docker Compose

**Phase 1 Completion Criteria:**
- [x] Project compiles successfully with `mvn clean compile`
- [x] Docker Compose starts all services without errors
- [x] Application connects to PostgreSQL successfully
- [x] Application connects to Kafka successfully
- [x] Application starts without errors

---

## 🏛️ PHASE 2: DOMAIN LAYER IMPLEMENTATION

### 2.1 Common Domain Components
- [x] Create base domain classes
  - [x] Create `BaseEntity.java` in domain (if needed)
  - [x] Create domain exception base classes

### 2.2 Payment Domain (Bounded Context)
- [x] **Payment Model**
  - [x] Create `Payment.java` (Aggregate Root)
    - [x] Define fields: id, merchantId, customerId, paymentMethodId, amount, currency, status, idempotencyKey, description, metadata, items, createdAt, updatedAt
    - [x] Implement business logic methods
    - [x] Implement validation methods
    - [x] Add factory methods for creation
  - [x] Create `PaymentStatus.java` (Enum)
    - [x] Define statuses: PENDING, AUTHORIZED, CAPTURED, FAILED, CANCELLED, REFUNDED
  - [x] Create `PaymentItem.java` (Value Object)
    - [x] Define fields: description, quantity, unitPrice, total
  - [x] Create `PaymentAmount.java` (Value Object) - Using Money VO instead
  - [x] Create `PaymentMetadata.java` (Value Object)
- [x] **Payment Repository**
  - [x] Create `PaymentRepositoryPort.java` interface
    - [x] Define save method
    - [x] Define findById method
    - [x] Define findByMerchantId method
    - [x] Define findByIdempotencyKey method
    - [x] Define custom query methods
- [x] **Payment Domain Services**
  - [x] Create `PaymentDomainService.java`
    - [x] Implement payment creation logic
    - [x] Implement payment validation logic
    - [x] Implement payment status transition logic
- [x] **Payment Exceptions**
  - [x] Create `PaymentNotFoundException.java`
  - [x] Create `PaymentProcessingException.java`
  - [x] Create `DuplicatePaymentException.java`
- [x] **Payment Domain Tests**
  - [x] Create `PaymentTest.java`
    - [x] Test payment creation
    - [x] Test payment status transitions
    - [x] Test payment validation
  - [x] Create `PaymentStatusTest.java`
  - [x] Create `PaymentAmountTest.java`
  - [x] Create `PaymentDomainServiceTest.java`
  - [x] Create `PaymentValidationServiceTest.java` (validation is in PaymentDomainService)

### 2.3 Transaction Domain (Bounded Context)
- [x] **Transaction Model**
  - [x] Create `Transaction.java` (Aggregate Root)
    - [x] Define fields: id, paymentId, type, status, amount, currency, provider, providerTransactionId, providerResponse, errorMessage, errorCode, processedAt, createdAt, updatedAt
  - [x] Create `TransactionStatus.java` (Enum)
    - [x] Define statuses: PENDING, PROCESSING, AUTHORIZED, CAPTURED, SETTLED, REVERSED, FAILED, REFUNDED, PARTIALLY_REFUNDED
  - [x] Create `TransactionType.java` (Enum)
    - [x] Define types: PAYMENT, CAPTURE, AUTHORIZATION, REFUND, PARTIAL_REFUND, REVERSAL, CHARGEBACK, ADJUSTMENT
  - [x] Create transaction status transition logic
- [x] **Transaction Repository**
  - [x] Create `TransactionRepositoryPort.java` interface
- [x] **Transaction Domain Services**
  - [x] Create `TransactionDomainService.java`
    - [x] Implement transaction creation logic
    - [x] Implement transaction status transition logic
    - [x] Implement transaction validation
- [x] **Transaction Exceptions**
  - [x] Create `TransactionNotFoundException.java`
  - [x] Create `TransactionProcessingException.java`
  - [x] Create `InvalidTransactionStateException.java`
  - [x] Create `TransactionException.java` (base)
- [x] **Transaction Domain Tests**
  - [x] Create `TransactionTest.java`
    - [x] Test transaction creation
    - [x] Test transaction status transitions
    - [x] Test transaction state checks
  - [x] Create `TransactionDomainServiceTest.java`

### 2.4 Refund Domain (Bounded Context)
- [x] **Refund Model**
  - [x] Create `Refund.java` (Aggregate Root)
    - [x] Define fields: id, paymentId, transactionId, amount, currency, status, type, reason, providerRefundId, providerResponse, errorMessage, processedAt, createdAt, updatedAt
  - [x] Create `RefundStatus.java` (Enum)
    - [x] Define statuses: PENDING, PROCESSING, APPROVED, REJECTED, COMPLETED, FAILED, CANCELLED
  - [x] Create `RefundType.java` (Enum)
    - [x] Define types: FULL, PARTIAL, MULTIPLE, CHARGEBACK, CANCELLATION
  - [x] Create `RefundItem.java` (Value Object)
- [x] **Refund Repository**
  - [x] Create `RefundRepositoryPort.java` interface
- [x] **Refund Domain Services**
  - [x] Create `RefundDomainService.java`
    - [x] Implement refund creation logic
    - [x] Implement refund amount validation (can't exceed original payment)
    - [x] Implement refund status transition logic
- [x] **Refund Exceptions**
  - [x] Create `RefundNotFoundException.java`
  - [x] Create `RefundProcessingException.java`
  - [x] Create `InvalidRefundAmountException.java`
  - [x] Create `RefundException.java` (base)
- [x] **Refund Domain Tests**
  - [x] Create `RefundTest.java`
    - [x] Test refund creation
    - [x] Test refund status transitions
    - [x] Test refund item management
    - [x] Test refund retry logic
  - [x] Create `RefundDomainServiceTest.java`

### 2.5 Merchant Domain (Bounded Context)
- [x] **Merchant Model**
  - [x] Create `Merchant.java` (Aggregate Root)
    - [x] Define fields: id, name, email, apiKeyHash, apiSecretHash, status, webhookUrl, webhookSecret, configuration, createdAt, updatedAt
  - [x] Create `MerchantStatus.java` (Enum)
    - [x] Define statuses: ACTIVE, SUSPENDED, PENDING, CLOSED
  - [x] Create `MerchantConfiguration.java` (Value Object)
  - [x] Create `ApiCredentials.java` (Value Object)
- [x] **Merchant Repository**
  - [x] Create `MerchantRepositoryPort.java` interface
- [x] **Merchant Domain Services**
  - [x] Create `MerchantDomainService.java`
    - [x] Implement merchant registration logic
    - [x] Implement API key generation logic
    - [x] Implement merchant status transition logic
- [x] **Merchant Exceptions**
  - [x] Create `MerchantNotFoundException.java`
  - [x] Create `MerchantConfigurationException.java`
  - [x] Create `InvalidMerchantException.java`
- [x] **Merchant Domain Tests**
  - [x] Create `MerchantTest.java`
    - [x] Test merchant registration
    - [x] Test merchant status transitions
    - [x] Test API key validation
    - [x] Test configuration updates
  - [x] Create `MerchantDomainServiceTest.java`

### 2.6 Customer Domain (Bounded Context)
- [x] **Customer Model**
  - [x] Create `Customer.java` (Aggregate Root)
    - [x] Define fields: id, merchantId, token, email, name, phone, metadata, createdAt, updatedAt
  - [x] Create `PaymentMethod.java` (Value Object)
    - [x] Define fields: type, cardBrand, lastFour, expiryMonth, expiryYear, token, isDefault
  - [x] Create `CardDetails.java` (Value Object)
    - [x] Define fields: cardNumberLast4, cardNumberBin, cardBrand, expiryMonth, expiryYear, cardholderName
  - [x] Create `CustomerStatus.java` (Enum)
- [x] **Customer Repository**
  - [x] Create `CustomerRepositoryPort.java` interface
- [x] **Customer Domain Services**
  - [x] Create `CustomerDomainService.java`
    - [x] Implement customer registration logic
    - [x] Implement payment method management logic
    - [x] Implement tokenization logic
- [x] **Customer Exceptions**
  - [x] Create `CustomerNotFoundException.java`
  - [x] Create `PaymentMethodException.java`
  - [x] Create `InvalidPaymentMethodException.java`
  - [x] Create `DuplicateCustomerException.java`
  - [x] Create `CustomerException.java` (base)
- [x] **Customer Domain Tests**
  - [x] Create `CustomerTest.java`
    - [x] Test customer creation
    - [x] Test payment method management
    - [x] Test card details validation
    - [x] Test customer status transitions
  - [x] Create `CustomerDomainServiceTest.java`

### 2.7 Outbox Domain (Bounded Context)
- [x] **Outbox Model**
  - [x] Create `OutboxEvent.java` (Aggregate Root)
    - [x] Define fields: id, aggregateId, aggregateType, eventType, payload, status, errorMessage, retryCount, createdAt, publishedAt
  - [x] Create `EventType.java` (Enum)
    - [x] Define events: PAYMENT_CREATED, PAYMENT_COMPLETED, PAYMENT_FAILED, PAYMENT_CANCELLED, REFUND_PROCESSED, TRANSACTION_CREATED, TRANSACTION_COMPLETED, TRANSACTION_FAILED, CUSTOMER_CREATED, CUSTOMER_UPDATED, MERCHANT_ACTIVATED, MERCHANT_SUSPENDED
  - [x] Create `EventStatus.java` (Enum)
    - [x] Define statuses: PENDING, PROCESSING, PUBLISHED, FAILED, RETRYING
- [x] **Outbox Repository**
  - [x] Create `OutboxEventRepositoryPort.java` interface
- [x] **Outbox Domain Services**
  - [x] Create `OutboxEventDomainService.java`
    - [x] Implement event publishing logic
    - [x] Implement event processing logic
    - [x] Implement event retry logic
- [x] **Outbox Domain Tests**
  - [x] Create `OutboxEventTest.java`
    - [x] Test outbox event creation
    - [x] Test event status transitions
    - [x] Test event retry logic
  - [x] Create `OutboxEventDomainServiceTest.java`

### 2.8 Idempotency Domain (Bounded Context)
- [x] **Idempotency Model**
  - [x] Create `IdempotencyKey.java` (Aggregate Root)
    - [x] Define fields: id, idempotencyKey, merchantId, operation, requestHash, status, responseCode, errorMessage, responseBody, lockedAt, lockToken, expiresAt, createdAt, updatedAt, completedAt
  - [x] Create `IdempotencyStatus.java` (Enum)
    - [x] Define statuses: PENDING, PROCESSING, COMPLETED, FAILED, EXPIRED
- [x] **Idempotency Repository**
  - [x] Create `IdempotencyKeyRepositoryPort.java` interface
- [x] **Idempotency Domain Services**
  - [x] Create `IdempotencyDomainService.java`
    - [x] Implement idempotency key creation/getting logic
    - [x] Implement lock acquisition logic
    - [x] Implement response completion logic
    - [x] Implement lock release logic
- [x] **Idempotency Domain Tests**
  - [x] Create `IdempotencyKeyTest.java`
    - [x] Test idempotency key creation
    - [x] Test locking mechanism
    - [x] Test completion and failure scenarios
    - [x] Test expiration handling
  - [x] Create `IdempotencyDomainServiceTest.java`

### 2.9 Reconciliation Domain (Bounded Context)
- [x] **Reconciliation Model**
  - [x] Create `ReconciliationBatch.java` (Aggregate Root)
    - [x] Define fields: id, merchantId, reconciliationDate, gatewayName, status, totalTransactions, matchedTransactions, unmatchedTransactions, totalAmount, matchedAmount, unmatchedAmount, discrepancyCount, initiatedBy, startedAt, completedAt, createdAt, updatedAt
  - [x] Create `Discrepancy.java` (Aggregate Root)
    - [x] Define fields: id, batchId, merchantId, transactionId, gatewayTransactionId, type, status, systemAmount, gatewayAmount, description, resolutionNotes, resolvedBy, resolvedAt, createdAt, updatedAt
  - [x] Create `SettlementReport.java` (Aggregate Root)
    - [x] Define fields: id, merchantId, gatewayName, settlementDate, gatewayReportId, grossAmount, feeAmount, netAmount, currency, transactionCount, status, filePath, reconciliationBatchId, settledAt, createdAt, updatedAt
  - [x] Create `ReconciliationStatus.java` (Enum)
  - [x] Create `DiscrepancyType.java` (Enum)
  - [x] Create `DiscrepancyStatus.java` (Enum)
- [x] **Reconciliation Repository**
  - [x] Create `ReconciliationBatchRepositoryPort.java` interface
  - [x] Create `DiscrepancyRepositoryPort.java` interface
  - [x] Create `SettlementReportRepositoryPort.java` interface
- [x] **Reconciliation Domain Services**
  - [x] Create `ReconciliationDomainService.java`
    - [x] Implement batch creation logic
    - [x] Implement discrepancy management logic
    - [x] Implement settlement report logic
- [x] **Reconciliation Domain Tests**
  - [x] Create `ReconciliationTest.java`
    - [x] Test reconciliation batch lifecycle
    - [x] Test discrepancy management
    - [x] Test settlement report handling
  - [x] Create `ReconciliationDomainServiceTest.java`
  - [x] Create `EventStatus.java` (Enum)
    - [x] Define statuses: PENDING, PUBLISHED, FAILED
- [x] **Outbox Repository**
  - [x] Create `OutboxEventRepository.java` interface
    - [x] Define findByStatus method
    - [x] Define markAsPublished method
    - [x] Define markAsFailed method
- [x] **Outbox Domain Services**
  - [x] Create `OutboxEventDomainService.java`
    - [x] Implement event creation logic
    - [x] Implement event publishing logic
    - [x] Implement retry logic
- [x] **Outbox Domain Tests**
  - [x] Create `OutboxEventTest.java`
  - [x] Create `OutboxEventDomainServiceTest.java`

**Phase 2 Completion Criteria:**
- [x] All domain models are created with proper business logic
- [x] All repository interfaces are defined
- [x] All domain services are implemented
- [x] All domain exceptions are created
- [x] All domain unit tests pass (400 tests)
- [x] No dependencies on infrastructure layer

---

## ⚙️ PHASE 3: APPLICATION LAYER IMPLEMENTATION

### 3.1 Payment Use Cases
- [x] **Input Ports (Use Case Interfaces)**
  - [x] Create `ProcessPaymentUseCase.java`
    - [x] Define processPayment command interface
  - [x] Create `GetPaymentUseCase.java`
    - [x] Define getPaymentById query interface
    - [x] Define getPaymentsByMerchant query interface
  - [x] Create `CancelPaymentUseCase.java`
    - [x] Define cancelPayment command interface
  - [x] Create `CapturePaymentUseCase.java`
    - [x] Define capturePayment command interface
- [x] **Output Ports (Adapter Interfaces)**
  - [x] Create `PaymentRepositoryPort.java`
  - [x] Create `TransactionRepositoryPort.java`
  - [x] Create `MerchantRepositoryPort.java`
  - [x] Create `CustomerRepositoryPort.java`
  - [x] Create `PaymentEventPublisherPort.java`
  - [x] Create `ExternalPaymentProviderPort.java`
  - [x] Create `TokenizationServicePort.java`
- [x] **Use Case Implementations**
  - [x] Create `ProcessPaymentService.java`
    - [x] Implement processPayment method
    - [x] Implement idempotency handling
    - [x] Implement transaction creation
    - [x] Implement outbox event creation
  - [x] Create `GetPaymentService.java`
    - [x] Implement getPaymentById method
    - [x] Implement getPaymentsByMerchant method
  - [x] Create `CancelPaymentService.java`
    - [x] Implement cancelPayment method
    - [x] Implement status validation
  - [x] Create `CapturePaymentService.java`
    - [x] Implement capturePayment method
- [x] **Payment Use Case Tests**
  - [x] Create `ProcessPaymentServiceTest.java`
    - [x] Test successful payment processing
    - [x] Test idempotent payment (duplicate request)
    - [x] Test payment with insufficient funds
    - [x] Test payment with invalid data
  - [x] Create `GetPaymentServiceTest.java`
  - [x] Create `CancelPaymentServiceTest.java`
  - [x] Create `CapturePaymentServiceTest.java`

### 3.2 Refund Use Cases
- [x] **Input Ports**
  - [x] Create `ProcessRefundUseCase.java`
  - [x] Create `GetRefundUseCase.java`
  - [x] Create `CancelRefundUseCase.java`
- [x] **Output Ports**
  - [x] Create `RefundRepositoryPort.java`
  - [x] Create `PaymentRepositoryPort.java` (reference to payment port)
  - [x] Create `TransactionRepositoryPort.java` (reference)
  - [x] Create `RefundEventPublisherPort.java`
- [x] **Use Case Implementations**
  - [x] Create `ProcessRefundService.java`
    - [x] Implement full refund logic
    - [x] Implement partial refund logic
    - [x] Implement refund validation
  - [x] Create `GetRefundService.java`
  - [x] Create `CancelRefundService.java`
- [x] **Refund Use Case Tests**
  - [x] Create `ProcessRefundServiceTest.java`
  - [x] Create `GetRefundServiceTest.java`
  - [x] Create `CancelRefundServiceTest.java`

### 3.3 Transaction Use Cases
- [x] **Input Ports**
  - [x] Create `GetTransactionUseCase.java`
  - [x] Create `VoidTransactionUseCase.java`
  - [x] Create `CaptureTransactionUseCase.java`
- [x] **Output Ports**
  - [x] Create `TransactionRepositoryPort.java`
  - [x] Create `PaymentRepositoryPort.java`
  - [x] Create `ExternalPaymentProviderPort.java`
- [x] **Use Case Implementations**
  - [x] Create `GetTransactionService.java`
  - [x] Create `VoidTransactionService.java`
  - [x] Create `CaptureTransactionService.java`
- [x] **Transaction Use Case Tests**
  - [x] Create `GetTransactionServiceTest.java`
  - [x] Create `VoidTransactionServiceTest.java`
  - [x] Create `CaptureTransactionServiceTest.java`

### 3.4 Merchant Use Cases
- [x] **Input Ports**
  - [x] Create `RegisterMerchantUseCase.java`
  - [x] Create `GetMerchantUseCase.java`
  - [x] Create `UpdateMerchantUseCase.java`
  - [x] Create `SuspendMerchantUseCase.java`
- [x] **Output Ports**
  - [x] Create `MerchantRepositoryPort.java`
- [x] **Use Case Implementations**
  - [x] Create `RegisterMerchantService.java`
  - [x] Create `GetMerchantService.java`
  - [x] Create `UpdateMerchantService.java`
  - [x] Create `SuspendMerchantService.java`
- [x] **Merchant Use Case Tests**
  - [x] Create `RegisterMerchantServiceTest.java`
  - [x] Create `GetMerchantServiceTest.java`
  - [x] Create `UpdateMerchantServiceTest.java`
  - [x] Create `SuspendMerchantServiceTest.java`

### 3.5 Customer Use Cases
- [x] **Input Ports**
  - [x] Create `RegisterCustomerUseCase.java`
  - [x] Create `GetCustomerUseCase.java`
  - [x] Create `AddPaymentMethodUseCase.java`
  - [x] Create `RemovePaymentMethodUseCase.java`
- [x] **Output Ports**
  - [x] Create `CustomerRepositoryPort.java`
  - [x] Create `TokenizationServicePort.java`
- [x] **Use Case Implementations**
  - [x] Create `RegisterCustomerService.java`
  - [x] Create `GetCustomerService.java`
  - [x] Create `AddPaymentMethodService.java`
  - [x] Create `RemovePaymentMethodService.java`
- [x] **Customer Use Case Tests**
  - [x] Create `RegisterCustomerServiceTest.java`
  - [x] Create `AddPaymentMethodServiceTest.java`
  - [x] Create `GetCustomerServiceTest.java`
  - [x] Create `RemovePaymentMethodServiceTest.java`

### 3.6 Reconciliation Use Cases
- [x] **Input Ports**
  - [x] Create `ReconcileTransactionsUseCase.java`
  - [x] Create `GenerateSettlementReportUseCase.java`
- [x] **Output Ports**
  - [x] Create `TransactionRepositoryPort.java`
  - [x] Create `PaymentRepositoryPort.java`
  - [x] Create `ReportGeneratorPort.java`
- [x] **Use Case Implementations**
  - [x] Create `ReconcileTransactionsService.java`
  - [x] Create `GenerateSettlementReportService.java`

**Phase 3 Completion Criteria:**
- [x] All use case interfaces are defined
- [x] All output port interfaces are defined
- [x] All use case implementations are complete
- [x] All use case unit tests pass
- [x] Use cases only depend on ports (interfaces), not concrete implementations

---

## 🔌 PHASE 4: INFRASTRUCTURE LAYER IMPLEMENTATION

### 4.1 Common Infrastructure Setup
- [x] **REST Common Components**
  - [x] Create `GlobalExceptionHandler.java`
    - [x] Handle BusinessException
    - [x] Handle NotFoundException
    - [x] Handle ValidationException
    - [x] Handle ExternalServiceException
  - [x] Create `ApiResponse.java` (generic response wrapper)
  - [ ] Create `BaseController.java`
  - [ ] Create `PageInfo.java` (pagination metadata)
  - [ ] Create `RequestValidationFilter.java`
- [x] **Persistence Common Components**
  - [x] Create `JpaConfig.java` (auto-configured via Spring Boot)
  - [ ] Create `BaseEntity.java` (JPA base with @MappedSuperclass)
  - [ ] Create `AuditingConfig.java`
  - [ ] Create `AuditorAwareImpl.java`
  - [ ] Create `PessimisticLockingConfig.java`
  - [ ] Create `DataSourceConfig.java`
  - [ ] Create `HikariPoolConfig.java`
- [x] **Security Common Components**
  - [x] Create `SecurityConfig.java` (CSRF disabled, stateless sessions, CORS configured)
  - [ ] Create `JwtAuthenticationFilter.java`
  - [ ] Create `JwtTokenProvider.java`
  - [ ] Create `RbacConfig.java`
  - [ ] Create `ApiKeyAuthenticationFilter.java`
- [ ] **Resilience Common Components**
  - [ ] Create `Resilience4jConfig.java`
  - [ ] Create `CircuitBreakerConfig.java`
  - [ ] Create `RetryConfig.java`
  - [ ] Create `RateLimiterConfig.java`
  - [ ] Create `BulkheadConfig.java`
  - [ ] Create `TimeLimiterConfig.java`
- [ ] **Logging Common Components**
  - [ ] Create `LoggingConfig.java`
  - [ ] Create `CorrelationIdInterceptor.java`
  - [ ] Create `AuditLogAspect.java`
  - [ ] Create `StructuredLogger.java`
- [ ] **Monitoring Common Components**
  - [ ] Create `MetricsConfig.java`
  - [ ] Create `HealthIndicatorConfig.java`
  - [ ] Create `CustomMetricsBinder.java`
  - [ ] Create `TracingConfig.java`
  - [ ] Create `SpanAspect.java`
- [x] **Async Common Components**
  - [x] Create `AsyncConfig.java` (@EnableAsync, @EnableScheduling)
  - [ ] Create `VirtualThreadsConfig.java`

### 4.2 Payment Infrastructure
- [x] **Persistence Adapter (Driven)**
  - [x] Create `PaymentJpaEntity.java` (JPA Entity)
    - [x] Map all fields from Payment domain model
    - [x] Configure relationships
  - [x] Create `PaymentMapper.java` (domain <-> entity mapper)
  - [x] Create `PaymentJpaRepository.java` (extends JpaRepository)
  - [x] Create `PaymentPersistenceAdapter.java` (implements PaymentRepositoryPort + PaymentQueryPort)
  - [x] Create database migration: `V2__create_payments_table.sql`
- [x] **REST Adapter (Driving)**
  - [x] Create `CreatePaymentRequest.java` (DTO)
  - [x] Create `PaymentResponse.java` (DTO)
  - [x] Create `PaymentRestMapper.java` (DTO <-> domain mapper)
  - [x] Create `PaymentController.java`
    - [x] Implement POST /api/v1/payments
    - [x] Implement GET /api/v1/payments/{id}
    - [x] Implement GET /api/v1/payments
    - [x] Implement POST /api/v1/payments/{id}/capture
    - [x] Implement POST /api/v1/payments/{id}/cancel
- [x] **Event Publisher Adapter**
  - [x] Create `KafkaPaymentEventPublisher.java`
    - [x] Implement PaymentEventPublisherPort
    - [x] Configure Kafka topic
    - [x] Implement event publishing logic
- [x] **External Payment Provider Adapters**
  - [x] Create `StubExternalPaymentProvider.java` (stub for development)
  - [ ] Create `StripePaymentProvider.java`
  - [ ] Create `PayPalPaymentProvider.java`
- [x] **Service Wiring**
  - [x] Add `@Service` to all application services
  - [x] Add `@Transactional` to all application and domain services
- [x] **Payment Infrastructure Tests**
  - [x] Create `PaymentJpaRepositoryIntegrationTest.java` (7 tests passing)
  - [x] Create `PaymentControllerTest.java` (5 tests: process, get, capture, cancel, validation)

### 4.3 Refund Infrastructure
- [x] **Persistence Adapter**
  - [x] Create `RefundJpaEntity.java`
  - [x] Create `RefundMapper.java`
  - [x] Create `RefundJpaRepository.java`
  - [x] Create `RefundPersistenceAdapter.java` (implements RefundQueryPort + RefundRepositoryPort)
  - [x] Create `RefundPaymentQueryAdapter.java` (implements RefundPaymentQueryPort)
  - [x] Create database migration: `V4__create_refunds_table.sql`
- [x] **REST Adapter**
  - [x] Create `RefundController.java`
    - [x] Implement POST /api/v1/refunds
    - [x] Implement GET /api/v1/refunds/{id}
    - [x] Implement POST /api/v1/refunds/{id}/cancel
- [x] **Event Publisher**
  - [x] Create `StubRefundEventPublisher.java` (stub for RefundEventPublisherPort)
- [x] **Refund Infrastructure Tests**
  - [x] Create `RefundControllerTest.java` (4 tests: process, get, cancel, validation)

### 4.4 Transaction Infrastructure
- [x] **Persistence Adapter**
  - [x] Create `TransactionJpaEntity.java`
  - [x] Create `TransactionMapper.java`
  - [x] Create `TransactionJpaRepository.java`
  - [x] Create `TransactionPersistenceAdapter.java` (implements TransactionQueryPort + TransactionCommandPort + TransactionRepositoryPort)
  - [x] Create database migration: `V3__create_transactions_table.sql`
- [x] **REST Adapter**
  - [x] Create `TransactionController.java`
    - [x] Implement GET /api/v1/transactions/{id}
    - [x] Implement POST /api/v1/transactions/{id}/capture
    - [x] Implement POST /api/v1/transactions/{id}/void
- [x] **Provider Adapter**
  - [x] Create `StubExternalTransactionProvider.java` (stub for ExternalTransactionProviderPort)
  - [x] Create `StubTransactionEventPublisher.java` (stub for TransactionEventPublisherPort)
- [x] **Transaction Infrastructure Tests**
  - [x] Create `TransactionControllerTest.java` (3 tests: get, capture, void)

### 4.5 Merchant Infrastructure
- [x] **Persistence Adapter**
  - [x] Create `MerchantJpaEntity.java`
  - [x] Create `MerchantMapper.java`
  - [x] Create `MerchantJpaRepository.java`
  - [x] Create `MerchantPersistenceAdapter.java` (implements MerchantRepositoryPort + MerchantQueryPort + MerchantCommandPort)
  - [x] Create database migration: `V1__create_merchants_table.sql`
- [x] **REST Adapter**
  - [x] Create `MerchantController.java`
    - [x] Implement POST /api/v1/merchants
    - [x] Implement GET /api/v1/merchants/{id}
    - [x] Implement PUT /api/v1/merchants/{id}
    - [x] Implement POST /api/v1/merchants/{id}/suspend
- [x] **Merchant Infrastructure Tests**
  - [x] Create `MerchantControllerTest.java` (5 tests: register, get, update, suspend, validation)

### 4.6 Customer Infrastructure
- [x] **Persistence Adapter**
  - [x] Create `CustomerJpaEntity.java`
  - [x] Create `PaymentMethodJpaEntity.java`
  - [x] Create `CustomerMapper.java`
  - [x] Create `PaymentMethodMapper.java`
  - [x] Create `CustomerJpaRepository.java`
  - [x] Create `PaymentMethodJpaRepository.java`
  - [x] Create `CustomerPersistenceAdapter.java` (implements CustomerQueryPort + CustomerCommandPort + CustomerRepositoryPort)
  - [x] Create database migrations: `V7__create_customers_table.sql`, `V12__create_payment_methods_table.sql`
- [x] **REST Adapter**
  - [x] Create `CustomerController.java`
    - [x] Implement POST /api/v1/customers
    - [x] Implement GET /api/v1/customers/{id}
    - [x] Implement POST /api/v1/customers/{id}/payment-methods
    - [x] Implement DELETE /api/v1/customers/{id}/payment-methods/{pmId}
- [x] **Security Adapter**
  - [x] Create `StubCustomerTokenizationService.java` (stub for TokenizationServicePort)
- [x] **Customer Infrastructure Tests**
  - [x] Create `CustomerControllerTest.java` (5 tests: register, get, add payment method, remove, validation)

### 4.7 Outbox Infrastructure
- [x] **Persistence Adapter**
  - [x] Create `OutboxEventJpaEntity.java`
  - [x] Create `OutboxEventMapper.java`
  - [x] Create `OutboxEventJpaRepository.java`
  - [x] Create `OutboxEventPersistenceAdapter.java` (implements OutboxEventRepositoryPort)
  - [x] Create database migration: `V5__create_outbox_events_table.sql`
- [x] **Event Publisher Adapter**
  - [x] Create `KafkaOutboxEventPublisher.java`
    - [x] Implement event publishing to Kafka
  - [x] Create `OutboxPollingScheduler.java`
    - [x] Implement @Scheduled method to poll pending events (5s interval)
    - [x] Implement event publishing logic
    - [x] Implement retry logic for failed events (30s interval, max 3 retries)

### 4.8 Idempotency Infrastructure
- [x] **Persistence Adapter**
  - [x] Create `IdempotencyKeyJpaEntity.java`
  - [x] Create `IdempotencyKeyMapper.java`
  - [x] Create `IdempotencyKeyJpaRepository.java`
  - [x] Create `IdempotencyKeyPersistenceAdapter.java` (implements IdempotencyKeyRepositoryPort)
  - [x] Create database migration: `V6__create_idempotency_keys_table.sql`

### 4.9 Reconciliation Infrastructure
- [x] **Persistence Adapters**
  - [x] Create `ReconciliationBatchJpaEntity.java`
  - [x] Create `ReconciliationMapper.java`
  - [x] Create `ReconciliationJpaRepository.java`
  - [x] Create `ReconciliationPersistenceAdapter.java` (implements ReconciliationBatchPort + ReconciliationBatchRepositoryPort)
  - [x] Create `SettlementReportJpaEntity.java`
  - [x] Create `SettlementReportMapper.java`
  - [x] Create `SettlementReportJpaRepository.java`
  - [x] Create `SettlementReportPersistenceAdapter.java` (implements SettlementReportPort + SettlementReportRepositoryPort)
  - [x] Create `DiscrepancyJpaEntity.java`
  - [x] Create `DiscrepancyMapper.java`
  - [x] Create `DiscrepancyJpaRepository.java`
  - [x] Create `DiscrepancyPersistenceAdapter.java` (implements DiscrepancyRepositoryPort)
  - [x] Create `StubReportGenerator.java` (stub for ReportGeneratorPort)
  - [x] Create database migration: `V8__create_reconciliation_tables.sql`
- [x] **REST Adapter**
  - [x] Create `ReconciliationController.java`
    - [x] Implement POST /api/v1/reconciliation/reconcile
    - [x] Implement POST /api/v1/reconciliation/settlement-report

### 4.10 Kafka Infrastructure
- [x] **Kafka Configuration**
  - [x] Create `KafkaConsumerConfig.java`
    - [x] Configure consumer properties
    - [x] Configure deserializers
  - [x] Create `KafkaProducerConfig.java` (via application.yml + KafkaTemplate auto-config)
  - [ ] Create `KafkaTopicInitializer.java`
  - [ ] Create `KafkaErrorHandler.java`

### 4.11 Global Configuration
- [x] Create `SwaggerConfig.java`
  - [x] Configure OpenAPI 3.0
  - [x] Configure API info
- [x] Create `SecurityConfig.java`
- [x] Create `AsyncConfig.java`
- [ ] Create `ActuatorConfig.java`
- [ ] Create `FlywayConfig.java`

**Phase 4 Completion Criteria:**
- [x] All persistence adapters are implemented (Payment, Refund, Transaction, Merchant, Customer, Outbox, Idempotency, Reconciliation)
- [x] All entities are mapped to domain models
- [x] All REST controllers are working (Payment, Refund, Transaction, Merchant, Customer, Reconciliation)
- [x] Kafka integration is functional (KafkaPaymentEventPublisher, KafkaOutboxEventPublisher, OutboxPollingScheduler)
- [x] Database migrations are created and applied (V1-V13)
- [x] All infrastructure tests pass (780 tests)
- [x] Application can process payments end-to-end (verified in Docker)
- [x] All domain and application services wired with @Service and @Transactional

---

## 🔒 PHASE 5: SECURITY IMPLEMENTATION

### 5.1 Authentication & Authorization
- [ ] **Spring Security Configuration**
  - [ ] Configure `SecurityConfig.java`
    - [ ] Disable CSRF for API
    - [ ] Configure session management (stateless)
    - [ ] Configure security filter chain
  - [ ] Configure `ApiKeyAuthenticationFilter.java`
    - [ ] Implement API key extraction from headers
    - [ ] Implement API key validation
    - [ ] Integrate with SecurityContext
  - [ ] Configure `JwtAuthenticationFilter.java`
    - [ ] Implement JWT token extraction
    - [ ] Implement JWT validation
    - [ ] Implement token refresh logic
  - [ ] Create `JwtTokenProvider.java`
    - [ ] Implement token generation
    - [ ] Implement token validation
    - [ ] Implement token expiration handling
- [ ] **RBAC Implementation**
  - [ ] Create `RbacConfig.java`
    - [ ] Define roles: ADMIN, MERCHANT, DEVELOPER
    - [ ] Define permissions per role
  - [ ] Add @PreAuthorize annotations to controllers
  - [ ] Implement method-level security
- [ ] **CORS Configuration**
  - [ ] Configure `CorsConfig.java`
    - [ ] Define allowed origins per environment
    - [ ] Define allowed methods
    - [ ] Define allowed headers

### 5.2 Data Protection (PCI DSS)
- [ ] **Tokenization**
  - [ ] Implement `TokenizationService.java`
    - [ ] Implement card number tokenization
    - [ ] Implement token storage
    - [ ] Implement detokenization (authorized only)
  - [ ] Create token format specification
  - [ ] Implement token lifecycle management
- [ ] **Encryption at Rest**
  - [ ] Implement `EncryptionService.java`
    - [ ] Implement AES-256 encryption
    - [ ] Implement key rotation strategy
    - [ ] Implement encrypted field storage
  - [ ] Configure PostgreSQL TDE (if needed)
  - [ ] Encrypt sensitive columns (card numbers, CVV)
- [ ] **Encryption in Transit**
  - [ ] Configure TLS 1.3 for all communications
  - [ ] Configure HTTPS for REST API
  - [ ] Configure SSL for database connection
  - [ ] Configure SSL for Kafka connection
- [ ] **HMAC Authentication**
  - [ ] Implement request signing for webhooks
  - [ ] Implement signature verification
  - [ ] Add signature validation middleware

### 5.3 Audit & Compliance
- [ ] **Audit Logging**
  - [ ] Implement `AuditLogAspect.java`
    - [ ] Log all data access
    - [ ] Log all data modifications
    - [ ] Include user context in logs
  - [ ] Create audit log entity and table
  - [ ] Implement audit log query interface
- [ ] **PCI DSS Compliance**
  - [ ] Implement network segmentation (Docker networks)
  - [ ] Remove all default passwords
  - [ ] Implement access logging
  - [ ] Implement intrusion detection
  - [ ] Create compliance documentation

### 5.4 Security Tests
- [ ] Create `SecurityConfigTest.java`
- [ ] Create `JwtTokenProviderTest.java`
- [ ] Create `TokenizationServiceTest.java`
- [ ] Create `EncryptionServiceTest.java`
- [ ] Perform security penetration testing

**Phase 5 Completion Criteria:**
- [ ] All API endpoints are secured
- [ ] API key authentication is working
- [ ] JWT authentication is working (if implemented)
- [ ] Card data is tokenized
- [ ] Sensitive data is encrypted at rest
- [ ] All communications are encrypted in transit
- [ ] Audit logging is functional
- [ ] Security tests pass

---

## ⚡ PHASE 6: RESILIENCE IMPLEMENTATION

### 6.1 Circuit Breaker
- [ ] **Configuration**
  - [ ] Configure `CircuitBreakerConfig.java`
    - [ ] Configure paymentProvider circuit breaker
    - [ ] Configure refundProvider circuit breaker
    - [ ] Set sliding window size
    - [ ] Set failure rate threshold
    - [ ] Set wait duration in open state
  - [ ] Register health indicators
- [ ] **Implementation**
  - [ ] Add @CircuitBreaker annotations to external provider calls
  - [ ] Implement fallback methods
  - [ ] Configure fallback responses
- [ ] **Testing**
  - [ ] Test circuit breaker open state
  - [ ] Test circuit breaker half-open state
  - [ ] Test fallback methods
  - [ ] Test circuit breaker recovery

### 6.2 Retry Pattern
- [ ] **Configuration**
  - [ ] Configure `RetryConfig.java`
    - [ ] Configure max attempts
    - [ ] Configure wait duration
    - [ ] Configure exponential backoff multiplier
    - [ ] Configure retry exceptions
    - [ ] Configure ignore exceptions
- [ ] **Implementation**
  - [ ] Add @Retry annotations to external provider calls
  - [ ] Implement retry listener for logging
- [ ] **Testing**
  - [ ] Test retry on transient failures
  - [ ] Test max attempts reached
  - [ ] Test exponential backoff timing

### 6.3 Rate Limiter
- [ ] **Configuration**
  - [ ] Configure `RateLimiterConfig.java`
    - [ ] Configure apiEndpoint rate limiter
    - [ ] Configure paymentEndpoint rate limiter
    - [ ] Set limitForPeriod
    - [ ] Set limitRefreshPeriod
    - [ ] Set timeoutDuration
- [ ] **Implementation**
  - [ ] Add @RateLimiter annotations to endpoints
  - [ ] Implement rate limit exceeded response
- [ ] **Testing**
  - [ ] Test rate limiting under load
  - [ ] Test rate limit recovery

### 6.4 Bulkhead Pattern
- [ ] **Configuration**
  - [ ] Configure `BulkheadConfig.java`
    - [ ] Configure paymentProcessing bulkhead
    - [ ] Configure externalProvider bulkhead
    - [ ] Set maxConcurrentCalls
    - [ ] Set maxWaitDuration
- [ ] **Implementation**
  - [ ] Add @Bulkhead annotations to critical operations
  - [ ] Implement bulkhead full response
- [ ] **Testing**
  - [ ] Test bulkhead under concurrent load
  - [ ] Test bulkhead isolation

### 6.5 Time Limiter
- [ ] **Configuration**
  - [ ] Configure `TimeLimiterConfig.java`
    - [ ] Configure timeoutDuration
    - [ ] Configure cancelRunningFuture
- [ ] **Implementation**
  - [ ] Add @TimeLimiter annotations to long-running operations
  - [ ] Implement timeout response
- [ ] **Testing**
  - [ ] Test timeout on slow operations
  - [ ] Test successful completion within timeout

### 6.6 Resilience Testing
- [ ] Create `Resilience4jConfigTest.java`
- [ ] Test all resilience patterns together
- [ ] Test resilience under chaos (simulate failures)

**Phase 6 Completion Criteria:**
- [ ] All resilience patterns are configured
- [ ] Circuit breaker protects external calls
- [ ] Retry handles transient failures
- [ ] Rate limiter protects against overload
- [ ] Bulkhead isolates failures
- [ ] Time limiter prevents hanging operations
- [ ] All resilience tests pass

---

## 📬 PHASE 7: EVENT-DRIVEN ARCHITECTURE

### 7.1 Kafka Topics Setup
- [ ] **Topic Creation**
  - [ ] Create `payment.created` topic (6 partitions)
  - [ ] Create `payment.completed` topic (6 partitions)
  - [ ] Create `payment.failed` topic (6 partitions)
  - [ ] Create `payment.cancelled` topic (6 partitions)
  - [ ] Create `refund.processed` topic (3 partitions)
  - [ ] Create `refund.failed` topic (3 partitions)
  - [ ] Create `settlement.batch` topic (3 partitions)
  - [ ] Create `merchant.notification` topic (3 partitions)
- [ ] **Topic Configuration**
  - [ ] Configure retention policies
  - [ ] Configure cleanup policies
  - [ ] Verify topics in Kafka UI

### 7.2 Outbox Pattern Implementation
- [ ] **Transactional Outbox**
  - [ ] Implement save entity + outbox event in same transaction
  - [ ] Verify transactional boundaries
- [ ] **Event Polling**
  - [ ] Configure scheduler interval
  - [ ] Implement pending event polling
  - [ ] Implement event publishing
  - [ ] Implement success/failure handling
- [ ] **Event Publishing**
  - [ ] Implement Kafka message publishing
  - [ ] Implement message acknowledgment
  - [ ] Implement dead letter queue (optional)

### 7.3 Event Consumers
- [ ] **Payment Event Listeners**
  - [ ] Create `PaymentConfirmationListener.java`
    - [ ] Listen to payment.completed events
    - [ ] Update payment status
  - [ ] Create payment.failed event listener
- [ ] **Refund Event Listeners**
  - [ ] Create refund event listeners
- [ ] **Merchant Notification Listeners**
  - [ ] Create merchant webhook trigger listener

### 7.4 Event Schema & Versioning
- [ ] Define event payload schemas
- [ ] Implement schema versioning strategy
- [ ] Implement backward compatibility

### 7.5 Event Testing
- [ ] Test outbox event creation
- [ ] Test event polling and publishing
- [ ] Test event consumption
- [ ] Test event ordering
- [ ] Test with Testcontainers Kafka

**Phase 7 Completion Criteria:**
- [ ] All Kafka topics are created
- [ ] Outbox pattern is working correctly
- [ ] Events are published reliably
- [ ] Event consumers process events correctly
- [ ] Event tests pass with Testcontainers

---

## 🧪 PHASE 8: TESTING

### 8.1 Unit Tests
- [ ] **Domain Layer Tests**
  - [ ] All domain model tests pass
  - [ ] All domain service tests pass
  - [ ] Achieve >80% domain layer coverage
- [ ] **Application Layer Tests**
  - [ ] All use case tests pass
  - [ ] All port tests pass
  - [ ] Achieve >80% application layer coverage

### 8.2 Integration Tests
- [ ] **Repository Tests**
  - [x] Create `PaymentJpaRepositoryIntegrationTest.java` (7 tests passing)
  - [ ] Create `JpaTransactionRepositoryTest.java`
  - [ ] Create `JpaRefundRepositoryTest.java`
  - [ ] Create `JpaMerchantRepositoryTest.java`
  - [ ] Create `JpaCustomerRepositoryTest.java`
  - [ ] Create `JpaOutboxEventRepositoryTest.java`
  - [ ] Use Testcontainers for PostgreSQL
- [ ] **Controller Tests**
  - [ ] All controller tests pass
  - [ ] Test success scenarios
  - [ ] Test error scenarios
  - [ ] Test validation scenarios
- [ ] **Provider Tests**
  - [ ] Create `StripePaymentProviderTest.java`
  - [ ] Create `PayPalPaymentProviderTest.java`
  - [ ] Use WireMock for HTTP mocking

### 8.3 E2E Tests
- [ ] **Payment Flow Tests**
  - [ ] Create `PaymentE2ETest.java`
    - [ ] Test complete payment flow
    - [ ] Test idempotent payment
    - [ ] Test payment failure scenarios
- [ ] **Refund Flow Tests**
  - [ ] Create `RefundE2ETest.java`
    - [ ] Test full refund
    - [ ] Test partial refund
    - [ ] Test refund failure scenarios
- [ ] **Merchant Onboarding Tests**
  - [ ] Create `MerchantOnboardingE2ETest.java`
    - [ ] Test merchant registration
    - [ ] Test API key generation
    - [ ] Test merchant suspension

### 8.4 Architecture Tests (ArchUnit)
- [ ] Create `HexagonalArchitectureTest.java`
  - [ ] Test domain layer has no infrastructure dependencies
  - [ ] Test application layer only depends on domain
  - [ ] Test infrastructure depends on application and domain
- [ ] Create `DomainLayerTest.java`
  - [ ] Test domain models are immutable where appropriate
  - [ ] Test domain services don't have state
- [ ] Create `ApplicationLayerTest.java`
  - [ ] Test use cases are stateless
  - [ ] Test use cases only use ports
- [ ] Create `PackageStructureTest.java`
  - [ ] Test package naming conventions
  - [ ] Test bounded context isolation

### 8.5 Performance Tests
- [ ] Load testing with concurrent payments
- [ ] Stress testing for rate limiting
- [ ] Endurance testing for memory leaks

### 8.6 Test Infrastructure
- [ ] Configure `application-test.yml`
- [ ] Create test data utilities
- [ ] Create test base classes
- [ ] Configure WireMock mappings

**Phase 8 Completion Criteria:**
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] All E2E tests pass
- [ ] All architecture tests pass
- [ ] Code coverage >80% for domain and application layers
- [ ] Tests run in CI/CD pipeline

---

## 📊 PHASE 9: OBSERVABILITY

### 9.1 Health Checks
- [ ] **Actuator Configuration**
  - [ ] Configure `ActuatorConfig.java`
  - [ ] Enable health endpoint
  - [ ] Enable metrics endpoint
  - [ ] Enable prometheus endpoint
  - [ ] Enable info endpoint
- [ ] **Health Indicators**
  - [ ] Configure database health indicator
  - [ ] Configure Kafka health indicator
  - [ ] Configure Redis health indicator (if used)
  - [ ] Configure circuit breaker health indicator
  - [ ] Configure rate limiter health indicator
  - [ ] Create custom health indicators

### 9.2 Metrics
- [ ] **Custom Metrics**
  - [ ] Create `CustomMetricsBinder.java`
  - [ ] Implement payment metrics (counters, timers)
  - [ ] Implement refund metrics
  - [ ] Implement merchant metrics
- [ ] **Resilience4j Metrics**
  - [ ] Expose circuit breaker metrics
  - [ ] Expose retry metrics
  - [ ] Expose rate limiter metrics
  - [ ] Expose bulkhead metrics
- [ ] **JVM Metrics**
  - [ ] Configure memory metrics
  - [ ] Configure GC metrics
  - [ ] Configure thread metrics

### 9.3 Distributed Tracing
- [ ] **Micrometer Tracing**
  - [ ] Configure `TracingConfig.java`
  - [ ] Configure correlation IDs
  - [ ] Configure span propagation
- [ ] **Logging Integration**
  - [ ] Add trace ID to log messages
  - [ ] Add span ID to log messages
- [ ] **Cross-Service Tracing**
  - [ ] Propagate trace context to Kafka
  - [ ] Propagate trace context to external providers

### 9.4 Logging
- [ ] **Structured Logging**
  - [ ] Configure JSON log format
  - [ ] Add correlation ID to all logs
  - [ ] Add contextual information to logs
- [ ] **Audit Logging**
  - [ ] Log all payment operations
  - [ ] Log all data access
  - [ ] Log all security events
- [ ] **Log Aggregation**
  - [ ] Configure log levels per package
  - [ ] Configure log rotation
  - [ ] Configure log retention

### 9.5 Dashboards & Alerts
- [ ] Create Grafana dashboard JSON (optional)
  - [ ] Payment metrics dashboard
  - [ ] System health dashboard
  - [ ] Resilience dashboard
- [ ] Define alerting rules
  - [ ] High error rate alert
  - [ ] Circuit breaker open alert
  - [ ] High latency alert
  - [ ] Low disk space alert

**Phase 9 Completion Criteria:**
- [ ] Health endpoints are accessible and accurate
- [ ] All metrics are exposed
- [ ] Distributed tracing is working
- [ ] Structured logging is configured
- [ ] Prometheus can scrape metrics
- [ ] Dashboards display correct data

---

## 📦 PHASE 10: API DOCUMENTATION

### 10.1 OpenAPI/Swagger
- [ ] **Swagger Configuration**
  - [ ] Configure `SwaggerConfig.java`
  - [ ] Set API title, version, description
  - [ ] Configure contact information
  - [ ] Configure license
- [ ] **API Documentation**
  - [ ] Add @Operation annotations to all endpoints
  - [ ] Add @ApiResponse annotations for all responses
  - [ ] Add @Parameter annotations for all parameters
  - [ ] Add @Schema annotations to all DTOs
  - [ ] Add security scheme documentation
- [ ] **API Examples**
  - [ ] Add request examples
  - [ ] Add response examples
  - [ ] Add error response examples

### 10.2 API Documentation Pages
- [ ] Create `docs/API_DOCUMENTATION.md`
  - [ ] Document authentication
  - [ ] Document all endpoints
  - [ ] Document error codes
  - [ ] Document rate limits
  - [ ] Document idempotency

### 10.3 Developer Portal (Optional)
- [ ] Create getting started guide
- [ ] Create SDK examples
- [ ] Create webhook documentation
- [ ] Create Postman collection

**Phase 10 Completion Criteria:**
- [ ] Swagger UI is accessible at /swagger-ui.html
- [ ] OpenAPI spec is available at /v3/api-docs
- [ ] All endpoints are documented
- [ ] API documentation is complete and accurate

---

## 🚀 PHASE 11: PRODUCTION READINESS

### 11.1 Docker & Deployment
- [ ] **Dockerfile**
  - [ ] Create multi-stage Dockerfile
  - [ ] Use JRE slim image for runtime
  - [ ] Configure non-root user
  - [ ] Configure health check
  - [ ] Optimize layer caching
- [ ] **Docker Compose Production**
  - [ ] Create production docker-compose
  - [ ] Configure resource limits
  - [ ] Configure logging drivers
  - [ ] Configure secrets management
- [ ] **Kubernetes Manifests** (Optional)
  - [ ] Create Deployment manifest
  - [ ] Create Service manifest
  - [ ] Create ConfigMap
  - [ ] Create Secret
  - [ ] Create Ingress
  - [ ] Create HPA

### 11.2 Configuration Management
- [ ] **Environment Variables**
  - [ ] Document all environment variables
  - [ ] Create .env.example file
  - [ ] Create environment-specific configs
- [ ] **Secrets Management**
  - [ ] Configure secrets for production
  - [ ] Never commit secrets to git
  - [ ] Use Docker secrets or external vault

### 11.3 Database Migration
- [ ] **Flyway Configuration**
  - [ ] Configure Flyway for production
  - [ ] Test all migrations
  - [ ] Create rollback scripts (optional)
- [ ] **Backup Strategy**
  - [ ] Document backup procedure
  - [ ] Create backup scripts
  - [ ] Test restore procedure

### 11.4 Documentation
- [ ] **README.md**
  - [ ] Project description
  - [ ] Quick start guide
  - [ ] Prerequisites
  - [ ] Installation instructions
  - [ ] Configuration guide
  - [ ] Usage examples
- [ ] **DEVELOPMENT_GUIDE.md**
  - [ ] Development setup
  - [ ] Running tests
  - [ ] Code style guide
  - [ ] Contribution guidelines
- [ ] **DEPLOYMENT_GUIDE.md**
  - [ ] Production deployment steps
  - [ ] Environment configuration
  - [ ] Monitoring setup
  - [ ] Troubleshooting guide
- [ ] **SECURITY_GUIDE.md**
  - [ ] Security best practices
  - [ ] PCI DSS compliance guide
  - [ ] Incident response procedure
- [ ] **TROUBLESHOOTING.md**
  - [ ] Common issues and solutions
  - [ ] FAQ
- [ ] **CHANGELOG.md**
  - [ ] Document all changes
  - [ ] Follow semantic versioning
- [ ] **CONTRIBUTING.md**
  - [ ] Contribution guidelines
  - [ ] Code of conduct
- [ ] **Architecture Decision Records (ADRs)**
  - [ ] Create `docs/decisions/ADR-001-hexagonal-architecture.md`
  - [ ] Create `docs/decisions/ADR-002-outbox-pattern.md`
  - [ ] Create `docs/decisions/ADR-003-kafka-event-streaming.md`
  - [ ] Create `docs/decisions/ADR-004-resilience-patterns.md`

### 11.5 CI/CD Pipeline (Optional)
- [ ] **GitHub Actions / GitLab CI**
  - [ ] Create CI pipeline
  - [ ] Configure build job
  - [ ] Configure test job
  - [ ] Configure code quality job
  - [ ] Configure security scan job
  - [ ] Configure Docker build job
  - [ ] Configure deployment job

### 11.6 Final Verification
- [ ] Run full test suite
- [ ] Run performance benchmarks
- [ ] Run security scan (OWASP dependency-check)
- [ ] Run architecture tests
- [ ] Verify all documentation is complete
- [ ] Create release tag

**Phase 11 Completion Criteria:**
- [ ] Application is containerized and production-ready
- [ ] All documentation is complete
- [ ] CI/CD pipeline is configured (optional)
- [ ] All tests pass
- [ ] Security scan passes
- [ ] Ready for deployment

---

## 📈 OVERALL PROJECT COMPLETION

### Final Checklist
- [ ] All 11 phases are complete
- [ ] All tests pass
- [ ] Code coverage >80%
- [ ] Documentation is complete
- [ ] Application runs in Docker Compose
- [ ] Application processes payments end-to-end
- [ ] Security is properly configured
- [ ] Resilience patterns are working
- [ ] Observability is configured
- [ ] Ready for production deployment

### Project Statistics
- Domain Models: **8/8** bounded contexts (all complete)
- Use Cases: **20/20** use cases (all complete)
- REST Endpoints: **1/~25** endpoints (PaymentController only)
- Database Tables: **12** migrations created
- Kafka Topics: **0/8** topics
- Unit Tests: **758** tests (all passing)
- Integration Tests: **1** test (PaymentJpaRepositoryIntegrationTest)
- E2E Tests: **0** tests
- Architecture Tests: **1** test (HexagonalArchitectureTest)
- Code Coverage: **~70%** (domain + application + commons)

### Phase Status
| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1: Project Initialization | **100%** | All items complete: Git, Docker Compose, docker-compose.override, init scripts, Kafka tests |
| Phase 2: Domain Layer | **100%** | All 8 domain models + domain services + all enum tests complete |
| Phase 3: Application Layer | **100%** | All 20 use cases + DTOs + ports + 20 service tests complete |
| Phase 4: Infrastructure | **~20%** | Payment/Merchant/Customer persistence + PaymentController + Kafka publisher done |
| Phase 5: Security | **0%** | Not started |
| Phase 6: Resilience | **0%** | Not started |
| Phase 7: Event-Driven | **0%** | Not started |
| Phase 8: Testing | **~40%** | Domain + application unit tests complete (758 tests), 1 integration test |
| Phase 9: Observability | **0%** | Not started |
| Phase 10: Documentation | **0%** | Not started |
| Phase 11: Production Ready | **0%** | Not started |

---

## 📝 NOTES

Add your notes, blockers, and observations here:

### Blockers
-

### Lessons Learned
- Money value object should use BigDecimal with consistent scale for equality comparisons
- Domain status transitions must be carefully tested to respect valid state changes
- Enum tests provide comprehensive coverage of all status transitions and edge cases
- All application services should have 1:1 test coverage with their service implementations

### Future Improvements
- Continue Phase 4: Infrastructure Layer (remaining adapters, controllers, persistence)
- Start Phase 5: Security (Spring Security config, JWT, API Key auth)

---

**Last Updated:** 2026-03-05
**Project Status:** Phase 1 (100%), Phase 2 (100%), Phase 3 (100%)
**Tests:** 758 passing (domain + application + commons + architecture)
**Test Coverage:**
- Domain Model Tests: 8 classes
- Domain Service Tests: 8 classes
- Enum Tests: 16 classes (all enums covered)
- Application Service Tests: 20 classes (all use cases covered)
- Commons Tests: 4 classes (Money, CryptoUtils, IdGenerator, StringUtils)
- Architecture Tests: 1 class (HexagonalArchitectureTest)
- Integration Tests: 1 class (PaymentJpaRepositoryIntegrationTest)
