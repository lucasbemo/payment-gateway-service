package com.payment.gateway.infrastructure.payment.adapter.in.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for payment operations.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private String id;
    private String merchantId;
    private String customerId;
    private String paymentMethodId;
    private Long amountInCents;
    private String currency;
    private String status;
    private String idempotencyKey;
    private String description;
    private String gatewayTransactionId;
    private String errorCode;
    private String errorMessage;
    private List<PaymentItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant authorizedAt;
    private Instant capturedAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentItemResponse {
        private String description;
        private Integer quantity;
        private Long unitPriceInCents;
        private Long totalInCents;
    }
}
