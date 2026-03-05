# Production Payment Gateway Service - DDD Modular Hexagonal Architecture

## 📋 Overview

A production-ready payment gateway service built with **Java 21 + Spring Boot 3**, following **DDD Modular Hexagonal Architecture**, with **Kafka** for event-driven messaging, **PostgreSQL** for persistence, **Redis** for idempotency, and full **Docker Compose** infrastructure.

---

## 🎯 Learning Goals

This project will give you hands-on experience with:
- ✅ AI-assisted development in a realistic production scenario
- ✅ Feature development & pull request reviews
- ✅ Bug fixing & testing strategies
- ✅ Modern architectural patterns & best practices
- ✅ Domain-Driven Design (DDD)
- ✅ Hexagonal Architecture (Ports & Adapters)
- ✅ Vertical Slice Organization
- ✅ Idempotency Handling
- ✅ Reconciliation Engine

---

## 🏗️ Architecture: DDD + Hexagonal (Organized by Domain)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         DOMAIN LAYER (Core)                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│  │ payment/ │ │merchant/ │ │transact/ │ │idempoten-│ │reconcilia-   │  │
│  │ - model  │ │ - model  │ │ - model  │ │ - cy/    │ │ - tion/      │  │
│  │ - service│ │ - service│ │ - service│ │ - model  │ │ - model      │  │
│  │ - port   │ │ - port   │ │ - port   │ │ - service│ │ - service    │  │
│  │ - event  │ │ - event  │ │ - event  │ │ - port   │ │ - port       │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────────┘  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                                │
│  │customer/ │ │notifica- │ │ common/  │                                │
│  │ - model  │ │ - tion/  │ │ - model  │                                │
│  │ - service│ │ - model  │ │ - service│                                │
│  │ - port   │ │ - service│ │ - port   │                                │
│  │ - event  │ │ - port   │ │ - event  │                                │
│  └──────────┘ └──────────┘ └──────────┘                                │
├─────────────────────────────────────────────────────────────────────────┤
│                      APPLICATION LAYER                                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│  │ payment/ │ │merchant/ │ │transact/ │ │idempoten-│ │reconcilia-   │  │
│  │ - usecase│ │ - usecase│ │ - usecase│ │ - cy/    │ │ - tion/      │  │
│  │ - dto    │ │ - dto    │ │ - dto    │ │ - dto    │ │ - dto        │  │
│  │ - mapper │ │ - mapper │ │ - mapper │ │ - mapper │ │ - mapper     │  │
│  │ - handler│ │ - handler│ │ - handler│ │ - handler│ │ - handler    │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────────┘  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                                │
│  │customer/ │ │notifica- │ │ common/  │                                │
│  │ - usecase│ │ - tion/  │ │ - usecase│                                │
│  │ - dto    │ │ - dto    │ │ - dto    │                                │
│  │ - mapper │ │ - mapper │ │ - mapper │                                │
│  │ - handler│ │ - handler│ │ - handler│                                │
│  └──────────┘ └──────────┘ └──────────┘                                │
├─────────────────────────────────────────────────────────────────────────┤
│                    INFRASTRUCTURE LAYER                                  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│  │ payment/ │ │merchant/ │ │transact/ │ │idempoten-│ │reconcilia-   │  │
│  │ - adapter│ │ - adapter│ │ - adapter│ │ - cy/    │ │ - tion/      │  │
│  │ - config │ │ - config │ │ - config │ │ - config │ │ - config     │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────────┘  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐                  │
│  │customer/ │ │notifica- │ │ security/│ │ config/  │                  │
│  │ - adapter│ │ - tion/  │ │ - kafka/ │ │ - redis/ │                  │
│  │ - config │ │ - adapter│ │          │ │          │                  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘                  │
└─────────────────────────────────────────────────────────────────────────┘
```

### Dependency Flow (DDD + Hexagonal)

```
Infrastructure (payment/adapter/in/rest)
         ↓
Application (payment/usecase)
         ↓
   Domain (payment/model, payment/service)
         ↓
