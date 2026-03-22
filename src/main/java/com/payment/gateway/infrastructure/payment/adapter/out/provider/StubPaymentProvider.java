package com.payment.gateway.infrastructure.payment.adapter.out.provider;

import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class StubPaymentProvider implements ExternalPaymentProviderPort {

    @Override
    public CompletableFuture<PaymentProviderResult> authorize(PaymentProviderRequest request) {
        log.info("StubPaymentProvider.authorize: paymentId={}, merchantId={}, amount={}",
                request.paymentId(), request.merchantId(), request.amount());
        return CompletableFuture.completedFuture(new PaymentProviderResult(
                true,
                "stub-txn-" + request.paymentId(),
                null,
                null
        ));
    }

    @Override
    public CompletableFuture<PaymentProviderResult> capture(PaymentProviderRequest request) {
        log.info("StubPaymentProvider.capture: paymentId={}, merchantId={}, amount={}",
                request.paymentId(), request.merchantId(), request.amount());
        return CompletableFuture.completedFuture(new PaymentProviderResult(
                true,
                "stub-capture-" + request.paymentId(),
                null,
                null
        ));
    }

    @Override
    public CompletableFuture<PaymentProviderResult> cancel(PaymentProviderRequest request) {
        log.info("StubPaymentProvider.cancel: paymentId={}, merchantId={}",
                request.paymentId(), request.merchantId());
        return CompletableFuture.completedFuture(new PaymentProviderResult(
                true,
                "stub-cancel-" + request.paymentId(),
                null,
                null
        ));
    }

    @Override
    public CompletableFuture<String> tokenizeCard(CardTokenizationRequest request) {
        log.info("StubPaymentProvider.tokenizeCard: cardNumber=****{}",
                request.cardNumber().substring(request.cardNumber().length() - 4));
        return CompletableFuture.completedFuture("tok_stub_" + System.currentTimeMillis());
    }

    @Override
    public String getProviderName() {
        return "STUB";
    }

    @Override
    public boolean isHealthy() {
        return true;
    }
}
