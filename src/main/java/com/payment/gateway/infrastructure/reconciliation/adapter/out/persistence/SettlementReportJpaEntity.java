package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "settlement_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementReportJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "merchant_id", length = 36)
    private String merchantId;

    @Column(name = "gateway_name", length = 100)
    private String gatewayName;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Column(name = "gateway_report_id", length = 255)
    private String gatewayReportId;

    @Column(name = "gross_amount", precision = 19, scale = 4)
    private BigDecimal grossAmount;

    @Column(name = "fee_amount", precision = 19, scale = 4)
    private BigDecimal feeAmount;

    @Column(name = "net_amount", precision = 19, scale = 4)
    private BigDecimal netAmount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "transaction_count")
    private Integer transactionCount;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;

    @Column(name = "report_format", nullable = false, length = 20)
    private String reportFormat;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "report_path", length = 512)
    private String filePath;

    @Column(name = "reconciliation_batch_id", length = 36)
    private String reconciliationBatchId;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private SettlementReportJpaEntity(Builder builder) {
        this.id = builder.id;
        this.merchantId = builder.merchantId;
        this.gatewayName = builder.gatewayName;
        this.settlementDate = builder.settlementDate;
        this.gatewayReportId = builder.gatewayReportId;
        this.grossAmount = builder.grossAmount;
        this.feeAmount = builder.feeAmount;
        this.netAmount = builder.netAmount;
        this.currency = builder.currency;
        this.transactionCount = builder.transactionCount;
        this.reportType = builder.reportType;
        this.reportFormat = builder.reportFormat;
        this.status = builder.status;
        this.filePath = builder.filePath;
        this.reconciliationBatchId = builder.reconciliationBatchId;
        this.settledAt = builder.settledAt;
        this.createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String merchantId;
        private String gatewayName;
        private LocalDate settlementDate;
        private String gatewayReportId;
        private BigDecimal grossAmount;
        private BigDecimal feeAmount;
        private BigDecimal netAmount;
        private String currency;
        private Integer transactionCount;
        private String reportType;
        private String reportFormat;
        private String status;
        private String filePath;
        private String reconciliationBatchId;
        private Instant settledAt;
        private Instant createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public Builder gatewayName(String gatewayName) { this.gatewayName = gatewayName; return this; }
        public Builder settlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; return this; }
        public Builder gatewayReportId(String gatewayReportId) { this.gatewayReportId = gatewayReportId; return this; }
        public Builder grossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; return this; }
        public Builder feeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; return this; }
        public Builder netAmount(BigDecimal netAmount) { this.netAmount = netAmount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder transactionCount(Integer transactionCount) { this.transactionCount = transactionCount; return this; }
        public Builder reportType(String reportType) { this.reportType = reportType; return this; }
        public Builder reportFormat(String reportFormat) { this.reportFormat = reportFormat; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder reconciliationBatchId(String reconciliationBatchId) { this.reconciliationBatchId = reconciliationBatchId; return this; }
        public Builder settledAt(Instant settledAt) { this.settledAt = settledAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public SettlementReportJpaEntity build() {
            return new SettlementReportJpaEntity(this);
        }
    }
}
