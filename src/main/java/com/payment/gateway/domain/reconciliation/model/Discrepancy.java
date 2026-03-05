package com.payment.gateway.domain.reconciliation.model;

import com.payment.gateway.commons.model.Money;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Discrepancy aggregate root.
 * Represents a discrepancy found during reconciliation.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Discrepancy {
    private String id;
    private String batchId;
    private String merchantId;
    private String transactionId;
    private String gatewayTransactionId;
    private DiscrepancyType type;
    private DiscrepancyStatus status;
    private Money systemAmount;
    private Money gatewayAmount;
    private String description;
    private String resolutionNotes;
    private String resolvedBy;
    private Instant resolvedAt;
    private Instant createdAt;
    private Instant updatedAt;

    private Discrepancy(Builder builder) {
        this.id = builder.id;
        this.batchId = builder.batchId;
        this.merchantId = builder.merchantId;
        this.transactionId = builder.transactionId;
        this.gatewayTransactionId = builder.gatewayTransactionId;
        this.type = builder.type;
        this.status = builder.status;
        this.systemAmount = builder.systemAmount;
        this.gatewayAmount = builder.gatewayAmount;
        this.description = builder.description;
        this.resolutionNotes = builder.resolutionNotes;
        this.resolvedBy = builder.resolvedBy;
        this.resolvedAt = builder.resolvedAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Discrepancy create(String batchId, String merchantId, DiscrepancyType type,
                                      String transactionId, String description) {
        Instant now = Instant.now();
        return new Builder()
                .id(UUID.randomUUID().toString())
                .batchId(batchId)
                .merchantId(merchantId)
                .type(type)
                .transactionId(transactionId)
                .status(DiscrepancyStatus.OPEN)
                .description(description)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void setAmounts(Money systemAmount, Money gatewayAmount) {
        this.systemAmount = systemAmount;
        this.gatewayAmount = gatewayAmount;
        this.updatedAt = Instant.now();
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
        this.updatedAt = Instant.now();
    }

    public void markUnderReview() {
        this.status = DiscrepancyStatus.UNDER_REVIEW;
        this.updatedAt = Instant.now();
    }

    public void resolve(String resolutionNotes, String resolvedBy) {
        this.status = DiscrepancyStatus.RESOLVED;
        this.resolutionNotes = resolutionNotes;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = Instant.now();
        this.updatedAt = this.resolvedAt;
    }

    public void escalate() {
        this.status = DiscrepancyStatus.ESCALATED;
        this.updatedAt = Instant.now();
    }

    public void close() {
        this.status = DiscrepancyStatus.CLOSED;
        this.updatedAt = Instant.now();
    }

    public static class Builder {
        private String id;
        private String batchId;
        private String merchantId;
        private String transactionId;
        private String gatewayTransactionId;
        private DiscrepancyType type;
        private DiscrepancyStatus status;
        private Money systemAmount;
        private Money gatewayAmount;
        private String description;
        private String resolutionNotes;
        private String resolvedBy;
        private Instant resolvedAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder batchId(String batchId) {
            this.batchId = batchId;
            return this;
        }

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder gatewayTransactionId(String gatewayTransactionId) {
            this.gatewayTransactionId = gatewayTransactionId;
            return this;
        }

        public Builder type(DiscrepancyType type) {
            this.type = type;
            return this;
        }

        public Builder status(DiscrepancyStatus status) {
            this.status = status;
            return this;
        }

        public Builder systemAmount(Money systemAmount) {
            this.systemAmount = systemAmount;
            return this;
        }

        public Builder gatewayAmount(Money gatewayAmount) {
            this.gatewayAmount = gatewayAmount;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder resolutionNotes(String resolutionNotes) {
            this.resolutionNotes = resolutionNotes;
            return this;
        }

        public Builder resolvedBy(String resolvedBy) {
            this.resolvedBy = resolvedBy;
            return this;
        }

        public Builder resolvedAt(Instant resolvedAt) {
            this.resolvedAt = resolvedAt;
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

        public Discrepancy build() {
            return new Discrepancy(this);
        }
    }
}
