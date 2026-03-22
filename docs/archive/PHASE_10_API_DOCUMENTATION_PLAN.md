# Phase 10: API Documentation - Implementation Plan

**Created:** 2026-03-20  
**Status:** Pending Review  
**Approach:** Interface-Based Documentation (Approach B)

---

## Table of Contents

1. [Decisions Summary](#decisions-summary)
2. [Architecture Overview](#architecture-overview)
3. [File Inventory](#file-inventory)
4. [Detailed Implementation Tasks](#detailed-implementation-tasks)
5. [DTO Schema Annotations](#dto-schema-annotations)
6. [Execution Order](#execution-order)
7. [Verification Checklist](#verification-checklist)
8. [Open Questions](#open-questions)

---

## Decisions Summary

| Decision | Choice |
|----------|--------|
| **Approach** | B - Interface-Based Documentation |
| **DTOs** | `@Schema` annotations on all DTOs |
| **Security** | Document API Key + JWT, allow empty for local |
| **Groups** | 6 separate domain groups |
| **Examples** | Full JSON examples for request/response |

### Why Approach B (Interface-Based)

- Controllers remain clean (no Swagger annotations)
- Declarative style with annotations (readable)
- Compile-time safety (interface contract)
- Good IDE support (autocomplete, jump to interface)
- Less verbose than programmatic customization

---

## Architecture Overview

### Package Structure

```
infrastructure/
├── config/
│   └── SwaggerConfig.java                    # Global config, security schemes, servers
│
├── docs/                                      # NEW PACKAGE - API Documentation
│   ├── PaymentApi.java                        # Payment endpoints interface
│   ├── MerchantApi.java                       # Merchant endpoints interface
│   ├── CustomerApi.java                       # Customer endpoints interface
│   ├── RefundApi.java                         # Refund endpoints interface
│   ├── TransactionApi.java                    # Transaction endpoints interface
│   └── ReconciliationApi.java                 # Reconciliation endpoints interface
│
├── payment/adapter/in/rest/
│   └── PaymentController.java                 # Implements PaymentApi (clean)
│
├── merchant/adapter/in/rest/
│   └── MerchantController.java                # Implements MerchantApi (clean)
│
├── customer/adapter/in/rest/
│   └── CustomerController.java                # Implements CustomerApi (clean)
│
├── refund/adapter/in/rest/
│   └── RefundController.java                  # Implements RefundApi (clean)
│
├── transaction/adapter/in/rest/
│   └── TransactionController.java             # Implements TransactionApi (clean)
│
└── reconciliation/adapter/in/rest/
    └── ReconciliationController.java          # Implements ReconciliationApi (clean)
```

### How It Works

```
┌─────────────────────┐         ┌─────────────────────┐
│   PaymentApi.java   │         │ PaymentController   │
│  (Interface with    │◄────────│   implements        │
│   @Tag, @Operation) │         │   PaymentApi       │
└─────────────────────┘         └─────────────────────┘
           │                              │
           │ Swagger annotations          │ Clean implementation
           │ define documentation         │ with @Override
           ▼                              ▼
┌─────────────────────────────────────────────────────┐
│              SpringDoc Runtime                       │
│  Scans interfaces and merges documentation into     │
│  OpenAPI specification                              │
└─────────────────────────────────────────────────────┘
```

---

## File Inventory

### New Files to Create (8 files)

| File | Purpose | Est. Lines |
|------|---------|------------|
| `infrastructure/config/SwaggerConfig.java` | Enhanced with groups, security, servers | 120 |
| `infrastructure/docs/PaymentApi.java` | Payment endpoints documentation interface | 200 |
| `infrastructure/docs/MerchantApi.java` | Merchant endpoints documentation interface | 150 |
| `infrastructure/docs/CustomerApi.java` | Customer endpoints documentation interface | 180 |
| `infrastructure/docs/RefundApi.java` | Refund endpoints documentation interface | 120 |
| `infrastructure/docs/TransactionApi.java` | Transaction endpoints documentation interface | 100 |
| `infrastructure/docs/ReconciliationApi.java` | Reconciliation endpoints documentation interface | 90 |
| **Total New** | | **960 lines** |

### Files to Modify (18 files)

| File | Changes | Est. Lines |
|------|---------|------------|
| `PaymentController.java` | Add `implements PaymentApi`, `@Override` annotations | 10 |
| `MerchantController.java` | Add `implements MerchantApi`, `@Override` annotations | 10 |
| `CustomerController.java` | Add `implements CustomerApi`, `@Override` annotations | 10 |
| `RefundController.java` | Add `implements RefundApi`, `@Override` annotations | 10 |
| `TransactionController.java` | Add `implements TransactionApi`, `@Override` annotations | 10 |
| `ReconciliationController.java` | Add `implements ReconciliationApi`, `@Override` | 10 |
| `CreatePaymentRequest.java` | Add `@Schema` annotations | 40 |
| `PaymentResponse.java` | Add `@Schema` annotations | 50 |
| `MerchantResponse.java` | Add `@Schema` annotations | 30 |
| `CustomerResponse.java` | Add `@Schema` annotations | 35 |
| `TransactionResponse.java` | Add `@Schema` annotations | 35 |
| `RefundResponse.java` | Add `@Schema` annotations | 35 |
| `ReconciliationResponse.java` | Add `@Schema` annotations | 25 |
| `SettlementReportDTO.java` | Add `@Schema` annotations | 30 |
| `ApiResponse.java` | Add `@Schema` annotations | 15 |
| `PagedResponse.java` | Add `@Schema` annotations | 15 |
| `PageInfo.java` | Add `@Schema` annotations | 15 |
| `application.yml` | Add springdoc configuration | 20 |
| **Total Modified** | | **495 lines** |

### Total Estimated Changes: ~1,455 lines

---

## Detailed Implementation Tasks

### 10.1 SwaggerConfig Enhancement

**File:** `src/main/java/com/payment/gateway/infrastructure/config/SwaggerConfig.java`

**Current State:**
- Basic OpenAPI bean with title, description, version, contact
- Located at: `infrastructure/config/SwaggerConfig.java`

**Required Changes:**

```java
package com.payment.gateway.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI paymentGatewayOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .components(securityComponents())
                .tags(tags());
    }

    private Info apiInfo() {
        return new Info()
                .title("Payment Gateway API")
                .description("""
                    Production-ready Payment Gateway Service REST API.
                    
                    ## Features
                    - Payment authorization, capture, and cancellation
                    - Customer and payment method management
                    - Refund processing (full and partial)
                    - Transaction reconciliation and settlement reports
                    
                    ## Authentication
                    - **Local/Dev**: No authentication required
                    - **Production**: API Key (X-API-Key header) or JWT Bearer token
                    
                    ## Idempotency
                    Payment and refund endpoints support idempotency via `X-Idempotency-Key` header.
                    """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Payment Gateway Team")
                        .email("team@paymentgateway.com")
                        .url("https://paymentgateway.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    private List<Server> servers() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server"),
                new Server()
                        .url("https://dev.payment-gateway.com")
                        .description("Development Server"),
                new Server()
                        .url("https://api.payment-gateway.com")
                        .description("Production Server")
        );
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("api-key", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                        .description("Production API key for merchant authentication"))
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token for authenticated users"));
    }

    private List<Tag> tags() {
        return Arrays.asList(
                new Tag().name("Health & Monitoring").description("Health checks and system monitoring endpoints"),
                new Tag().name("Merchant Management").description("Merchant registration, updates, and suspension"),
                new Tag().name("Customer Management").description("Customer registration and payment method management"),
                new Tag().name("Payment Processing").description("Payment authorization, capture, and cancellation"),
                new Tag().name("Transaction Management").description("Transaction operations and voiding"),
                new Tag().name("Refund Processing").description("Full and partial refund processing"),
                new Tag().name("Reconciliation").description("Transaction reconciliation and settlement reports")
        );
    }
}
```

**Tasks:**
- [ ] Add `apiInfo()` method with extended description, license, contact
- [ ] Add `servers()` method with Local, Dev, Production servers
- [ ] Add `securityComponents()` method with API Key and JWT schemes
- [ ] Add `tags()` method with all 7 domain tags
- [ ] Add `@Value` for application name injection

---

### 10.2 PaymentApi Interface

**File:** `src/main/java/com/payment/gateway/infrastructure/docs/PaymentApi.java`

**Endpoints to Document:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments` | Process a new payment |
| GET | `/api/v1/payments/{id}` | Get payment by ID |
| GET | `/api/v1/payments` | Get all payments for merchant |
| POST | `/api/v1/payments/{id}/capture` | Capture an authorized payment |
| POST | `/api/v1/payments/{id}/cancel` | Cancel an authorized payment |

**Full Implementation:**

```java
package com.payment.gateway.infrastructure.docs;

import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import com.payment.gateway.infrastructure.payment.adapter.in.rest.CreatePaymentRequest;
import com.payment.gateway.infrastructure.payment.adapter.in.rest.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(
    name = "Payment Processing",
    description = """
        Payment authorization, capture, and cancellation operations.
        
        ## Payment Lifecycle
        1. **Process Payment** - Creates a payment in AUTHORIZED status
        2. **Capture Payment** - Captures an authorized payment
        3. **Cancel Payment** - Cancels an authorized payment
        
        ## Idempotency
        All payment creation requests support idempotency via the `X-Idempotency-Key` header.
        Use the same key to safely retry requests without creating duplicate charges.
        """
)
public interface PaymentApi {

    @Operation(
        operationId = "processPayment",
        summary = "Process a new payment",
        description = """
            Authorizes a payment for the specified amount. The payment will be in 
            AUTHORIZED status and can be captured later using the capture endpoint.
            
            **Idempotency:** Include `X-Idempotency-Key` header to prevent duplicate charges.
            Using the same key will return the original payment response without creating a new charge.
            
            **Supported Currencies:** USD, EUR, GBP
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment authorized successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Successful Payment",
                    summary = "Payment authorized successfully",
                    value = """
                        {
                          "success": true,
                          "message": "Payment processed successfully",
                          "data": {
                            "id": "pay_abc123def456",
                            "merchantId": "merch_xyz789",
                            "customerId": "cust_123abc",
                            "amountInCents": 10000,
                            "currency": "USD",
                            "status": "AUTHORIZED",
                            "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
                            "description": "Order #12345",
                            "items": [
                              {
                                "description": "Premium Widget",
                                "quantity": 2,
                                "unitPriceInCents": 5000,
                                "totalInCents": 10000
                              }
                            ],
                            "createdAt": "2026-03-20T10:30:00Z",
                            "updatedAt": "2026-03-20T10:30:00Z",
                            "authorizedAt": "2026-03-20T10:30:00Z"
                          },
                          "timestamp": "2026-03-20T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                        {
                          "success": false,
                          "message": "Validation failed",
                          "data": null,
                          "timestamp": "2026-03-20T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Merchant or customer not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Not Found Error",
                    value = """
                        {
                          "success": false,
                          "message": "Merchant not found: merch_invalid",
                          "data": null,
                          "timestamp": "2026-03-20T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Duplicate idempotency key - returning original payment",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Idempotency Hit",
                    value = """
                        {
                          "success": true,
                          "message": "Payment already processed",
                          "data": {
                            "id": "pay_abc123def456",
                            "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
                            "status": "AUTHORIZED"
                          },
                          "timestamp": "2026-03-20T10:30:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    @SecurityRequirement(name = "bearer-jwt")
    ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Parameter(
                name = "X-Idempotency-Key",
                description = "Unique key for idempotent requests (UUID recommended). Stored for 24 hours.",
                required = true,
                in = ParameterIn.HEADER,
                example = "550e8400-e29b-41d4-a716-446655440000"
            )
            String idempotencyKey,
            
            @RequestBody(
                description = "Payment request details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreatePaymentRequest.class),
                    examples = @ExampleObject(
                        name = "Payment Request",
                        summary = "Standard payment request",
                        value = """
                            {
                              "merchantId": "merch_xyz789",
                              "amountInCents": 10000,
                              "currency": "USD",
                              "customerId": "cust_123abc",
                              "description": "Order #12345",
                              "items": [
                                {
                                  "description": "Premium Widget",
                                  "quantity": 2,
                                  "unitPriceInCents": 5000
                                }
                              ]
                            }
                            """
                    )
                )
            )
            @Valid CreatePaymentRequest request
    );

    @Operation(
        operationId = "getPayment",
        summary = "Get payment by ID",
        description = "Retrieves detailed information about a specific payment."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Payment Details",
                    value = """
                        {
                          "success": true,
                          "message": "Success",
                          "data": {
                            "id": "pay_abc123def456",
                            "merchantId": "merch_xyz789",
                            "customerId": "cust_123abc",
                            "amountInCents": 10000,
                            "currency": "USD",
                            "status": "AUTHORIZED",
                            "createdAt": "2026-03-20T10:30:00Z"
                          },
                          "timestamp": "2026-03-20T11:00:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "message": "Payment not found: pay_invalid",
                          "data": null,
                          "timestamp": "2026-03-20T11:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @Parameter(
                name = "id",
                description = "Unique payment identifier",
                required = true,
                example = "pay_abc123def456"
            )
            String id,
            
            @Parameter(
                name = "merchantId",
                description = "Merchant ID for authorization",
                required = true,
                example = "merch_xyz789"
            )
            String merchantId
    );

    @Operation(
        operationId = "getPayments",
        summary = "Get all payments for a merchant",
        description = "Retrieves all payments for a specific merchant, ordered by creation date descending."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payments retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "Payment List",
                    value = """
                        {
                          "success": true,
                          "message": "Success",
                          "data": [
                            {
                              "id": "pay_abc123def456",
                              "merchantId": "merch_xyz789",
                              "amountInCents": 10000,
                              "currency": "USD",
                              "status": "AUTHORIZED"
                            },
                            {
                              "id": "pay_def456ghi789",
                              "merchantId": "merch_xyz789",
                              "amountInCents": 5000,
                              "currency": "USD",
                              "status": "CAPTURED"
                            }
                          ],
                          "timestamp": "2026-03-20T11:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<ApiResponse<List<PaymentResponse>>> getPayments(
            @Parameter(
                name = "merchantId",
                description = "Merchant ID to filter payments",
                required = true,
                example = "merch_xyz789"
            )
            String merchantId
    );

    @Operation(
        operationId = "capturePayment",
        summary = "Capture an authorized payment",
        description = """
            Captures a previously authorized payment. The payment must be in AUTHORIZED status.
            After capture, the payment status changes to CAPTURED.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment captured successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Captured Payment",
                    value = """
                        {
                          "success": true,
                          "message": "Payment captured successfully",
                          "data": {
                            "id": "pay_abc123def456",
                            "status": "CAPTURED",
                            "capturedAt": "2026-03-20T12:00:00Z"
                          },
                          "timestamp": "2026-03-20T12:00:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Payment cannot be captured (wrong status)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "message": "Payment cannot be captured: current status is CAPTURED",
                          "data": null,
                          "timestamp": "2026-03-20T12:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<ApiResponse<PaymentResponse>> capturePayment(
            @Parameter(
                name = "id",
                description = "Payment ID to capture",
                required = true,
                example = "pay_abc123def456"
            )
            String id,
            
            @Parameter(
                name = "merchantId",
                description = "Merchant ID for authorization",
                required = true,
                example = "merch_xyz789"
            )
            String merchantId
    );

    @Operation(
        operationId = "cancelPayment",
        summary = "Cancel an authorized payment",
        description = """
            Cancels a previously authorized payment. The payment must be in AUTHORIZED status.
            After cancellation, the payment status changes to CANCELLED and the authorization is released.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment cancelled successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Cancelled Payment",
                    value = """
                        {
                          "success": true,
                          "message": "Payment cancelled successfully",
                          "data": {
                            "id": "pay_abc123def456",
                            "status": "CANCELLED"
                          },
                          "timestamp": "2026-03-20T12:00:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Payment cannot be cancelled",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "message": "Payment cannot be cancelled: current status is CAPTURED",
                          "data": null,
                          "timestamp": "2026-03-20T12:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @Parameter(
                name = "id",
                description = "Payment ID to cancel",
                required = true,
                example = "pay_abc123def456"
            )
            String id,
            
            @Parameter(
                name = "merchantId",
                description = "Merchant ID for authorization",
                required = true,
                example = "merch_xyz789"
            )
            String merchantId
    );
}
```

**Tasks:**
- [ ] Create `PaymentApi.java` interface in `infrastructure/docs/`
- [ ] Add `@Tag` annotation with description
- [ ] Document `processPayment()` with full examples
- [ ] Document `getPayment()` with examples
- [ ] Document `getPayments()` with examples
- [ ] Document `capturePayment()` with examples
- [ ] Document `cancelPayment()` with examples
- [ ] Add `@SecurityRequirement` annotations
- [ ] Add `@Parameter` annotations for all parameters
- [ ] Add full JSON examples for all responses

---

### 10.3 MerchantApi Interface

**File:** `src/main/java/com/payment/gateway/infrastructure/docs/MerchantApi.java`

**Endpoints to Document:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/merchants` | Register merchant |
| GET | `/api/v1/merchants/{id}` | Get merchant |
| PUT | `/api/v1/merchants/{id}` | Update merchant |
| POST | `/api/v1/merchants/{id}/suspend` | Suspend merchant |

**Tasks:**
- [ ] Create `MerchantApi.java` interface
- [ ] Add `@Tag` annotation with description
- [ ] Document `registerMerchant()` with request/response examples
- [ ] Document `getMerchant()` with examples
- [ ] Document `updateMerchant()` with examples
- [ ] Document `suspendMerchant()` with examples
- [ ] Add security requirements
- [ ] Add full JSON examples

---

### 10.4 CustomerApi Interface

**File:** `src/main/java/com/payment/gateway/infrastructure/docs/CustomerApi.java`

**Endpoints to Document:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/customers` | Register customer |
| GET | `/api/v1/customers/{id}` | Get customer |
| POST | `/api/v1/customers/{id}/payment-methods` | Add payment method |
| DELETE | `/api/v1/customers/{id}/payment-methods/{pmId}` | Remove payment method |

**Tasks:**
- [ ] Create `CustomerApi.java` interface
- [ ] Add `@Tag` annotation with description
- [ ] Document `registerCustomer()` with examples
- [ ] Document `getCustomer()` with examples
- [ ] Document `addPaymentMethod()` with card examples (test cards)
- [ ] Document `removePaymentMethod()` with examples
- [ ] Add security requirements
- [ ] Add full JSON examples

---

### 10.5 RefundApi Interface

**File:** `src/main/java/com/payment/gateway/infrastructure/docs/RefundApi.java`

**Endpoints to Document:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/refunds` | Process refund |
| GET | `/api/v1/refunds/{id}` | Get refund |
| POST | `/api/v1/refunds/{id}/cancel` | Cancel refund |

**Tasks:**
- [ ] Create `RefundApi.java` interface
- [ ] Add `@Tag` annotation with description
- [ ] Document `processRefund()` with full/partial refund examples
- [ ] Document `getRefund()` with examples
- [ ] Document `cancelRefund()` with examples
- [ ] Add security requirements
- [ ] Add full JSON examples

---

### 10.6 TransactionApi Interface

**File:** `src/main/java/com/payment/gateway/infrastructure/docs/TransactionApi.java`

**Endpoints to Document:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/transactions/{id}` | Get transaction |
| POST | `/api/v1/transactions/{id}/capture` | Capture transaction |
| POST | `/api/v1/transactions/{id}/void` | Void transaction |

**Tasks:**
- [ ] Create `TransactionApi.java` interface
- [ ] Add `@Tag` annotation with description
- [ ] Document `getTransaction()` with examples
- [ ] Document `captureTransaction()` with examples
- [ ] Document `voidTransaction()` with examples
- [ ] Add security requirements
- [ ] Add full JSON examples

---

### 10.7 ReconciliationApi Interface

**File:** `src/main/java/com/payment/gateway/infrastructure/docs/ReconciliationApi.java`

**Endpoints to Document:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/reconciliation/reconcile` | Reconcile transactions |
| POST | `/api/v1/reconciliation/settlement-report` | Generate settlement report |

**Tasks:**
- [ ] Create `ReconciliationApi.java` interface
- [ ] Add `@Tag` annotation with description
- [ ] Document `reconcileTransactions()` with examples
- [ ] Document `generateSettlementReport()` with examples
- [ ] Add security requirements
- [ ] Add full JSON examples

---

### 10.8 Controller Modifications

Each controller needs to implement its corresponding API interface.

**PaymentController.java Example:**

```java
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final CapturePaymentUseCase capturePaymentUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final PaymentRestMapper paymentRestMapper;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request) {
        // Implementation unchanged
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable String id,
            @RequestParam String merchantId) {
        // Implementation unchanged
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPayments(
            @RequestParam String merchantId) {
        // Implementation unchanged
    }

    @Override
    @PostMapping("/{id}/capture")
    public ResponseEntity<ApiResponse<PaymentResponse>> capturePayment(
            @PathVariable String id,
            @RequestParam String merchantId) {
        // Implementation unchanged
    }

    @Override
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable String id,
            @RequestParam String merchantId) {
        // Implementation unchanged
    }
}
```

**Tasks:**
- [ ] Modify `PaymentController` to implement `PaymentApi`
- [ ] Add `@Override` annotations to all methods
- [ ] Modify `MerchantController` to implement `MerchantApi`
- [ ] Modify `CustomerController` to implement `CustomerApi`
- [ ] Modify `RefundController` to implement `RefundApi`
- [ ] Modify `TransactionController` to implement `TransactionApi`
- [ ] Modify `ReconciliationController` to implement `ReconciliationApi`

---

## DTO Schema Annotations

### Files Requiring @Schema Annotations

| DTO File | Fields | Priority |
|----------|--------|----------|
| `CreatePaymentRequest.java` | 9 fields + nested class | HIGH |
| `PaymentResponse.java` | 16 fields + nested class | HIGH |
| `MerchantResponse.java` | ~8 fields | HIGH |
| `CustomerResponse.java` | ~10 fields | HIGH |
| `TransactionResponse.java` | ~12 fields | MEDIUM |
| `RefundResponse.java` | ~10 fields | MEDIUM |
| `ReconciliationResponse.java` | ~6 fields | MEDIUM |
| `SettlementReportDTO.java` | ~10 fields | MEDIUM |
| `ApiResponse.java` | 4 fields (generic) | HIGH |
| `PagedResponse.java` | 3 fields | MEDIUM |
| `PageInfo.java` | 5 fields | MEDIUM |
| `MerchantController.CreateMerchantRequest` | 3 fields | HIGH |
| `MerchantController.UpdateMerchantRequest` | 3 fields | HIGH |
| `CustomerController.CreateCustomerRequest` | 6 fields | HIGH |
| `CustomerController.AddPaymentMethodRequest` | 8 fields | HIGH |
| `RefundController.CreateRefundRequest` | 5 fields | MEDIUM |

### Example: CreatePaymentRequest.java

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new payment")
public class CreatePaymentRequest {

    @Schema(
        description = "Unique merchant identifier",
        example = "merch_xyz789",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @Schema(
        description = "Payment amount in smallest currency unit (cents for USD/EUR, pence for GBP)",
        example = "10000",
        minimum = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amountInCents;

    @Schema(
        description = "ISO 4217 currency code",
        example = "USD",
        allowableValues = {"USD", "EUR", "GBP"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Currency is required")
    private String currency;

    @Schema(
        description = "Customer ID (optional if using paymentMethodId directly)",
        example = "cust_123abc"
    )
    private String customerId;

    @Schema(
        description = "Payment method ID (optional if customer has default payment method)",
        example = "pm_card_visa123"
    )
    private String paymentMethodId;

    @Schema(
        description = "Idempotency key (extracted from X-Idempotency-Key header)",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String idempotencyKey;

    @Schema(
        description = "Payment description for merchant records",
        example = "Order #12345"
    )
    private String description;

    @Schema(
        description = "List of payment items (optional, for detailed receipts)"
    )
    private List<PaymentItemRequest> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Payment line item")
    public static class PaymentItemRequest {
        
        @Schema(description = "Item description", example = "Premium Widget")
        private String description;
        
        @Schema(description = "Quantity", example = "2", minimum = "1")
        private Integer quantity;
        
        @Schema(description = "Unit price in cents", example = "5000")
        private Long unitPriceInCents;
    }
}
```

### Example: PaymentResponse.java

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payment response with full details")
public class PaymentResponse {

    @Schema(description = "Unique payment identifier", example = "pay_abc123def456")
    private String id;

    @Schema(description = "Merchant ID", example = "merch_xyz789")
    private String merchantId;

    @Schema(description = "Customer ID", example = "cust_123abc")
    private String customerId;

    @Schema(description = "Payment method ID used", example = "pm_card_visa123")
    private String paymentMethodId;

    @Schema(description = "Payment amount in cents", example = "10000")
    private Long amountInCents;

    @Schema(description = "ISO 4217 currency code", example = "USD")
    private String currency;

    @Schema(
        description = "Payment status",
        example = "AUTHORIZED",
        allowableValues = {"PENDING", "AUTHORIZED", "CAPTURED", "CANCELLED", "FAILED"}
    )
    private String status;

    @Schema(description = "Idempotency key used", example = "550e8400-e29b-41d4-a716-446655440000")
    private String idempotencyKey;

    @Schema(description = "Payment description", example = "Order #12345")
    private String description;

    @Schema(description = "Gateway transaction ID", example = "txn_stripe_abc123")
    private String gatewayTransactionId;

    @Schema(description = "Error code if payment failed", example = "INSUFFICIENT_FUNDS")
    private String errorCode;

    @Schema(description = "Error message if payment failed", example = "Card has insufficient funds")
    private String errorMessage;

    @Schema(description = "Payment line items")
    private List<PaymentItemResponse> items;

    @Schema(description = "Payment creation timestamp", example = "2026-03-20T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2026-03-20T10:30:00Z")
    private Instant updatedAt;

    @Schema(description = "Authorization timestamp", example = "2026-03-20T10:30:00Z")
    private Instant authorizedAt;

    @Schema(description = "Capture timestamp", example = "2026-03-20T12:00:00Z")
    private Instant capturedAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Payment line item response")
    public static class PaymentItemResponse {
        
        @Schema(description = "Item description", example = "Premium Widget")
        private String description;
        
        @Schema(description = "Quantity", example = "2")
        private Integer quantity;
        
        @Schema(description = "Unit price in cents", example = "5000")
        private Long unitPriceInCents;
        
        @Schema(description = "Total price in cents", example = "10000")
        private Long totalInCents;
    }
}
```

**Tasks:**
- [ ] Add `@Schema` to `CreatePaymentRequest` (9 fields + nested)
- [ ] Add `@Schema` to `PaymentResponse` (16 fields + nested)
- [ ] Add `@Schema` to `MerchantResponse`
- [ ] Add `@Schema` to `CustomerResponse`
- [ ] Add `@Schema` to `TransactionResponse`
- [ ] Add `@Schema` to `RefundResponse`
- [ ] Add `@Schema` to `ReconciliationResponse`
- [ ] Add `@Schema` to `SettlementReportDTO`
- [ ] Add `@Schema` to `ApiResponse`
- [ ] Add `@Schema` to `PagedResponse`
- [ ] Add `@Schema` to `PageInfo`
- [ ] Add `@Schema` to request DTOs in controllers

---

### 10.10 Application.yml Configuration

**Add to application.yml:**

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
    tagsSorter: alpha
    display-request-duration: true
    show-extensions: true
    show-common-extensions: true
    deep-linking: true
    default-models-expand-depth: 1
    default-model-expand-depth: 1
  show-actuator: true
  default-flat-param-object: true
  override-with-generic-response: false
  cache:
    disabled: false
  packages-to-scan: com.payment.gateway.infrastructure
  paths-to-match: /api/v1/**
```

**Tasks:**
- [ ] Add `springdoc` configuration section to `application.yml`
- [ ] Configure API docs path
- [ ] Configure Swagger UI options
- [ ] Configure package scanning

---

## Execution Order

| Order | Task | Dependencies | Estimated Time |
|-------|------|--------------|----------------|
| 1 | Update `SwaggerConfig.java` | None | 15 min |
| 2 | Create `PaymentApi.java` | SwaggerConfig | 30 min |
| 3 | Create `MerchantApi.java` | SwaggerConfig | 20 min |
| 4 | Create `CustomerApi.java` | SwaggerConfig | 25 min |
| 5 | Create `RefundApi.java` | SwaggerConfig | 20 min |
| 6 | Create `TransactionApi.java` | SwaggerConfig | 15 min |
| 7 | Create `ReconciliationApi.java` | SwaggerConfig | 15 min |
| 8 | Add `@Schema` to DTOs (parallel) | None | 45 min |
| 9 | Modify controllers | Api interfaces | 20 min |
| 10 | Update `application.yml` | None (parallel) | 5 min |
| 11 | Verify Swagger UI | All above | 15 min |
| 12 | Update `CHECKPOINT.md` | All above | 5 min |
| **Total** | | | **~4 hours** |

---

## Verification Checklist

### Swagger UI Verification

| Check | URL | Expected Result |
|-------|-----|-----------------|
| Swagger UI loads | `http://localhost:8080/swagger-ui.html` | UI renders with all endpoints |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` | Valid JSON response |
| Tags displayed | Swagger UI sidebar | 7 tags visible |

### Functional Verification

| Check | Action | Expected Result |
|-------|--------|-----------------|
| All endpoints documented | Count in Swagger UI | 21 endpoints |
| Try it out works | Click "Try it out" on any endpoint | Request can be executed |
| Examples display | Check Examples dropdown | Full JSON examples visible |
| Security button | Click "Authorize" | Modal with API Key and JWT options |
| Parameter docs | Check any parameter | Description and example visible |
| Response docs | Check any response | All status codes documented |

### Content Verification

| Check | Expected |
|-------|----------|
| Tag descriptions | Each tag has meaningful description |
| Operation summaries | Each endpoint has clear summary |
| Parameter examples | All parameters have examples |
| Response examples | All responses have JSON examples |
| Security requirements | Endpoints show lock icon |
| Error responses | 400, 404, 409, 500 documented |

---

## Open Questions

### Question 1: Inner DTO Classes in Controllers

Controllers like `MerchantController` have inner classes (`CreateMerchantRequest`, `UpdateMerchantRequest`). 

**Options:**
- **A) Keep as inner classes** - Document them where they are
- **B) Extract to separate files** - Move to `infrastructure/{domain}/adapter/in/rest/dto/`

**Recommendation:** Keep as inner classes (simpler, already working)

**Your Decision:** _______________

---

### Question 2: Group Display Order

Should the groups appear in Swagger UI dropdown in a specific order?

**Planned Order:**
1. Health & Monitoring
2. Merchant Management
3. Customer Management
4. Payment Processing
5. Transaction Management
6. Refund Processing
7. Reconciliation

**Your Decision:** _______________

---

### Question 3: Ready to Implement

After you review this plan, should I proceed with implementation?

**Your Decision:** _______________

---

## Notes

- This plan follows **Approach B: Interface-Based Documentation**
- Controllers remain clean with only `@Override` annotations
- All documentation is centralized in `infrastructure/docs/` package
- DTOs have `@Schema` annotations as they define API data contracts
- Full JSON examples are included for all request/response combinations
- Security schemes documented but not enforced in Swagger UI (works with empty values)

---

**Document Version:** 1.0  
**Last Updated:** 2026-03-20