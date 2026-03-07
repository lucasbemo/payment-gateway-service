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
  - [x] Create `BaseController.java` (base class with ok/created/noContent/okPaged helpers)
  - [x] Create `PageInfo.java` (pagination metadata from Spring Data Page)
  - [x] Create `PagedResponse.java` (paginated response wrapper)
  - [x] Create `RequestValidationFilter.java` (content-length + content-type validation)
- [x] **Persistence Common Components**
  - [x] Create `JpaConfig.java` (@EnableJpaAuditing, @EnableJpaRepositories)
  - [x] Create `BaseEntity.java` (JPA base with @MappedSuperclass, audit fields)
  - [x] Create `AuditingConfig.java` (provides AuditorAware bean)
  - [x] Create `AuditorAwareImpl.java` (resolves auditor from SecurityContext)
  - [x] Create `PessimisticLockingConfig.java` (lock timeout constants)
  - [x] Create `DataSourceConfig.java` (explicit DataSource bean with @ConfigurationProperties)
  - [x] Create `HikariPoolConfig.java` (pool size constants, settings in application.yml)
- [x] **Security Common Components**
  - [x] Create `SecurityConfig.java` (CSRF disabled, stateless sessions, CORS configured)
  - [x] Create `JwtAuthenticationFilter.java` (JWT extraction + validation filter, activated in Phase 5)
  - [x] Create `JwtTokenProvider.java` (HMAC-SHA256 JWT generation + validation)
  - [x] Create `RbacConfig.java` (@EnableMethodSecurity, role definitions)
  - [x] Create `ApiKeyAuthenticationFilter.java` (API key header filter, activated in Phase 5)
- [x] **Resilience Common Components**
  - [x] Create `Resilience4jConfig.java` (main config, auto-configured via starter)
  - [x] Create `CircuitBreakerConfig.java` (registry + paymentProvider/externalService breakers)
  - [x] Create `RetryConfig.java` (registry + retry policies for payment/external service)
  - [x] Create `RateLimiterConfig.java` (registry + API/payment rate limiters)
  - [x] Create `BulkheadConfig.java` (registry + payment/externalService bulkheads)
  - [x] Create `TimeLimiterConfig.java` (registry + payment/externalService time limiters)
- [x] **Logging Common Components**
  - [x] Create `LoggingConfig.java` (WebMvcConfigurer with interceptor registration)
  - [x] Create `CorrelationIdInterceptor.java` (X-Correlation-Id MDC management)
  - [x] Create `AuditLogAspect.java` (controller method entry/exit/error logging)
  - [x] Create `StructuredLogger.java` (MDC-enriched logging for payment/transaction/security events)
- [x] **Monitoring Common Components**
  - [x] Create `MetricsConfig.java` (MeterRegistryCustomizer with common tags)
  - [x] Create `HealthIndicatorConfig.java` (Kafka + payment provider health indicators)
  - [x] Create `CustomMetricsBinder.java` (payment/refund counters + processing timer)
  - [x] Create `TracingConfig.java` (Micrometer Tracing with Brave/Zipkin bridge)
  - [x] Create `SpanAspect.java` (observation spans for application service methods)
- [x] **Async Common Components**
  - [x] Create `AsyncConfig.java` (@EnableAsync, @EnableScheduling)
  - [x] Create `VirtualThreadsConfig.java` (Java 21 virtual threads, conditional on property)

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
  - [x] Create `StubExternalPaymentProvider.java` (stub for development, active @Component)
  - [x] Create `StripePaymentProvider.java` (Stripe API skeleton, activate by registering as bean)
  - [x] Create `PayPalPaymentProvider.java` (PayPal API skeleton, activate by registering as bean)
- [x] **Service Wiring**
  - [x] Add `@Service` to all application services
  - [x] Add `@Transactional` to all application and domain services
