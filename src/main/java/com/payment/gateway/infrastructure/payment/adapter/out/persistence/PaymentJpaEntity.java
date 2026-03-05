package com.payment.gateway.infrastructure.payment.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for Payment.
 * Maps the domain Payment model to the database.
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentJpaEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @Column(name = "customer_id", length = 64)
    private String customerId;

    @Column(name = "payment_method_id", length = 64)
    private String paymentMethodId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "idempotency_key", length = 128, unique = true)
    private String idempotencyKey;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "gateway_transaction_id", length = 128)
    private String gatewayTransactionId;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "authorized_at")
    private Instant authorizedAt;

    @Column(name = "captured_at")
    private Instant capturedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PaymentItemJpaEntity> items = new ArrayList<>();

    // Builder-style constructor
    private PaymentJpaEntity(Builder builder) {
        this.id = builder.id;
        this.merchantId = builder.merchantId;
        this.customerId = builder.customerId;
        this.paymentMethodId = builder.paymentMethodId;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.status = builder.status;
        this.idempotencyKey = builder.idempotencyKey;
        this.description = builder.description;
        this.gatewayTransactionId = builder.gatewayTransactionId;
        this.errorCode = builder.errorCode;
        this.errorMessage = builder.errorMessage;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.authorizedAt = builder.authorizedAt;
        this.capturedAt = builder.capturedAt;
        this.cancelledAt = builder.cancelledAt;
        this.failedAt = builder.failedAt;
        this.items = builder.items;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String merchantId;
        private String customerId;
        private String paymentMethodId;
        private BigDecimal amount;
        private String currency;
        private PaymentStatus status;
        private String idempotencyKey;
        private String description;
        private String gatewayTransactionId;
        private String errorCode;
        private String errorMessage;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant authorizedAt;
        private Instant capturedAt;
        private Instant cancelledAt;
        private Instant failedAt;
        private List<PaymentItemJpaEntity> items = new ArrayList<>();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder paymentMethodId(String paymentMethodId) {
            this.paymentMethodId = paymentMethodId;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
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

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder authorizedAt(Instant authorizedAt) {
            this.authorizedAt = authorizedAt;
            return this;
        }

        public Builder capturedAt(Instant capturedAt) {
            this.capturedAt = capturedAt;
            return this;
        }

        public Builder cancelledAt(Instant cancelledAt) {
            this.cancelledAt = cancelledAt;
            return this;
        }

        public Builder failedAt(Instant failedAt) {
            this.failedAt = failedAt;
            return this;
        }

        public Builder items(List<PaymentItemJpaEntity> items) {
            this.items = items;
            return this;
        }

        public PaymentJpaEntity build() {
            return new PaymentJpaEntity(this);
        }
    }
}
