package com.payment.gateway.infrastructure.idempotency.adapter.out.persistence;

import com.payment.gateway.domain.idempotency.model.IdempotencyKey;
import com.payment.gateway.domain.idempotency.model.IdempotencyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyKeyMapperTest {

    private IdempotencyKeyMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IdempotencyKeyMapper();
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map domain IdempotencyKey to IdempotencyKeyJpaEntity with all fields")
        void shouldMapDomainToEntity() {
            // given
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(3600);
            IdempotencyKey domain = IdempotencyKey.builder()
                    .id("idem-001")
                    .idempotencyKey("key-hash-abc")
                    .merchantId("merchant-001")
                    .status(IdempotencyStatus.COMPLETED)
                    .operation("POST")
                    .requestHash("{\"amount\":100}")
                    .responseCode("200")
                    .responseBody("{\"status\":\"ok\"}")
                    .expiresAt(expiresAt)
                    .lockToken("lock-token-xyz")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // when
            IdempotencyKeyJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo("idem-001");
            assertThat(entity.getKeyHash()).isEqualTo("key-hash-abc");
            assertThat(entity.getMerchantId()).isEqualTo("merchant-001");
            assertThat(entity.getStatus()).isEqualTo("COMPLETED");
            assertThat(entity.getRequestMethod()).isEqualTo("POST");
            assertThat(entity.getRequestBody()).isEqualTo("{\"amount\":100}");
            assertThat(entity.getResponseCode()).isEqualTo(200);
            assertThat(entity.getResponseBody()).isEqualTo("{\"status\":\"ok\"}");
            assertThat(entity.getLockedUntil()).isEqualTo(expiresAt);
            assertThat(entity.getLockedBy()).isEqualTo("lock-token-xyz");
            assertThat(entity.getCreatedAt()).isEqualTo(now);
            assertThat(entity.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should handle null responseCode")
        void shouldHandleNullResponseCode() {
            IdempotencyKey domain = IdempotencyKey.builder()
                    .id("idem-null")
                    .idempotencyKey("key-null")
                    .merchantId("merchant-null")
                    .status(IdempotencyStatus.PENDING)
                    .operation("GET")
                    .responseCode(null)
                    .createdAt(Instant.now())
                    .build();

            IdempotencyKeyJpaEntity entity = mapper.toEntity(domain);

            assertThat(entity.getResponseCode()).isNull();
        }

        @Test
        @DisplayName("should handle null status")
        void shouldHandleNullStatus() {
            IdempotencyKey domain = IdempotencyKey.builder()
                    .id("idem-no-status")
                    .idempotencyKey("key-no-status")
                    .merchantId("merchant-no-status")
                    .status(null)
                    .createdAt(Instant.now())
                    .build();

            IdempotencyKeyJpaEntity entity = mapper.toEntity(domain);

            assertThat(entity.getStatus()).isNull();
        }

        @Test
        @DisplayName("should parse non-numeric responseCode as null")
        void shouldParseNonNumericResponseCodeAsNull() {
            IdempotencyKey domain = IdempotencyKey.builder()
                    .id("idem-bad-code")
                    .idempotencyKey("key-bad")
                    .merchantId("merchant-bad")
                    .status(IdempotencyStatus.FAILED)
                    .responseCode("not-a-number")
                    .createdAt(Instant.now())
                    .build();

            IdempotencyKeyJpaEntity entity = mapper.toEntity(domain);

            assertThat(entity.getResponseCode()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("should map IdempotencyKeyJpaEntity to domain IdempotencyKey with all fields")
        void shouldMapEntityToDomain() {
            // given
            Instant now = Instant.now();
            Instant lockedUntil = now.plusSeconds(300);
            IdempotencyKeyJpaEntity entity = IdempotencyKeyJpaEntity.builder()
                    .id("idem-entity-001")
                    .keyHash("entity-key-hash")
                    .merchantId("merchant-entity-001")
                    .status("PROCESSING")
                    .requestMethod("PUT")
                    .requestBody("{\"update\":true}")
                    .responseCode(201)
                    .responseBody("{\"id\":\"new-001\"}")
                    .lockedUntil(lockedUntil)
                    .lockedBy("lock-entity-token")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // when
            IdempotencyKey domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getId()).isEqualTo("idem-entity-001");
            assertThat(domain.getIdempotencyKey()).isEqualTo("entity-key-hash");
            assertThat(domain.getMerchantId()).isEqualTo("merchant-entity-001");
            assertThat(domain.getStatus()).isEqualTo(IdempotencyStatus.PROCESSING);
            assertThat(domain.getOperation()).isEqualTo("PUT");
            assertThat(domain.getRequestHash()).isEqualTo("{\"update\":true}");
            assertThat(domain.getResponseCode()).isEqualTo("201");
            assertThat(domain.getResponseBody()).isEqualTo("{\"id\":\"new-001\"}");
            assertThat(domain.getExpiresAt()).isEqualTo(lockedUntil);
            assertThat(domain.getLockToken()).isEqualTo("lock-entity-token");
            assertThat(domain.getCreatedAt()).isEqualTo(now);
            assertThat(domain.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should handle null status in entity")
        void shouldHandleNullStatus() {
            IdempotencyKeyJpaEntity entity = IdempotencyKeyJpaEntity.builder()
                    .id("idem-null-status")
                    .keyHash("key-null")
                    .merchantId("merchant-null")
                    .status(null)
                    .createdAt(Instant.now())
                    .build();

            IdempotencyKey domain = mapper.toDomain(entity);

            assertThat(domain.getStatus()).isNull();
        }

        @Test
        @DisplayName("should handle null responseCode in entity")
        void shouldHandleNullResponseCode() {
            IdempotencyKeyJpaEntity entity = IdempotencyKeyJpaEntity.builder()
                    .id("idem-null-rc")
                    .keyHash("key-null-rc")
                    .merchantId("merchant-null-rc")
                    .status("PENDING")
                    .responseCode(null)
                    .createdAt(Instant.now())
                    .build();

            IdempotencyKey domain = mapper.toDomain(entity);

            assertThat(domain.getResponseCode()).isNull();
        }
    }

    @Nested
    @DisplayName("round-trip")
    class RoundTrip {

        @Test
        @DisplayName("should preserve key data through toEntity then toDomain")
        void shouldPreserveDataThroughRoundTrip() {
            IdempotencyKey original = IdempotencyKey.create(
                    "idem-rt-key", "merchant-rt", "POST",
                    "request-hash-rt", 3600
            );

            IdempotencyKeyJpaEntity entity = mapper.toEntity(original);
            IdempotencyKey restored = mapper.toDomain(entity);

            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getIdempotencyKey()).isEqualTo(original.getIdempotencyKey());
            assertThat(restored.getMerchantId()).isEqualTo(original.getMerchantId());
            assertThat(restored.getOperation()).isEqualTo(original.getOperation());
            assertThat(restored.getRequestHash()).isEqualTo(original.getRequestHash());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        }
    }
}
