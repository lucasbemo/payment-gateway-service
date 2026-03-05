package com.payment.gateway.application.transaction.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Response DTO for transaction operations.
 */
@Getter
@Builder
public class TransactionResponse {

    private final String id;
    private final String paymentId;
    private final String merchantId;
    private final String type;
    private final Long amount;
    private final String currency;
    private final String status;
    private final String gatewayTransactionId;
    private final String errorCode;
    private final String errorMessage;
    private final Instant createdAt;
    private final Instant processedAt;
}
