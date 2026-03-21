package com.payment.gateway.application.reconciliation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@Schema(description = "Reconciliation batch results")
public class ReconciliationResponse {

    @Schema(description = "Reconciliation batch ID", example = "batch_abc123")
    private final String batchId;

    @Schema(
        description = "Reconciliation status",
        example = "COMPLETED",
        allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "FAILED"}
    )
    private final String status;

    @Schema(description = "Total number of transactions reconciled", example = "150")
    private final Integer totalTransactions;

    @Schema(description = "Number of matched transactions", example = "145")
    private final Integer matchedCount;

    @Schema(description = "Number of discrepancies found", example = "5")
    private final Integer discrepancyCount;

    @Schema(description = "Total transaction amount in cents", example = "1500000")
    private final Long totalAmount;

    @Schema(description = "Reconciled amount in cents", example = "1495000")
    private final Long reconciledAmount;

    @Schema(description = "Discrepancy amount in cents", example = "5000")
    private final Long discrepancyAmount;

    @Schema(description = "Batch creation timestamp", example = "2026-03-20T10:00:00Z")
    private final Instant createdAt;

    @Schema(description = "Batch completion timestamp", example = "2026-03-20T10:05:00Z")
    private final Instant completedAt;
}