Infrastructure (payment/adapter/out/persistence)
```

**Dependencies point inward:**
- Same-domain dependencies stay within the domain package
- Cross-domain communication happens via Domain Events
- Domain has ZERO dependencies on other layers

---

## 📁 Project Structure

```
payment-gateway/
├── docker-compose.yml
├── pom.xml
├── Makefile
├── .github/workflows/
│   ├── ci-build.yml
│   ├── ci-test.yml
│   └── code-quality.yml
│
├── src/main/java/com/payment/gateway/
│   │
│   ├── domain/                          # DOMAIN LAYER (Pure Java)
│   │   │
│   │   ├── payment/                     # PAYMENT DOMAIN
│   │   │   ├── model/
│   │   │   │   ├── Payment.java              (Aggregate Root)
│   │   │   │   ├── PaymentStatus.java        (Enum)
│   │   │   │   └── PaymentMethod.java        (Value Object)
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── PaymentProcessor.java
│   │   │   │   ├── PaymentValidator.java
│   │   │   │   └── RefundProcessor.java
│   │   │   │
│   │   │   ├── port/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ProcessPaymentPort.java
│   │   │   │   │   ├── RefundPaymentPort.java
│   │   │   │   │   └── CancelPaymentPort.java
│   │   │   │   │
│   │   │   │   └── out/
│   │   │   │       ├── PaymentRepositoryPort.java
│   │   │   │       ├── PaymentGatewayPort.java
│   │   │   │       └── PaymentEventPublisherPort.java
│   │   │   │
│   │   │   └── event/
│   │   │       ├── PaymentCreatedEvent.java
│   │   │       ├── PaymentCompletedEvent.java
│   │   │       ├── PaymentFailedEvent.java
│   │   │       └── RefundProcessedEvent.java
│   │   │
│   │   ├── merchant/                    # MERCHANT DOMAIN
│   │   │   ├── model/
│   │   │   │   ├── Merchant.java             (Aggregate Root)
│   │   │   │   ├── MerchantStatus.java       (Enum)
│   │   │   │   └── ApiKey.java               (Value Object)
│   │   │   │
│   │   │   ├── service/
│   │   │   │   └── MerchantValidationService.java
│   │   │   │
│   │   │   ├── port/
│   │   │   │   ├── in/
│   │   │   │   │   ├── CreateMerchantPort.java
│   │   │   │   │   ├── UpdateMerchantPort.java
│   │   │   │   │   └── DeleteMerchantPort.java
│   │   │   │   │
│   │   │   │   └── out/
│   │   │   │       └── MerchantRepositoryPort.java
│   │   │   │
│   │   │   └── event/
│   │   │       ├── MerchantCreatedEvent.java
│   │   │       └── MerchantUpdatedEvent.java
│   │   │
│   │   ├── transaction/                 # TRANSACTION DOMAIN
│   │   │   ├── model/
│   │   │   │   ├── Transaction.java          (Aggregate Root)
│   │   │   │   ├── TransactionStatus.java    (Enum)
│   │   │   │   └── TransactionType.java      (Enum)
│   │   │   │
│   │   │   ├── service/
│   │   │   │   └── TransactionQueryService.java
│   │   │   │
│   │   │   ├── port/
│   │   │   │   ├── in/
│   │   │   │   │   └── QueryTransactionPort.java
│   │   │   │   │
│   │   │   │   └── out/
│   │   │   │       └── TransactionRepositoryPort.java
│   │   │   │
│   │   │   └── event/
│   │   │       └── TransactionCreatedEvent.java
│   │   │
│   │   ├── idempotency/                 # IDEMPOTENCY DOMAIN
│   │   │   ├── model/
│   │   │   │   ├── IdempotencyKey.java         (Aggregate Root)
│   │   │   │   ├── IdempotencyStatus.java      (Enum)
│   │   │   │   └── IdempotencyRecord.java      (Value Object)
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── IdempotencyValidator.java
│   │   │   │   └── IdempotencyService.java
│   │   │   │
│   │   │   ├── port/
│   │   │   │   ├── in/
│   │   │   │   │   └── ValidateIdempotencyPort.java
│   │   │   │   │
│   │   │   │   └── out/
│   │   │   │       └── IdempotencyRepositoryPort.java
│   │   │   │
│   │   │   └── event/
│   │   │       └── IdempotencyKeyEvent.java
│   │   │
│   │   ├── reconciliation/              # RECONCILIATION DOMAIN
│   │   │   ├── model/
│   │   │   │   ├── ReconciliationBatch.java    (Aggregate Root)
│   │   │   │   ├── ReconciliationStatus.java   (Enum)
│   │   │   │   ├── ReconciliationRecord.java   (Value Object)
│   │   │   │   ├── Discrepancy.java            (Aggregate Root)
│   │   │   │   ├── DiscrepancyType.java        (Enum)
│   │   │   │   └── SettlementReport.java       (Aggregate Root)
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── ReconciliationEngine.java
│   │   │   │   ├── DiscrepancyDetector.java
│   │   │   │   ├── SettlementMatcher.java
│   │   │   │   └── ReportGenerator.java
│   │   │   │
│   │   │   ├── port/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ExecuteReconciliationPort.java
│   │   │   │   │   ├── GenerateReportPort.java
│   │   │   │   │   └── ResolveDiscrepancyPort.java
│   │   │   │   │
│   │   │   │   └── out/
│   │   │   │       ├── ReconciliationRepositoryPort.java
│   │   │   │       ├── DiscrepancyRepositoryPort.java
│   │   │   │       ├── SettlementGatewayPort.java
│   │   │   │       └── ReportStoragePort.java
│   │   │   │
│   │   │   └── event/
│   │   │       ├── ReconciliationStartedEvent.java
│   │   │       ├── ReconciliationCompletedEvent.java
│   │   │       ├── DiscrepancyDetectedEvent.java
│   │   │       └── SettlementReportGeneratedEvent.java
│   │   │
│   │   ├── customer/                    # CUSTOMER DOMAIN
│   │   │   ├── model/
│   │   │   │   ├── Customer.java             (Value Object)
│   │   │   │   └── Document.java             (Value Object)
│   │   │   │
│   │   │   ├── service/
│   │   │   │   └── CustomerValidationService.java
│   │   │   │
│   │   │   └── port/
│   │   │       └── out/
│   │   │           └── CustomerRepositoryPort.java
│   │   │
│   │   ├── notification/                # NOTIFICATION DOMAIN
│   │   │   ├── model/
│   │   │   │   ├── Notification.java         (Aggregate Root)
│   │   │   │   ├── NotificationType.java     (Enum)
│   │   │   │   └── NotificationChannel.java  (Enum)
│   │   │   │
│   │   │   ├── service/
│   │   │   │   └── NotificationService.java
│   │   │   │
│   │   │   ├── port/
│   │   │   │   ├── in/
│   │   │   │   │   └── SendNotificationPort.java
│   │   │   │   │
│   │   │   │   └── out/
│   │   │   │       ├── EmailSenderPort.java
│   │   │   │       └── SmsSenderPort.java
│   │   │   │
│   │   │   └── event/
│   │   │       └── NotificationSentEvent.java
│   │   │
│   │   └── common/                      # SHARED KERNEL
│   │       ├── model/
│   │       │   ├── Money.java
│   │       │   ├── AuditFields.java
│   │       │   └── BaseEntity.java
│   │       │
│   │       ├── service/
│   │       │   ├── ClockProvider.java
│   │       │   ├── IdGenerator.java
│   │       │   └── DomainEventPublisher.java
│   │       │
│   │       └── event/
│   │           ├── DomainEvent.java
│   │           └── IntegrationEvent.java
│   │
│   ├── application/                   # APPLICATION LAYER
│   │   │
│   │   ├── payment/                     # PAYMENT APPLICATION
│   │   │   ├── usecase/
│   │   │   │   ├── ProcessPaymentUseCase.java
│   │   │   │   ├── RefundPaymentUseCase.java
│   │   │   │   ├── CancelPaymentUseCase.java
│   │   │   │   └── GetPaymentUseCase.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── PaymentRequest.java
│   │   │   │   ├── PaymentResponse.java
│   │   │   │   ├── RefundRequest.java
│   │   │   │   └── RefundResponse.java
│   │   │   │
│   │   │   ├── mapper/
│   │   │   │   ├── PaymentRequestMapper.java
│   │   │   │   └── PaymentResponseMapper.java
│   │   │   │
│   │   │   └── handler/
│   │   │       └── PaymentDomainEventHandler.java
│   │   │
│   │   ├── merchant/                    # MERCHANT APPLICATION
│   │   │   ├── usecase/
│   │   │   │   ├── CreateMerchantUseCase.java
│   │   │   │   ├── UpdateMerchantUseCase.java
│   │   │   │   ├── DeleteMerchantUseCase.java
│   │   │   │   ├── GetMerchantUseCase.java
│   │   │   │   └── RegenerateApiKeyUseCase.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── MerchantRequest.java
│   │   │   │   ├── MerchantResponse.java
│   │   │   │   └── ApiKeyResponse.java
│   │   │   │
│   │   │   ├── mapper/
│   │   │   │   └── MerchantMapper.java
│   │   │   │
│   │   │   └── handler/
│   │   │       └── MerchantDomainEventHandler.java
│   │   │
│   │   ├── transaction/                 # TRANSACTION APPLICATION
│   │   │   ├── usecase/
│   │   │   │   ├── GetTransactionUseCase.java
│   │   │   │   └── ListTransactionsUseCase.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── TransactionResponse.java
│   │   │   │   └── TransactionFilter.java
│   │   │   │
│   │   │   ├── mapper/
│   │   │   │   └── TransactionMapper.java
│   │   │   │
│   │   │   └── handler/
│   │   │       └── TransactionDomainEventHandler.java
│   │   │
│   │   ├── idempotency/                 # IDEMPOTENCY APPLICATION
│   │   │   ├── usecase/
│   │   │   │   ├── CreateIdempotencyKeyUseCase.java
│   │   │   │   ├── ValidateIdempotencyKeyUseCase.java
│   │   │   │   └── ReleaseIdempotencyKeyUseCase.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   └── IdempotencyKeyDTO.java
│   │   │   │
│   │   │   └── handler/
│   │   │       └── IdempotencyEventHandler.java
│   │   │
│   │   ├── reconciliation/              # RECONCILIATION APPLICATION
│   │   │   ├── usecase/
│   │   │   │   ├── ExecuteReconciliationUseCase.java
│   │   │   │   ├── GenerateSettlementReportUseCase.java
│   │   │   │   ├── ResolveDiscrepancyUseCase.java
│   │   │   │   └── GetReconciliationHistoryUseCase.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── ReconciliationRequest.java
│   │   │   │   ├── ReconciliationResult.java
│   │   │   │   ├── DiscrepancyDTO.java
│   │   │   │   ├── SettlementReportDTO.java
│   │   │   │   └── DiscrepancyResolution.java
│   │   │   │
│   │   │   ├── mapper/
│   │   │   │   ├── ReconciliationMapper.java
│   │   │   │   └── DiscrepancyMapper.java
│   │   │   │
│   │   │   └── handler/
│   │   │       └── ReconciliationEventHandler.java
│   │   │
│   │   ├── notification/                # NOTIFICATION APPLICATION
│   │   │   ├── usecase/
│   │   │   │   └── SendNotificationUseCase.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   └── NotificationRequest.java
│   │   │   │
│   │   │   └── handler/
│   │   │       └── NotificationEventHandler.java
│   │   │
│   │   └── common/                      # SHARED APPLICATION
│   │       ├── handler/
│   │       │   └── GlobalDomainEventHandler.java
│   │       │
│   │       └── exception/
│   │           ├── BusinessException.java
│   │           ├── PaymentException.java
│   │           ├── MerchantException.java
│   │           ├── FraudDetectedException.java
│   │           ├── InsufficientFundsException.java
│   │           └── IdempotencyConflictException.java
│   │
│   └── infrastructure/                # INFRASTRUCTURE LAYER
│       │
│       ├── payment/                     # PAYMENT INFRASTRUCTURE
│       │   ├── adapter/
│       │   │   ├── in/
│       │   │   │   ├── rest/
│       │   │   │   │   ├── PaymentController.java
│       │   │   │   │   └── PaymentDtoMapper.java
│       │   │   │   │
│       │   │   │   └── kafka/
│       │   │   │       └── PaymentEventKafkaConsumer.java
│       │   │   │
│       │   │   └── out/
│       │   │       ├── persistence/
│       │   │       │   ├── entity/
│       │   │       │   │   └── PaymentEntity.java
│       │   │       │   ├── repository/
│       │   │       │   │   ├── SpringDataPaymentRepository.java
│       │   │       │   │   └── JpaPaymentRepositoryAdapter.java
│       │   │       │   └── converter/
│       │   │       │       └── PaymentEntityMapper.java
│       │   │       │
│       │   │       ├── kafka/
│       │   │       │   └── PaymentEventKafkaPublisher.java
│       │   │       │
│       │   │       ├── external/
│       │   │       │   ├── stripe/
│       │   │       │   │   ├── StripeClient.java
│       │   │       │   │   ├── StripePaymentGatewayAdapter.java
│       │   │       │   │   └── StripeResponseMapper.java
│       │   │       │   │
│       │   │       │   └── webhook/
│       │   │       │       └── PaymentWebhookAdapter.java
│       │   │       │
│       │   │       └── outbox/
│       │   │           ├── OutboxEvent.java
│       │   │           ├── OutboxRepository.java
│       │   │           ├── OutboxPublisher.java
│       │   │           └── OutboxProcessor.java
│       │   │
│       │   └── config/
│       │       ├── PaymentCircuitBreakerConfig.java
│       │       ├── PaymentRetryConfig.java
│       │       └── PaymentRateLimiterConfig.java
│       │
│       ├── merchant/                    # MERCHANT INFRASTRUCTURE
│       │   ├── adapter/
│       │   │   ├── in/
│       │   │   │   └── rest/
│       │   │   │       ├── MerchantController.java
│       │   │   │       └── MerchantDtoMapper.java
│       │   │   │
│       │   │   └── out/
│       │   │       └── persistence/
│       │   │           ├── entity/
│       │   │           │   └── MerchantEntity.java
│       │   │           ├── repository/
│       │   │           │   ├── SpringDataMerchantRepository.java
│       │   │           │   └── JpaMerchantRepositoryAdapter.java
│       │   │           └── converter/
│       │   │               └── MerchantEntityMapper.java
│       │   │
│       │   └── config/
│       │       └── MerchantConfig.java
│       │
│       ├── transaction/                 # TRANSACTION INFRASTRUCTURE
│       │   ├── adapter/
│       │   │   ├── in/
│       │   │   │   └── rest/
│       │   │   │       ├── TransactionController.java
│       │   │   │       └── TransactionDtoMapper.java
│       │   │   │
│       │   │   └── out/
│       │   │       └── persistence/
│       │   │           ├── entity/
│       │   │           │   └── TransactionEntity.java
│       │   │           ├── repository/
│       │   │           │   ├── SpringDataTransactionRepository.java
│       │   │           │   └── JpaTransactionRepositoryAdapter.java
│       │   │           └── converter/
│       │   │               └── TransactionEntityMapper.java
│       │   │
│       │   └── config/
│       │       └── TransactionConfig.java
│       │
│       ├── idempotency/                 # IDEMPOTENCY INFRASTRUCTURE
│       │   ├── adapter/
│       │   │   ├── in/
│       │   │   │   └── rest/
│       │   │   │       ├── IdempotencyFilter.java
│       │   │   │       └── IdempotencyKeyInterceptor.java
│       │   │   │
│       │   │   └── out/
│       │   │       ├── redis/
│       │   │       │   ├── RedisIdempotencyRepository.java
│       │   │       │   ├── RedisIdempotencyConfig.java
│       │   │       │   └── DistributedLockService.java
│       │   │       │
│       │   │       └── persistence/
│       │   │           ├── entity/
│       │   │           │   └── IdempotencyKeyEntity.java
│       │   │           ├── repository/
│       │   │           │   ├── SpringDataIdempotencyRepository.java
│       │   │           │   └── JpaIdempotencyRepositoryAdapter.java
│       │   │           └── converter/
│       │   │               └── IdempotencyEntityMapper.java
│       │   │
│       │   └── config/
│       │       └── IdempotencyConfig.java
│       │
│       ├── reconciliation/              # RECONCILIATION INFRASTRUCTURE
│       │   ├── adapter/
│       │   │   ├── in/
│       │   │   │   ├── rest/
│       │   │   │   │   ├── ReconciliationController.java
│       │   │   │   │   └── DiscrepancyController.java
│       │   │   │   │
│       │   │   │   └── scheduler/
│       │   │   │       └── DailyReconciliationScheduler.java
│       │   │   │
│       │   │   └── out/
│       │   │       ├── persistence/
│       │   │       │   ├── entity/
│       │   │       │   │   ├── ReconciliationBatchEntity.java
│       │   │       │   │   ├── DiscrepancyEntity.java
│       │   │       │   │   └── SettlementReportEntity.java
│       │   │       │   │
│       │   │       │   ├── repository/
│       │   │       │   │   ├── SpringDataReconciliationRepository.java
│       │   │       │   │   ├── SpringDataDiscrepancyRepository.java
│       │   │       │   │   └── adapter/
│       │   │       │   │       ├── JpaReconciliationRepositoryAdapter.java
│       │   │       │   │       └── JpaDiscrepancyRepositoryAdapter.java
│       │   │       │   │
│       │   │       │   └── converter/
│       │   │       │       └── ReconciliationEntityMapper.java
│       │   │       │
│       │   │       ├── external/
│       │   │       │   └── stripe/
│       │   │       │       ├── StripeSettlementClient.java
│       │   │       │       └── StripeSettlementAdapter.java
│       │   │       │
│       │   │       └── report/
│       │   │           ├── S3ReportStorage.java
│       │   │           ├── PdfReportGenerator.java
│       │   │           └── CsvReportGenerator.java
│       │   │
│       │   └── config/
│       │       ├── ReconciliationConfig.java
│       │       └── BatchProcessingConfig.java
│       │
│       ├── notification/                # NOTIFICATION INFRASTRUCTURE
│       │   ├── adapter/
│       │   │   └── out/
│       │   │       ├── email/
│       │   │       │   ├── EmailSenderAdapter.java
│       │   │       │   └── SmtpEmailService.java
│       │   │       │
│       │   │       └── sms/
│       │   │           ├── SmsSenderAdapter.java
│       │   │           └── TwilioSmsService.java
│       │   │
│       │   └── config/
│       │       ├── EmailConfig.java
│       │       └── SmsConfig.java
│       │
│       ├── kafka/                       # SHARED KAFKA
│       │   ├── KafkaConfig.java
│       │   ├── KafkaConsumerConfig.java
│       │   ├── KafkaProducerConfig.java
│       │   └── topics/
│       │       ├── PaymentTopics.java
│       │       ├── NotificationTopics.java
│       │       ├── ReconciliationTopics.java
│       │       └── TopicNames.java
│       │
│       ├── security/                    # SHARED SECURITY
│       │   ├── WebSecurityConfig.java
│       │   ├── JwtAuthenticationFilter.java
│       │   ├── JwtTokenProvider.java
│       │   ├── ApiKeyAuthenticationFilter.java
│       │   └── UserPrincipal.java
│       │
│       ├── config/                      # GLOBAL CONFIG
│       │   ├── OpenApiConfig.java
│       │   ├── WebMvcConfig.java
│       │   ├── FlywayConfig.java
│       │   └── ClockConfig.java
│       │
│       └── exception/                   # GLOBAL EXCEPTION HANDLING
│           ├── GlobalExceptionHandler.java
│           ├── ApiErrorResponse.java
│           └── ValidationErrorResponse.java
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
│       ├── V1__create_merchants_table.sql
│       ├── V2__create_payments_table.sql
│       ├── V3__create_transactions_table.sql
│       ├── V4__create_refunds_table.sql
│       ├── V5__create_outbox_table.sql
│       ├── V6__create_idempotency_keys_table.sql
│       ├── V7__create_reconciliation_tables.sql
│       └── V8__create_discrepancies_table.sql
│
└── src/test/
    ├── unit/
    │   ├── domain/
    │   │   ├── payment/
    │   │   │   ├── PaymentTest.java
    │   │   │   └── PaymentProcessorTest.java
    │   │   ├── merchant/
    │   │   │   └── MerchantTest.java
    │   │   ├── idempotency/
    │   │   │   └── IdempotencyKeyTest.java
    │   │   ├── reconciliation/
    │   │   │   ├── ReconciliationBatchTest.java
    │   │   │   └── DiscrepancyTest.java
    │   │   └── common/
    │   │       └── MoneyTest.java
    │   │
    │   └── application/
    │       ├── payment/
    │       │   └── ProcessPaymentUseCaseTest.java
    │       ├── merchant/
    │       │   └── CreateMerchantUseCaseTest.java
    │       ├── idempotency/
    │       │   └── ValidateIdempotencyKeyUseCaseTest.java
    │       ├── reconciliation/
    │       │   └── ExecuteReconciliationUseCaseTest.java
    │       └── transaction/
    │           └── GetTransactionUseCaseTest.java
    │
    ├── integration/
    │   ├── payment/
    │   │   ├── repository/
    │   │   │   └── PaymentRepositoryIntegrationTest.java
    │   │   └── adapter/
    │   │       └── rest/
    │   │           └── PaymentControllerIntegrationTest.java
    │   ├── merchant/
    │   │   └── adapter/
    │   │       └── rest/
    │   │           └── MerchantControllerIntegrationTest.java
    │   ├── idempotency/
    │   │   └── redis/
    │   │       └── RedisIdempotencyIntegrationTest.java
    │   ├── reconciliation/
    │   │   └── batch/
    │   │       └── ReconciliationBatchIntegrationTest.java
    │   └── kafka/
    │       └── KafkaIntegrationTest.java
    │
    └── e2e/
        ├── PaymentFlowE2ETest.java
        ├── RefundFlowE2ETest.java
        ├── IdempotencyFlowE2ETest.java
        ├── ReconciliationFlowE2ETest.java
        └── MerchantOnboardingE2ETest.java
