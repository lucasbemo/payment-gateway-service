package com.payment.gateway.infrastructure.payment.adapter.out.provider;

import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import lombok.extern.slf4j.Slf4j;

/**
 * Stripe payment provider implementation.
 * Not annotated with @Component — activate by registering as a bean
 * and removing/qualifying the StubPaymentProvider.
 */
@Slf4j
public class StripePaymentProvider implements ExternalPaymentProviderPort {

    private final String apiKey;
    private final String apiBaseUrl;

    public StripePaymentProvider(String apiKey, String apiBaseUrl) {
        this.apiKey = apiKey;
        this.apiBaseUrl = apiBaseUrl;
    }

    @Override
    public PaymentProviderResult authorize(PaymentProviderRequest request) {
        log.info("Stripe authorize: paymentId={}, merchantId={}, amount={}",
                request.paymentId(), request.merchantId(), request.amount());
        // TODO: Implement Stripe API call for authorization
        return new PaymentProviderResult(
                true,
                "stripe-auth-" + request.paymentId(),
                null,
                null
        );
    }

    @Override
    public PaymentProviderResult capture(PaymentProviderRequest request) {
        log.info("Stripe capture: paymentId={}, merchantId={}, amount={}",
                request.paymentId(), request.merchantId(), request.amount());
        // TODO: Implement Stripe API call for capture
        return new PaymentProviderResult(
                true,
                "stripe-capture-" + request.paymentId(),
                null,
                null
        );
    }

    @Override
    public PaymentProviderResult cancel(PaymentProviderRequest request) {
        log.info("Stripe cancel: paymentId={}, merchantId={}",
                request.paymentId(), request.merchantId());
        // TODO: Implement Stripe API call for cancellation
        return new PaymentProviderResult(
                true,
                "stripe-cancel-" + request.paymentId(),
                null,
                null
        );
    }

    @Override
    public String tokenizeCard(CardTokenizationRequest request) {
        log.info("Stripe tokenizeCard: card=****{}",
                request.cardNumber().substring(request.cardNumber().length() - 4));
        // TODO: Implement Stripe tokenization API call
        return "tok_stripe_" + System.currentTimeMillis();
    }
}
