package com.payment.gateway.application.payment.service;

import com.payment.gateway.application.payment.dto.PaymentResponse;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Get Payment Service Tests")
@ExtendWith(MockitoExtension.class)
class GetPaymentServiceTest {

    @Mock
    private PaymentQueryPort paymentQueryPort;

    private GetPaymentService getPaymentService;

    @BeforeEach
    void setUp() {
        getPaymentService = new GetPaymentService(paymentQueryPort);
    }

    @Nested
    @DisplayName("Get Payment By ID")
    class GetPaymentByIdTests {

        @Test
        @DisplayName("Should get payment successfully by ID")
        void shouldGetPaymentSuccessfullyById() {
            // Given
            String paymentId = "pay_abc123";
            String merchantId = "merchant-123";
            Payment payment = createPayment(paymentId, merchantId);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));

            // When
            PaymentResponse response = getPaymentService.getPaymentById(paymentId, merchantId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(paymentId);
            assertThat(response.getMerchantId()).isEqualTo(merchantId);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING.name());
            assertThat(response.getAmount()).isEqualTo(10000L);
            assertThat(response.getCurrency()).isEqualTo("USD");

            then(paymentQueryPort).should().findById(paymentId);
        }

        @Test
        @DisplayName("Should throw exception when payment not found")
        void shouldThrowExceptionWhenPaymentNotFound() {
            // Given
            String paymentId = "invalid-payment";
            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> getPaymentService.getPaymentById(paymentId, "merchant-123"))
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
            Payment payment = createPayment(paymentId, paymentMerchantId);

            given(paymentQueryPort.findById(paymentId)).willReturn(Optional.of(payment));

            // When & Then
            assertThatThrownBy(() -> getPaymentService.getPaymentById(paymentId, requestMerchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Payment does not belong to merchant");
        }
    }

    @Nested
    @DisplayName("Get Payments By Merchant ID")
    class GetPaymentsByMerchantIdTests {

        @Test
        @DisplayName("Should return empty list for merchant with no payments")
        void shouldReturnEmptyListForMerchantWithNoPayments() {
            // Given
            String merchantId = "merchant-123";

            // When
            List<PaymentResponse> responses = getPaymentService.getPaymentsByMerchantId(merchantId);

            // Then
            assertThat(responses).isEmpty();
        }
    }

    // Helper methods

    private Payment createPayment(String id, String merchantId) {
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
}