```

---

## 🐳 Docker Compose Infrastructure

```yaml
services:
  # Database
  postgres:
    image: postgres:16
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: payment_gateway
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U admin"]
      interval: 10s
      timeout: 5s
      retries: 5
    volumes:
      - postgres_data:/var/lib/postgresql/data

  pgadmin:
    image: dpage/pgadmin4
    ports:
      - "8080:80"
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@admin.com
      PGADMIN_DEFAULT_PASSWORD: admin
    depends_on:
      - postgres
    volumes:
      - pgadmin_data:/var/lib/pgadmin

  # Message Broker
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    volumes:
      - kafka_data:/var/lib/kafka/data

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    ports:
      - "8081:8080"
    depends_on:
      - kafka
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181

  # Redis for Idempotency
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Monitoring
  prometheus:
    image: prom/prometheus:v2.48.0
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'

  grafana:
    image: grafana/grafana:10.1.0
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    depends_on:
      - prometheus
    volumes:
      - grafana_data:/var/lib/grafana

  # Zipkin for Tracing
  zipkin:
    image: openzipkin/zipkin:3.0
    ports:
      - "9411:9411"

  # MinIO for Report Storage (S3-compatible)
  minio:
    image: minio/minio:latest
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    command: server /data --console-address ":9001"
    volumes:
      - minio_data:/data

  # Application
  payment-gateway:
    build: .
    ports:
      - "8000:8080"
    depends_on:
      - postgres
      - kafka
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/payment_gateway
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
      SPRING_DATA_REDIS_HOST: redis
      SPRING_PROFILES_ACTIVE: dev

