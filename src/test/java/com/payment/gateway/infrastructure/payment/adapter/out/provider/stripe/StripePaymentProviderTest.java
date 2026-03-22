package com.payment.gateway.infrastructure.payment.adapter.out.provider.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripePaymentProviderTest {

    private StripePaymentProvider provider;

    @BeforeEach
    void setUp() {
        provider = new StripePaymentProvider("sk_test_abc123", "https://api.stripe.com");
    }

    @Test
    @DisplayName("getProviderName should return STRIPE")
    void getProviderName_shouldReturnStripe() {
        assertThat(provider.getProviderName()).isEqualTo("STRIPE");
    }

    @Test
    @DisplayName("authorize should return successful result when Stripe API succeeds")
    void authorize_shouldReturnSuccessfulResult() throws StripeException {
        PaymentIntent mockIntent = mock(PaymentIntent.class);
        when(mockIntent.getId()).thenReturn("pi_1234567890");

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class))).thenReturn(mockIntent);

            var request = new com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.PaymentProviderRequest(
                    "pay-001", "merchant-001", 5000L, "USD", "tok_visa"
            );

            CompletableFuture<com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.PaymentProviderResult> future =
                    provider.authorize(request);
            var result = future.join();

            assertThat(result.success()).isTrue();
            assertThat(result.providerTransactionId()).isEqualTo("pi_1234567890");
            assertThat(result.errorCode()).isNull();
            assertThat(result.errorMessage()).isNull();
        }
    }

    @Test
    @DisplayName("capture should return successful result when Stripe API succeeds")
    void capture_shouldReturnSuccessfulResult() throws StripeException {
        PaymentIntent mockIntent = mock(PaymentIntent.class);
        when(mockIntent.getId()).thenReturn("pi_1234567890");

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.retrieve(any())).thenReturn(mockIntent);

            var request = new com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.PaymentProviderRequest(
                    "pi_1234567890", "merchant-001", 5000L, "USD", "tok_visa"
            );

            CompletableFuture<com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.PaymentProviderResult> future =
                    provider.capture(request);
            var result = future.join();

            assertThat(result.success()).isTrue();
            assertThat(result.providerTransactionId()).isEqualTo("pi_1234567890");
        }
    }

    @Test
    @DisplayName("cancel should return successful result when Stripe API succeeds")
    void cancel_shouldReturnSuccessfulResult() throws StripeException {
        PaymentIntent mockIntent = mock(PaymentIntent.class);
        when(mockIntent.getId()).thenReturn("pi_1234567890");

        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.when(() -> PaymentIntent.retrieve(any())).thenReturn(mockIntent);

            var request = new com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.PaymentProviderRequest(
                    "pi_1234567890", "merchant-001", 5000L, "USD", "tok_visa"
            );

            CompletableFuture<com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.PaymentProviderResult> future =
                    provider.cancel(request);
            var result = future.join();

            assertThat(result.success()).isTrue();
            assertThat(result.providerTransactionId()).isEqualTo("pi_1234567890");
        }
    }
}