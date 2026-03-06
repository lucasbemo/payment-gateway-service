package com.payment.gateway.infrastructure.transaction.adapter.out.persistence;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.transaction.model.Transaction;
import com.payment.gateway.domain.transaction.model.TransactionStatus;
import com.payment.gateway.domain.transaction.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperTest {

    private TransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransactionMapper();
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map domain Transaction to TransactionJpaEntity with all fields")
        void shouldMapDomainToEntity() {
            // given
            Instant now = Instant.now();
            Currency usd = Currency.getInstance("USD");
            Transaction transaction = Transaction.builder()
                    .id("txn-001")
                    .paymentId("pay-001")
                    .merchantId("merchant-001")
                    .type(TransactionType.PAYMENT)
                    .amount(Money.of(new BigDecimal("100.00"), usd))
                    .netAmount(Money.of(new BigDecimal("97.50"), usd))
                    .currency("USD")
                    .status(TransactionStatus.PENDING)
                    .gatewayTransactionId("gw-txn-001")
                    .errorCode(null)
                    .errorMessage(null)
                    .retryCount(0)
                    .createdAt(now)
                    .updatedAt(now)
                    .processedAt(null)
                    .build();

            // when
            TransactionJpaEntity entity = mapper.toEntity(transaction);

            // then
            assertThat(entity.getId()).isEqualTo("txn-001");
            assertThat(entity.getPaymentId()).isEqualTo("pay-001");
            assertThat(entity.getMerchantId()).isEqualTo("merchant-001");
            assertThat(entity.getType()).isEqualTo("PAYMENT");
            assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(entity.getNetAmount()).isEqualByComparingTo(new BigDecimal("97.50"));
            assertThat(entity.getCurrency()).isEqualTo("USD");
            assertThat(entity.getStatus()).isEqualTo("PENDING");
            assertThat(entity.getGatewayTransactionId()).isEqualTo("gw-txn-001");
            assertThat(entity.getErrorCode()).isNull();
            assertThat(entity.getErrorMessage()).isNull();
            assertThat(entity.getRetryCount()).isEqualTo(0);
            assertThat(entity.getCreatedAt()).isEqualTo(now);
            assertThat(entity.getUpdatedAt()).isEqualTo(now);
            assertThat(entity.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("should handle null type and status")
        void shouldHandleNullTypeAndStatus() {
            Transaction transaction = Transaction.builder()
                    .id("txn-null")
                    .paymentId("pay-null")
                    .type(null)
                    .amount(null)
                    .netAmount(null)
                    .currency("USD")
                    .status(null)
                    .createdAt(Instant.now())
                    .build();

            TransactionJpaEntity entity = mapper.toEntity(transaction);

            assertThat(entity.getType()).isNull();
            assertThat(entity.getStatus()).isNull();
            assertThat(entity.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(entity.getNetAmount()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("should map TransactionJpaEntity to domain Transaction with all fields")
        void shouldMapEntityToDomain() {
            // given
            Instant now = Instant.now();
            Instant processedAt = now.plusSeconds(30);
            TransactionJpaEntity entity = TransactionJpaEntity.builder()
                    .id("txn-entity-001")
                    .paymentId("pay-entity-001")
                    .merchantId("merchant-entity-001")
                    .type("AUTHORIZATION")
                    .amount(new BigDecimal("200.00"))
                    .netAmount(new BigDecimal("195.00"))
                    .currency("EUR")
                    .status("AUTHORIZED")
                    .gatewayTransactionId("gw-txn-entity-001")
                    .errorCode("E001")
                    .errorMessage("Minor warning")
                    .retryCount(1)
                    .createdAt(now)
                    .updatedAt(now)
                    .processedAt(processedAt)
                    .build();

            // when
            Transaction transaction = mapper.toDomain(entity);

            // then
            assertThat(transaction.getId()).isEqualTo("txn-entity-001");
            assertThat(transaction.getPaymentId()).isEqualTo("pay-entity-001");
            assertThat(transaction.getMerchantId()).isEqualTo("merchant-entity-001");
            assertThat(transaction.getType()).isEqualTo(TransactionType.AUTHORIZATION);
            assertThat(transaction.getAmount().getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(transaction.getNetAmount().getAmount()).isEqualByComparingTo(new BigDecimal("195.00"));
            assertThat(transaction.getCurrency()).isEqualTo("EUR");
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.AUTHORIZED);
            assertThat(transaction.getGatewayTransactionId()).isEqualTo("gw-txn-entity-001");
            assertThat(transaction.getErrorCode()).isEqualTo("E001");
            assertThat(transaction.getErrorMessage()).isEqualTo("Minor warning");
            assertThat(transaction.getRetryCount()).isEqualTo(1);
            assertThat(transaction.getProcessedAt()).isEqualTo(processedAt);
        }

        @Test
        @DisplayName("should handle null amount in entity with zero money")
        void shouldHandleNullAmountWithZero() {
            TransactionJpaEntity entity = TransactionJpaEntity.builder()
                    .id("txn-zero")
                    .paymentId("pay-zero")
                    .amount(null)
                    .netAmount(null)
                    .currency("USD")
                    .type("PAYMENT")
                    .status("PENDING")
                    .createdAt(Instant.now())
                    .build();

            Transaction transaction = mapper.toDomain(entity);

            assertThat(transaction.getAmount().isZero()).isTrue();
            assertThat(transaction.getNetAmount()).isNull();
        }

        @Test
        @DisplayName("should handle null type and status in entity")
        void shouldHandleNullTypeAndStatus() {
            TransactionJpaEntity entity = TransactionJpaEntity.builder()
                    .id("txn-nulls")
                    .paymentId("pay-nulls")
                    .amount(new BigDecimal("10.00"))
                    .currency("USD")
                    .type(null)
                    .status(null)
                    .createdAt(Instant.now())
                    .build();

            Transaction transaction = mapper.toDomain(entity);

            assertThat(transaction.getType()).isNull();
            assertThat(transaction.getStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("round-trip")
    class RoundTrip {

        @Test
        @DisplayName("should preserve key data through toEntity then toDomain")
        void shouldPreserveDataThroughRoundTrip() {
            Currency usd = Currency.getInstance("USD");
            Transaction original = Transaction.create(
                    "pay-rt", "merchant-rt",
                    TransactionType.CAPTURE,
                    Money.of(new BigDecimal("75.00"), usd),
                    "USD"
            );

            TransactionJpaEntity entity = mapper.toEntity(original);
            Transaction restored = mapper.toDomain(entity);

            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getPaymentId()).isEqualTo(original.getPaymentId());
            assertThat(restored.getMerchantId()).isEqualTo(original.getMerchantId());
            assertThat(restored.getType()).isEqualTo(original.getType());
            assertThat(restored.getCurrency()).isEqualTo(original.getCurrency());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getAmount().getAmount()).isEqualByComparingTo(original.getAmount().getAmount());
        }
    }
}
