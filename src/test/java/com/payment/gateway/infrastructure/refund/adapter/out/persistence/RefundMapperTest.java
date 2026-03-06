package com.payment.gateway.infrastructure.refund.adapter.out.persistence;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.refund.model.Refund;
import com.payment.gateway.domain.refund.model.RefundStatus;
import com.payment.gateway.domain.refund.model.RefundType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class RefundMapperTest {

    private RefundMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RefundMapper();
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map domain Refund to RefundJpaEntity with all fields")
        void shouldMapDomainToEntity() {
            // given
            Instant now = Instant.now();
            Currency usd = Currency.getInstance("USD");
            Refund refund = Refund.builder()
                    .id("refund-001")
                    .paymentId("pay-001")
                    .transactionId("txn-001")
                    .merchantId("merchant-001")
                    .refundIdempotencyKey("refund-idem-001")
                    .type(RefundType.FULL)
                    .amount(Money.of(new BigDecimal("50.00"), usd))
                    .refundedAmount(Money.of(new BigDecimal("25.00"), usd))
                    .currency("USD")
                    .status(RefundStatus.PENDING)
                    .reason("Customer request")
                    .gatewayRefundId("gw-refund-001")
                    .errorCode(null)
                    .errorMessage(null)
                    .items(new ArrayList<>())
                    .retryCount(0)
                    .createdAt(now)
                    .updatedAt(now)
                    .processedAt(null)
                    .build();

            // when
            RefundJpaEntity entity = mapper.toEntity(refund);

            // then
            assertThat(entity.getId()).isEqualTo("refund-001");
            assertThat(entity.getPaymentId()).isEqualTo("pay-001");
            assertThat(entity.getTransactionId()).isEqualTo("txn-001");
            assertThat(entity.getMerchantId()).isEqualTo("merchant-001");
            assertThat(entity.getRefundIdempotencyKey()).isEqualTo("refund-idem-001");
            assertThat(entity.getType()).isEqualTo("FULL");
            assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(entity.getRefundedAmount()).isEqualByComparingTo(new BigDecimal("25.00"));
            assertThat(entity.getCurrency()).isEqualTo("USD");
            assertThat(entity.getStatus()).isEqualTo("PENDING");
            assertThat(entity.getReason()).isEqualTo("Customer request");
            assertThat(entity.getGatewayRefundId()).isEqualTo("gw-refund-001");
            assertThat(entity.getRetryCount()).isEqualTo(0);
            assertThat(entity.getCreatedAt()).isEqualTo(now);
            assertThat(entity.getUpdatedAt()).isEqualTo(now);
            assertThat(entity.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("should handle null type and status")
        void shouldHandleNullTypeAndStatus() {
            Refund refund = Refund.builder()
                    .id("refund-null")
                    .paymentId("pay-null")
                    .type(null)
                    .amount(null)
                    .refundedAmount(null)
                    .currency("USD")
                    .status(null)
                    .createdAt(Instant.now())
                    .build();

            RefundJpaEntity entity = mapper.toEntity(refund);

            assertThat(entity.getType()).isNull();
            assertThat(entity.getStatus()).isNull();
            assertThat(entity.getAmount()).isNull();
            assertThat(entity.getRefundedAmount()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("should map RefundJpaEntity to domain Refund with all fields")
        void shouldMapEntityToDomain() {
            // given
            Instant now = Instant.now();
            Instant processedAt = now.plusSeconds(60);
            RefundJpaEntity entity = RefundJpaEntity.builder()
                    .id("refund-entity-001")
                    .paymentId("pay-entity-001")
                    .transactionId("txn-entity-001")
                    .merchantId("merchant-entity-001")
                    .refundIdempotencyKey("refund-idem-entity-001")
                    .type("PARTIAL")
                    .amount(new BigDecimal("30.00"))
                    .refundedAmount(new BigDecimal("10.00"))
                    .currency("USD")
                    .status("COMPLETED")
                    .reason("Partial return")
                    .gatewayRefundId("gw-refund-entity-001")
                    .errorCode("ERR01")
                    .errorMessage("Some error")
                    .retryCount(2)
                    .createdAt(now)
                    .updatedAt(now)
                    .processedAt(processedAt)
                    .build();

            // when
            Refund refund = mapper.toDomain(entity);

            // then
            assertThat(refund.getId()).isEqualTo("refund-entity-001");
            assertThat(refund.getPaymentId()).isEqualTo("pay-entity-001");
            assertThat(refund.getTransactionId()).isEqualTo("txn-entity-001");
            assertThat(refund.getMerchantId()).isEqualTo("merchant-entity-001");
            assertThat(refund.getRefundIdempotencyKey()).isEqualTo("refund-idem-entity-001");
            assertThat(refund.getType()).isEqualTo(RefundType.PARTIAL);
            assertThat(refund.getAmount().getAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
            assertThat(refund.getRefundedAmount().getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
            assertThat(refund.getCurrency()).isEqualTo("USD");
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.COMPLETED);
            assertThat(refund.getReason()).isEqualTo("Partial return");
            assertThat(refund.getGatewayRefundId()).isEqualTo("gw-refund-entity-001");
            assertThat(refund.getErrorCode()).isEqualTo("ERR01");
            assertThat(refund.getErrorMessage()).isEqualTo("Some error");
            assertThat(refund.getRetryCount()).isEqualTo(2);
            assertThat(refund.getItems()).isNotNull().isEmpty();
            assertThat(refund.getProcessedAt()).isEqualTo(processedAt);
        }

        @Test
        @DisplayName("should handle null amount in entity with zero money")
        void shouldHandleNullAmountWithZero() {
            RefundJpaEntity entity = RefundJpaEntity.builder()
                    .id("refund-zero")
                    .paymentId("pay-zero")
                    .amount(null)
                    .refundedAmount(null)
                    .currency("EUR")
                    .status("PENDING")
                    .type("FULL")
                    .createdAt(Instant.now())
                    .build();

            Refund refund = mapper.toDomain(entity);

            assertThat(refund.getAmount().isZero()).isTrue();
            assertThat(refund.getRefundedAmount().isZero()).isTrue();
        }

        @Test
        @DisplayName("should handle null type and status in entity")
        void shouldHandleNullTypeAndStatus() {
            RefundJpaEntity entity = RefundJpaEntity.builder()
                    .id("refund-null")
                    .paymentId("pay-null")
                    .amount(new BigDecimal("10.00"))
                    .currency("USD")
                    .type(null)
                    .status(null)
                    .createdAt(Instant.now())
                    .build();

            Refund refund = mapper.toDomain(entity);

            assertThat(refund.getType()).isNull();
            assertThat(refund.getStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("round-trip")
    class RoundTrip {

        @Test
        @DisplayName("should preserve key data through toEntity then toDomain")
        void shouldPreserveDataThroughRoundTrip() {
            Currency usd = Currency.getInstance("USD");
            Refund original = Refund.create(
                    "pay-rt", "txn-rt", "merchant-rt",
                    RefundType.FULL, Money.of(new BigDecimal("100.00"), usd),
                    "USD", "idem-rt", "Round trip reason"
            );

            RefundJpaEntity entity = mapper.toEntity(original);
            Refund restored = mapper.toDomain(entity);

            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getPaymentId()).isEqualTo(original.getPaymentId());
            assertThat(restored.getMerchantId()).isEqualTo(original.getMerchantId());
            assertThat(restored.getType()).isEqualTo(original.getType());
            assertThat(restored.getCurrency()).isEqualTo(original.getCurrency());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getReason()).isEqualTo(original.getReason());
        }
    }
}
