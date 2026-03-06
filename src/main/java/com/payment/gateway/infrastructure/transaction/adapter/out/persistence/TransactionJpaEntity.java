package com.payment.gateway.infrastructure.transaction.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "payment_id", nullable = false, length = 36)
    private String paymentId;

    @Column(name = "merchant_id", length = 36)
    private String merchantId;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "net_amount", precision = 19, scale = 4)
    private BigDecimal netAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "provider_transaction_id", length = 255)
    private String gatewayTransactionId;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    private TransactionJpaEntity(Builder builder) {
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

    public static class Builder {
        private String id;
        private String paymentId;
        private String merchantId;
        private String type;
        private BigDecimal amount;
        private BigDecimal netAmount;
        private String currency;
        private String status;
        private String gatewayTransactionId;
        private String errorCode;
        private String errorMessage;
        private Integer retryCount;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant processedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public Builder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder netAmount(BigDecimal netAmount) { this.netAmount = netAmount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder gatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; return this; }
        public Builder errorCode(String errorCode) { this.errorCode = errorCode; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder retryCount(Integer retryCount) { this.retryCount = retryCount; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder processedAt(Instant processedAt) { this.processedAt = processedAt; return this; }

        public TransactionJpaEntity build() {
            return new TransactionJpaEntity(this);
        }
    }
}
