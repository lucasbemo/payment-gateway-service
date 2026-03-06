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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@DisplayName("Capture Payment Service Tests")
@ExtendWith(MockitoExtension.class)
class CapturePaymentServiceTest {

    @Mock
    private PaymentQueryPort paymentQueryPort;

    @Mock
    private ExternalPaymentProviderPort externalPaymentProviderPort;

    private CapturePaymentService capturePaymentService;

    @BeforeEach
    void setUp() {
        capturePaymentService = new CapturePaymentService(paymentQueryPort, externalPaymentProviderPort);
    }

    @Nested
    @DisplayName("Successful Payment Capture")
    class SuccessfulCaptureTests {

        @Test
        @DisplayName("Should capture authorized payment successfully")
        void shouldCaptureAuthorizedPaymentSuccessfully() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.AUTHORIZED);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));
            given(externalPaymentProviderPort.capture(any())).willReturn(
                    new ExternalPaymentProviderPort.PaymentProviderResult(true, "capture_txn_123", null, null)
            );
            given(paymentQueryPort.savePayment(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentResponse response = capturePaymentService.capturePayment(paymentId, merchantId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(paymentId);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.CAPTURED.name());
            assertThat(response.getMerchantId()).isEqualTo(merchantId);

            then(paymentQueryPort).should().findById(paymentId);
            then(externalPaymentProviderPort).should().capture(any());
            then(paymentQueryPort).should().savePayment(any(Payment.class));
        }

        @Test
        @DisplayName("Should return correct payment details after capture")
        void shouldReturnCorrectPaymentDetailsAfterCapture() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.AUTHORIZED);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));
            given(externalPaymentProviderPort.capture(any())).willReturn(
                    new ExternalPaymentProviderPort.PaymentProviderResult(true, "capture_txn_456", null, null)
            );
            given(paymentQueryPort.savePayment(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            PaymentResponse response = capturePaymentService.capturePayment(paymentId, merchantId);

            // Then
            assertThat(response.getAmount()).isEqualTo(10000L);
            assertThat(response.getCurrency()).isEqualTo("USD");
            assertThat(response.getDescription()).isEqualTo("Test payment");
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
            assertThatThrownBy(() -> capturePaymentService.capturePayment(paymentId, "merchant-123"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment not found");

            then(externalPaymentProviderPort).should(never()).capture(any());
            then(paymentQueryPort).should(never()).savePayment(any());
        }

        @Test
        @DisplayName("Should throw exception when merchant does not own payment")
        void shouldThrowExceptionWhenMerchantDoesNotOwnPayment() {
            // Given
            String paymentId = "pay_abc123";
            String paymentMerchantId = "merchant-123";
            String requestMerchantId = "merchant-456";
            Payment payment = createPayment(paymentId, paymentMerchantId, PaymentStatus.AUTHORIZED);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> capturePaymentService.capturePayment(paymentId, requestMerchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment does not belong to merchant");

            then(externalPaymentProviderPort).should(never()).capture(any());
        }

        @Test
        @DisplayName("Should throw exception when payment is pending (not authorized)")
        void shouldThrowExceptionWhenPaymentIsPending() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.PENDING);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> capturePaymentService.capturePayment(paymentId, merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment must be authorized before capture");

            then(externalPaymentProviderPort).should(never()).capture(any());
        }

        @Test
        @DisplayName("Should throw exception when payment is already captured")
        void shouldThrowExceptionWhenPaymentAlreadyCaptured() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.CAPTURED);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> capturePaymentService.capturePayment(paymentId, merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment must be authorized before capture");
        }

        @Test
        @DisplayName("Should throw exception when payment is cancelled")
        void shouldThrowExceptionWhenPaymentIsCancelled() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.CANCELLED);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> capturePaymentService.capturePayment(paymentId, merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment must be authorized before capture");
        }

        @Test
        @DisplayName("Should throw exception when payment is failed")
        void shouldThrowExceptionWhenPaymentIsFailed() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.FAILED);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> capturePaymentService.capturePayment(paymentId, merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment must be authorized before capture");
        }
    }

    @Nested
    @DisplayName("Provider Failure Tests")
    class ProviderFailureTests {

        @Test
        @DisplayName("Should throw exception when provider capture fails")
        void shouldThrowExceptionWhenProviderCaptureFails() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.AUTHORIZED);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));
            given(externalPaymentProviderPort.capture(any())).willReturn(
                    new ExternalPaymentProviderPort.PaymentProviderResult(false, null, "CAPTURE_FAILED", "Insufficient funds")
            );

            // When & Then
            assertThatThrownBy(() -> capturePaymentService.capturePayment(paymentId, merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment capture failed");

            then(paymentQueryPort).should(never()).savePayment(any());
        }

        @Test
        @DisplayName("Should include provider error message in exception")
        void shouldIncludeProviderErrorMessageInException() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId, PaymentStatus.AUTHORIZED);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));
            given(externalPaymentProviderPort.capture(any())).willReturn(
                    new ExternalPaymentProviderPort.PaymentProviderResult(false, null, "TIMEOUT", "Gateway timeout")
            );

            // When & Then
            assertThatThrownBy(() -> capturePaymentService.capturePayment(paymentId, merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Gateway timeout");
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
