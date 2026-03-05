package com.payment.gateway.infrastructure.merchant.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA Entity for Merchant.
 */
@Entity
@Table(name = "merchants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MerchantJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "api_key_hash", length = 255)
    private String apiKeyHash;

    @Column(name = "api_secret_hash", length = 255)
    private String apiSecretHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private MerchantStatus status;

    @Column(name = "webhook_url", length = 512)
    private String webhookUrl;

    @Column(name = "webhook_secret", length = 255)
    private String webhookSecret;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Builder-style constructor
    private MerchantJpaEntity(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.email = builder.email;
        this.apiKeyHash = builder.apiKeyHash;
        this.apiSecretHash = builder.apiSecretHash;
        this.status = builder.status;
        this.webhookUrl = builder.webhookUrl;
        this.webhookSecret = builder.webhookSecret;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String email;
        private String apiKeyHash;
        private String apiSecretHash;
        private MerchantStatus status;
        private String webhookUrl;
        private String webhookSecret;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder apiKeyHash(String apiKeyHash) {
            this.apiKeyHash = apiKeyHash;
            return this;
        }

        public Builder apiSecretHash(String apiSecretHash) {
            this.apiSecretHash = apiSecretHash;
            return this;
        }

        public Builder status(MerchantStatus status) {
            this.status = status;
            return this;
        }

        public Builder webhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
            return this;
        }

        public Builder webhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
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

        public MerchantJpaEntity build() {
            return new MerchantJpaEntity(this);
        }
    }
}
