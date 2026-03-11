package com.payment.gateway.infrastructure.customer.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA Entity for Payment Method.
 */
@Entity
@Table(name = "payment_methods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentMethodJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "merchant_id", nullable = false, length = 36)
    private String merchantId;

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private PaymentMethodType type;

    @Column(name = "token", nullable = false, length = 255, unique = true)
    private String token;

    @Column(name = "status", nullable = false, length = 50)
    private PaymentMethodStatus status;

    @Column(name = "expiry_month", length = 2)
    private String expiryMonth;

    @Column(name = "expiry_year", length = 4)
    private String expiryYear;

    @Column(name = "last_four", length = 4)
    private String lastFour;

    @Column(name = "brand", length = 50)
    private String brand;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Builder-style constructor
    private PaymentMethodJpaEntity(Builder builder) {
        this.id = builder.id;
        this.customerId = builder.customerId;
        this.merchantId = builder.merchantId;
        this.type = builder.type;
        this.token = builder.token;
        this.status = builder.status;
        this.expiryMonth = builder.expiryMonth;
        this.expiryYear = builder.expiryYear;
        this.lastFour = builder.lastFour;
        this.brand = builder.brand;
        this.isDefault = builder.isDefault;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String customerId;
        private String merchantId;
        private PaymentMethodType type;
        private String token;
        private PaymentMethodStatus status;
        private String expiryMonth;
        private String expiryYear;
        private String lastFour;
        private String brand;
        private boolean isDefault;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder type(PaymentMethodType type) {
            this.type = type;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder status(PaymentMethodStatus status) {
            this.status = status;
            return this;
        }

        public Builder expiryMonth(String expiryMonth) {
            this.expiryMonth = expiryMonth;
            return this;
        }

        public Builder expiryYear(String expiryYear) {
            this.expiryYear = expiryYear;
            return this;
        }

        public Builder lastFour(String lastFour) {
            this.lastFour = lastFour;
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
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

        public PaymentMethodJpaEntity build() {
            return new PaymentMethodJpaEntity(this);
        }
    }
}
