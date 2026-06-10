package com.payment.gateway.infrastructure.payment.adapter.out.provider;

import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.PaymentProviderRequest;
import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort.PaymentProviderResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Stub Payment Provider Tests")
class StubPaymentProviderTest {

    private final StubPaymentProvider provider = new StubPaymentProvider();

    private PaymentProviderRequest request(long amount) {
        return new PaymentProviderRequest("pay_123", "merchant-123", amount, "USD", "tok_xyz");
    }

    @Test
    @DisplayName("Should decline authorization when amount ends in 99")
    void shouldDeclineWhenAmountEndsIn99() {
        PaymentProviderResult result = provider.authorize(request(1099L)).join();

        assertThat(result.success()).isFalse();
        assertThat(result.providerTransactionId()).isNull();
        assertThat(result.errorCode()).isEqualTo("card_declined");
        assertThat(result.errorMessage()).isEqualTo("Insufficient funds (test decline: amount ends in 99)");
    }

    @Test
    @DisplayName("Should authorize successfully when amount does not end in 99")
    void shouldAuthorizeWhenAmountDoesNotEndIn99() {
        PaymentProviderResult result = provider.authorize(request(1000L)).join();

        assertThat(result.success()).isTrue();
        assertThat(result.providerTransactionId()).isEqualTo("stub-txn-pay_123");
        assertThat(result.errorCode()).isNull();
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    @DisplayName("Capture and cancel should always succeed, even for magic decline amounts")
    void captureAndCancelAlwaysSucceed() {
        assertThat(provider.capture(request(1099L)).join().success()).isTrue();
        assertThat(provider.cancel(request(1099L)).join().success()).isTrue();
    }
}
