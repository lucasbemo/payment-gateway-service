package com.payment.gateway.infrastructure.payment.adapter.out.provider;

import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of ExternalPaymentProviderPort for testing/development.
 */
@Slf4j
@Component
public class StubPaymentProvider implements ExternalPaymentProviderPort {

    @Override
    public PaymentProviderResult authorize(PaymentProviderRequest request) {
        log.info("StubPaymentProvider.authorize: paymentId={}, merchantId={}, amount={}",
                request.paymentId(), request.merchantId(), request.amount());
        return new PaymentProviderResult(
                true,
                "stub-txn-" + request.paymentId(),
                null,
                null
        );
    }

    @Override
    public PaymentProviderResult capture(PaymentProviderRequest request) {
        log.info("StubPaymentProvider.capture: paymentId={}, merchantId={}, amount={}",
                request.paymentId(), request.merchantId(), request.amount());
        return new PaymentProviderResult(
                true,
                "stub-capture-" + request.paymentId(),
                null,
                null
        );
    }

    @Override
    public PaymentProviderResult cancel(PaymentProviderRequest request) {
        log.info("StubPaymentProvider.cancel: paymentId={}, merchantId={}",
                request.paymentId(), request.merchantId());
        return new PaymentProviderResult(
                true,
                "stub-cancel-" + request.paymentId(),
                null,
                null
        );
    }

    @Override
    public String tokenizeCard(CardTokenizationRequest request) {
        log.info("StubPaymentProvider.tokenizeCard: cardNumber=****{}",
                request.cardNumber().substring(request.cardNumber().length() - 4));
        return "tok_stub_" + System.currentTimeMillis();
    }
}
