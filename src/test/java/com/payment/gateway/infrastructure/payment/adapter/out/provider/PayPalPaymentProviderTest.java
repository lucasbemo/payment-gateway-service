package com.payment.gateway.infrastructure.payment.adapter.out.provider;

import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.CardTokenizationRequest;
import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.PaymentProviderRequest;
import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.PaymentProviderResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class PayPalPaymentProviderTest {

    private PayPalPaymentProvider provider;

    @BeforeEach
    void setUp() {
        provider = new PayPalPaymentProvider("client-id-test", "client-secret-test", "https://api.sandbox.paypal.com");
    }

    @Nested
    @DisplayName("authorize")
    class Authorize {

        @Test
        @DisplayName("should return successful result with paypal-auth prefix")
        void shouldReturnSuccessfulAuthorization() {
            PaymentProviderRequest request = new PaymentProviderRequest(
                    "pay-001", "merchant-001", 5000L, "USD", "tok_paypal_visa"
            );

            PaymentProviderResult result = provider.authorize(request).join();

            assertThat(result.success()).isTrue();
            assertThat(result.providerTransactionId()).isEqualTo("paypal-auth-pay-001");
            assertThat(result.errorCode()).isNull();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("should include payment ID in provider transaction ID")
        void shouldIncludePaymentIdInTransactionId() {
            PaymentProviderRequest request = new PaymentProviderRequest(
                    "pay-abc-456", "merchant-002", 7500L, "EUR", "tok_paypal_mc"
            );

            PaymentProviderResult result = provider.authorize(request).join();

            assertThat(result.providerTransactionId()).contains("pay-abc-456");
        }
    }

    @Nested
    @DisplayName("capture")
    class Capture {

        @Test
        @DisplayName("should return successful result with paypal-capture prefix")
        void shouldReturnSuccessfulCapture() {
            PaymentProviderRequest request = new PaymentProviderRequest(
                    "pay-002", "merchant-001", 5000L, "USD", "tok_paypal_visa"
            );

            PaymentProviderResult result = provider.capture(request).join();

            assertThat(result.success()).isTrue();
            assertThat(result.providerTransactionId()).isEqualTo("paypal-capture-pay-002");
            assertThat(result.errorCode()).isNull();
            assertThat(result.errorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("should return successful result with paypal-cancel prefix")
        void shouldReturnSuccessfulCancellation() {
            PaymentProviderRequest request = new PaymentProviderRequest(
                    "pay-003", "merchant-001", 5000L, "USD", "tok_paypal_visa"
            );

            PaymentProviderResult result = provider.cancel(request).join();

            assertThat(result.success()).isTrue();
            assertThat(result.providerTransactionId()).isEqualTo("paypal-cancel-pay-003");
            assertThat(result.errorCode()).isNull();
            assertThat(result.errorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("tokenizeCard")
    class TokenizeCard {

        @Test
        @DisplayName("should return token with tok_paypal_ prefix")
        void shouldReturnPayPalToken() {
            CardTokenizationRequest request = new CardTokenizationRequest(
                    "5555555555554444", "06", "2028", "456"
            );

            String token = provider.tokenizeCard(request).join();

            assertThat(token).startsWith("tok_paypal_");
        }

        @Test
        @DisplayName("should return tokens based on current time millis")
        void shouldReturnTimestampBasedTokens() throws InterruptedException {
            CardTokenizationRequest request = new CardTokenizationRequest(
                    "5555555555554444", "06", "2028", "456"
            );

            String token1 = provider.tokenizeCard(request).join();
            Thread.sleep(5);
            String token2 = provider.tokenizeCard(request).join();

            assertThat(token1).startsWith("tok_paypal_");
            assertThat(token2).startsWith("tok_paypal_");
            assertThat(token1).isNotEqualTo(token2);
        }
    }
}
