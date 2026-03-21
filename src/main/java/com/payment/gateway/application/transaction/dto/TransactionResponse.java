package com.payment.gateway.application.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@Schema(description = "Transaction details")
public class TransactionResponse {

    @Schema(description = "Unique transaction identifier", example = "txn_abc123")
    private final String id;

    @Schema(description = "Associated payment ID", example = "pay_xyz789")
    private final String paymentId;

    @Schema(description = "Merchant ID", example = "merch_abc123")
    private final String merchantId;

    @Schema(
        description = "Transaction type",
        example = "PAYMENT",
        allowableValues = {"PAYMENT", "CAPTURE", "AUTHORIZATION", "REFUND", "PARTIAL_REFUND", "REVERSAL", "CHARGEBACK", "ADJUSTMENT"}
    )
    private final String type;

    @Schema(description = "Transaction amount in cents", example = "10000")
    private final Long amount;

    @Schema(description = "ISO 4217 currency code", example = "USD")
    private final String currency;

    @Schema(
        description = "Transaction status",
        example = "AUTHORIZED",
        allowableValues = {"PENDING", "PROCESSING", "AUTHORIZED", "CAPTURED", "SETTLED", "REVERSED", "FAILED", "REFUNDED", "PARTIALLY_REFUNDED"}
    )
    private final String status;

    @Schema(description = "Payment gateway transaction ID", example = "ch_stripe_abc123")
    private final String gatewayTransactionId;

    @Schema(description = "Error code if transaction failed", example = "CARD_DECLINED")
    private final String errorCode;

    @Schema(description = "Error message if transaction failed", example = "Card was declined")
    private final String errorMessage;

    @Schema(description = "Transaction creation timestamp", example = "2026-03-20T10:00:00Z")
    private final Instant createdAt;

    @Schema(description = "Transaction processing timestamp", example = "2026-03-20T10:00:05Z")
    private final Instant processedAt;
}
