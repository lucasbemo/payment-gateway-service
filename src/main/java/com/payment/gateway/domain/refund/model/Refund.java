package com.payment.gateway.domain.refund.model;

import com.payment.gateway.commons.model.Money;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

/**
 * Refund aggregate root.
 * Represents a refund operation within the payment gateway.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund {
    private String id;
    private String paymentId;
    private String transactionId;
    private String merchantId;
    private String refundIdempotencyKey;
    private RefundType type;
    private Money amount;
    private Money refundedAmount;
    private String currency;
    private RefundStatus status;
    private String reason;
    private String gatewayRefundId;
    private String errorCode;
    private String errorMessage;
    private List<RefundItem> items;
    private Integer retryCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant processedAt;

    private Refund(Builder builder) {
        this.id = builder.id;
        this.paymentId = builder.paymentId;
        this.transactionId = builder.transactionId;
        this.merchantId = builder.merchantId;
        this.refundIdempotencyKey = builder.refundIdempotencyKey;
        this.type = builder.type;
        this.amount = builder.amount;
        this.refundedAmount = builder.refundedAmount;
        this.currency = builder.currency;
        this.status = builder.status;
        this.reason = builder.reason;
        this.gatewayRefundId = builder.gatewayRefundId;
        this.errorCode = builder.errorCode;
        this.errorMessage = builder.errorMessage;
        this.items = builder.items;
        this.retryCount = builder.retryCount;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.processedAt = builder.processedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Refund create(String paymentId, String transactionId, String merchantId,
                                 RefundType type, Money amount, String currency,
                                 String refundIdempotencyKey, String reason) {
        Instant now = Instant.now();
        return new Builder()
                .id(UUID.randomUUID().toString())
                .paymentId(paymentId)
                .transactionId(transactionId)
                .merchantId(merchantId)
                .type(type)
                .amount(amount)
                .refundedAmount(Money.zero(Currency.getInstance(currency)))
                .currency(currency)
                .status(RefundStatus.PENDING)
                .refundIdempotencyKey(refundIdempotencyKey)
                .reason(reason)
                .retryCount(0)
                .createdAt(now)
                .updatedAt(now)
                .items(new ArrayList<>())
                .build();
    }

    public void approve() {
        validateStatusTransition(RefundStatus.APPROVED);
        this.status = RefundStatus.APPROVED;
        this.updatedAt = Instant.now();
    }

    public void reject(String rejectionReason) {
        validateStatusTransition(RefundStatus.REJECTED);
        this.status = RefundStatus.REJECTED;
        this.reason = rejectionReason != null ? rejectionReason : this.reason;
        this.updatedAt = Instant.now();
        this.processedAt = this.updatedAt;
    }

    public void complete() {
        validateStatusTransition(RefundStatus.COMPLETED);
        this.status = RefundStatus.COMPLETED;
        this.updatedAt = Instant.now();
        this.processedAt = this.updatedAt;
    }

    public void fail(String errorCode, String errorMessage) {
        validateStatusTransition(RefundStatus.FAILED);
        this.status = RefundStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.updatedAt = Instant.now();
        this.processedAt = this.updatedAt;
    }

    public void cancel(String cancellationReason) {
        validateStatusTransition(RefundStatus.CANCELLED);
        this.status = RefundStatus.CANCELLED;
        this.reason = cancellationReason != null ? cancellationReason : this.reason;
        this.updatedAt = Instant.now();
    }

    public void retry() {
        if (this.status != RefundStatus.FAILED) {
            throw new IllegalStateException("Can only retry failed refunds");
        }
        this.status = RefundStatus.PENDING;
        this.errorCode = null;
        this.errorMessage = null;
        this.retryCount = this.retryCount != null ? this.retryCount + 1 : 1;
        this.updatedAt = Instant.now();
    }

    public void updateGatewayRefundId(String gatewayRefundId) {
        this.gatewayRefundId = gatewayRefundId;
        this.updatedAt = Instant.now();
    }

    public void addItem(RefundItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        this.updatedAt = Instant.now();
    }

    public void addItems(List<RefundItem> items) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.addAll(items);
        this.updatedAt = Instant.now();
    }

    private void validateStatusTransition(RefundStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition refund from %s to %s", this.status, newStatus)
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
        private String transactionId;
        private String merchantId;
        private String refundIdempotencyKey;
        private RefundType type;
        private Money amount;
        private Money refundedAmount;
        private String currency;
        private RefundStatus status;
        private String reason;
        private String gatewayRefundId;
        private String errorCode;
        private String errorMessage;
        private List<RefundItem> items;
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

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder refundIdempotencyKey(String refundIdempotencyKey) {
            this.refundIdempotencyKey = refundIdempotencyKey;
            return this;
        }

        public Builder type(RefundType type) {
            this.type = type;
            return this;
        }

        public Builder amount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder refundedAmount(Money refundedAmount) {
            this.refundedAmount = refundedAmount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder status(RefundStatus status) {
            this.status = status;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder gatewayRefundId(String gatewayRefundId) {
            this.gatewayRefundId = gatewayRefundId;
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

        public Builder items(List<RefundItem> items) {
            this.items = items;
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

        public Refund build() {
            return new Refund(this);
        }
    }
}
