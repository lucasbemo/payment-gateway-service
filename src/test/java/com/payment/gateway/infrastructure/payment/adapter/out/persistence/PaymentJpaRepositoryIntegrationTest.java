package com.payment.gateway.infrastructure.payment.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.payment.gateway.infrastructure.merchant.adapter.out.persistence.MerchantJpaEntity;
import com.payment.gateway.infrastructure.merchant.adapter.out.persistence.MerchantJpaRepository;
import com.payment.gateway.infrastructure.merchant.adapter.out.persistence.MerchantStatus;
import com.payment.gateway.test.TestAwsConfig;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("PaymentJpaRepository Integration Tests")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "spring.main.allow-bean-definition-override=true",
            "spring.main.web-application-type=none",
            "spring.main.lazy-initialization=true",
            // Share the container's schema with the Flyway-managed e2e contexts:
            // use migrations + validate instead of create-drop, so this context
            // never leaves a non-empty schema without a flyway_schema_history table.
            "spring.flyway.enabled=true",
            "spring.jpa.hibernate.ddl-auto=validate"
        })
@Import({TestAwsConfig.class, com.payment.gateway.test.MockPortsConfig.class})
@Transactional
@ActiveProfiles("test")
class PaymentJpaRepositoryIntegrationTest extends com.payment.gateway.test.ContainerConfig {

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @Autowired
    private MerchantJpaRepository merchantJpaRepository;

    // The Flyway schema enforces fk_payments_merchant, so every merchant_id
    // used by a payment fixture must exist in the merchants table. Inserts
    // roll back with the test transaction.
    @BeforeEach
    void createDefaultMerchant() {
        createMerchant("merchant-123");
    }