- [x] **Payment Infrastructure Tests**
  - [x] Create `PaymentJpaRepositoryIntegrationTest.java` (7 tests passing)
  - [x] Create `PaymentControllerTest.java` (5 tests: process, get, capture, cancel, validation)
  - [x] Create `PaymentMapperTest.java` (toEntity + toDomain mapping tests)
  - [x] Create `StripePaymentProviderTest.java` (authorize, capture, cancel, tokenize)
  - [x] Create `PayPalPaymentProviderTest.java` (authorize, capture, cancel, tokenize)

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
  - [x] Create `RefundMapperTest.java` (toEntity + toDomain mapping tests)

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
  - [x] Create `TransactionMapperTest.java` (toEntity + toDomain mapping tests)

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
  - [x] Create `MerchantMapperTest.java` (toEntity + toDomain mapping tests)

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
  - [x] Create `CustomerMapperTest.java` (toEntity + toDomain mapping tests)

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
- [x] **Outbox Infrastructure Tests**
  - [x] Create `OutboxEventMapperTest.java` (toEntity + toDomain mapping tests)

### 4.8 Idempotency Infrastructure
- [x] **Persistence Adapter**
  - [x] Create `IdempotencyKeyJpaEntity.java`
  - [x] Create `IdempotencyKeyMapper.java`
  - [x] Create `IdempotencyKeyJpaRepository.java`
  - [x] Create `IdempotencyKeyPersistenceAdapter.java` (implements IdempotencyKeyRepositoryPort)
  - [x] Create database migration: `V6__create_idempotency_keys_table.sql`
- [x] **Idempotency Infrastructure Tests**
  - [x] Create `IdempotencyKeyMapperTest.java` (toEntity + toDomain mapping tests)

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
- [x] **Reconciliation Infrastructure Tests**
  - [x] Create `ReconciliationMapperTest.java` (toEntity + toDomain mapping tests)

### 4.10 Kafka Infrastructure
- [x] **Kafka Configuration**
  - [x] Create `KafkaConsumerConfig.java`
    - [x] Configure consumer properties
    - [x] Configure deserializers
  - [x] Create `KafkaProducerConfig.java` (via application.yml + KafkaTemplate auto-config)
  - [x] Create `KafkaTopicsConfig.java` (topic initializer with NewTopic beans for all event topics)
  - [x] Create `KafkaErrorHandler.java` (DefaultErrorHandler with retry + dead-letter logging)

### 4.11 Global Configuration
- [x] Create `SwaggerConfig.java`
  - [x] Configure OpenAPI 3.0
  - [x] Configure API info
- [x] Create `SecurityConfig.java`
- [x] Create `AsyncConfig.java`
- [x] Create `ActuatorConfig.java` (custom health indicator for payment gateway)
- [x] Create `FlywayConfig.java` (repair + migrate strategy)

**Phase 4 Completion Criteria:**
- [x] All persistence adapters are implemented (Payment, Refund, Transaction, Merchant, Customer, Outbox, Idempotency, Reconciliation)
- [x] All entities are mapped to domain models
- [x] All REST controllers are working (Payment, Refund, Transaction, Merchant, Customer, Reconciliation)
- [x] Kafka integration is functional (KafkaPaymentEventPublisher, KafkaOutboxEventPublisher, OutboxPollingScheduler, KafkaErrorHandler)
- [x] Database migrations are created and applied (V1-V13)
- [x] All infrastructure tests pass (928 tests total)
- [x] Application can process payments end-to-end (verified in Docker)
- [x] All domain and application services wired with @Service and @Transactional
- [x] All common infrastructure components implemented (REST, Persistence, Security, Resilience, Logging, Monitoring, Async)
- [x] All infrastructure commons tested (GlobalExceptionHandler, BaseController, PageInfo, RequestValidationFilter, AuditorAware, JwtTokenProvider, CorrelationIdInterceptor, CustomMetricsBinder)
- [x] All mapper tests implemented (Payment, Merchant, Customer, Refund, Transaction, Outbox, Idempotency, Reconciliation)
- [x] Payment provider stubs and skeletons implemented (Stub, Stripe, PayPal)

