package com.payment.gateway.domain.customer.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * PaymentMethod value object representing a customer's payment method.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentMethod {
    private String id;
    private String customerId;
    private PaymentMethodType type;
    private PaymentMethodStatus status;
    private String token;
    private String gatewayPaymentMethodId;
    private CardDetails cardDetails;
    private String bankAccountLast4;
    private String bankAccountRoutingNumberLast4;
    private String digitalWalletProvider;
    private String billingAddressId;
    private Boolean isDefault;
    private Integer usageCount;
    private Instant lastUsedAt;
    private Instant createdAt;
    private Instant updatedAt;

    private PaymentMethod(Builder builder) {
        this.id = builder.id;
        this.customerId = builder.customerId;
        this.type = builder.type;
        this.status = builder.status;
        this.token = builder.token;
        this.gatewayPaymentMethodId = builder.gatewayPaymentMethodId;
        this.cardDetails = builder.cardDetails;
        this.bankAccountLast4 = builder.bankAccountLast4;
        this.bankAccountRoutingNumberLast4 = builder.bankAccountRoutingNumberLast4;
        this.digitalWalletProvider = builder.digitalWalletProvider;
        this.billingAddressId = builder.billingAddressId;
        this.isDefault = builder.isDefault;
        this.usageCount = builder.usageCount;
        this.lastUsedAt = builder.lastUsedAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static PaymentMethod createCard(String customerId, CardDetails cardDetails, String token) {
        Instant now = Instant.now();
        return new Builder()
                .id(UUID.randomUUID().toString())
                .customerId(customerId)
                .type(PaymentMethodType.CREDIT_CARD)
                .status(PaymentMethodStatus.PENDING_VERIFICATION)
                .token(token)
                .cardDetails(cardDetails)
                .isDefault(false)
                .usageCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static PaymentMethod createBankAccount(String customerId, String bankAccountLast4,
                                                   String routingNumberLast4, String token) {
        Instant now = Instant.now();
        return new Builder()
                .id(UUID.randomUUID().toString())
                .customerId(customerId)
                .type(PaymentMethodType.BANK_ACCOUNT)
                .status(PaymentMethodStatus.PENDING_VERIFICATION)
                .token(token)
                .bankAccountLast4(bankAccountLast4)
                .bankAccountRoutingNumberLast4(routingNumberLast4)
                .isDefault(false)
                .usageCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static PaymentMethod createDigitalWallet(String customerId, String provider, String token) {
        Instant now = Instant.now();
        return new Builder()
                .id(UUID.randomUUID().toString())
                .customerId(customerId)
                .type(PaymentMethodType.DIGITAL_WALLET)
                .status(PaymentMethodStatus.VERIFIED)
                .token(token)
                .digitalWalletProvider(provider)
                .isDefault(false)
                .usageCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void verify() {
        this.status = PaymentMethodStatus.VERIFIED;
        this.updatedAt = Instant.now();
    }

    public void failVerification() {
        this.status = PaymentMethodStatus.FAILED_VERIFICATION;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = PaymentMethodStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = PaymentMethodStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public void revoke() {
        this.status = PaymentMethodStatus.REVOKED;
        this.updatedAt = Instant.now();
    }

    public void markAsExpired() {
        this.status = PaymentMethodStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    public void markAsDefault() {
        this.isDefault = true;
        this.updatedAt = Instant.now();
    }

    public void incrementUsage() {
        this.usageCount = this.usageCount != null ? this.usageCount + 1 : 1;
        this.lastUsedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateGatewayPaymentMethodId(String gatewayPaymentMethodId) {
        this.gatewayPaymentMethodId = gatewayPaymentMethodId;
        this.updatedAt = Instant.now();
    }

    public boolean isCard() {
        return this.type == PaymentMethodType.CREDIT_CARD || this.type == PaymentMethodType.DEBIT_CARD;
    }

    public boolean isActive() {
        return this.status == PaymentMethodStatus.ACTIVE || this.status == PaymentMethodStatus.VERIFIED;
    }

    public static class Builder {
        private String id;
        private String customerId;
        private PaymentMethodType type;
        private PaymentMethodStatus status;
        private String token;
        private String gatewayPaymentMethodId;
        private CardDetails cardDetails;
        private String bankAccountLast4;
        private String bankAccountRoutingNumberLast4;
        private String digitalWalletProvider;
        private String billingAddressId;
        private Boolean isDefault;
        private Integer usageCount;
        private Instant lastUsedAt;
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

        public Builder type(PaymentMethodType type) {
            this.type = type;
            return this;
        }

        public Builder status(PaymentMethodStatus status) {
            this.status = status;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder gatewayPaymentMethodId(String gatewayPaymentMethodId) {
            this.gatewayPaymentMethodId = gatewayPaymentMethodId;
            return this;
        }

        public Builder cardDetails(CardDetails cardDetails) {
            this.cardDetails = cardDetails;
            return this;
        }

        public Builder bankAccountLast4(String bankAccountLast4) {
            this.bankAccountLast4 = bankAccountLast4;
            return this;
        }

        public Builder bankAccountRoutingNumberLast4(String bankAccountRoutingNumberLast4) {
            this.bankAccountRoutingNumberLast4 = bankAccountRoutingNumberLast4;
            return this;
        }

        public Builder digitalWalletProvider(String digitalWalletProvider) {
            this.digitalWalletProvider = digitalWalletProvider;
            return this;
        }

        public Builder billingAddressId(String billingAddressId) {
            this.billingAddressId = billingAddressId;
            return this;
        }

        public Builder isDefault(Boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public Builder usageCount(Integer usageCount) {
            this.usageCount = usageCount;
            return this;
        }

        public Builder lastUsedAt(Instant lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
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

        public PaymentMethod build() {
            return new PaymentMethod(this);
        }
    }
}
