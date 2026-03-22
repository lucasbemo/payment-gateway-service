package com.payment.gateway.infrastructure.payment.adapter.out.provider.stripe;

import com.payment.gateway.application.payment.port.out.ExternalPaymentProviderPort;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Balance;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.PaymentIntentCreateParams;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class StripePaymentProvider implements ExternalPaymentProviderPort {

    private final StripeExceptionMapper exceptionMapper;

    public StripePaymentProvider(String apiKey, String apiBaseUrl) {
        Stripe.apiKey = apiKey;
        this.exceptionMapper = new StripeExceptionMapper();
        log.info("StripePaymentProvider initialized with apiBaseUrl: {}", apiBaseUrl);
    }

    @Override
    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "authorizeFallback")
    @Retry(name = "paymentProvider")
    @TimeLimiter(name = "payment")
    @Bulkhead(name = "payment")
    @RateLimiter(name = "payment")
    @Async
    public CompletableFuture<PaymentProviderResult> authorize(PaymentProviderRequest request) {
        log.info("Stripe authorize: paymentId={}, merchantId={}, amount={}",
                request.paymentId(), request.merchantId(), request.amount());
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(request.amount())
                .setCurrency(request.currency().toLowerCase())
                .setPaymentMethod(request.paymentMethodToken())
                .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                .setDescription("Payment for merchant: " + request.merchantId())
                .putMetadata("payment_id", request.paymentId())
                .putMetadata("merchant_id", request.merchantId())
                .build();
            
            PaymentIntent intent = PaymentIntent.create(params);
            
            log.info("Stripe authorize success: paymentIntentId={}", intent.getId());
            
            return CompletableFuture.completedFuture(new PaymentProviderResult(
                true,
                intent.getId(),
                null,
                null
            ));
        } catch (StripeException e) {
            log.error("Stripe authorize failed: {}", e.getMessage());
            return CompletableFuture.completedFuture(exceptionMapper.mapException(e, request.paymentId()));
        }
    }

    @SuppressWarnings("unused")
    public CompletableFuture<PaymentProviderResult> authorizeFallback(PaymentProviderRequest request, Throwable t) {
        log.error("Stripe authorize fallback: paymentId={}, error={}", request.paymentId(), t.getMessage());
        return CompletableFuture.completedFuture(new PaymentProviderResult(
            false,
            null,
            "PROVIDER_UNAVAILABLE",
            "Payment provider temporarily unavailable. Please retry."
        ));
    }

    @Override
    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "captureFallback")
    @Retry(name = "paymentProvider")
    @TimeLimiter(name = "payment")
    @Bulkhead(name = "payment")
    @RateLimiter(name = "payment")
    @Async
    public CompletableFuture<PaymentProviderResult> capture(PaymentProviderRequest request) {
        log.info("Stripe capture: paymentIntentId={}, amount={}", request.paymentId(), request.amount());
        try {
            PaymentIntent intent = PaymentIntent.retrieve(request.paymentId());
            
            PaymentIntentCaptureParams params = PaymentIntentCaptureParams.builder()
                .setAmountToCapture(request.amount())
                .build();
            
            intent.capture(params);
            
            log.info("Stripe capture success: paymentIntentId={}", intent.getId());
            
            return CompletableFuture.completedFuture(new PaymentProviderResult(
                true,
                intent.getId(),
                null,
                null
            ));
        } catch (StripeException e) {
            log.error("Stripe capture failed: {}", e.getMessage());
            return CompletableFuture.completedFuture(exceptionMapper.mapException(e, request.paymentId()));
        }
    }

    @SuppressWarnings("unused")
    public CompletableFuture<PaymentProviderResult> captureFallback(PaymentProviderRequest request, Throwable t) {
        log.error("Stripe capture fallback: paymentId={}, error={}", request.paymentId(), t.getMessage());
        return CompletableFuture.completedFuture(new PaymentProviderResult(
            false,
            null,
            "PROVIDER_UNAVAILABLE",
            "Payment capture temporarily unavailable. Please retry."
        ));
    }

    @Override
    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "cancelFallback")
    @Retry(name = "paymentProvider")
    @TimeLimiter(name = "payment")
    @Bulkhead(name = "payment")
    @RateLimiter(name = "payment")
    @Async
    public CompletableFuture<PaymentProviderResult> cancel(PaymentProviderRequest request) {
        log.info("Stripe cancel: paymentIntentId={}", request.paymentId());
        try {
            PaymentIntent intent = PaymentIntent.retrieve(request.paymentId());
            
            PaymentIntentCancelParams params = PaymentIntentCancelParams.builder().build();
            intent.cancel(params);
            
            log.info("Stripe cancel success: paymentIntentId={}", intent.getId());
            
            return CompletableFuture.completedFuture(new PaymentProviderResult(
                true,
                intent.getId(),
                null,
                null
            ));
        } catch (StripeException e) {
            log.error("Stripe cancel failed: {}", e.getMessage());
            return CompletableFuture.completedFuture(exceptionMapper.mapException(e, request.paymentId()));
        }
    }

    @SuppressWarnings("unused")
    public CompletableFuture<PaymentProviderResult> cancelFallback(PaymentProviderRequest request, Throwable t) {
        log.error("Stripe cancel fallback: paymentId={}, error={}", request.paymentId(), t.getMessage());
        return CompletableFuture.completedFuture(new PaymentProviderResult(
            false,
            null,
            "PROVIDER_UNAVAILABLE",
            "Payment cancellation temporarily unavailable. Please retry."
        ));
    }

    @Override
    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "tokenizeCardFallback")
    @Retry(name = "paymentProvider")
    @TimeLimiter(name = "payment")
    @Bulkhead(name = "payment")
    @RateLimiter(name = "payment")
    @Async
    public CompletableFuture<String> tokenizeCard(CardTokenizationRequest request) {
        log.info("Stripe tokenizeCard: card=****{}",
                request.cardNumber().substring(request.cardNumber().length() - 4));
        try {
            com.stripe.model.Token token = com.stripe.model.Token.create(
                new com.stripe.param.TokenCreateParams.Builder()
                    .setCard(
                        com.stripe.param.TokenCreateParams.Card.builder()
                            .setNumber(request.cardNumber())
                            .setExpMonth(request.expiryMonth())
                            .setExpYear(request.expiryYear())
                            .setCvc(request.cvv())
                            .build()
                    )
                    .build()
            );
            
            log.info("Stripe tokenizeCard success: tokenId={}", token.getId());
            return CompletableFuture.completedFuture(token.getId());
        } catch (StripeException e) {
            log.error("Stripe tokenizeCard failed: {}", e.getMessage());
            return CompletableFuture.completedFuture("tok_error_" + System.currentTimeMillis());
        }
    }

    @SuppressWarnings("unused")
    public CompletableFuture<String> tokenizeCardFallback(CardTokenizationRequest request, Throwable t) {
        log.error("Stripe tokenizeCard fallback: error={}", t.getMessage());
        return CompletableFuture.completedFuture("tok_fallback_" + System.currentTimeMillis());
    }

    @Override
    public String getProviderName() {
        return "STRIPE";
    }

    @Override
    public boolean isHealthy() {
        try {
            Balance.retrieve();
            return true;
        } catch (Exception e) {
            log.error("Stripe health check failed", e);
            return false;
        }
    }
}