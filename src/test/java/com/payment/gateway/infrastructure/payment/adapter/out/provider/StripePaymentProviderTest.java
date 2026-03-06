package com.payment.gateway.infrastructure.payment.adapter.out.provider;

import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.CardTokenizationRequest;
import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.PaymentProviderRequest;
import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.PaymentProviderResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StripePaymentProviderTest {

    private StripePaymentProvider provider;

    @BeforeEach
    void setUp() {
        provider = new StripePaymentProvider("sk_test_abc123", "https://api.stripe.com/v1");
    }

    @Nested
    @DisplayName("authorize")
    class Authorize {

        @Test
        @DisplayName("should return successful result with stripe-auth prefix")
        void shouldReturnSuccessfulAuthorization() {
            PaymentProviderRequest request = new PaymentProviderRequest(
                    "pay-001", "merchant-001", 5000L, "USD", "tok_visa"
            );

            PaymentProviderResult result = provider.authorize(request);

            assertThat(result.success()).isTrue();
            assertThat(result.providerTransactionId()).isEqualTo("stripe-auth-pay-001");
            assertThat(result.errorCode()).isNull();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("should include payment ID in provider transaction ID")
        void shouldIncludePaymentIdInTransactionId() {
            PaymentProviderRequest request = new PaymentProviderRequest(
                    "pay-xyz-789", "merchant-002", 10000L, "EUR", "tok_mastercard"
            );

            PaymentProviderResult result = provider.authorize(request);

            assertThat(result.providerTransactionId()).contains("pay-xyz-789");
        }
    }

    @Nested
    @DisplayName("capture")
    class Capture {

        @Test
        @DisplayName("should return successful result with stripe-capture prefix")
        void shouldReturnSuccessfulCapture() {
            PaymentProviderRequest request = new PaymentProviderRequest(
                    "pay-002", "merchant-001", 5000L, "USD", "tok_visa"
            );

            PaymentProviderResult result = provider.capture(request);

            assertThat(result.success()).isTrue();
            assertThat(result.providerTransactionId()).isEqualTo("stripe-capture-pay-002");
            assertThat(result.errorCode()).isNull();
            assertThat(result.errorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("should return successful result with stripe-cancel prefix")
        void shouldReturnSuccessfulCancellation() {
            PaymentProviderRequest request = new PaymentProviderRequest(
                    "pay-003", "merchant-001", 5000L, "USD", "tok_visa"
            );

            PaymentProviderResult result = provider.cancel(request);

            assertThat(result.success()).isTrue();
            assertThat(result.providerTransactionId()).isEqualTo("stripe-cancel-pay-003");
            assertThat(result.errorCode()).isNull();
            assertThat(result.errorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("tokenizeCard")
    class TokenizeCard {

        @Test
        @DisplayName("should return token with tok_stripe_ prefix")
        void shouldReturnStripeToken() {
            CardTokenizationRequest request = new CardTokenizationRequest(
                    "4242424242424242", "12", "2027", "123"
            );

            String token = provider.tokenizeCard(request);

            assertThat(token).startsWith("tok_stripe_");
        }

        @Test
        @DisplayName("should return tokens based on current time millis")
        void shouldReturnTimestampBasedTokens() throws InterruptedException {
            CardTokenizationRequest request = new CardTokenizationRequest(
                    "4242424242424242", "12", "2027", "123"
            );

            String token1 = provider.tokenizeCard(request);
            Thread.sleep(5);
            String token2 = provider.tokenizeCard(request);

            assertThat(token1).startsWith("tok_stripe_");
            assertThat(token2).startsWith("tok_stripe_");
            assertThat(token1).isNotEqualTo(token2);
        }
    }
}
