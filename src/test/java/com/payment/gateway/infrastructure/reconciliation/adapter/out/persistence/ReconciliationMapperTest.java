package com.payment.gateway.infrastructure.reconciliation.adapter.out.persistence;

import com.payment.gateway.domain.reconciliation.model.ReconciliationBatch;
import com.payment.gateway.domain.reconciliation.model.ReconciliationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReconciliationMapperTest {

    private ReconciliationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ReconciliationMapper();
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map domain ReconciliationBatch to JPA entity with all fields")
        void shouldMapDomainToEntity() {
            // given
            Instant now = Instant.now();
            Instant startedAt = now.minusSeconds(60);
            Instant completedAt = now;
            ReconciliationBatch batch = ReconciliationBatch.builder()
                    .id("recon-001")
                    .merchantId("merchant-001")
                    .reconciliationDate(LocalDate.of(2025, 6, 15))
                    .gatewayName("stripe")
                    .status(ReconciliationStatus.COMPLETED)
                    .totalTransactions(100)
                    .matchedTransactions(95)
                    .unmatchedTransactions(5)
                    .totalAmount(new BigDecimal("10000.00"))
                    .matchedAmount(new BigDecimal("9500.00"))
                    .unmatchedAmount(new BigDecimal("500.00"))
                    .discrepancyCount(3)
                    .initiatedBy("admin@system.com")
                    .startedAt(startedAt)
                    .completedAt(completedAt)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // when
            ReconciliationBatchJpaEntity entity = mapper.toEntity(batch);

            // then
            assertThat(entity.getId()).isEqualTo("recon-001");
            assertThat(entity.getMerchantId()).isEqualTo("merchant-001");
            assertThat(entity.getReconciliationDate()).isEqualTo(LocalDate.of(2025, 6, 15));
            assertThat(entity.getGatewayName()).isEqualTo("stripe");
            assertThat(entity.getStatus()).isEqualTo("COMPLETED");
            assertThat(entity.getTotalTransactions()).isEqualTo(100);
            assertThat(entity.getMatchedTransactions()).isEqualTo(95);
            assertThat(entity.getUnmatchedTransactions()).isEqualTo(5);
            assertThat(entity.getTotalAmount()).isEqualByComparingTo(new BigDecimal("10000.00"));
            assertThat(entity.getMatchedAmount()).isEqualByComparingTo(new BigDecimal("9500.00"));
            assertThat(entity.getDiscrepancyCount()).isEqualTo(3);
            assertThat(entity.getInitiatedBy()).isEqualTo("admin@system.com");
            assertThat(entity.getStartedAt()).isEqualTo(startedAt);
            assertThat(entity.getCompletedAt()).isEqualTo(completedAt);
            assertThat(entity.getCreatedAt()).isEqualTo(now);
            assertThat(entity.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should handle null status")
        void shouldHandleNullStatus() {
            ReconciliationBatch batch = ReconciliationBatch.builder()
                    .id("recon-null")
                    .merchantId("merchant-null")
                    .reconciliationDate(LocalDate.of(2025, 1, 1))
                    .status(null)
                    .createdAt(Instant.now())
                    .build();

            ReconciliationBatchJpaEntity entity = mapper.toEntity(batch);

            assertThat(entity.getStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("should map JPA entity to domain ReconciliationBatch with all fields")
        void shouldMapEntityToDomain() {
            // given
            Instant now = Instant.now();
            ReconciliationBatchJpaEntity entity = ReconciliationBatchJpaEntity.builder()
                    .id("recon-entity-001")
                    .merchantId("merchant-entity-001")
                    .reconciliationDate(LocalDate.of(2025, 3, 20))
                    .gatewayName("paypal")
                    .status("PROCESSING")
                    .totalTransactions(50)
                    .matchedTransactions(48)
                    .unmatchedTransactions(2)
                    .totalAmount(new BigDecimal("5000.00"))
                    .matchedAmount(new BigDecimal("4800.00"))
                    .discrepancyCount(1)
                    .initiatedBy("batch-job")
                    .startedAt(now.minusSeconds(120))
                    .completedAt(null)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // when
            ReconciliationBatch batch = mapper.toDomain(entity);

            // then
            assertThat(batch.getId()).isEqualTo("recon-entity-001");
            assertThat(batch.getMerchantId()).isEqualTo("merchant-entity-001");
            assertThat(batch.getReconciliationDate()).isEqualTo(LocalDate.of(2025, 3, 20));
            assertThat(batch.getGatewayName()).isEqualTo("paypal");
            assertThat(batch.getStatus()).isEqualTo(ReconciliationStatus.PROCESSING);
            assertThat(batch.getTotalTransactions()).isEqualTo(50);
            assertThat(batch.getMatchedTransactions()).isEqualTo(48);
            assertThat(batch.getUnmatchedTransactions()).isEqualTo(2);
            assertThat(batch.getTotalAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(batch.getMatchedAmount()).isEqualByComparingTo(new BigDecimal("4800.00"));
            // unmatchedAmount is always set to BigDecimal.ZERO in the mapper
            assertThat(batch.getUnmatchedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(batch.getDiscrepancyCount()).isEqualTo(1);
            assertThat(batch.getInitiatedBy()).isEqualTo("batch-job");
            assertThat(batch.getStartedAt()).isEqualTo(now.minusSeconds(120));
            assertThat(batch.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("should handle null totalAmount and matchedAmount with ZERO defaults")
        void shouldHandleNullAmountsWithZeroDefaults() {
            ReconciliationBatchJpaEntity entity = ReconciliationBatchJpaEntity.builder()
                    .id("recon-zero")
                    .merchantId("merchant-zero")
                    .reconciliationDate(LocalDate.of(2025, 1, 1))
                    .status("PENDING")
                    .totalAmount(null)
                    .matchedAmount(null)
                    .createdAt(Instant.now())
                    .build();

            ReconciliationBatch batch = mapper.toDomain(entity);

            assertThat(batch.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(batch.getMatchedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(batch.getUnmatchedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should handle null status in entity")
        void shouldHandleNullStatus() {
            ReconciliationBatchJpaEntity entity = ReconciliationBatchJpaEntity.builder()
                    .id("recon-null-status")
                    .merchantId("merchant-null-status")
                    .reconciliationDate(LocalDate.of(2025, 1, 1))
                    .status(null)
                    .createdAt(Instant.now())
                    .build();

            ReconciliationBatch batch = mapper.toDomain(entity);

            assertThat(batch.getStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("round-trip")
    class RoundTrip {

        @Test
        @DisplayName("should preserve key data through toEntity then toDomain")
        void shouldPreserveDataThroughRoundTrip() {
            ReconciliationBatch original = ReconciliationBatch.create(
                    "merchant-rt",
                    LocalDate.of(2025, 8, 10),
                    "stripe",
                    "admin-rt"
            );

            ReconciliationBatchJpaEntity entity = mapper.toEntity(original);
            ReconciliationBatch restored = mapper.toDomain(entity);

            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getMerchantId()).isEqualTo(original.getMerchantId());
            assertThat(restored.getReconciliationDate()).isEqualTo(original.getReconciliationDate());
            assertThat(restored.getGatewayName()).isEqualTo(original.getGatewayName());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getInitiatedBy()).isEqualTo(original.getInitiatedBy());
            assertThat(restored.getTotalTransactions()).isEqualTo(original.getTotalTransactions());
        }
    }
}
