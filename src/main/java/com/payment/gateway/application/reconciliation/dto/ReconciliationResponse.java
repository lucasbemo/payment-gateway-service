package com.payment.gateway.application.reconciliation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Response DTO for reconciliation operations.
 */
@Getter
@Builder
public class ReconciliationResponse {

    private final String batchId;
    private final String status;
    private final Integer totalTransactions;
    private final Integer matchedCount;
    private final Integer discrepancyCount;
    private final Long totalAmount;
    private final Long reconciledAmount;
    private final Long discrepancyAmount;
    private final Instant createdAt;
    private final Instant completedAt;
}
