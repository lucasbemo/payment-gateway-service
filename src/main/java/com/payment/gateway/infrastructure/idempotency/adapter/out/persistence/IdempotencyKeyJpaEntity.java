package com.payment.gateway.infrastructure.idempotency.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyKeyJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "key_hash", nullable = false, length = 255)
    private String keyHash;

    @Column(name = "merchant_id", nullable = false, length = 36)
    private String merchantId;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_path", length = 512)
    private String requestPath;

    @Column(name = "request_headers", columnDefinition = "jsonb")
    private String requestHeaders;

    @Column(name = "request_body", columnDefinition = "jsonb")
    private String requestBody;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "response_body", columnDefinition = "jsonb")
    private String responseBody;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "locked_by", length = 36)
    private String lockedBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    private IdempotencyKeyJpaEntity(Builder builder) {
        this.id = builder.id;
        this.keyHash = builder.keyHash;
        this.merchantId = builder.merchantId;
        this.status = builder.status;
        this.requestMethod = builder.requestMethod;
        this.requestPath = builder.requestPath;
        this.requestHeaders = builder.requestHeaders;
        this.requestBody = builder.requestBody;
        this.responseCode = builder.responseCode;
        this.responseBody = builder.responseBody;
        this.lockedUntil = builder.lockedUntil;
        this.lockedBy = builder.lockedBy;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String keyHash;
        private String merchantId;
        private String status;
        private String requestMethod;
        private String requestPath;
        private String requestHeaders;
        private String requestBody;
        private Integer responseCode;
        private String responseBody;
        private Instant lockedUntil;
        private String lockedBy;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder keyHash(String keyHash) { this.keyHash = keyHash; return this; }
        public Builder merchantId(String merchantId) { this.merchantId = merchantId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder requestMethod(String requestMethod) { this.requestMethod = requestMethod; return this; }
        public Builder requestPath(String requestPath) { this.requestPath = requestPath; return this; }
        public Builder requestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; return this; }
        public Builder requestBody(String requestBody) { this.requestBody = requestBody; return this; }
        public Builder responseCode(Integer responseCode) { this.responseCode = responseCode; return this; }
        public Builder responseBody(String responseBody) { this.responseBody = responseBody; return this; }
        public Builder lockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; return this; }
        public Builder lockedBy(String lockedBy) { this.lockedBy = lockedBy; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public IdempotencyKeyJpaEntity build() {
            return new IdempotencyKeyJpaEntity(this);
        }
    }
}
