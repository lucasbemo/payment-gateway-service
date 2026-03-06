package com.payment.gateway.application.payment.service;

import com.payment.gateway.application.payment.dto.PaymentResponse;
import com.payment.gateway.application.payment.dto.ProcessPaymentCommand;
import com.payment.gateway.application.payment.port.out.CustomerQueryPort;
import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import com.payment.gateway.application.payment.port.out.MerchantQueryPort;
import com.payment.gateway.application.payment.port.out.PaymentQueryPort;
import com.payment.gateway.application.payment.port.out.TokenizationServicePort;
import com.payment.gateway.application.payment.port.out.TransactionCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.model.Money;
import com.payment.gateway.commons.utils.IdGenerator;
import com.payment.gateway.domain.customer.model.Customer;
import com.payment.gateway.domain.merchant.model.Merchant;
import com.payment.gateway.domain.merchant.model.MerchantConfiguration;
import com.payment.gateway.domain.merchant.model.MerchantStatus;
import com.payment.gateway.domain.payment.model.Payment;
import com.payment.gateway.domain.payment.model.PaymentMetadata;
import com.payment.gateway.domain.payment.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@DisplayName("Process Payment Service Tests")
@ExtendWith(MockitoExtension.class)
class ProcessPaymentServiceTest {

    @Mock
    private PaymentQueryPort paymentQueryPort;

    @Mock
    private MerchantQueryPort merchantQueryPort;

    @Mock
    private CustomerQueryPort customerQueryPort;

    @Mock
    private ExternalPaymentProviderPort externalPaymentProviderPort;

    @Mock
    private TokenizationServicePort tokenizationServicePort;

    @Mock
    private TransactionCommandPort transactionCommandPort;

    @Mock
    private IdGenerator idGenerator;

    private ProcessPaymentService processPaymentService;

    @BeforeEach
    void setUp() {
        processPaymentService = new ProcessPaymentService(
                paymentQueryPort,
                merchantQueryPort,
                customerQueryPort,
                externalPaymentProviderPort,
                tokenizationServicePort,
                transactionCommandPort,
                idGenerator
        );
    }

    @Nested
    @DisplayName("Successful Payment Processing")
    class SuccessfulPaymentProcessingTests {

        @Test
        @DisplayName("Should process payment successfully with valid card")
        void shouldProcessPaymentSuccessfully() {
            // Given
            String merchantId = "merchant-123";
            String idempotencyKey = "idem-key-456";
            String token = "tok_card_xyz";
            String paymentId = "pay_abc123";

            Merchant merchant = createMerchant(merchantId);
            ProcessPaymentCommand command = createPaymentCommand(merchantId, idempotencyKey);

            given(merchantQueryPort.findById(merchantId)).willReturn(Optional.of(merchant));
            given(paymentQueryPort.findByIdempotencyKey(idempotencyKey)).willReturn(Optional.empty());
            given(tokenizationServicePort.tokenize(any(), any(), any(), any())).willReturn(token);
            given(paymentQueryPort.savePayment(any(Payment.class))).willAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                setId(payment, paymentId);
                return payment;
            });
            given(externalPaymentProviderPort.authorize(any())).willReturn(
                    CompletableFuture.completedFuture(new ExternalPaymentProviderPort.PaymentProviderResult(true, "txn_xyz", null, null))
            );

            // When
            PaymentResponse response = processPaymentService.processPayment(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(paymentId);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED.name());
            assertThat(response.getAmount()).isEqualTo(command.getAmount());
            assertThat(response.getCurrency()).isEqualTo(command.getCurrency());

