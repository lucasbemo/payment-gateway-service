package com.payment.gateway.domain.idempotency.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * IdempotencyKey aggregate root.
 * Used to prevent duplicate requests.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyKey {
    private String id;
    private String idempotencyKey;
    private String merchantId;
    private String operation;
    private String requestHash;
    private IdempotencyStatus status;
    private String responseCode;
    private String errorMessage;
    private String responseBody;
    private Instant lockedAt;
    private String lockToken;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    private IdempotencyKey(Builder builder) {
        this.id = builder.id;
        this.idempotencyKey = builder.idempotencyKey;
        this.merchantId = builder.merchantId;
        this.operation = builder.operation;
        this.requestHash = builder.requestHash;
        this.status = builder.status;
        this.responseCode = builder.responseCode;
        this.errorMessage = builder.errorMessage;
        this.responseBody = builder.responseBody;
        this.lockedAt = builder.lockedAt;
        this.lockToken = builder.lockToken;
        this.expiresAt = builder.expiresAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.completedAt = builder.completedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static IdempotencyKey create(String idempotencyKey, String merchantId, String operation,
                                         String requestHash, int ttlSeconds) {
        Instant now = Instant.now();
        return new Builder()
                .id(java.util.UUID.randomUUID().toString())
                .idempotencyKey(idempotencyKey)
                .merchantId(merchantId)
                .operation(operation)
                .requestHash(requestHash)
                .status(IdempotencyStatus.PENDING)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void lock(String lockToken) {
        this.status = IdempotencyStatus.PROCESSING;
        this.lockedAt = Instant.now();
        this.lockToken = lockToken;
        this.updatedAt = Instant.now();
    }

    public void complete(String responseCode, String responseBody) {
        this.status = IdempotencyStatus.COMPLETED;
        this.responseCode = responseCode;
        this.responseBody = responseBody;
        this.completedAt = Instant.now();
        this.updatedAt = this.completedAt;
        this.lockToken = null;
    }

    public void fail(String errorMessage) {
        this.status = IdempotencyStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = Instant.now();
        this.lockToken = null;
    }

    public void expire() {
        this.status = IdempotencyStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    public void releaseLock(String token) {
        if (this.lockToken != null && this.lockToken.equals(token)) {
            this.status = IdempotencyStatus.PENDING;
            this.lockToken = null;
            this.lockedAt = null;
            this.updatedAt = Instant.now();
        }
    }

    public boolean isExpired() {
        return this.expiresAt != null && Instant.now().isAfter(this.expiresAt);
    }

    public boolean isLocked() {
        return this.lockToken != null;
    }

    public boolean isTerminal() {
        return this.status == IdempotencyStatus.COMPLETED ||
               this.status == IdempotencyStatus.FAILED ||
               this.status == IdempotencyStatus.EXPIRED;
    }

    public static class Builder {
        private String id;
        private String idempotencyKey;
        private String merchantId;
        private String operation;
        private String requestHash;
        private IdempotencyStatus status;
        private String responseCode;
        private String errorMessage;
        private String responseBody;
        private Instant lockedAt;
        private String lockToken;
        private Instant expiresAt;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant completedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }

        public Builder requestHash(String requestHash) {
            this.requestHash = requestHash;
            return this;
        }

        public Builder status(IdempotencyStatus status) {
            this.status = status;
            return this;
        }

        public Builder responseCode(String responseCode) {
            this.responseCode = responseCode;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder responseBody(String responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public Builder lockedAt(Instant lockedAt) {
            this.lockedAt = lockedAt;
            return this;
        }

        public Builder lockToken(String lockToken) {
            this.lockToken = lockToken;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
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

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public IdempotencyKey build() {
            return new IdempotencyKey(this);
        }
    }
}
