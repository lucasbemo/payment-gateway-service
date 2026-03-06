package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "reconciliation_batches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReconciliationBatchJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "merchant_id", length = 36)
    private String merchantId;

    @Column(name = "batch_date", nullable = false)
    private LocalDate reconciliationDate;

    @Column(name = "gateway_name", length = 100)
    private String gatewayName;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "total_transactions")
    private Integer totalTransactions;

    @Column(name = "matched_transactions")
    private Integer matchedTransactions;

    @Column(name = "mismatched_transactions")
    private Integer unmatchedTransactions;

    @Column(name = "expected_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "actual_amount", precision = 19, scale = 4)
    private BigDecimal matchedAmount;

    @Column(name = "discrepancy_count")
    private Integer discrepancyCount;

    @Column(name = "initiated_by", length = 100)
    private String initiatedBy;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    private ReconciliationBatchJpaEntity(Builder builder) {
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

    public static class Builder {
        private String id;
        private String merchantId;
        private LocalDate reconciliationDate;
        private String gatewayName;
        private String status;
        private Integer totalTransactions;
        private Integer matchedTransactions;
        private Integer unmatchedTransactions;
        private BigDecimal totalAmount;
        private BigDecimal matchedAmount;
        private Integer discrepancyCount;
        private String initiatedBy;
        private Instant startedAt;
        private Instant completedAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public Builder reconciliationDate(LocalDate reconciliationDate) { this.reconciliationDate = reconciliationDate; return this; }
        public Builder gatewayName(String gatewayName) { this.gatewayName = gatewayName; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder totalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; return this; }
        public Builder matchedTransactions(Integer matchedTransactions) { this.matchedTransactions = matchedTransactions; return this; }
        public Builder unmatchedTransactions(Integer unmatchedTransactions) { this.unmatchedTransactions = unmatchedTransactions; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder matchedAmount(BigDecimal matchedAmount) { this.matchedAmount = matchedAmount; return this; }
        public Builder discrepancyCount(Integer discrepancyCount) { this.discrepancyCount = discrepancyCount; return this; }
        public Builder initiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; return this; }
        public Builder startedAt(Instant startedAt) { this.startedAt = startedAt; return this; }
        public Builder completedAt(Instant completedAt) { this.completedAt = completedAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public ReconciliationBatchJpaEntity build() {
            return new ReconciliationBatchJpaEntity(this);
        }
    }
}
