package com.payment.gateway.infrastructure.payment.adapter.out.persistence;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.payment.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    private PaymentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaymentMapper();
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map domain Payment to PaymentJpaEntity with all fields")
        void shouldMapDomainToEntity() {
            // given
            Money amount = Money.of(5000L, Currency.getInstance("USD"));
            Money unitPrice = Money.of(2500L, Currency.getInstance("USD"));
            Money itemTotal = Money.of(5000L, Currency.getInstance("USD"));

            PaymentItem item = new PaymentItem("Widget", 2, unitPrice, itemTotal);

            Payment payment = Payment.create(
                    "merchant-123",
                    amount,
                    "USD",
                    PaymentMethod.CREDIT_CARD,
                    "idem-key-001",
                    "Test payment",
                    PaymentMetadata.empty(),
                    List.of(item),
                    "customer-456"
            );

            // when
            PaymentJpaEntity entity = mapper.toEntity(payment);

            // then
            assertThat(entity.getId()).isEqualTo(payment.getId());
            assertThat(entity.getMerchantId()).isEqualTo("merchant-123");
            assertThat(entity.getCustomerId()).isEqualTo("customer-456");
            assertThat(entity.getPaymentMethodId()).isEqualTo("CREDIT_CARD");
            assertThat(entity.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
            assertThat(entity.getCurrency()).isEqualTo("USD");
            assertThat(entity.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(entity.getIdempotencyKey()).isEqualTo("idem-key-001");
            assertThat(entity.getDescription()).isEqualTo("Test payment");
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getUpdatedAt()).isNotNull();
            assertThat(entity.getItems()).hasSize(1);

            PaymentItemJpaEntity itemEntity = entity.getItems().get(0);
            assertThat(itemEntity.getDescription()).isEqualTo("Widget");
            assertThat(itemEntity.getQuantity()).isEqualTo(2);
            assertThat(itemEntity.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(2500));
            assertThat(itemEntity.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        }

        @Test
        @DisplayName("should handle null items list")
        void shouldHandleNullItems() {
            Money amount = Money.of(1000L, Currency.getInstance("USD"));

            Payment payment = Payment.create(
                    "merchant-123",
                    amount,
                    "USD",
                    PaymentMethod.DEBIT_CARD,
                    "idem-key-002",
                    "No items",
                    PaymentMetadata.empty(),
                    null,
                    null
            );

            PaymentJpaEntity entity = mapper.toEntity(payment);

            assertThat(entity.getMerchantId()).isEqualTo("merchant-123");
            assertThat(entity.getCustomerId()).isNull();
            // Payment.create wraps null items into an empty ArrayList,
            // so the entity will have an empty list rather than null
            assertThat(entity.getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("should map PaymentJpaEntity to domain Payment with all fields")
        void shouldMapEntityToDomain() {
            // given
            Instant now = Instant.now();
            PaymentItemJpaEntity itemEntity = PaymentItemJpaEntity.builder()
                    .description("Gadget")
                    .quantity(3)
                    .unitPrice(BigDecimal.valueOf(1000))
                    .total(BigDecimal.valueOf(3000))
                    .build();

            PaymentJpaEntity entity = PaymentJpaEntity.builder()
                    .id("pay-999")
                    .merchantId("merchant-abc")
                    .customerId("cust-xyz")
                    .paymentMethodId("CREDIT_CARD")
                    .amount(BigDecimal.valueOf(3000))
                    .currency("USD")
                    .status(PaymentStatus.PENDING)
                    .idempotencyKey("idem-key-100")
                    .description("Entity to domain test")
                    .createdAt(now)
                    .updatedAt(now)
                    .items(List.of(itemEntity))
                    .build();

            // when
            Payment payment = mapper.toDomain(entity);

            // then
            assertThat(payment.getId()).isEqualTo("pay-999");
            assertThat(payment.getMerchantId()).isEqualTo("merchant-abc");
            assertThat(payment.getCustomerId()).isEqualTo("cust-xyz");
            assertThat(payment.getPaymentMethodId()).isEqualTo("CREDIT_CARD");
            assertThat(payment.getAmount().getAmountInCents()).isEqualTo(3000L);
            assertThat(payment.getCurrency()).isEqualTo("USD");
            assertThat(payment.getStatus()).isEqualTo(com.payment.gateway.domain.payment.model.PaymentStatus.PENDING);
            assertThat(payment.getIdempotencyKey()).isEqualTo("idem-key-100");
            assertThat(payment.getDescription()).isEqualTo("Entity to domain test");
            assertThat(payment.getItems()).hasSize(1);

            PaymentItem item = payment.getItems().get(0);
            assertThat(item.getDescription()).isEqualTo("Gadget");
            assertThat(item.getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("should handle null items in entity")
        void shouldHandleNullItemsInEntity() {
            Instant now = Instant.now();
            PaymentJpaEntity entity = PaymentJpaEntity.builder()
                    .id("pay-111")
                    .merchantId("merchant-def")
                    .customerId(null)
                    .paymentMethodId(null)
                    .amount(BigDecimal.valueOf(500))
                    .currency("USD")
                    .status(PaymentStatus.PENDING)
                    .idempotencyKey("idem-key-200")
                    .description(null)
                    .createdAt(now)
                    .updatedAt(now)
                    .items(null)
                    .build();

            Payment payment = mapper.toDomain(entity);

            assertThat(payment.getId()).isEqualTo("pay-111");
            assertThat(payment.getMerchantId()).isEqualTo("merchant-def");
            assertThat(payment.getPaymentMethodId()).isEqualTo("CREDIT_CARD");
            // toDomain passes null items to Payment.create which wraps it in an empty ArrayList
            assertThat(payment.getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("round-trip")
    class RoundTrip {

        @Test
        @DisplayName("should preserve key data through toEntity then toDomain")
        void shouldPreserveDataThroughRoundTrip() {
            Money amount = Money.of(7500L, Currency.getInstance("USD"));
            Payment original = Payment.create(
                    "merchant-rt",
                    amount,
                    "USD",
                    PaymentMethod.PIX,
                    "idem-rt-001",
                    "Round trip test",
                    PaymentMetadata.empty(),
                    null,
                    "cust-rt"
            );

            PaymentJpaEntity entity = mapper.toEntity(original);
            Payment restored = mapper.toDomain(entity);

            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getMerchantId()).isEqualTo(original.getMerchantId());
            assertThat(restored.getCustomerId()).isEqualTo(original.getCustomerId());
            assertThat(restored.getCurrency()).isEqualTo(original.getCurrency());
            assertThat(restored.getIdempotencyKey()).isEqualTo(original.getIdempotencyKey());
            assertThat(restored.getDescription()).isEqualTo(original.getDescription());
        }
    }
}