volumes:
  postgres_data:
  pgadmin_data:
  kafka_data:
  redis_data:
  grafana_data:
  minio_data:
```

---

## 📦 Dependencies (Maven - pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.payment.gateway</groupId>
    <artifactId>payment-gateway</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>payment-gateway</name>
    <description>Production Payment Gateway Service - DDD Hexagonal Architecture</description>
    
    <properties>
        <java.version>21</java.version>
        <resilience4j.version>2.2.0</resilience4j.version>
        <bucket4j.version>8.10.1</bucket4j.version>
        <testcontainers.version>1.19.3</testcontainers.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <lombok.version>1.18.30</lombok.version>
        <redisson.version>3.24.3</redisson.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        
        <!-- Redis for Idempotency -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
            <version>${redisson.version}</version>
        </dependency>
        
        <!-- Resilience4j -->
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-spring-boot3</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-circuitbreaker</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-retry</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-ratelimiter</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        
        <!-- Bucket4j for Rate Limiting -->
        <dependency>
            <groupId>com.bucket4j</groupId>
            <artifactId>bucket4j-core</artifactId>
            <version>${bucket4j.version}</version>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <dependency>
            <groupId>org.flyway</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.flyway</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>
        
        <!-- API Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>
        
        <!-- Object Mapping -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>
        
        <!-- Observability -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>
        
        <dependency>
            <groupId>io.zipkin.reporter2</groupId>
            <artifactId>zipkin-reporter-brave</artifactId>
        </dependency>
        
        <!-- AWS SDK for S3 (Report Storage) -->
        <dependency>
            <groupId>io.awspring.cloud</groupId>
            <artifactId>spring-cloud-aws-starter-s3</artifactId>
            <version>3.0.2</version>
        </dependency>
        
        <!-- PDF Generation -->
        <dependency>
            <groupId>com.itextpdf</groupId>
            <artifactId>itext7-core</artifactId>
            <version>8.0.2</version>
            <type>pom</type>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <optional>true</optional>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>kafka</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>redis</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.awaitility</groupId>
            <artifactId>awaitility</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.11</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 🔄 Phase Breakdown

### **Phase 1: Foundation & Infrastructure**
| Task | Description |
|------|-------------|
| 1.1 | Initialize Spring Boot 3 + Java 21 project |
| 1.2 | Configure `pom.xml` with all dependencies |
| 1.3 | Create `docker-compose.yml` (PostgreSQL, Kafka, pgAdmin, Kafka UI, Redis, Prometheus, Grafana, Zipkin, MinIO) |
| 1.4 | Create Flyway migrations (V1-V8) |
| 1.5 | Configure application.yml (dev, prod profiles) |
| 1.6 | Configure health checks |

---

### **Phase 2: Domain Layer (Core)**
| Task | Description |
|------|-------------|
| 2.1 | Create Common module (Money, BaseEntity, DomainEvent, IdGenerator) |
| 2.2 | Create Payment domain (Payment aggregate, PaymentProcessor, ports, events) |
| 2.3 | Create Merchant domain (Merchant aggregate, validation, ports, events) |
| 2.4 | Create Transaction domain (Transaction aggregate, query service, ports) |
| 2.5 | Create **Idempotency domain** (IdempotencyKey aggregate, IdempotencyService, ports, events) |
| 2.6 | Create **Reconciliation domain** (ReconciliationBatch, Discrepancy, SettlementReport, ReconciliationEngine) |
| 2.7 | Create Customer domain (Customer VO, Document VO, validation) |
| 2.8 | Create Notification domain (Notification aggregate, ports, events) |

---

### **Phase 3: Application Layer (Use Cases)**
| Task | Description |
|------|-------------|
| 3.1 | Implement Payment use cases (ProcessPayment, RefundPayment, CancelPayment, GetPayment) |
| 3.2 | Implement Merchant use cases (CreateMerchant, UpdateMerchant, DeleteMerchant, GetMerchant, RegenerateApiKey) |
| 3.3 | Implement Transaction use cases (GetTransaction, ListTransactions) |
| 3.4 | Implement **Idempotency use cases** (CreateIdempotencyKey, ValidateIdempotencyKey, ReleaseIdempotencyKey) |
| 3.5 | Implement **Reconciliation use cases** (ExecuteReconciliation, GenerateSettlementReport, ResolveDiscrepancy) |
| 3.6 | Implement Notification use cases (SendNotification) |
| 3.7 | Create DTOs and mappers (MapStruct) for each domain |
| 3.8 | Implement domain event handlers |
| 3.9 | Create business exceptions |

---

### **Phase 4: Infrastructure Adapters**
| Task | Description |
|------|-------------|
| 4.1 | Create Payment adapters (REST controller, JPA repository, Kafka publisher/consumer) |
| 4.2 | Create Merchant adapters (REST controller, JPA repository) |
| 4.3 | Create Transaction adapters (REST controller, JPA repository) |
| 4.4 | Create **Idempotency adapters** (Redis repository, distributed lock, JPA repository) |
| 4.5 | Create **Reconciliation adapters** (scheduler, JPA repositories, Stripe settlement client, report generators) |
| 4.6 | Create Notification adapters (Email, SMS) |
| 4.7 | **Implement Outbox Pattern** (OutboxEvent, OutboxPublisher, OutboxProcessor) |
| 4.8 | Create Stripe external adapter (StripeClient, StripePaymentGatewayAdapter) |
| 4.9 | Create Webhook adapter for PSP callbacks |

---

### **Phase 5: Security & Configuration**
| Task | Description |
|------|-------------|
| 5.1 | Implement Spring Security configuration |
| 5.2 | Implement JWT authentication (JwtTokenProvider, JwtAuthenticationFilter) |
| 5.3 | Implement API Key authentication for merchants |
| 5.4 | Configure Kafka (producer, consumer, topics) |
| 5.5 | Configure Redis (connection, serialization) |
| 5.6 | Configure OpenAPI/Swagger documentation |
| 5.7 | Configure global exception handling |

---

### **Phase 6: Resilience Patterns**
| Task | Description |
|------|-------------|
| 6.1 | Configure **Circuit Breaker** (Resilience4j) for Stripe integration |
| 6.2 | Configure **Retry Pattern** with exponential backoff |
| 6.3 | Configure **Rate Limiting** (Bucket4j) per merchant |
| 6.4 | Implement fallback mechanisms |
| 6.5 | Configure transaction boundaries & optimistic locking |

---

### **Phase 7: Observability**
| Task | Description |
|------|-------------|
| 7.1 | Configure structured JSON logging with correlation IDs |
| 7.2 | Configure Micrometer + Prometheus metrics |
| 7.3 | Implement custom business metrics |
| 7.4 | Configure OpenTelemetry + Zipkin tracing |
| 7.5 | Create Grafana dashboards |
| 7.6 | Implement health indicators (DB, Kafka, Redis, Stripe) |

---

### **Phase 8: Testing**
| Task | Description |
|------|-------------|
| 8.1 | Create Unit tests for Domain layer (Payment, Merchant, Idempotency, Reconciliation) |
| 8.2 | Create Unit tests for Application layer (Use Cases) |
| 8.3 | Create Integration tests with Testcontainers (repositories, controllers, Redis) |
| 8.4 | Create Kafka integration tests |
| 8.5 | Create E2E tests (Payment flow, Refund flow, Idempotency flow, Reconciliation flow) |
| 8.6 | Configure JaCoCo for code coverage |

---

### **Phase 9: CI/CD**
| Task | Description |
|------|-------------|
| 9.1 | Create GitHub Actions: Build pipeline |
| 9.2 | Create GitHub Actions: Test pipeline |
| 9.3 | Configure SonarQube integration |
| 9.4 | Create Docker image build & push workflow |
| 9.5 | Create Makefile for local development |

---

### **Phase 10: Documentation**
| Task | Description |
|------|-------------|
| 10.1 | Generate OpenAPI documentation |
| 10.2 | Create Architecture Decision Records (ADR) |
| 10.3 | Write README with setup instructions |
| 10.4 | Create operations runbook |

---

## 🎯 Key Features to Implement

| Feature | Description |
|---------|-------------|
| **Process Payment** | Accept payment requests, validate, process via Stripe |
| **Refund** | Full/partial refunds with validation |
| **Cancel Payment** | Cancel pending payments |
| **Transaction History** | Query transactions with filters |
| **Merchant Management** | CRUD merchants, API keys |
| **Webhooks** | Receive PSP callbacks |
| **Fraud Detection** | Basic rules engine |
| **Notifications** | Email/SMS on payment events |
| **Idempotency Handling** | ⭐ Prevent duplicate charges with idempotency keys |
| **Idempotency Handling** | ⭐ Redis/distributed cache for key tracking |
| **Idempotency Handling** | ⭐ Distributed locking for concurrent requests |
| **Reconciliation Engine** | ⭐ Batch processing for daily settlements |
| **Reconciliation Engine** | ⭐ Discrepancy detection (missing, amount mismatch, status mismatch) |
| **Reconciliation Engine** | ⭐ Automated report generation (PDF, CSV) |
| **Reconciliation Engine** | ⭐ Discrepancy resolution workflow |

---

## 📊 Kafka Topics

| Topic | Purpose |
|-------|---------|
| `payment.created` | Payment initiated |
| `payment.completed` | Payment successful |
| `payment.failed` | Payment failed |
| `refund.processed` | Refund completed |
| `notification.email` | Send email notifications |
| `notification.sms` | Send SMS notifications |
| `outbox.events` | Outbox pattern events |
| **`idempotency.key.created`** | **NEW** - Idempotency key registered |
| **`idempotency.key.conflict`** | **NEW** - Duplicate key detected |
| **`reconciliation.batch.started`** | **NEW** - Reconciliation batch started |
| **`reconciliation.batch.completed`** | **NEW** - Reconciliation completed |
| **`discrepancy.detected`** | **NEW** - Discrepancy found |
| **`discrepancy.resolved`** | **NEW** - Discrepancy resolved |
| **`settlement.report.generated`** | **NEW** - Settlement report ready |

---

## 🛡️ Resilience Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      stripePaymentGateway:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 5
        automaticTransitionFromOpenToHalfOpenEnabled: true
        registerHealthIndicator: true
        eventConsumerBufferSize: 10
        
      stripeSettlementApi:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
        registerHealthIndicator: true
        
  retry:
    instances:
      stripePaymentGateway:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
          - org.springframework.web.reactive.function.client.WebClientException
          
      reconciliationEngine:
        maxAttempts: 5
        waitDuration: 5s
        exponentialBackoffMultiplier: 2
        
  ratelimiter:
    instances:
      perMerchant:
        limitForPeriod: 100
        limitRefreshPeriod: 1m
        timeoutDuration: 0s
        registerHealthIndicator: true
        
      reconciliationBatch:
        limitForPeriod: 10
        limitRefreshPeriod: 1h
        timeoutDuration: 0s
```

