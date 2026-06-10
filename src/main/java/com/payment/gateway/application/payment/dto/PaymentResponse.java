package com.payment.gateway.application.payment.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * Response DTO for payment operations.
 */
@Getter
@Builder
public class PaymentResponse {

    private final String id;
    private final String merchantId;
    private final String customerId;
    private final String paymentMethodId;
    private final String transactionId;
    private final Long amount;
    private final String currency;
    private final String status;
    private final String idempotencyKey;
    private final String description;
    private final List<PaymentItemResponse> items;
    private final Instant createdAt;
    private final Instant updatedAt;

    @Getter
    @Builder
    public static class PaymentItemResponse {
        private final String description;
        private final Integer quantity;
        private final Long unitPrice;
        private final Long total;
    }
}