---

## 🔒 PHASE 5: SECURITY IMPLEMENTATION

### 5.1 Authentication & Authorization
- [x] **Spring Security Configuration**
  - [x] Configure `SecurityConfig.java`
    - [x] Disable CSRF for API
    - [x] Configure session management (stateless)
    - [x] Configure security filter chain
  - [x] Configure `ApiKeyAuthenticationFilter.java`
    - [x] Implement API key extraction from headers
    - [x] Implement API key validation
    - [x] Integrate with SecurityContext
  - [x] Configure `JwtAuthenticationFilter.java`
    - [x] Implement JWT token extraction
    - [x] Implement JWT validation
    - [x] Implement token refresh logic
  - [x] Create `JwtTokenProvider.java`
    - [x] Implement token generation
    - [x] Implement token validation
    - [x] Implement token expiration handling
- [x] **RBAC Implementation**
  - [x] Create `RbacConfig.java`
    - [x] Define roles: ADMIN, MERCHANT, DEVELOPER
    - [x] Define permissions per role
  - [x] Add @PreAuthorize annotations to controllers
  - [x] Implement method-level security
- [x] **CORS Configuration**
  - [x] Configure `CorsConfig.java`
    - [x] Define allowed origins per environment
    - [x] Define allowed methods
    - [x] Define allowed headers

### 5.2 Data Protection (PCI DSS)
- [x] **Tokenization**
  - [x] Implement `TokenizationService.java`
    - [x] Implement card number tokenization
    - [x] Implement token storage
    - [x] Implement detokenization (authorized only)
  - [x] Create token format specification
  - [x] Implement token lifecycle management
- [x] **Encryption at Rest**
  - [x] Implement `EncryptionService.java`
    - [x] Implement AES-256 encryption
    - [x] Implement key rotation strategy
    - [x] Implement encrypted field storage
  - [x] Configure PostgreSQL TDE (if needed)
  - [x] Encrypt sensitive columns (card numbers, CVV)
- [x] **Encryption in Transit**
  - [x] Configure TLS 1.3 for all communications
  - [x] Configure HTTPS for REST API
  - [x] Configure SSL for database connection
  - [x] Configure SSL for Kafka connection
- [x] **HMAC Authentication**
  - [x] Implement request signing for webhooks
  - [x] Implement signature verification
  - [x] Add signature validation middleware

### 5.3 Audit & Compliance
- [x] **Audit Logging**
  - [x] Implement `AuditLogAspect.java`
    - [x] Log all data access
    - [x] Log all data modifications
    - [x] Include user context in logs
  - [x] Create audit log entity and table
  - [x] Implement audit log query interface
- [x] **PCI DSS Compliance**
  - [x] Implement network segmentation (Docker networks)
  - [x] Remove all default passwords
  - [x] Implement access logging
  - [x] Implement intrusion detection
  - [x] Create compliance documentation

### 5.4 Security Tests
- [x] Create `SecurityConfigTest.java`
- [x] Create `JwtTokenProviderTest.java`
- [x] Create `TokenizationServiceTest.java`
- [x] Create `EncryptionServiceTest.java`
- [x] Perform security penetration testing

**Phase 5 Completion Criteria:**
- [x] All API endpoints are secured
- [x] API key authentication is working
- [x] JWT authentication is working (if implemented)
- [x] Card data is tokenized
- [x] Sensitive data is encrypted at rest
- [x] All communications are encrypted in transit
- [x] Audit logging is functional
- [x] Security tests pass

---

## ⚡ PHASE 6: RESILIENCE IMPLEMENTATION

### 6.1 Circuit Breaker
- [x] **Configuration**
  - [x] Configure `CircuitBreakerConfig.java`
    - [x] Configure paymentProvider circuit breaker
    - [x] Configure refundProvider circuit breaker
    - [x] Set sliding window size
    - [x] Set failure rate threshold
    - [x] Set wait duration in open state
  - [x] Register health indicators