---

## 📈 Metrics to Track

| Metric | Type | Description |
|--------|------|-------------|
| `payment.processed.total` | Counter | Total payments processed |
| `payment.processed.success` | Counter | Successful payments |
| `payment.processed.failed` | Counter | Failed payments |
| `payment.refunded.total` | Counter | Total refunds |
| `payment.processing.duration` | Histogram | Processing time distribution |
| `payment.amount` | Distribution Summary | Payment amounts |
| **`idempotency.keys.total`** | Counter | **Total idempotency keys created** |
| **`idempotency.keys.hit`** | Counter | **Cache hits (duplicate requests)** |
| **`idempotency.keys.miss`** | Counter | **Cache misses (new requests)** |
| **`idempotency.conflicts`** | Counter | **Duplicate payment attempts blocked** |
| **`reconciliation.batches.total`** | Counter | **Total reconciliation batches** |
| **`reconciliation.batches.duration`** | Histogram | **Batch processing time** |
| **`reconciliation.transactions.matched`** | Counter | **Successfully matched transactions** |
| **`discrepancies.detected.total`** | Counter | **Total discrepancies found** |
| **`discrepancies.by.type`** | Counter | **Discrepancies by type** |
| **`discrepancies.resolved.total`** | Counter | **Resolved discrepancies** |
| **`discrepancies.open`** | Gauge | **Currently open discrepancies** |
| **`settlement.amount.expected`** | Gauge | **Expected settlement amount** |
| **`settlement.amount.actual`** | Gauge | **Actual settlement amount** |
| `circuitbreaker.state` | Gauge | Circuit breaker state |
| `circuitbreaker.calls` | Counter | Circuit breaker calls |
| `ratelimiter.requests` | Counter | Rate limited requests |
| `kafka.messages.published` | Counter | Kafka messages published |
| `kafka.messages.consumed` | Counter | Kafka messages consumed |
| `db.connections.active` | Gauge | Active database connections |
| `http.requests.total` | Counter | HTTP requests by endpoint |
| `http.requests.duration` | Histogram | HTTP request duration |

