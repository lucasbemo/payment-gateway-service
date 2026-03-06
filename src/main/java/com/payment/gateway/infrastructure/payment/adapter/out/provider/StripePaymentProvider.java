package com.payment.gateway.infrastructure.payment.adapter.out.provider;

import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

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
    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "authorizeFallback")
    @Retry(name = "paymentProvider", fallbackMethod = "authorizeFallback")
    @TimeLimiter(name = "payment", fallbackMethod = "authorizeFallback")
    @Bulkhead(name = "payment", fallbackMethod = "authorizeFallback")
    @RateLimiter(name = "payment", fallbackMethod = "authorizeFallback")
    @Async
    public CompletableFuture<PaymentProviderResult> authorize(PaymentProviderRequest request) {
        log.info("Stripe authorize: paymentId={}, merchantId={}, amount={}",
                request.paymentId(), request.merchantId(), request.amount());
        // TODO: Implement Stripe API call for authorization
        return CompletableFuture.completedFuture(new PaymentProviderResult(
                true,
                "stripe-auth-" + request.paymentId(),
                null,
                null
        ));
    }

    @SuppressWarnings("unused")
    public CompletableFuture<PaymentProviderResult> authorizeFallback(PaymentProviderRequest request, Throwable t) {
        log.error("Stripe authorize failed after retries (fallback): paymentId={}, error={}",
                request.paymentId(), t.getMessage());
        return CompletableFuture.completedFuture(new PaymentProviderResult(
                false,
                null,
                "FALLBACK",
                "Payment authorization temporarily unavailable. Please retry."
        ));
    }

    @Override
    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "captureFallback")
    @Retry(name = "paymentProvider", fallbackMethod = "captureFallback")
    @TimeLimiter(name = "payment", fallbackMethod = "captureFallback")
    @Bulkhead(name = "payment", fallbackMethod = "captureFallback")
    @RateLimiter(name = "payment", fallbackMethod = "captureFallback")
    @Async
    public CompletableFuture<PaymentProviderResult> capture(PaymentProviderRequest request) {
        log.info("Stripe capture: paymentId={}, merchantId={}, amount={}",
                request.paymentId(), request.merchantId(), request.amount());
        // TODO: Implement Stripe API call for capture
        return CompletableFuture.completedFuture(new PaymentProviderResult(
                true,
                "stripe-capture-" + request.paymentId(),
                null,
                null
        ));
    }

    @SuppressWarnings("unused")
    public CompletableFuture<PaymentProviderResult> captureFallback(PaymentProviderRequest request, Throwable t) {
        log.error("Stripe capture failed after retries (fallback): paymentId={}, error={}",
                request.paymentId(), t.getMessage());
        return CompletableFuture.completedFuture(new PaymentProviderResult(
                false,
                null,
                "FALLBACK",
                "Payment capture temporarily unavailable. Please retry."
        ));
    }

    @Override
    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "cancelFallback")
    @Retry(name = "paymentProvider", fallbackMethod = "cancelFallback")
    @TimeLimiter(name = "payment", fallbackMethod = "cancelFallback")
    @Bulkhead(name = "payment", fallbackMethod = "cancelFallback")
    @RateLimiter(name = "payment", fallbackMethod = "cancelFallback")
    @Async
    public CompletableFuture<PaymentProviderResult> cancel(PaymentProviderRequest request) {
        log.info("Stripe cancel: paymentId={}, merchantId={}",
                request.paymentId(), request.merchantId());
        // TODO: Implement Stripe API call for cancellation
        return CompletableFuture.completedFuture(new PaymentProviderResult(
                true,
                "stripe-cancel-" + request.paymentId(),
                null,
                null
        ));
    }

    @SuppressWarnings("unused")
    public CompletableFuture<PaymentProviderResult> cancelFallback(PaymentProviderRequest request, Throwable t) {
        log.error("Stripe cancel failed after retries (fallback): paymentId={}, error={}",
                request.paymentId(), t.getMessage());
        return CompletableFuture.completedFuture(new PaymentProviderResult(
                false,
                null,
                "FALLBACK",
                "Payment cancellation temporarily unavailable. Please retry."
        ));
    }

    @Override
    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "tokenizeCardFallback")
    @Retry(name = "paymentProvider", fallbackMethod = "tokenizeCardFallback")
    @TimeLimiter(name = "payment", fallbackMethod = "tokenizeCardFallback")
    @Bulkhead(name = "payment", fallbackMethod = "tokenizeCardFallback")
    @RateLimiter(name = "payment", fallbackMethod = "tokenizeCardFallback")
    @Async
    public CompletableFuture<String> tokenizeCard(CardTokenizationRequest request) {
        log.info("Stripe tokenizeCard: card=****{}",
                request.cardNumber().substring(request.cardNumber().length() - 4));
        // TODO: Implement Stripe tokenization API call
        return CompletableFuture.completedFuture("tok_stripe_" + System.currentTimeMillis());
    }

    @SuppressWarnings("unused")
    public String tokenizeCardFallback(CardTokenizationRequest request, Throwable t) {
        log.error("Stripe tokenizeCard failed after retries (fallback): error={}", t.getMessage());
        return "tok_fallback_" + System.currentTimeMillis();
    }
}
