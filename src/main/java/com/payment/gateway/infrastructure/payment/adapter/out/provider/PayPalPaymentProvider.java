package com.payment.gateway.infrastructure.payment.adapter.out.provider;

import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import lombok.extern.slf4j.Slf4j;

/**
 * PayPal payment provider implementation.
 * Not annotated with @Component — activate by registering as a bean
 * and removing/qualifying the StubPaymentProvider.
 */
@Slf4j
public class PayPalPaymentProvider implements ExternalPaymentProviderPort {

    private final String clientId;
    private final String clientSecret;
    private final String apiBaseUrl;

    public PayPalPaymentProvider(String clientId, String clientSecret, String apiBaseUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.apiBaseUrl = apiBaseUrl;
    }

    @Override
    public PaymentProviderResult authorize(PaymentProviderRequest request) {
        log.info("PayPal authorize: paymentId={}, merchantId={}, amount={}",
                request.paymentId(), request.merchantId(), request.amount());
        // TODO: Implement PayPal API call for authorization
        return new PaymentProviderResult(
                true,
                "paypal-auth-" + request.paymentId(),
                null,
                null
        );
    }

    @Override
    public PaymentProviderResult capture(PaymentProviderRequest request) {
        log.info("PayPal capture: paymentId={}, merchantId={}, amount={}",
                request.paymentId(), request.merchantId(), request.amount());
        // TODO: Implement PayPal API call for capture
        return new PaymentProviderResult(
                true,
                "paypal-capture-" + request.paymentId(),
                null,
                null
        );
    }

    @Override
    public PaymentProviderResult cancel(PaymentProviderRequest request) {
        log.info("PayPal cancel: paymentId={}, merchantId={}",
                request.paymentId(), request.merchantId());
        // TODO: Implement PayPal API call for cancellation/void
        return new PaymentProviderResult(
                true,
                "paypal-cancel-" + request.paymentId(),
                null,
                null
        );
    }

    @Override
    public String tokenizeCard(CardTokenizationRequest request) {
        log.info("PayPal tokenizeCard: card=****{}",
                request.cardNumber().substring(request.cardNumber().length() - 4));
        // TODO: Implement PayPal vault API call
        return "tok_paypal_" + System.currentTimeMillis();
    }
}
