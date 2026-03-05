package com.payment.gateway.infrastructure.merchant.adapter.out.persistence;

import com.payment.gateway.domain.merchant.model.Merchant;
import org.springframework.stereotype.Component;

/**
 * Mapper between Merchant domain model and MerchantJpaEntity.
 */
@Component
public class MerchantMapper {

    public MerchantJpaEntity toEntity(Merchant merchant) {
        return MerchantJpaEntity.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .apiKeyHash(merchant.getApiKeyHash())
                .apiSecretHash(merchant.getApiSecretHash())
                .status(MerchantStatus.valueOf(merchant.getStatus().name()))
                .webhookUrl(merchant.getWebhookUrl())
                .webhookSecret(merchant.getWebhookSecret())
                .createdAt(merchant.getCreatedAt())
                .updatedAt(merchant.getUpdatedAt())
                .build();
    }

    public Merchant toDomain(MerchantJpaEntity entity) {
        Merchant merchant = Merchant.register(
                entity.getName(),
                entity.getEmail(),
                entity.getApiKeyHash(),
                entity.getApiSecretHash(),
                entity.getWebhookUrl(),
                null
        );
        // Set the ID using reflection since Merchant doesn't have a setter
        try {
            java.lang.reflect.Field idField = Merchant.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(merchant, entity.getId());

            java.lang.reflect.Field statusField = Merchant.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(merchant, com.payment.gateway.domain.merchant.model.MerchantStatus.valueOf(entity.getStatus().name()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set merchant fields", e);
        }
        return merchant;
    }
}
