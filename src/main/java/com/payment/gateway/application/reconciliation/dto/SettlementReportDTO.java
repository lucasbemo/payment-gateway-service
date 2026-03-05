package com.payment.gateway.application.reconciliation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * DTO for settlement reports.
 */
@Getter
@Builder
public class SettlementReportDTO {

    private final String id;
    private final String merchantId;
    private final String gatewayName;
    private final String settlementDate;
    private final String gatewayReportId;
    private final Long grossAmount;
    private final Long feeAmount;
    private final Long netAmount;
    private final String currency;
    private final Integer transactionCount;
    private final Integer refundCount;
    private final Integer chargebackCount;
    private final String filePath;
    private final String format;
    private final Instant createdAt;
}