    private void createMerchant(String merchantId) {
        merchantJpaRepository.save(MerchantJpaEntity.builder()
                .id(merchantId)
                .name("Test Merchant " + merchantId)
                .email(merchantId + "@test.example")
                .apiKey("key-" + merchantId)
                .apiKeyHash("key-hash-" + merchantId)
                .apiSecretHash("secret-hash-" + merchantId)
                .status(MerchantStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
    }

    @Nested
    @DisplayName("Save Payment Tests")
    class SavePaymentTests {

        @Test
        @DisplayName("Should save payment successfully")
        void shouldSavePaymentSuccessfully() {
            PaymentJpaEntity paymentEntity = createPaymentEntity();
            PaymentJpaEntity saved = paymentJpaRepository.save(paymentEntity);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getMerchantId()).isEqualTo("merchant-123");
            assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(saved.getCurrency()).isEqualTo("USD");
            assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("Find Payment Tests")
    class FindPaymentTests {

        @Test
        @DisplayName("Should find payment by ID")
        void shouldFindPaymentById() {
            PaymentJpaEntity savedPayment = paymentJpaRepository.save(createPaymentEntity());
            Optional<PaymentJpaEntity> found = paymentJpaRepository.findById(savedPayment.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getMerchantId()).isEqualTo("merchant-123");
        }

        @Test
        @DisplayName("Should return empty when payment not found")
        void shouldReturnEmptyWhenPaymentNotFound() {
            Optional<PaymentJpaEntity> found = paymentJpaRepository.findById("non-existent-id");
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should find payment by idempotency key")
        void shouldFindPaymentByIdempotencyKey() {
            String idempotencyKey = "idem-key-" + UUID.randomUUID();
            PaymentJpaEntity paymentEntity = createPaymentEntityForMerchant("merchant-123", idempotencyKey);
            PaymentJpaEntity saved = paymentJpaRepository.save(paymentEntity);

            Optional<PaymentJpaEntity> found = paymentJpaRepository.findByIdempotencyKey(idempotencyKey);

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
        }

        @Test
        @DisplayName("Should find payments by merchant ID")
        void shouldFindPaymentsByMerchantId() {
            String uniqueMerchantId = "merchant-test-" + shortUuid();
            createMerchant(uniqueMerchantId);
            PaymentJpaEntity payment1 = paymentJpaRepository.save(
                    createPaymentEntityForMerchant(uniqueMerchantId, "idem-key-1-" + UUID.randomUUID()));
            PaymentJpaEntity payment2 = paymentJpaRepository.save(
                    createPaymentEntityForMerchant(uniqueMerchantId, "idem-key-2-" + UUID.randomUUID()));

            List<PaymentJpaEntity> payments = paymentJpaRepository.findByMerchantId(uniqueMerchantId);

            assertThat(payments).hasSize(2);
            assertThat(payments)
                    .extracting(PaymentJpaEntity::getId)
                    .containsExactlyInAnyOrder(payment1.getId(), payment2.getId());
        }
    }

    @Nested
    @DisplayName("Update Payment Tests")
    class UpdatePaymentTests {

        @Test
        @DisplayName("Should update payment status")
        void shouldUpdatePaymentStatus() {
            PaymentJpaEntity saved = paymentJpaRepository.save(createPaymentEntity());
            PaymentJpaEntity updated = PaymentJpaEntity.builder()
                    .id(saved.getId())
                    .merchantId(saved.getMerchantId())
                    .amount(saved.getAmount())
                    .currency(saved.getCurrency())
                    .status(PaymentStatus.AUTHORIZED)
                    .idempotencyKey(saved.getIdempotencyKey())
                    .description(saved.getDescription())
                    .createdAt(saved.getCreatedAt())
                    .updatedAt(Instant.now())
                    .build();

            paymentJpaRepository.save(updated);

            Optional<PaymentJpaEntity> fetched = paymentJpaRepository.findById(saved.getId());
            assertThat(fetched).isPresent();
            assertThat(fetched.get().getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        }

        @Test
        @DisplayName("Should update gateway transaction ID")
        void shouldUpdateGatewayTransactionId() {
            PaymentJpaEntity saved = paymentJpaRepository.save(createPaymentEntity());
            PaymentJpaEntity updated = PaymentJpaEntity.builder()
                    .id(saved.getId())
                    .merchantId(saved.getMerchantId())
                    .amount(saved.getAmount())
                    .currency(saved.getCurrency())
                    .status(saved.getStatus())
                    .idempotencyKey(saved.getIdempotencyKey())
                    .description(saved.getDescription())
                    .gatewayTransactionId("gateway-txn-xyz")
                    .createdAt(saved.getCreatedAt())
                    .updatedAt(Instant.now())
                    .build();

            paymentJpaRepository.save(updated);

            Optional<PaymentJpaEntity> fetched = paymentJpaRepository.findById(saved.getId());
            assertThat(fetched).isPresent();
            assertThat(fetched.get().getGatewayTransactionId()).isEqualTo("gateway-txn-xyz");
        }
    }

    @Nested
    @DisplayName("Exists By Idempotency Key Tests")
    class ExistsByIdempotencyKeyTests {

        @Test
        @DisplayName("Should return true when payment exists")
        void shouldReturnTrueWhenPaymentExists() {
            String idempotencyKey = "idem-key-exists-" + UUID.randomUUID();
            PaymentJpaEntity paymentEntity = createPaymentEntityForMerchant("merchant-123", idempotencyKey);
            paymentJpaRepository.save(paymentEntity);

            boolean exists = paymentJpaRepository.existsByIdempotencyKey(idempotencyKey);

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when payment does not exist")
        void shouldReturnFalseWhenPaymentNotExists() {
            boolean exists = paymentJpaRepository.existsByIdempotencyKey("non-existent-key");
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Payment with Items Tests")
    class PaymentWithItemsTests {

        @Test
        @DisplayName("Should save payment with items")
        void shouldSavePaymentWithItems() {
            String paymentId = "pay-with-items-" + shortUuid();
            PaymentJpaEntity paymentEntity = PaymentJpaEntity.builder()
                    .id(paymentId)
                    .merchantId("merchant-123")
                    .amount(new BigDecimal("100.00"))
                    .currency("USD")
                    .idempotencyKey("idem-key-items-" + UUID.randomUUID())
                    .status(PaymentStatus.PENDING)
                    .description("Payment with items")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            PaymentJpaEntity saved = paymentJpaRepository.save(paymentEntity);

            PaymentItemJpaEntity item1 = PaymentItemJpaEntity.builder()
                    .payment(saved)
                    .description("Product 1")
                    .quantity(2)
                    .unitPrice(new BigDecimal("50.00"))
                    .total(new BigDecimal("100.00"))
                    .build();

            saved.getItems().add(item1);
            paymentJpaRepository.save(saved);

            Optional<PaymentJpaEntity> fetched = paymentJpaRepository.findById(paymentId);
            assertThat(fetched).isPresent();
            assertThat(fetched.get().getItems()).hasSize(1);
            assertThat(fetched.get().getItems().get(0).getDescription()).isEqualTo("Product 1");
        }

        @Test
        @DisplayName("Should update payment items")
        void shouldUpdatePaymentItems() {
            PaymentJpaEntity saved = paymentJpaRepository.save(createPaymentEntity());

            PaymentItemJpaEntity newItem = PaymentItemJpaEntity.builder()
                    .payment(saved)
                    .description("New Product")
                    .quantity(1)
                    .unitPrice(new BigDecimal("25.00"))
                    .total(new BigDecimal("25.00"))
                    .build();

            saved.getItems().add(newItem);
            paymentJpaRepository.save(saved);

            Optional<PaymentJpaEntity> fetched = paymentJpaRepository.findById(saved.getId());
            assertThat(fetched).isPresent();
            assertThat(fetched.get().getItems()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Payment Timestamp Tests")
    class PaymentTimestampTests {

        @Test
        @DisplayName("Should save payment with lifecycle timestamps")
        void shouldSavePaymentWithLifecycleTimestamps() {
            Instant now = Instant.now();
            PaymentJpaEntity paymentEntity = PaymentJpaEntity.builder()
                    .id("pay-timestamp-" + shortUuid())
                    .merchantId("merchant-123")
                    .amount(new BigDecimal("100.00"))
                    .currency("USD")
                    .idempotencyKey("idem-key-ts-" + UUID.randomUUID())
                    .status(PaymentStatus.AUTHORIZED)
                    .description("Payment with timestamps")
                    .createdAt(now)
                    .updatedAt(now)
                    .authorizedAt(now.plusSeconds(1))
                    .capturedAt(now.plusSeconds(2))
                    .build();

            PaymentJpaEntity saved = paymentJpaRepository.save(paymentEntity);

            assertThat(saved.getAuthorizedAt()).isAfterOrEqualTo(now);
            assertThat(saved.getCapturedAt()).isAfterOrEqualTo(saved.getAuthorizedAt());
        }

        @Test
        @DisplayName("Should update payment with error timestamps")
        void shouldUpdatePaymentWithErrorTimestamps() {
            PaymentJpaEntity saved = paymentJpaRepository.save(createPaymentEntity());

            PaymentJpaEntity updated = PaymentJpaEntity.builder()
                    .id(saved.getId())
                    .merchantId(saved.getMerchantId())
                    .amount(saved.getAmount())
                    .currency(saved.getCurrency())
                    .status(PaymentStatus.FAILED)
                    .idempotencyKey(saved.getIdempotencyKey())
                    .description(saved.getDescription())
                    .createdAt(saved.getCreatedAt())
                    .updatedAt(Instant.now())
                    .errorCode("PAYMENT_DECLINED")
                    .errorMessage("Insufficient funds")
                    .failedAt(Instant.now())
                    .build();

            paymentJpaRepository.save(updated);

            Optional<PaymentJpaEntity> fetched = paymentJpaRepository.findById(saved.getId());
            assertThat(fetched).isPresent();
            assertThat(fetched.get().getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(fetched.get().getFailedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Payment Customer and Payment Method Tests")
    class PaymentCustomerAndPaymentMethodTests {

        @Test
        @DisplayName("Should save payment with customer and payment method")
        void shouldSavePaymentWithCustomerAndPaymentMethod() {
            PaymentJpaEntity paymentEntity = PaymentJpaEntity.builder()
                    .id("pay-customer-" + shortUuid())
                    .merchantId("merchant-123")
                    .customerId("customer-456")
                    .paymentMethodId("pm-789")
                    .amount(new BigDecimal("100.00"))
                    .currency("USD")
                    .idempotencyKey("idem-key-customer-" + UUID.randomUUID())
                    .status(PaymentStatus.PENDING)
                    .description("Payment with customer")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            PaymentJpaEntity saved = paymentJpaRepository.save(paymentEntity);

            assertThat(saved.getCustomerId()).isEqualTo("customer-456");
            assertThat(saved.getPaymentMethodId()).isEqualTo("pm-789");
        }

        @Test
        @DisplayName("Should find payments by customer")
        void shouldFindPaymentsByCustomer() {
            // Note: This test documents expected behavior
            // A query method findByCustomerId would need to be added to the repository
            String customerId = "customer-test-" + shortUuid();
            PaymentJpaEntity payment1 =
                    paymentJpaRepository.save(createPaymentWithCustomer(customerId, "idem-1-" + UUID.randomUUID()));
            PaymentJpaEntity payment2 =
                    paymentJpaRepository.save(createPaymentWithCustomer(customerId, "idem-2-" + UUID.randomUUID()));

            // Currently no findByCustomerId method, this test documents the need
            assertThat(payment1.getCustomerId()).isEqualTo(customerId);
            assertThat(payment2.getCustomerId()).isEqualTo(customerId);
        }
    }

    @Nested
    @DisplayName("Payment Status Query Tests")
    class PaymentStatusQueryTests {

        @Test
        @DisplayName("Should find pending payments")
        void shouldFindPendingPayments() {
            String merchantId = "merchant-pending-" + shortUuid();
            createMerchant(merchantId);
            paymentJpaRepository.save(createPaymentEntityForMerchant(merchantId, "idem-1-" + UUID.randomUUID()));
            paymentJpaRepository.save(createPaymentEntityForMerchant(merchantId, "idem-2-" + UUID.randomUUID()));

            List<PaymentJpaEntity> allPayments = paymentJpaRepository.findByMerchantId(merchantId);
            assertThat(allPayments).hasSize(2);
            assertThat(allPayments).allMatch(p -> p.getStatus() == PaymentStatus.PENDING);
        }
    }

    // ID columns (id, merchant_id, customer_id) are VARCHAR(36) in the Flyway
    // schema; a prefixed full UUID overflows them, so use an 8-char suffix.
    private static String shortUuid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private PaymentJpaEntity createPaymentWithCustomer(String customerId, String idempotencyKey) {
        return PaymentJpaEntity.builder()
                .id("pay-cust-" + shortUuid())
                .merchantId("merchant-123")
                .customerId(customerId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey(idempotencyKey)
                .status(PaymentStatus.PENDING)
                .description("Test payment with customer")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private PaymentJpaEntity createPaymentEntity() {
        return createPaymentEntityForMerchant("merchant-123", "idem-key-" + UUID.randomUUID());
    }

    private PaymentJpaEntity createPaymentEntityForMerchant(String merchantId, String idempotencyKey) {
        return PaymentJpaEntity.builder()
                .id("pay-" + shortUuid())
                .merchantId(merchantId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey(idempotencyKey)
                .status(PaymentStatus.PENDING)
                .description("Test payment")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
