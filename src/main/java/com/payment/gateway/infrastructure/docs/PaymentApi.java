package com.payment.gateway.infrastructure.docs;

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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Payment authorized successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.payment.gateway.infrastructure.commons.rest.ApiResponse.class),
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
    ResponseEntity<com.payment.gateway.infrastructure.commons.rest.ApiResponse<com.payment.gateway.infrastructure.payment.adapter.in.rest.PaymentResponse>> processPayment(
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
                    schema = @Schema(implementation = com.payment.gateway.infrastructure.payment.adapter.in.rest.CreatePaymentRequest.class),
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
            @Valid com.payment.gateway.infrastructure.payment.adapter.in.rest.CreatePaymentRequest request
    );

    @Operation(
        operationId = "getPayment",
        summary = "Get payment by ID",
        description = "Retrieves detailed information about a specific payment."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Payment found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.payment.gateway.infrastructure.commons.rest.ApiResponse.class),
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
    ResponseEntity<com.payment.gateway.infrastructure.commons.rest.ApiResponse<com.payment.gateway.infrastructure.payment.adapter.in.rest.PaymentResponse>> getPayment(
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Payments retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.payment.gateway.infrastructure.commons.rest.ApiResponse.class),
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
    ResponseEntity<com.payment.gateway.infrastructure.commons.rest.ApiResponse<List<com.payment.gateway.infrastructure.payment.adapter.in.rest.PaymentResponse>>> getPayments(
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
    ResponseEntity<com.payment.gateway.infrastructure.commons.rest.ApiResponse<com.payment.gateway.infrastructure.payment.adapter.in.rest.PaymentResponse>> capturePayment(
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
    ResponseEntity<com.payment.gateway.infrastructure.commons.rest.ApiResponse<com.payment.gateway.infrastructure.payment.adapter.in.rest.PaymentResponse>> cancelPayment(
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