package com.payment.gateway.infrastructure.merchant.adapter.out.persistence;

import com.payment.gateway.domain.merchant.model.Merchant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantMapperTest {

    private MerchantMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MerchantMapper();
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map domain Merchant to MerchantJpaEntity with all fields")
        void shouldMapDomainToEntity() {
            // given
            Merchant merchant = Merchant.register(
                    "Acme Corp",
                    "acme@example.com",
                    "api-key-hash-123",
                    "api-secret-hash-456",
                    "https://acme.com/webhook",
                    null
            );

            // when
            MerchantJpaEntity entity = mapper.toEntity(merchant);

            // then
            assertThat(entity.getId()).isEqualTo(merchant.getId());
            assertThat(entity.getName()).isEqualTo("Acme Corp");
            assertThat(entity.getEmail()).isEqualTo("acme@example.com");
            assertThat(entity.getApiKeyHash()).isEqualTo("api-key-hash-123");
            assertThat(entity.getApiSecretHash()).isEqualTo("api-secret-hash-456");
            assertThat(entity.getStatus()).isEqualTo(MerchantStatus.PENDING);
            assertThat(entity.getWebhookUrl()).isEqualTo("https://acme.com/webhook");
            assertThat(entity.getWebhookSecret()).isEqualTo(merchant.getWebhookSecret());
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should map activated merchant with ACTIVE status")
        void shouldMapActivatedMerchant() {
            Merchant merchant = Merchant.register(
                    "Active Corp",
                    "active@example.com",
                    "key-hash",
                    "secret-hash",
                    null,
                    null
            );
            merchant.activate();

            MerchantJpaEntity entity = mapper.toEntity(merchant);

            assertThat(entity.getStatus()).isEqualTo(MerchantStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("should map MerchantJpaEntity to domain Merchant with all fields")
        void shouldMapEntityToDomain() {
            // given
            Instant now = Instant.now();
            MerchantJpaEntity entity = MerchantJpaEntity.builder()
                    .id("merchant-id-999")
                    .name("Test Merchant")
                    .email("test@merchant.com")
                    .apiKeyHash("hash-key-abc")
                    .apiSecretHash("hash-secret-def")
                    .status(MerchantStatus.ACTIVE)
                    .webhookUrl("https://test.com/hook")
                    .webhookSecret("whsec_test123")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // when
            Merchant merchant = mapper.toDomain(entity);

            // then
            assertThat(merchant.getId()).isEqualTo("merchant-id-999");
            assertThat(merchant.getName()).isEqualTo("Test Merchant");
            assertThat(merchant.getEmail()).isEqualTo("test@merchant.com");
            assertThat(merchant.getApiKeyHash()).isEqualTo("hash-key-abc");
            assertThat(merchant.getApiSecretHash()).isEqualTo("hash-secret-def");
            assertThat(merchant.getStatus()).isEqualTo(com.payment.gateway.domain.merchant.model.MerchantStatus.ACTIVE);
            assertThat(merchant.getWebhookUrl()).isEqualTo("https://test.com/hook");
        }

        @Test
        @DisplayName("should map entity with SUSPENDED status to domain")
        void shouldMapSuspendedStatusToDomain() {
            Instant now = Instant.now();
            MerchantJpaEntity entity = MerchantJpaEntity.builder()
                    .id("merchant-suspended")
                    .name("Suspended Merchant")
                    .email("suspended@merchant.com")
                    .apiKeyHash("key-hash")
                    .apiSecretHash("secret-hash")
                    .status(MerchantStatus.SUSPENDED)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            Merchant merchant = mapper.toDomain(entity);

            assertThat(merchant.getStatus()).isEqualTo(com.payment.gateway.domain.merchant.model.MerchantStatus.SUSPENDED);
        }
    }

    @Nested
    @DisplayName("round-trip")
    class RoundTrip {

        @Test
        @DisplayName("should preserve key data through toEntity then toDomain")
        void shouldPreserveDataThroughRoundTrip() {
            Merchant original = Merchant.register(
                    "RoundTrip Corp",
                    "rt@example.com",
                    "rt-key-hash",
                    "rt-secret-hash",
                    "https://rt.com/webhook",
                    null
            );

            MerchantJpaEntity entity = mapper.toEntity(original);
            Merchant restored = mapper.toDomain(entity);

            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getName()).isEqualTo(original.getName());
            assertThat(restored.getEmail()).isEqualTo(original.getEmail());
            assertThat(restored.getApiKeyHash()).isEqualTo(original.getApiKeyHash());
            assertThat(restored.getApiSecretHash()).isEqualTo(original.getApiSecretHash());
            assertThat(restored.getWebhookUrl()).isEqualTo(original.getWebhookUrl());
        }
    }
}
