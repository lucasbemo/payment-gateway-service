package com.payment.gateway.application.refund.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@Schema(description = "Refund details")
public class RefundResponse {

    @Schema(description = "Unique refund identifier", example = "ref_abc123")
    private final String id;

    @Schema(description = "Associated payment ID", example = "pay_xyz789")
    private final String paymentId;

    @Schema(description = "Associated transaction ID", example = "txn_abc123")
    private final String transactionId;

    @Schema(description = "Merchant ID", example = "merch_abc123")
    private final String merchantId;

    @Schema(description = "Refund amount in cents", example = "5000")
    private final Long amount;

    @Schema(description = "ISO 4217 currency code", example = "USD")
    private final String currency;

    @Schema(
        description = "Refund status",
        example = "COMPLETED",
        allowableValues = {"PENDING", "PROCESSING", "APPROVED", "REJECTED", "COMPLETED", "FAILED", "CANCELLED"}
    )
    private final String status;

    @Schema(
        description = "Refund type",
        example = "PARTIAL",
        allowableValues = {"FULL", "PARTIAL", "MULTIPLE", "CHARGEBACK", "CANCELLATION"}
    )
    private final String type;

    @Schema(description = "Reason for the refund", example = "Customer requested")
    private final String reason;

    @Schema(description = "Refund creation timestamp", example = "2026-03-20T10:00:00Z")
    private final Instant createdAt;

    @Schema(description = "Refund processing timestamp", example = "2026-03-20T10:00:05Z")
    private final Instant processedAt;
}