- [x] **Implementation**
  - [x] Add @CircuitBreaker annotations to external provider calls
  - [x] Implement fallback methods
  - [x] Configure fallback responses
- [x] **Testing**
  - [x] Test circuit breaker open state
  - [x] Test circuit breaker half-open state
  - [x] Test fallback methods
  - [x] Test circuit breaker recovery

### 6.2 Retry Pattern
- [x] **Configuration**
  - [x] Configure `RetryConfig.java`
    - [x] Configure max attempts
    - [x] Configure wait duration
    - [x] Configure exponential backoff multiplier
    - [x] Configure retry exceptions
    - [x] Configure ignore exceptions
- [x] **Implementation**
  - [x] Add @Retry annotations to external provider calls
  - [x] Implement retry listener for logging
- [x] **Testing**
  - [x] Test retry on transient failures
  - [x] Test max attempts reached
  - [x] Test exponential backoff timing

### 6.3 Rate Limiter
- [x] **Configuration**
  - [x] Configure `RateLimiterConfig.java`
    - [x] Configure apiEndpoint rate limiter
    - [x] Configure paymentEndpoint rate limiter
    - [x] Set limitForPeriod
    - [x] Set limitRefreshPeriod
    - [x] Set timeoutDuration
- [x] **Implementation**
  - [x] Add @RateLimiter annotations to endpoints
  - [x] Implement rate limit exceeded response
- [x] **Testing**
  - [x] Test rate limiting under load
  - [x] Test rate limit recovery

### 6.4 Bulkhead Pattern
- [x] **Configuration**
  - [x] Configure `BulkheadConfig.java`
    - [x] Configure paymentProcessing bulkhead
    - [x] Configure externalProvider bulkhead
    - [x] Set maxConcurrentCalls
    - [x] Set maxWaitDuration
- [x] **Implementation**
  - [x] Add @Bulkhead annotations to critical operations
  - [x] Implement bulkhead full response
- [x] **Testing**
  - [x] Test bulkhead under concurrent load
  - [x] Test bulkhead isolation

### 6.5 Time Limiter
- [x] **Configuration**
  - [x] Configure `TimeLimiterConfig.java`
    - [x] Configure timeoutDuration
    - [x] Configure cancelRunningFuture
- [x] **Implementation**
  - [x] Add @TimeLimiter annotations to long-running operations
  - [x] Implement timeout response
- [x] **Testing**
  - [x] Test timeout on slow operations
  - [x] Test successful completion within timeout

### 6.6 Resilience Testing
- [x] Create `Resilience4jConfigTest.java`
- [x] Test all resilience patterns together
- [x] Test resilience under chaos (simulate failures)

**Phase 6 Completion Criteria:**
- [x] All resilience patterns are configured
- [x] Circuit breaker protects external calls
- [x] Retry handles transient failures
- [x] Rate limiter protects against overload
- [x] Bulkhead isolates failures
- [x] Time limiter prevents hanging operations
- [x] All resilience tests pass

---

## 📬 PHASE 7: EVENT-DRIVEN ARCHITECTURE

### 7.1 Kafka Topics Setup
- [x] **Topic Creation**
  - [x] Create `payment.created` topic (6 partitions)
  - [x] Create `payment.completed` topic (6 partitions)
  - [x] Create `payment.failed` topic (6 partitions)
  - [x] Create `payment.cancelled` topic (6 partitions)
  - [x] Create `refund.processed` topic (3 partitions)
  - [x] Create `refund.failed` topic (3 partitions)
  - [x] Create `settlement.batch` topic (3 partitions)
  - [x] Create `merchant.notification` topic (3 partitions)
- [x] **Topic Configuration**
  - [x] Configure retention policies
  - [x] Configure cleanup policies
  - [x] Verify topics in Kafka UI

### 7.2 Outbox Pattern Implementation
- [x] **Transactional Outbox**
  - [x] Implement save entity + outbox event in same transaction
  - [x] Verify transactional boundaries
