package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "discrepancies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscrepancyJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "reconciliation_batch_id", nullable = false, length = 36)
    private String reconciliationBatchId;

    @Column(name = "transaction_id", length = 36)
    private String transactionId;

    @Column(name = "payment_id", nullable = false, length = 36)
    private String paymentId;

    @Column(name = "discrepancy_type", nullable = false, length = 100)
    private String discrepancyType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "expected_amount", precision = 19, scale = 4)
    private BigDecimal expectedAmount;

    @Column(name = "actual_amount", precision = 19, scale = 4)
    private BigDecimal actualAmount;

    @Column(name = "expected_status", length = 50)
    private String expectedStatus;

    @Column(name = "actual_status", length = 50)
    private String actualStatus;

    @Column(name = "resolution_status", nullable = false, length = 50)
    private String resolutionStatus;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_by", length = 36)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    private DiscrepancyJpaEntity(Builder builder) {
        this.id = builder.id;
        this.reconciliationBatchId = builder.reconciliationBatchId;
        this.transactionId = builder.transactionId;
        this.paymentId = builder.paymentId;
        this.discrepancyType = builder.discrepancyType;
        this.description = builder.description;
        this.expectedAmount = builder.expectedAmount;
        this.actualAmount = builder.actualAmount;
        this.expectedStatus = builder.expectedStatus;
        this.actualStatus = builder.actualStatus;
        this.resolutionStatus = builder.resolutionStatus;
        this.resolutionNotes = builder.resolutionNotes;
        this.resolvedBy = builder.resolvedBy;
        this.resolvedAt = builder.resolvedAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String reconciliationBatchId;
        private String transactionId;
        private String paymentId;
        private String discrepancyType;
        private String description;
        private BigDecimal expectedAmount;
        private BigDecimal actualAmount;
        private String expectedStatus;
        private String actualStatus;
        private String resolutionStatus;
        private String resolutionNotes;
        private String resolvedBy;
        private Instant resolvedAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder reconciliationBatchId(String reconciliationBatchId) { this.reconciliationBatchId = reconciliationBatchId; return this; }
        public Builder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public Builder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public Builder discrepancyType(String discrepancyType) { this.discrepancyType = discrepancyType; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder expectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; return this; }
        public Builder actualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; return this; }
        public Builder expectedStatus(String expectedStatus) { this.expectedStatus = expectedStatus; return this; }
        public Builder actualStatus(String actualStatus) { this.actualStatus = actualStatus; return this; }
        public Builder resolutionStatus(String resolutionStatus) { this.resolutionStatus = resolutionStatus; return this; }
        public Builder resolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; return this; }
        public Builder resolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; return this; }
        public Builder resolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public DiscrepancyJpaEntity build() {
            return new DiscrepancyJpaEntity(this);
        }
    }
}
