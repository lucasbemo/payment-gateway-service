package com.payment.gateway.domain.reconciliation.model;

import com.payment.gateway.commons.model.Money;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

/**
 * SettlementReport aggregate root.
 * Represents a settlement report from a payment gateway.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementReport {
    private String id;
    private String merchantId;
    private String gatewayName;
    private LocalDate settlementDate;
    private String gatewayReportId;
    private Money grossAmount;
    private Money feeAmount;
    private Money netAmount;
    private String currency;
    private Integer transactionCount;
    private String status;
    private String filePath;
    private String reconciliationBatchId;
    private Instant settledAt;
    private Instant createdAt;
    private Instant updatedAt;

    private SettlementReport(Builder builder) {
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
        this.status = builder.status;
        this.filePath = builder.filePath;
        this.reconciliationBatchId = builder.reconciliationBatchId;
        this.settledAt = builder.settledAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SettlementReport create(String merchantId, String gatewayName, LocalDate settlementDate,
                                           String gatewayReportId, Money grossAmount, Money feeAmount,
                                           Money netAmount, String currency) {
        Instant now = Instant.now();
        return new Builder()
                .id(UUID.randomUUID().toString())
                .merchantId(merchantId)
                .gatewayName(gatewayName)
                .settlementDate(settlementDate)
                .gatewayReportId(gatewayReportId)
                .grossAmount(grossAmount)
                .feeAmount(feeAmount)
                .netAmount(netAmount)
                .currency(currency)
                .transactionCount(0)
                .status("PENDING")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void linkToReconciliation(String batchId) {
        this.reconciliationBatchId = batchId;
        this.updatedAt = Instant.now();
    }

    public void markSettled() {
        this.status = "SETTLED";
        this.settledAt = Instant.now();
        this.updatedAt = this.settledAt;
    }

    public void updateFilePath(String filePath) {
        this.filePath = filePath;
        this.updatedAt = Instant.now();
    }

    public void updateTransactionCount(int count) {
        this.transactionCount = count;
        this.updatedAt = Instant.now();
    }

    public static class Builder {
        private String id;
        private String merchantId;
        private String gatewayName;
        private LocalDate settlementDate;
        private String gatewayReportId;
        private Money grossAmount;
        private Money feeAmount;
        private Money netAmount;
        private String currency;
        private Integer transactionCount;
        private String status;
        private String filePath;
        private String reconciliationBatchId;
        private Instant settledAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder gatewayName(String gatewayName) {
            this.gatewayName = gatewayName;
            return this;
        }

        public Builder settlementDate(LocalDate settlementDate) {
            this.settlementDate = settlementDate;
            return this;
        }

        public Builder gatewayReportId(String gatewayReportId) {
            this.gatewayReportId = gatewayReportId;
            return this;
        }

        public Builder grossAmount(Money grossAmount) {
            this.grossAmount = grossAmount;
            return this;
        }

        public Builder feeAmount(Money feeAmount) {
            this.feeAmount = feeAmount;
            return this;
        }

        public Builder netAmount(Money netAmount) {
            this.netAmount = netAmount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder transactionCount(Integer transactionCount) {
            this.transactionCount = transactionCount;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder reconciliationBatchId(String reconciliationBatchId) {
            this.reconciliationBatchId = reconciliationBatchId;
            return this;
        }

        public Builder settledAt(Instant settledAt) {
            this.settledAt = settledAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public SettlementReport build() {
            return new SettlementReport(this);
        }
    }
}