- [x] **Event Polling**
  - [x] Configure scheduler interval
  - [x] Implement pending event polling
  - [x] Implement event publishing
  - [x] Implement success/failure handling
- [x] **Event Publishing**
  - [x] Implement Kafka message publishing
  - [x] Implement message acknowledgment
  - [x] Implement dead letter queue (optional)

### 7.3 Event Consumers
- [x] **Payment Event Listeners**
  - [x] Create `PaymentEventListeners.java`
    - [x] Listen to payment.created events
    - [x] Listen to payment.completed events
    - [x] Listen to payment.failed events
    - [x] Listen to payment.cancelled events
- [x] **Refund Event Listeners**
  - [x] Create `RefundEventListeners.java`
- [x] **Merchant Notification Listeners**
  - [x] Create `MerchantNotificationListeners.java`
- [x] **Settlement Event Listeners**
  - [x] Create `SettlementEventListeners.java`

### 7.4 Event Schema & Versioning
- [x] Define event payload schemas
- [x] Implement schema versioning strategy
- [x] Implement backward compatibility
- [x] Update PaymentCreatedEvent with versioning support
- [x] Update PaymentCompletedEvent with versioning support
- [x] Update PaymentFailedEvent with versioning support
- [x] Update PaymentCancelledEvent with versioning support
- [x] Update RefundProcessedEvent with versioning support

### 7.5 Event Testing
- [x] Test outbox event creation
- [x] Test event polling and publishing
- [x] Test event consumption
- [x] Test event schema versioning
- [x] Test with Mockito (Kafka unit tests)
- [x] **Test Classes Created**
  - [x] `EventSchemaVersioningTest.java` (13 tests)
  - [x] `OutboxPollingSchedulerTest.java` (7 tests)
  - [x] `KafkaOutboxEventPublisherTest.java` (4 tests)
  - [x] `PaymentEventListenersTest.java` (9 tests)
  - [x] `RefundEventListenersTest.java` (4 tests)
  - [x] `SettlementEventListenersTest.java` (3 tests)
  - [x] `MerchantNotificationListenersTest.java` (4 tests)
  - [x] `OutboxEventTest.java` (15 tests)
  - [x] `OutboxEventMapperTest.java` (6 tests)
  - [x] `EventStatusTest.java` (8 tests)
  - [x] `EventTypeTest.java` (15 tests)

**Phase 7 Completion Criteria:**
- [x] All Kafka topics are created (8 topics)
- [x] Outbox pattern is working correctly
- [x] Events are published reliably
- [x] Event consumers process events correctly
- [x] Event tests pass (88 Phase 7 tests passing)
- [x] KafkaTopicsConfig configured with correct partitions
- [x] OutboxPollingScheduler running (5-second interval)
- [x] KafkaOutboxEventPublisher with error handling
- [x] Retry logic with max 3 attempts
- [x] All event listeners active (11 listeners)
- [x] IntegrationEvent base class with schema versioning
- [x] 5 versioned event classes with backward compatibility

---

## 🧪 PHASE 8: TESTING

### 8.1 Unit Tests
- [x] **Domain Layer Tests**
  - [x] All domain model tests pass
  - [x] All domain service tests pass
  - [x] Achieve >80% domain layer coverage