---

## 🚀 Local Development Setup

```bash
# Start all infrastructure services
docker-compose up -d

# Wait for services to be ready
sleep 30

# Run application
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/payment-gateway-0.0.1-SNAPSHOT.jar

# Access services
# ┌─────────────────────────────────────────────┐
# │ Service     │ URL                           │
# ├─────────────────────────────────────────────┤
# │ API         │ http://localhost:8000         │
# │ Swagger UI  │ http://localhost:8000/swagger │
# │ pgAdmin     │ http://localhost:8080         │
# │ Kafka UI    │ http://localhost:8081         │
# │ Redis       │ localhost:6379                │
# │ Grafana     │ http://localhost:3000         │
# │ Prometheus  │ http://localhost:9090         │
# │ Zipkin      │ http://localhost:9411         │
# │ MinIO       │ http://localhost:9000         │
# └─────────────────────────────────────────────┘

# Default credentials
# pgAdmin: admin@admin.com / admin
# Grafana: admin / admin
# MinIO: minioadmin / minioadmin
```

---

## 📚 Learning Outcomes

After completing this project, you'll have experience with:

| Area | Skills |
|------|--------|
| **Architecture** | DDD, Hexagonal, Clean Architecture, Vertical Slice |
| **Design Patterns** | Repository, Factory, Strategy, Observer, Adapter |
| **Enterprise Patterns** | Outbox, Circuit Breaker, Retry, Rate Limiter, Saga |
| **Payment Systems** | Idempotency, Reconciliation, Settlement, Discrepancy handling |
| **Spring Ecosystem** | Boot 3, Data JPA, Security, Kafka, Actuator, Validation, Data Redis |
| **Resilience** | Fault tolerance, circuit breakers, retry, rate limiting |
| **Messaging** | Kafka producers/consumers, event-driven architecture |
| **Database** | JPA, PostgreSQL, Flyway migrations, optimistic locking |
| **Caching** | Redis, distributed locking, TTL strategies |
| **Testing** | Unit, Integration, Contract, E2E, Testcontainers |
| **DevOps** | Docker, Docker Compose, CI/CD, GitHub Actions |
| **Observability** | Logging, Metrics (Prometheus), Tracing (Zipkin), Grafana |
| **Security** | JWT, API Keys, Spring Security |
| **API Design** | REST, OpenAPI/Swagger, Versioning, Error handling |
| **AI Collaboration** | Prompt engineering, code review with AI, pair programming |

