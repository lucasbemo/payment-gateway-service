package com.payment.gateway.infrastructure.payment.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a payment.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {

    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amountInCents;

    @NotBlank(message = "Currency is required")
    private String currency;

    private String customerId;

    private String paymentMethodId;

    private String idempotencyKey;

    private String description;

    private List<PaymentItemRequest> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentItemRequest {
        private String description;
        private Integer quantity;
        private Long unitPriceInCents;
    }
}
