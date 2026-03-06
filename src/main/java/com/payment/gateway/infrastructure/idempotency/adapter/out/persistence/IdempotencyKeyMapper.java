package com.payment.gateway.infrastructure.idempotency.adapter.out.persistence;

import com.payment.gateway.domain.idempotency.model.IdempotencyKey;
import com.payment.gateway.domain.idempotency.model.IdempotencyStatus;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyKeyMapper {

    public IdempotencyKeyJpaEntity toEntity(IdempotencyKey domain) {
        return IdempotencyKeyJpaEntity.builder()
                .id(domain.getId())
                .keyHash(domain.getIdempotencyKey())
                .merchantId(domain.getMerchantId())
                .status(domain.getStatus() != null ? domain.getStatus().name() : null)
                .requestMethod(domain.getOperation())
                .requestBody(domain.getRequestHash())
                .responseCode(domain.getResponseCode() != null ? parseResponseCode(domain.getResponseCode()) : null)
                .responseBody(domain.getResponseBody())
                .lockedUntil(domain.getExpiresAt())
                .lockedBy(domain.getLockToken())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public IdempotencyKey toDomain(IdempotencyKeyJpaEntity entity) {
        return IdempotencyKey.builder()
                .id(entity.getId())
                .idempotencyKey(entity.getKeyHash())
                .merchantId(entity.getMerchantId())
                .status(entity.getStatus() != null ? IdempotencyStatus.valueOf(entity.getStatus()) : null)
                .operation(entity.getRequestMethod())
                .requestHash(entity.getRequestBody())
                .responseCode(entity.getResponseCode() != null ? String.valueOf(entity.getResponseCode()) : null)
                .responseBody(entity.getResponseBody())
                .expiresAt(entity.getLockedUntil())
                .lockToken(entity.getLockedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Integer parseResponseCode(String responseCode) {
        try {
            return Integer.parseInt(responseCode);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