---

## ⏱️ Estimated Effort

| Phase | Files | Time Estimate |
|-------|-------|---------------|
| Phase 1: Foundation | ~10 | 1-2 days |
| Phase 2: Domain Layer | ~35 | 4-5 days |
| Phase 3: Application Layer | ~30 | 3-4 days |
| Phase 4: Infrastructure | ~50 | 5-7 days |
| Phase 5: Security & Config | ~12 | 1-2 days |
| Phase 6: Resilience | ~10 | 1-2 days |
| Phase 7: Observability | ~12 | 1-2 days |
| Phase 8: Testing | ~30 | 3-4 days |
| Phase 9: CI/CD | ~5 | 1 day |
| Phase 10: Documentation | ~5 | 1 day |
| **Total** | **~199 files** | **~22-30 days** |

*Note: Time estimates assume 4-6 hours of focused work per day. Adjust based on your pace.*

---

## 📋 API Endpoints Overview

### Payment Domain (`/api/v1/payments`)
```
POST   /api/v1/payments                    - ProcessPaymentUseCase
         (Header: Idempotency-Key)
GET    /api/v1/payments/{id}               - GetPaymentUseCase
GET    /api/v1/payments                    - ListPaymentsUseCase
POST   /api/v1/payments/{id}/cancel        - CancelPaymentUseCase
POST   /api/v1/payments/{id}/refund        - RefundPaymentUseCase
         (Header: Idempotency-Key)
```

### Merchant Domain (`/api/v1/merchants`)
```
POST   /api/v1/merchants                   - CreateMerchantUseCase
GET    /api/v1/merchants/{id}              - GetMerchantUseCase
PUT    /api/v1/merchants/{id}              - UpdateMerchantUseCase
DELETE /api/v1/merchants/{id}              - DeleteMerchantUseCase
POST   /api/v1/merchants/{id}/api-key      - RegenerateApiKeyUseCase
```

### Transaction Domain (`/api/v1/transactions`)
```
GET    /api/v1/transactions                - ListTransactionsUseCase
GET    /api/v1/transactions/{id}           - GetTransactionUseCase
GET    /api/v1/transactions/payment/{paymentId}
```

### Idempotency Domain (`/api/v1/idempotency`)
```
GET    /api/v1/idempotency/{key}           - Check idempotency key status
DELETE /api/v1/idempotency/{key}           - Release idempotency key
GET    /api/v1/idempotency/{key}/response  - Get cached response
```

### Reconciliation Domain (`/api/v1/reconciliation`)
```
POST   /api/v1/reconciliation/execute      - ExecuteReconciliationUseCase
GET    /api/v1/reconciliation/batches      - GetReconciliationHistoryUseCase
GET    /api/v1/reconciliation/batches/{id} - Get batch details
GET    /api/v1/reconciliation/reports/{id} - Download settlement report
```

### Discrepancy Domain (`/api/v1/discrepancies`)
```
GET    /api/v1/discrepancies               - List discrepancies (with filters)
GET    /api/v1/discrepancies/{id}          - Get discrepancy details
POST   /api/v1/discrepancies/{id}/resolve  - ResolveDiscrepancyUseCase
PUT    /api/v1/discrepancies/{id}          - Update discrepancy
```

### Health & Monitoring
```
GET    /actuator/health                    - Health check
GET    /actuator/metrics                   - Metrics
GET    /actuator/prometheus                - Prometheus metrics
GET    /actuator/circuitbreakers           - Circuit breaker status
```

---

## 📝 Sample Request/Response

### Process Payment Request (with Idempotency)
```json
POST /api/v1/payments
Idempotency-Key: idem_abc123xyz789
{
  "merchantId": "merch_123",
  "amount": 1500.00,
  "currency": "BRL",
  "paymentMethod": {
    "type": "CREDIT_CARD",
    "cardNumber": "4111111111111111",
    "cardholderName": "John Doe",
    "expiryMonth": 12,
    "expiryYear": 2027,
    "cvv": "123"
  },
  "customer": {
    "name": "John Doe",
    "email": "john@example.com",
    "document": "123.456.789-00"
  },
  "description": "Order #12345",
  "metadata": {
    "orderId": "12345",
    "ipAddress": "192.168.1.1"
  }
}
```

### Process Payment Response
```json
{
  "id": "pay_abc123",
  "status": "COMPLETED",
  "amount": 1500.00,
  "currency": "BRL",
  "transactionId": "txn_xyz789",
  "merchantId": "merch_123",
  "createdAt": "2024-01-15T10:30:00Z",
  "completedAt": "2024-01-15T10:30:02Z"
}
```

### Idempotency Key Response (Duplicate Request)
```json
GET /api/v1/idempotency/idem_abc123xyz789
{
  "key": "idem_abc123xyz789",
  "status": "COMPLETED",
  "responseBody": {
    "id": "pay_abc123",
    "status": "COMPLETED",
    ...
  },
  "httpStatus": 201,
  "createdAt": "2024-01-15T10:30:00Z",
  "expiresAt": "2024-01-16T10:30:00Z"
}
```

---

## 🗂️ Database Schema (Initial)

