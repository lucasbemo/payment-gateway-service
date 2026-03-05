package com.payment.gateway.domain.payment.service;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.payment.exception.DuplicatePaymentException;
import com.payment.gateway.domain.payment.model.*;
import com.payment.gateway.domain.payment.port.PaymentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentDomainService Tests")
class PaymentDomainServiceTest {

    @Mock
    private PaymentRepositoryPort paymentRepository;

    private PaymentDomainService paymentDomainService;

    private final String MERCHANT_ID = "merch_123";
    private final String CURRENCY = "BRL";
    private final Currency CURRENCY_USD = Currency.getInstance("BRL");
    private final String IDEMPOTENCY_KEY = "idem_abc123";
    private final String DESCRIPTION = "Test payment";

    @BeforeEach
    void setUp() {
        paymentDomainService = new PaymentDomainService(paymentRepository);
    }

    @Nested
    @DisplayName("Process Payment")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Should create payment successfully when data is valid")
        void shouldCreatePaymentSuccessfully() {
            // Given
            Money amount = Money.of(150000, CURRENCY_USD);
            PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
            Payment payment = Payment.create(
                MERCHANT_ID,
                amount,
                CURRENCY,
                paymentMethod,
                IDEMPOTENCY_KEY,
                DESCRIPTION,
                PaymentMetadata.empty(),
                List.of(),
                null
            );

            given(paymentRepository.existsByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(false);
            given(paymentRepository.save(any(Payment.class))).willReturn(payment);

            // When
            Payment result = paymentDomainService.processPayment(
                MERCHANT_ID,
                amount,
                CURRENCY,
                paymentMethod,
                IDEMPOTENCY_KEY,
                DESCRIPTION,
                PaymentMetadata.empty(),
                List.of(),
                null
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getMerchantId()).isEqualTo(MERCHANT_ID);
            assertThat(result.getAmount()).isEqualTo(amount);
            verify(paymentRepository).existsByIdempotencyKey(IDEMPOTENCY_KEY);
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should throw DuplicatePaymentException when idempotency key already exists")
        void shouldThrowDuplicatePaymentExceptionWhenIdempotencyKeyExists() {
            // Given
            Money amount = Money.of(150000, CURRENCY_USD);
            PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

            given(paymentRepository.existsByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> paymentDomainService.processPayment(
                MERCHANT_ID,
                amount,
                CURRENCY,
                paymentMethod,
                IDEMPOTENCY_KEY,
                DESCRIPTION,
                PaymentMetadata.empty(),
                List.of(),
                null
            ))
                .isInstanceOf(DuplicatePaymentException.class)
                .hasMessageContaining(IDEMPOTENCY_KEY);

            verify(paymentRepository).existsByIdempotencyKey(IDEMPOTENCY_KEY);
            verify(paymentRepository, never()).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("Get Payment")
    class GetPaymentTests {

        @Test
        @DisplayName("Should return payment when found by ID")
        void shouldReturnPaymentWhenFoundById() {
            // Given
            String paymentId = "pay_123";
            Payment payment = Payment.create(
                MERCHANT_ID,
                Money.of(150000, CURRENCY_USD),
                CURRENCY,
                PaymentMethod.CREDIT_CARD,
                IDEMPOTENCY_KEY,
                DESCRIPTION,
                PaymentMetadata.empty(),
                List.of(),
                null
            );

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

            // When
            Payment result = paymentDomainService.getPayment(paymentId);

            // Then
            assertThat(result).isEqualTo(payment);
            verify(paymentRepository).findById(paymentId);
        }

        @Test
        @DisplayName("Should throw exception when payment not found by ID")
        void shouldThrowExceptionWhenPaymentNotFoundById() {
            // Given
            String paymentId = "pay_nonexistent";
            given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentDomainService.getPayment(paymentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found");

            verify(paymentRepository).findById(paymentId);
        }

        @Test
        @DisplayName("Should return payment when found by idempotency key")
        void shouldReturnPaymentWhenFoundByIdempotencyKey() {
            // Given
            Payment payment = Payment.create(
                MERCHANT_ID,
                Money.of(150000, CURRENCY_USD),
                CURRENCY,
                PaymentMethod.CREDIT_CARD,
                IDEMPOTENCY_KEY,
                DESCRIPTION,
                PaymentMetadata.empty(),
                List.of(),
                null
            );

            given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.of(payment));

            // When
            Payment result = paymentDomainService.getPaymentByIdempotencyKey(IDEMPOTENCY_KEY);

            // Then
            assertThat(result).isEqualTo(payment);
            verify(paymentRepository).findByIdempotencyKey(IDEMPOTENCY_KEY);
        }

        @Test
        @DisplayName("Should throw exception when payment not found by idempotency key")
        void shouldThrowExceptionWhenPaymentNotFoundByIdempotencyKey() {
            // Given
            given(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentDomainService.getPaymentByIdempotencyKey(IDEMPOTENCY_KEY))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found for idempotency key");

            verify(paymentRepository).findByIdempotencyKey(IDEMPOTENCY_KEY);
        }
    }

    @Nested
    @DisplayName("Validate Payment Amount")
    class ValidatePaymentAmountTests {

        @Test
        @DisplayName("Should not throw when amount is valid")
        void shouldNotThrowWhenAmountIsValid() {
            // Given
            Money validAmount = Money.of(10000, CURRENCY_USD);

            // When & Then
            assertThatCode(() -> paymentDomainService.validatePaymentAmount(validAmount))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw when amount is null")
        void shouldThrowWhenAmountIsNull() {
            // When & Then
            assertThatThrownBy(() -> paymentDomainService.validatePaymentAmount(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment amount must be greater than zero");
        }

        @Test
        @DisplayName("Should throw when amount is zero")
        void shouldThrowWhenAmountIsZero() {
            // Given
            Money zeroAmount = Money.zero(CURRENCY_USD);

            // When & Then
            assertThatThrownBy(() -> paymentDomainService.validatePaymentAmount(zeroAmount))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment amount must be greater than zero");
        }
    }

    @Nested
    @DisplayName("Validate Currency")
    class ValidateCurrencyTests {

        @Test
        @DisplayName("Should not throw when currency code is valid")
        void shouldNotThrowWhenCurrencyCodeIsValid() {
            // Given
            String validCurrency = "USD";

            // When & Then
            assertThatCode(() -> paymentDomainService.validateCurrency(validCurrency))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw when currency is null")
        void shouldThrowWhenCurrencyIsNull() {
            // When & Then
            assertThatThrownBy(() -> paymentDomainService.validateCurrency(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid currency code");
        }

        @Test
        @DisplayName("Should throw when currency code length is invalid")
        void shouldThrowWhenCurrencyCodeLengthIsInvalid() {
            // Given
            String invalidCurrency = "US";

            // When & Then
            assertThatThrownBy(() -> paymentDomainService.validateCurrency(invalidCurrency))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid currency code");
        }
    }
}