            then(paymentQueryPort).should(times(2)).savePayment(any(Payment.class));
            then(externalPaymentProviderPort).should().authorize(any());
            then(transactionCommandPort).should().createTransaction(any());
        }

        @Test
        @DisplayName("Should return existing payment for duplicate idempotency key")
        void shouldReturnExistingPaymentForDuplicateIdempotencyKey() {
            // Given
            String merchantId = "merchant-123";
            String idempotencyKey = "idem-key-456";
            String existingPaymentId = "pay_existing";

            Merchant merchant = createMerchant(merchantId);
            Payment existingPayment = createPayment(existingPaymentId, merchantId, idempotencyKey);
            ProcessPaymentCommand command = createPaymentCommand(merchantId, idempotencyKey);

            given(merchantQueryPort.findById(merchantId)).willReturn(Optional.of(merchant));
            given(paymentQueryPort.findByIdempotencyKey(idempotencyKey)).willReturn(Optional.of(existingPayment));

            // When
            PaymentResponse response = processPaymentService.processPayment(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(existingPaymentId);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING.name());

            then(paymentQueryPort).should(never()).savePayment(any());
            then(externalPaymentProviderPort).should(never()).authorize(any());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when merchant not found")
        void shouldThrowExceptionWhenMerchantNotFound() {
            // Given
            String merchantId = "invalid-merchant";
            ProcessPaymentCommand command = createPaymentCommand(merchantId, "idem-key");

            given(merchantQueryPort.findById(merchantId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> processPaymentService.processPayment(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Merchant not found");
        }

        @Test
        @DisplayName("Should throw exception when card tokenization fails")
        void shouldThrowExceptionWhenTokenizationFails() {
            // Given
            String merchantId = "merchant-123";
            Merchant merchant = createMerchant(merchantId);
            ProcessPaymentCommand command = createPaymentCommand(merchantId, "idem-key");

            given(merchantQueryPort.findById(merchantId)).willReturn(Optional.of(merchant));
            given(paymentQueryPort.findByIdempotencyKey(any())).willReturn(Optional.empty());
            given(tokenizationServicePort.tokenize(any(), any(), any(), any()))
                    .willThrow(new BusinessException("Tokenization failed"));

            // When & Then
            assertThatThrownBy(() -> processPaymentService.processPayment(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Tokenization failed");
        }

        @Test
        @DisplayName("Should throw exception when payment authorization fails")
        void shouldThrowExceptionWhenAuthorizationFails() {
            // Given
            String merchantId = "merchant-123";
            String idempotencyKey = "idem-key";
            String token = "tok_xyz";

            Merchant merchant = createMerchant(merchantId);
            ProcessPaymentCommand command = createPaymentCommand(merchantId, idempotencyKey);

            given(merchantQueryPort.findById(merchantId)).willReturn(Optional.of(merchant));
            given(paymentQueryPort.findByIdempotencyKey(idempotencyKey)).willReturn(Optional.empty());
            given(tokenizationServicePort.tokenize(any(), any(), any(), any())).willReturn(token);
            given(paymentQueryPort.savePayment(any(Payment.class))).willAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                setId(payment, "pay_123");
                return payment;
            });
            given(externalPaymentProviderPort.authorize(any())).willReturn(
                    CompletableFuture.completedFuture(new ExternalPaymentProviderPort.PaymentProviderResult(false, null, "ERR_001", "Authorization declined"))
            );

            // When & Then
            assertThatThrownBy(() -> processPaymentService.processPayment(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Authorization declined");
        }
    }

    // Helper methods

    private Merchant createMerchant(String id) {
        Merchant merchant = Merchant.register(
                "Test Merchant",
                "test@merchant.com",
                "test-api-key",
                "hashed_key",
                "hashed_secret",
                "https://webhook.example.com",
                MerchantConfiguration.empty()
        );
        setId(merchant, id);
        setStatus(merchant, MerchantStatus.ACTIVE);
        return merchant;
    }

    private Payment createPayment(String id, String merchantId, String idempotencyKey) {
        Payment payment = Payment.create(
                merchantId,
                Money.of(10000L, Currency.getInstance("USD")),
                "USD",
                null,
                idempotencyKey,
                "Test payment",
                PaymentMetadata.empty(),
                List.of(),
                null
        );
        setId(payment, id);
        return payment;
    }

    private ProcessPaymentCommand createPaymentCommand(String merchantId, String idempotencyKey) {
        return ProcessPaymentCommand.builder()
                .merchantId(merchantId)
                .amount(10000L)
                .currency("USD")
                .paymentMethodType("CREDIT_CARD")
                .idempotencyKey(idempotencyKey)
                .description("Test payment")
                .cardNumber("4111111111111111")
                .cardExpiryMonth("12")
                .cardExpiryYear("25")
                .cardCvv("123")
                .items(List.of(
                        ProcessPaymentCommand.PaymentItemDto.builder()
                                .description("Test item")
                                .quantity(1)
                                .unitPrice(10000L)
                                .build()
                ))
                .build();
    }

    private ProcessPaymentCommand createPaymentCommandWithCustomer(String merchantId, String customerId, String idempotencyKey) {
        return ProcessPaymentCommand.builder()
                .merchantId(merchantId)
                .amount(10000L)
                .currency("USD")
                .paymentMethodType("CREDIT_CARD")
                .idempotencyKey(idempotencyKey)
                .description("Test payment")
                .customerId(customerId)
                .cardNumber("4111111111111111")
                .cardExpiryMonth("12")
                .cardExpiryYear("25")
                .cardCvv("123")
                .build();
    }

    private void setId(Object obj, String id) {
        try {
            Field idField = obj.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(obj, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id field", e);
        }
    }

    private void setStatus(Object obj, Enum status) {
        try {
            Field statusField = obj.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(obj, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set status field", e);
        }
    }
}
