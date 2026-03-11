package com.payment.gateway.domain.transaction.model;

import com.payment.gateway.commons.model.Money;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Transaction aggregate root.
 * Represents a financial transaction within the payment gateway.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {
    private String id;
    private String paymentId;
    private String merchantId;
    private TransactionType type;
    private Money amount;
    private Money netAmount;
    private String currency;
    private TransactionStatus status;
    private String gatewayTransactionId;
    private String errorCode;
    private String errorMessage;
    private Integer retryCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant processedAt;

    private Transaction(Builder builder) {
        this.id = builder.id;
        this.paymentId = builder.paymentId;
        this.merchantId = builder.merchantId;
        this.type = builder.type;
        this.amount = builder.amount;
        this.netAmount = builder.netAmount;
        this.currency = builder.currency;
        this.status = builder.status;
        this.gatewayTransactionId = builder.gatewayTransactionId;
        this.errorCode = builder.errorCode;
        this.errorMessage = builder.errorMessage;
        this.retryCount = builder.retryCount;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.processedAt = builder.processedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Transaction create(String paymentId, String merchantId, TransactionType type,
                                      Money amount, String currency) {
        return create(paymentId, merchantId, type, amount, currency, TransactionStatus.PENDING.name());
    }

    public static Transaction create(String paymentId, String merchantId, TransactionType type,
                                      Money amount, String currency, String status) {
        Instant now = Instant.now();
        return new Builder()
                .id(UUID.randomUUID().toString())
                .paymentId(paymentId)
                .merchantId(merchantId)
                .type(type)
                .amount(amount)
                .netAmount(calculateNetAmount(amount, type))
                .currency(currency)
                .status(TransactionStatus.fromString(status))
                .retryCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static Money calculateNetAmount(Money grossAmount, TransactionType type) {
        // Net amount calculation can include fees
        // For now, return the same as gross amount
        return grossAmount;
    }

    public void authorize() {
        validateStatusTransition(TransactionStatus.AUTHORIZED);
        this.status = TransactionStatus.AUTHORIZED;
        this.updatedAt = Instant.now();
        this.processedAt = this.updatedAt;
    }

    public void capture() {
        validateStatusTransition(TransactionStatus.CAPTURED);
        this.status = TransactionStatus.CAPTURED;
        this.updatedAt = Instant.now();
        this.processedAt = this.updatedAt;
    }

    public void settle() {
        validateStatusTransition(TransactionStatus.SETTLED);
        this.status = TransactionStatus.SETTLED;
        this.updatedAt = Instant.now();
        this.processedAt = this.updatedAt;
    }

    public void fail(String errorCode, String errorMessage) {
        validateStatusTransition(TransactionStatus.FAILED);
        this.status = TransactionStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.updatedAt = Instant.now();
        this.processedAt = this.updatedAt;
    }

    public void reverse() {
        validateStatusTransition(TransactionStatus.REVERSED);
        this.status = TransactionStatus.REVERSED;
        this.updatedAt = Instant.now();
        this.processedAt = this.updatedAt;
    }

    public void refund() {
        validateStatusTransition(TransactionStatus.REFUNDED);
        this.status = TransactionStatus.REFUNDED;
        this.updatedAt = Instant.now();
        this.processedAt = this.updatedAt;
    }

    public void partialRefund() {
        validateStatusTransition(TransactionStatus.PARTIALLY_REFUNDED);
        this.status = TransactionStatus.PARTIALLY_REFUNDED;
        this.updatedAt = Instant.now();
        this.processedAt = this.updatedAt;
    }

    public void incrementRetry() {
        this.retryCount = this.retryCount != null ? this.retryCount + 1 : 1;
        this.updatedAt = Instant.now();
    }

    public void updateGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
        this.updatedAt = Instant.now();
    }

    private void validateStatusTransition(TransactionStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition transaction from %s to %s", this.status, newStatus)
            );
        }
    }

    public boolean isPending() {
        return this.status.isPending();
    }

    public boolean isSuccessful() {
        return this.status.isSuccessful();
    }

    public boolean isTerminal() {
        return this.status.isTerminal();
    }

    public static class Builder {
        private String id;
        private String paymentId;
        private String merchantId;
        private TransactionType type;
        private Money amount;
        private Money netAmount;
        private String currency;
        private TransactionStatus status;
        private String gatewayTransactionId;
        private String errorCode;
        private String errorMessage;
        private Integer retryCount;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant processedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder paymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder amount(Money amount) {
            this.amount = amount;
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

        public Builder status(TransactionStatus status) {
            this.status = status;
            return this;
        }

        public Builder gatewayTransactionId(String gatewayTransactionId) {
            this.gatewayTransactionId = gatewayTransactionId;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder retryCount(Integer retryCount) {
            this.retryCount = retryCount;
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

        public Builder processedAt(Instant processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
    }
}
