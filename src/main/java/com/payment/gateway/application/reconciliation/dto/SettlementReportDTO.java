package com.payment.gateway.application.reconciliation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@Schema(description = "Settlement report details")
public class SettlementReportDTO {

    @Schema(description = "Unique report identifier", example = "report_abc123")
    private final String id;

    @Schema(description = "Merchant ID", example = "merch_abc123")
    private final String merchantId;

    @Schema(description = "Payment gateway name", example = "STRIPE")
    private final String gatewayName;

    @Schema(description = "Settlement date", example = "2026-03-20")
    private final String settlementDate;

    @Schema(description = "Gateway report ID", example = "settle_xyz789")
    private final String gatewayReportId;

    @Schema(description = "Gross amount in cents", example = "100000")
    private final Long grossAmount;

    @Schema(description = "Fee amount in cents", example = "2900")
    private final Long feeAmount;

    @Schema(description = "Net amount in cents", example = "97100")
    private final Long netAmount;

    @Schema(description = "ISO 4217 currency code", example = "USD")
    private final String currency;

    @Schema(description = "Number of transactions", example = "50")
    private final Integer transactionCount;

    @Schema(description = "Number of refunds", example = "3")
    private final Integer refundCount;

    @Schema(description = "Number of chargebacks", example = "0")
    private final Integer chargebackCount;

    @Schema(description = "File path for downloaded report", example = "/reports/2026-03-20/settlement_merch_abc123.json")
    private final String filePath;

    @Schema(description = "Report format", example = "JSON", allowableValues = {"JSON", "CSV", "PDF"})
    private final String format;

    @Schema(description = "Report generation timestamp", example = "2026-03-20T10:00:00Z")
    private final Instant createdAt;
}
