package com.payment.gateway.infrastructure.payment.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
        description = "Payment amount in smallest currency unit (cents for USD/EUR)",
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

    @Schema(description = "List of payment items (optional, for detailed receipts)")
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