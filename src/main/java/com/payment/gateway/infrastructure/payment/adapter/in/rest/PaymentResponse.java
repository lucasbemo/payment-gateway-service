package com.payment.gateway.infrastructure.payment.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

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