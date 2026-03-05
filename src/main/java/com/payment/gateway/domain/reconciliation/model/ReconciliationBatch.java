package com.payment.gateway.domain.reconciliation.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

/**
 * ReconciliationBatch aggregate root.
 * Represents a batch of transactions being reconciled.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReconciliationBatch {
    private String id;
    private String merchantId;
    private LocalDate reconciliationDate;
    private String gatewayName;
    private ReconciliationStatus status;
    private Integer totalTransactions;
    private Integer matchedTransactions;
    private Integer unmatchedTransactions;
    private BigDecimal totalAmount;
    private BigDecimal matchedAmount;
    private BigDecimal unmatchedAmount;
    private Integer discrepancyCount;
    private String initiatedBy;
    private Instant startedAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;

    private ReconciliationBatch(Builder builder) {
        this.id = builder.id;
        this.merchantId = builder.merchantId;
        this.reconciliationDate = builder.reconciliationDate;
        this.gatewayName = builder.gatewayName;
        this.status = builder.status;
        this.totalTransactions = builder.totalTransactions;
        this.matchedTransactions = builder.matchedTransactions;
        this.unmatchedTransactions = builder.unmatchedTransactions;
        this.totalAmount = builder.totalAmount;
        this.matchedAmount = builder.matchedAmount;
        this.unmatchedAmount = builder.unmatchedAmount;
        this.discrepancyCount = builder.discrepancyCount;
        this.initiatedBy = builder.initiatedBy;
        this.startedAt = builder.startedAt;
        this.completedAt = builder.completedAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ReconciliationBatch create(String merchantId, LocalDate reconciliationDate,
                                              String gatewayName, String initiatedBy) {
        Instant now = Instant.now();
        return new Builder()
                .id(UUID.randomUUID().toString())
                .merchantId(merchantId)
                .reconciliationDate(reconciliationDate)
                .gatewayName(gatewayName)
                .status(ReconciliationStatus.PENDING)
                .totalTransactions(0)
                .matchedTransactions(0)
                .unmatchedTransactions(0)
                .totalAmount(BigDecimal.ZERO)
                .matchedAmount(BigDecimal.ZERO)
                .unmatchedAmount(BigDecimal.ZERO)
                .discrepancyCount(0)
                .initiatedBy(initiatedBy)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void startProcessing() {
        this.status = ReconciliationStatus.PROCESSING;
        this.startedAt = Instant.now();
        this.updatedAt = this.startedAt;
    }

    public void markAsReconciling() {
        this.status = ReconciliationStatus.RECONCILING;
        this.updatedAt = Instant.now();
    }

    public void updateCounts(int total, int matched, int unmatched) {
        this.totalTransactions = total;
        this.matchedTransactions = matched;
        this.unmatchedTransactions = unmatched;
        this.updatedAt = Instant.now();
    }

    public void updateAmounts(BigDecimal total, BigDecimal matched, BigDecimal unmatched) {
        this.totalAmount = total;
        this.matchedAmount = matched;
        this.unmatchedAmount = unmatched;
        this.updatedAt = Instant.now();
    }

    public void incrementDiscrepancyCount() {
        this.discrepancyCount = this.discrepancyCount != null ? this.discrepancyCount + 1 : 1;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        if (this.unmatchedTransactions != null && this.unmatchedTransactions > 0) {
            this.status = ReconciliationStatus.PARTIALLY_RECONCILED;
        } else {
            this.status = ReconciliationStatus.COMPLETED;
        }
        this.completedAt = Instant.now();
        this.updatedAt = this.completedAt;
    }

    public void fail(String reason) {
        this.status = ReconciliationStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public boolean isComplete() {
        return this.status == ReconciliationStatus.COMPLETED ||
               this.status == ReconciliationStatus.PARTIALLY_RECONCILED;
    }

    public static class Builder {
        private String id;
        private String merchantId;
        private LocalDate reconciliationDate;
        private String gatewayName;
        private ReconciliationStatus status;
        private Integer totalTransactions;
        private Integer matchedTransactions;
        private Integer unmatchedTransactions;
        private BigDecimal totalAmount;
        private BigDecimal matchedAmount;
        private BigDecimal unmatchedAmount;
        private Integer discrepancyCount;
        private String initiatedBy;
        private Instant startedAt;
        private Instant completedAt;
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

        public Builder reconciliationDate(LocalDate reconciliationDate) {
            this.reconciliationDate = reconciliationDate;
            return this;
        }

        public Builder gatewayName(String gatewayName) {
            this.gatewayName = gatewayName;
            return this;
        }

        public Builder status(ReconciliationStatus status) {
            this.status = status;
            return this;
        }

        public Builder totalTransactions(Integer totalTransactions) {
            this.totalTransactions = totalTransactions;
            return this;
        }

        public Builder matchedTransactions(Integer matchedTransactions) {
            this.matchedTransactions = matchedTransactions;
            return this;
        }

        public Builder unmatchedTransactions(Integer unmatchedTransactions) {
            this.unmatchedTransactions = unmatchedTransactions;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder matchedAmount(BigDecimal matchedAmount) {
            this.matchedAmount = matchedAmount;
            return this;
        }

        public Builder unmatchedAmount(BigDecimal unmatchedAmount) {
            this.unmatchedAmount = unmatchedAmount;
            return this;
        }

        public Builder discrepancyCount(Integer discrepancyCount) {
            this.discrepancyCount = discrepancyCount;
            return this;
        }

        public Builder initiatedBy(String initiatedBy) {
            this.initiatedBy = initiatedBy;
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
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

        public ReconciliationBatch build() {
            return new ReconciliationBatch(this);
        }
    }
}
