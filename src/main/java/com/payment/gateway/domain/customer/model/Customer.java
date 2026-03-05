package com.payment.gateway.domain.customer.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Customer aggregate root.
 * Represents a customer within the payment gateway.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {
    private String id;
    private String merchantId;
    private String email;
    private String name;
    private String phone;
    private String externalId;
    private String defaultPaymentMethodId;
    private List<PaymentMethod> paymentMethods;
    private CustomerStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private Customer(Builder builder) {
        this.id = builder.id;
        this.merchantId = builder.merchantId;
        this.email = builder.email;
        this.name = builder.name;
        this.phone = builder.phone;
        this.externalId = builder.externalId;
        this.defaultPaymentMethodId = builder.defaultPaymentMethodId;
        this.paymentMethods = builder.paymentMethods;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Customer create(String merchantId, String email, String name) {
        Instant now = Instant.now();
        return new Builder()
                .id(UUID.randomUUID().toString())
                .merchantId(merchantId)
                .email(email)
                .name(name)
                .status(CustomerStatus.ACTIVE)
                .paymentMethods(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void addPaymentMethod(PaymentMethod paymentMethod) {
        if (this.paymentMethods == null) {
            this.paymentMethods = new ArrayList<>();
        }

        // If this is marked as default, update the default payment method id
        if (Boolean.TRUE.equals(paymentMethod.getIsDefault())) {
            this.defaultPaymentMethodId = paymentMethod.getId();
        }

        this.paymentMethods.add(paymentMethod);
        this.updatedAt = Instant.now();
    }

    public void removePaymentMethod(String paymentMethodId) {
        if (this.paymentMethods != null) {
            this.paymentMethods.removeIf(pm -> pm.getId().equals(paymentMethodId));
        }

        // If the removed payment method was the default, clear it
        if (paymentMethodId.equals(this.defaultPaymentMethodId)) {
            this.defaultPaymentMethodId = null;
        }

        this.updatedAt = Instant.now();
    }

    public PaymentMethod getPaymentMethod(String paymentMethodId) {
        if (this.paymentMethods == null) {
            return null;
        }
        return this.paymentMethods.stream()
                .filter(pm -> pm.getId().equals(paymentMethodId))
                .findFirst()
                .orElse(null);
    }

    public void setDefaultPaymentMethod(String paymentMethodId) {
        PaymentMethod pm = getPaymentMethod(paymentMethodId);
        if (pm == null) {
            throw new IllegalArgumentException("Payment method not found: " + paymentMethodId);
        }

        this.defaultPaymentMethodId = paymentMethodId;
        pm.markAsDefault();
        this.updatedAt = Instant.now();
    }

    public void updateEmail(String email) {
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public void updatePhone(String phone) {
        this.phone = phone;
        this.updatedAt = Instant.now();
    }

    public void updateName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void updateExternalId(String externalId) {
        this.externalId = externalId;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = CustomerStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = CustomerStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public void suspend() {
        this.status = CustomerStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return this.status == CustomerStatus.ACTIVE;
    }

    public static class Builder {
        private String id;
        private String merchantId;
        private String email;
        private String name;
        private String phone;
        private String externalId;
        private String defaultPaymentMethodId;
        private List<PaymentMethod> paymentMethods;
        private CustomerStatus status;
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

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder externalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder defaultPaymentMethodId(String defaultPaymentMethodId) {
            this.defaultPaymentMethodId = defaultPaymentMethodId;
            return this;
        }

        public Builder paymentMethods(List<PaymentMethod> paymentMethods) {
            this.paymentMethods = paymentMethods;
            return this;
        }

        public Builder status(CustomerStatus status) {
            this.status = status;
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

        public Customer build() {
            return new Customer(this);
        }
    }
}