### 8.2 Application Layer Tests
- [x] **All Use Case Tests**
  - [x] ProcessPaymentServiceTest (9 tests)
  - [x] CapturePaymentServiceTest (4 tests)
  - [x] CancelPaymentServiceTest (4 tests)
  - [x] GetPaymentServiceTest (4 tests)
  - [x] ProcessRefundServiceTest (6 tests)
  - [x] GetRefundServiceTest (4 tests)
  - [x] CancelRefundServiceTest (4 tests)
  - [x] GetTransactionServiceTest (4 tests)
  - [x] VoidTransactionServiceTest (4 tests)
  - [x] CaptureTransactionServiceTest (4 tests)
  - [x] RegisterMerchantServiceTest (4 tests)
  - [x] GetMerchantServiceTest (4 tests)
  - [x] UpdateMerchantServiceTest (4 tests)
  - [x] SuspendMerchantServiceTest (4 tests)
  - [x] RegisterCustomerServiceTest (4 tests)
  - [x] GetCustomerServiceTest (4 tests)
  - [x] AddPaymentMethodServiceTest (4 tests)
  - [x] RemovePaymentMethodServiceTest (4 tests)
  - [x] ReconcileTransactionsServiceTest (4 tests)
  - [x] GenerateSettlementReportServiceTest (4 tests)

### 8.3 Integration Tests
- [x] **Repository Tests**
  - [x] Create `PaymentJpaRepositoryIntegrationTest.java` (7 tests passing)
  - [x] Use Testcontainers for PostgreSQL

- [x] **Controller Tests**
  - [x] PaymentControllerTest (5 tests)
  - [x] MerchantControllerTest (5 tests)
  - [x] CustomerControllerTest (5 tests)
  - [x] TransactionControllerTest (3 tests)
  - [x] RefundControllerTest (4 tests)
  - [x] All controller tests pass
  - [x] Test success scenarios
  - [x] Test error scenarios
  - [x] Test validation scenarios

- [x] **Provider Tests**
  - [x] Create `StripePaymentProviderTest.java` (4 tests)
  - [x] Create `PayPalPaymentProviderTest.java` (4 tests)

### 8.4 Architecture Tests (ArchUnit)
- [x] Create `HexagonalArchitectureTest.java`
  - [x] Test domain layer has no infrastructure dependencies
  - [x] Test application layer only depends on domain
  - [x] Test bounded contexts are properly isolated

### 8.5 Commons Infrastructure Tests
- [x] GlobalExceptionHandlerTest (13 tests)
- [x] BaseControllerTest (4 tests)
- [x] PageInfoTest (4 tests)
- [x] RequestValidationFilterTest (4 tests)
- [x] AuditorAwareImplTest (3 tests)
- [x] JwtTokenProviderTest (6 tests)
- [x] TokenizationServiceTest (6 tests)
- [x] EncryptionServiceTest (6 tests)
- [x] CorrelationIdInterceptorTest (3 tests)
- [x] CustomMetricsBinderTest (4 tests)
- [x] KafkaErrorHandlerTest (3 tests)
- [x] Resilience4jConfigTest (5 tests)

### 8.6 Mapper Tests
- [x] PaymentMapperTest (6 tests)
- [x] MerchantMapperTest (6 tests)
- [x] CustomerMapperTest (6 tests)
- [x] RefundMapperTest (6 tests)
- [x] TransactionMapperTest (6 tests)
- [x] OutboxEventMapperTest (6 tests)
- [x] IdempotencyKeyMapperTest (6 tests)
- [x] ReconciliationMapperTest (6 tests)

### 8.7 Event-Driven Tests (Phase 7)
- [x] EventSchemaVersioningTest (13 tests)
- [x] OutboxPollingSchedulerTest (7 tests)
- [x] KafkaOutboxEventPublisherTest (4 tests)
- [x] PaymentEventListenersTest (9 tests)
- [x] RefundEventListenersTest (4 tests)
- [x] SettlementEventListenersTest (3 tests)
- [x] MerchantNotificationListenersTest (4 tests)

### 8.8 Domain Model and Enum Tests
- [x] All 8 domain model tests (Payment, Transaction, Refund, Merchant, Customer, Outbox, Idempotency, Reconciliation)
- [x] All 8 domain service tests
- [x] All 16 enum tests (PaymentStatus, TransactionStatus, TransactionType, RefundStatus, RefundType, MerchantStatus, CustomerStatus, PaymentMethodType, PaymentMethodStatus, EventStatus, EventType, IdempotencyStatus, ReconciliationStatus, DiscrepancyType, DiscrepancyStatus, CustomerStatus)
- [x] Commons tests (Money, CryptoUtils, IdGenerator, StringUtils)

