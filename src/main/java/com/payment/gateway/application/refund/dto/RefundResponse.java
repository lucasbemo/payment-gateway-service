package com.payment.gateway.application.refund.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Response DTO for refund operations.
 */
@Getter
@Builder
public class RefundResponse {

    private final String id;
    private final String paymentId;
    private final String transactionId;
    private final String merchantId;
    private final Long amount;
    private final String currency;
    private final String status;
    private final String type;
    private final String reason;
    private final Instant createdAt;
    private final Instant processedAt;
}
