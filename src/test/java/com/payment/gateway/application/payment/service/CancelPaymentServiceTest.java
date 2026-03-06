package com.payment.gateway.application.payment.service;

import com.payment.gateway.application.payment.dto.PaymentResponse;
import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import com.payment.gateway.application.payment.port.out.PaymentQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.model.Money;
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

@DisplayName("Cancel Payment Service Tests")
@ExtendWith(MockitoExtension.class)
class CancelPaymentServiceTest {

    @Mock
    private PaymentQueryPort paymentQueryPort;

    @Mock
    private ExternalPaymentProviderPort externalPaymentProviderPort;

    private CancelPaymentService cancelPaymentService;

    @BeforeEach
    void setUp() {
        cancelPaymentService = new CancelPaymentService(paymentQueryPort, externalPaymentProviderPort);
    }

    @Nested
    @DisplayName("Successful Payment Cancellation")
    class SuccessfulCancellationTests {

        @Test
        @DisplayName("Should cancel payment successfully")
        void shouldCancelPaymentSuccessfully() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.PENDING);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));
            given(externalPaymentProviderPort.cancel(any())).willReturn(
                    CompletableFuture.completedFuture(new ExternalPaymentProviderPort.PaymentProviderResult(true, "cancel_txn", null, null))
            );
            given(paymentQueryPort.savePayment(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentResponse response = cancelPaymentService.cancelPayment(paymentId, merchantId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(paymentId);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.CANCELLED.name());

            then(paymentQueryPort).should().findById(paymentId);
            then(externalPaymentProviderPort).should().cancel(any());
            then(paymentQueryPort).should().savePayment(any(Payment.class));
        }

        @Test
        @DisplayName("Should allow local cancellation even if provider cancellation fails")
        void shouldAllowLocalCancellationEvenIfProviderFails() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.PENDING);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));
            given(externalPaymentProviderPort.cancel(any())).willReturn(
                    CompletableFuture.completedFuture(new ExternalPaymentProviderPort.PaymentProviderResult(false, null, "ERR_CANCEL", "Provider cancel failed"))
            );
            given(paymentQueryPort.savePayment(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentResponse response = cancelPaymentService.cancelPayment(paymentId, merchantId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(paymentId);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.CANCELLED.name());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when payment not found")
        void shouldThrowExceptionWhenPaymentNotFound() {
            // Given
            String paymentId = "invalid-payment";
            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> cancelPaymentService.cancelPayment(paymentId, "merchant-123"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment not found");
        }

        @Test
        @DisplayName("Should throw exception when merchant does not own payment")
        void shouldThrowExceptionWhenMerchantDoesNotOwnPayment() {
            // Given
            String paymentId = "pay_abc123";
            String paymentMerchantId = "merchant-123";
            String requestMerchantId = "merchant-456";
            Payment payment = createPayment(paymentId, paymentMerchantId, PaymentStatus.PENDING);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> cancelPaymentService.cancelPayment(paymentId, requestMerchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment does not belong to merchant");
        }

        @Test
        @DisplayName("Should throw exception when payment is in terminal state")
        void shouldThrowExceptionWhenPaymentInTerminalState() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.CAPTURED);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> cancelPaymentService.cancelPayment(paymentId, merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot cancel payment in terminal state");
        }

        @Test
        @DisplayName("Should throw exception when payment is already cancelled")
        void shouldThrowExceptionWhenPaymentAlreadyCancelled() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.CANCELLED);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> cancelPaymentService.cancelPayment(paymentId, merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot cancel payment in terminal state");
        }

        @Test
        @DisplayName("Should throw exception when payment is refunded")
        void shouldThrowExceptionWhenPaymentRefunded() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.REFUNDED);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> cancelPaymentService.cancelPayment(paymentId, merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot cancel payment in terminal state");
        }
    }

    // Helper methods

    private Payment createPayment(String id, String merchantId, PaymentStatus status) {
        Payment payment = Payment.create(
                merchantId,
                Money.of(10000L, Currency.getInstance("USD")),
                "USD",
                null,
                "idem-key-123",
                "Test payment",
                PaymentMetadata.empty(),
                List.of(),
                null
        );
        setId(payment, id);
        setStatus(payment, status);
        return payment;
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

    private void setStatus(Object obj, PaymentStatus status) {
        try {
            Field statusField = obj.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(obj, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set status field", e);
        }
    }
}