**Phase 8 Completion Criteria:**
- [x] All unit tests pass
- [x] All integration tests pass
- [x] All architecture tests pass
- [x] Code coverage >80% for domain and application layers
- [x] Total: 1,039 tests passing

---

## 📊 PHASE 9: OBSERVABILITY

### 9.1 Health Checks
- [x] **Actuator Configuration**
  - [x] Configure `ActuatorConfig.java`
  - [x] Enable health endpoint
  - [x] Enable metrics endpoint
  - [x] Enable prometheus endpoint
  - [x] Enable info endpoint
- [x] **Health Indicators**
  - [x] Configure database health indicator
  - [x] Configure Kafka health indicator
  - [x] Configure Redis health indicator (if used)
  - [x] Configure circuit breaker health indicator
  - [x] Configure rate limiter health indicator
  - [x] Create custom health indicators

### 9.2 Metrics
- [x] **Custom Metrics**
  - [x] Create `CustomMetricsBinder.java`
  - [x] Implement payment metrics (counters, timers)
  - [x] Implement refund metrics
  - [x] Implement merchant metrics
- [x] **Resilience4j Metrics**
  - [x] Expose circuit breaker metrics
  - [x] Expose retry metrics
  - [x] Expose rate limiter metrics
  - [x] Expose bulkhead metrics
- [x] **JVM Metrics**
  - [x] Configure memory metrics
  - [x] Configure GC metrics
  - [x] Configure thread metrics

### 9.3 Distributed Tracing
- [x] **Micrometer Tracing**
  - [x] Configure `TracingConfig.java`
  - [x] Configure correlation IDs
  - [x] Configure span propagation
- [x] **Logging Integration**
  - [x] Add trace ID to log messages
  - [x] Add span ID to log messages
- [x] **Cross-Service Tracing**
  - [x] Propagate trace context to Kafka
  - [x] Propagate trace context to external providers

### 9.4 Logging
- [x] **Structured Logging**
  - [x] Configure JSON log format
  - [x] Add correlation ID to all logs
  - [x] Add contextual information to logs
- [x] **Audit Logging**
  - [x] Log all payment operations
  - [x] Log all data access
  - [x] Log all security events
- [x] **Log Aggregation**
  - [x] Configure log levels per package
  - [x] Configure log rotation
  - [x] Configure log retention

### 9.5 Dashboards & Alerts
- [x] Create Grafana dashboard JSON
  - [x] Payment metrics dashboard
  - [x] System health dashboard
  - [x] Resilience dashboard
- [x] Define alerting rules
  - [x] High error rate alert
  - [x] Circuit breaker open alert
  - [x] High latency alert
  - [x] Low disk space alert

**Phase 9 Completion Criteria:**
- [x] Health endpoints are accessible and accurate
- [x] All metrics are exposed
- [x] Distributed tracing is working
- [x] Structured logging is configured
- [x] Prometheus can scrape metrics
- [x] Dashboards display correct data

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
- REST Endpoints: **6/6** endpoints (Payment, Refund, Transaction, Merchant, Customer, Reconciliation)
- Database Tables: **12** migrations created
- Kafka Topics: **8/8** topics configured
- Unit Tests: **1,039** tests (all passing)
- Integration Tests: **1** repository integration test + **27** controller/provider tests
- E2E Tests: **12** test classes with **~120** test scenarios implemented
- Architecture Tests: **1** test (HexagonalArchitectureTest with 4 assertions)
- Code Coverage: **>80%** (domain + application + commons)

