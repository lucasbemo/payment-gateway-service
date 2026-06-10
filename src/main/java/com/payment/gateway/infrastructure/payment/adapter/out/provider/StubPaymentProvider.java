package com.payment.gateway.infrastructure.payment.adapter.out.provider;

import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Stub payment provider used for local development and testing.
 *
 * <p>Magic decline rule: {@code authorize} deterministically declines any request whose
 * amount (in cents) ends in 99 (i.e. {@code amount % 100 == 99}, e.g. 1099, 99, 250099)
 * with error code {@code card_declined}. All other amounts are authorized successfully.
 * {@code capture} and {@code cancel} always succeed.
 */
@Slf4j
@Component
public class StubPaymentProvider implements ExternalPaymentProviderPort {

    static final String DECLINE_ERROR_CODE = "card_declined";
    static final String DECLINE_ERROR_MESSAGE = "Insufficient funds (test decline: amount ends in 99)";

    @Override
    public CompletableFuture<PaymentProviderResult> authorize(PaymentProviderRequest request) {
        log.info("StubPaymentProvider.authorize: paymentId={}, merchantId={}, amount={}",
                request.paymentId(), request.merchantId(), request.amount());
        if (request.amount() != null && request.amount() % 100 == 99) {
            log.info("StubPaymentProvider.authorize: declining paymentId={} (magic amount {})",
                    request.paymentId(), request.amount());
            return CompletableFuture.completedFuture(new PaymentProviderResult(
                    false,
                    null,
                    DECLINE_ERROR_CODE,
                    DECLINE_ERROR_MESSAGE
            ));
        }
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