```sql
-- V1: Merchants
CREATE TABLE merchants (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    api_key_hash VARCHAR(255) NOT NULL,
    api_key_prefix VARCHAR(8) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V2: Payments
CREATE TABLE payments (
    id VARCHAR(36) PRIMARY KEY,
    merchant_id VARCHAR(36) NOT NULL REFERENCES merchants(id),
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method_type VARCHAR(20) NOT NULL,
    payment_method_data JSONB,
    customer_data JSONB,
    description TEXT,
    metadata JSONB,
    external_transaction_id VARCHAR(255),
    failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- V3: Transactions
CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY,
    payment_id VARCHAR(36) NOT NULL REFERENCES payments(id),
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    status VARCHAR(20) NOT NULL,
    external_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V4: Refunds
CREATE TABLE refunds (
    id VARCHAR(36) PRIMARY KEY,
    payment_id VARCHAR(36) NOT NULL REFERENCES payments(id),
    amount DECIMAL(19,4) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(255),
    external_refund_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V5: Outbox Events
CREATE TABLE outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(36) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    retry_count INT DEFAULT 0
);

-- V6: Idempotency Keys
CREATE TABLE idempotency_keys (
    id VARCHAR(36) PRIMARY KEY,
    key_hash VARCHAR(64) UNIQUE NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(36),
    status VARCHAR(20) NOT NULL,
    response_body JSONB,
    http_status INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    locked_at TIMESTAMP,
    locked_by VARCHAR(36)
);

-- V7: Reconciliation Batches & Settlement Reports
CREATE TABLE reconciliation_batches (
    id VARCHAR(36) PRIMARY KEY,
    batch_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_transactions INT,
    matched_count INT,
    discrepancy_count INT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE settlement_reports (
    id VARCHAR(36) PRIMARY KEY,
    reconciliation_batch_id VARCHAR(36) REFERENCES reconciliation_batches(id),
    report_date DATE NOT NULL,
    gross_amount DECIMAL(19,4),
    total_fees DECIMAL(19,4),
    net_amount DECIMAL(19,4),
    transaction_count INT,
    refund_count INT,
    chargeback_count INT,
    file_path VARCHAR(500),
    format VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V8: Discrepancies
CREATE TABLE discrepancies (
    id VARCHAR(36) PRIMARY KEY,
    reconciliation_batch_id VARCHAR(36) REFERENCES reconciliation_batches(id),
    payment_id VARCHAR(36) REFERENCES payments(id),
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    expected_value JSONB,
    actual_value JSONB,
    difference DECIMAL(19,4),
    status VARCHAR(20) DEFAULT 'OPEN',
    resolution_notes TEXT,
    resolved_by VARCHAR(36),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_payments_merchant ON payments(merchant_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created ON payments(created_at);
CREATE INDEX idx_transactions_payment ON transactions(payment_id);
CREATE INDEX idx_outbox_status ON outbox_events(status);
CREATE INDEX idx_outbox_retry ON outbox_events(retry_count, created_at);
CREATE INDEX idx_idempotency_key_hash ON idempotency_keys(key_hash);
CREATE INDEX idx_idempotency_expires ON idempotency_keys(expires_at);
CREATE INDEX idx_reconciliation_batch_date ON reconciliation_batches(batch_date);
CREATE INDEX idx_discrepancy_payment ON discrepancies(payment_id);
CREATE INDEX idx_discrepancy_status ON discrepancies(status);
CREATE INDEX idx_discrepancy_type ON discrepancies(type);
```

---

## 🔐 Security Considerations

| Aspect | Implementation |
|--------|----------------|
| **Authentication** | JWT for admin users, API Key for merchants |
| **Authorization** | Role-based (ADMIN, MERCHANT) |
| **PCI-DSS** | Never store full card numbers, use tokenization via Stripe |
| **Data Encryption** | TLS in transit, encryption at rest |
| **Sensitive Data** | Card data tokenized, CVV never stored |
| **API Key Storage** | Hashed with BCrypt, only prefix stored in plain text |
| **Idempotency Keys** | Hashed with SHA256, distributed locking |
| **Audit Logging** | Log all payment operations |
| **Rate Limiting** | Per-merchant limits to prevent abuse |
| **Input Validation** | Validate all inputs, sanitize outputs |

---

## 🧪 Testing Strategy

### Unit Tests (70% coverage target)
```
- Domain entities (Payment, Merchant, IdempotencyKey, ReconciliationBatch, etc.)
- Domain services (PaymentProcessor, FraudDetection, IdempotencyService, ReconciliationEngine)
- Use cases (ProcessPaymentUseCase, ValidateIdempotencyKeyUseCase, ExecuteReconciliationUseCase)
- Mappers and converters
```

### Integration Tests
```
- Repository layer with Testcontainers PostgreSQL
- Kafka producers/consumers with Testcontainers Kafka
- Redis idempotency with Testcontainers Redis
- Controller layer with @WebMvcTest
- External API integrations with WireMock
```

### E2E Tests
```
- Full payment flow with Docker Compose
- Refund flow
- Idempotency flow (duplicate request handling)
- Reconciliation flow (batch processing, discrepancy detection)
- Merchant onboarding flow
```

---

## 📖 Recommended Reading

| Topic | Resource |
|-------|----------|
| DDD | "Domain-Driven Design" by Eric Evans |
| Hexagonal Architecture | "Clean Architecture" by Robert C. Martin |
| Resilience Patterns | "Building Microservices" by Sam Newman |
| Payment Systems | "Designing Data-Intensive Applications" by Martin Kleppmann |
| Spring Boot | Official Spring Boot Documentation |
| Kafka | "Kafka: The Definitive Guide" by Neha Narkhede |
| Redis | "Redis in Action" by Josiah L. Carlson |

---

## ✅ Success Criteria

| Criteria | Definition |
|----------|------------|
| **Functional** | All payment flows work correctly |
| **Idempotency** | Duplicate requests return cached response without reprocessing |
| **Reconciliation** | Daily automated reconciliation with discrepancy detection |
| **Resilient** | System handles failures gracefully |
| **Observable** | Full visibility into system behavior |
| **Tested** | >70% code coverage, all critical paths tested |
| **Documented** | API docs, architecture docs, runbook |
| **Deployable** | One-command deployment with Docker |

---

## 🚦 Next Steps

1. **Review this plan** - Make sure you understand all components
2. **Start Phase 1** - Begin with Docker Compose and project setup
3. **Iterate together** - Build feature by feature, domain by domain
4. **Learn continuously** - Ask questions, experiment, refactor

---

## 📞 Support & Resources

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Resilience4j**: https://resilience4j.readme.io
- **Testcontainers**: https://www.testcontainers.org
- **Kafka Docs**: https://kafka.apache.org/documentation
- **MapStruct**: https://mapstruct.org/documentation
- **Redisson**: https://redisson.org/

---

*Generated for AI-Assisted Learning Project*
*Last Updated: 2026-03-01*
*Architecture: DDD Modular Hexagonal with Idempotency & Reconciliation*