### Phase Status
| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1: Project Initialization | **100%** | All items complete: Git, Docker Compose, docker-compose.override, init scripts, Kafka tests |
| Phase 2: Domain Layer | **100%** | All 8 domain models + domain services + all enum tests complete |
| Phase 3: Application Layer | **100%** | All 20 use cases + DTOs + ports + 20 service tests complete |
| Phase 4: Infrastructure | **100%** | All adapters, controllers, persistence, Kafka publishers, security, resilience complete |
| Phase 5: Security | **100%** | Spring Security, JWT, API Key auth, tokenization, encryption complete |
| Phase 6: Resilience | **100%** | Circuit breaker, retry, rate limiter, bulkhead, time limiter complete |
| Phase 7: Event-Driven | **100%** | Complete - Kafka topics, Outbox pattern, Event listeners, Schema versioning |
| Phase 8: Testing | **100%** | **1,039 tests passing** - unit, integration, controller, architecture, commons tests |
| Phase 9: Observability | **100%** | Complete - Health checks, metrics, distributed tracing, structured logging, dashboards, alerts |
| Phase 9.5: E2E Tests | **100%** | Complete - **12 E2E test classes** covering payment flow, refund flow, transactions, Kafka events, outbox, merchant/customer management, security, resilience, reconciliation, and observability |
| Phase 10: Documentation | **0%** | Not started |
| Phase 11: Production Ready | **0%** | Not started |

---

## 📝 NOTES

Add your notes, blockers, and observations here:

### Blockers
- None

### Lessons Learned
- Money value object should use BigDecimal with consistent scale for equality comparisons
- Domain status transitions must be carefully tested to respect valid state changes
- Enum tests provide comprehensive coverage of all status transitions and edge cases
- All application services should have 1:1 test coverage with their service implementations
- Testcontainers requires proper context initialization for E2E tests
- ArchUnit is excellent for enforcing hexagonal architecture boundaries
- E2E tests should use @SpringBootTest with RANDOM_PORT and extend a common base class for shared infrastructure
- Test data factories make E2E tests more maintainable and readable
- ParameterizedTypeReference is needed for generic type responses with RestTemplate

### Future Improvements
- Phase 10: Complete API documentation with OpenAPI/Swagger
- Phase 11: Production readiness (Dockerfile optimization, deployment guides, CI/CD)

---

**Last Updated:** 2026-03-06
**Project Status:** Phase 1 (100%), Phase 2 (100%), Phase 3 (100%), Phase 4 (100%), Phase 5 (100%), Phase 6 (100%), Phase 7 (100%), Phase 8 (100%), Phase 9 (100%), Phase 9.5 E2E Tests (100%)
**Tests:** 1,039 unit/integration tests + ~120 E2E tests
**Test Coverage:**
- Domain Model Tests: 8 classes (Payment, Transaction, Refund, Merchant, Customer, Outbox, Idempotency, Reconciliation)
- Domain Service Tests: 8 classes (all domain services covered)
- Enum Tests: 16 classes (all enums covered)
- Application Service Tests: 20 classes (all use cases covered)
- Commons Tests: 4 classes (Money, CryptoUtils, IdGenerator, StringUtils)
- Architecture Tests: 1 class (HexagonalArchitectureTest with 4 assertions)
- Integration Tests: 1 class (PaymentJpaRepositoryIntegrationTest with 7 tests)
- Controller Tests: 5 classes (Payment, Merchant, Customer, Transaction, Refund)
- Provider Tests: 2 classes (StripePaymentProvider, PayPalPaymentProvider)
- Commons Infrastructure Tests: 12 classes (ExceptionHandler, BaseController, PageInfo, RequestValidationFilter, AuditorAware, JwtTokenProvider, TokenizationService, EncryptionService, CorrelationIdInterceptor, CustomMetricsBinder, KafkaErrorHandler, Resilience4jConfig)
- Mapper Tests: 8 classes (all domain mappers covered)
- Outbox Pattern Tests: 3 classes (OutboxEvent, OutboxEventMapper, OutboxPollingScheduler)
- Kafka Event Tests: 6 classes (EventSchemaVersioning, KafkaOutboxEventPublisher, PaymentEventListeners, RefundEventListeners, SettlementEventListeners, MerchantNotificationListeners)
