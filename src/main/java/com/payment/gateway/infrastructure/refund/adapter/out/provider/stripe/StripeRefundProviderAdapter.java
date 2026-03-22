package com.payment.gateway.infrastructure.refund.adapter.out.provider.stripe;

import com.payment.gateway.application.refund.port.out.ExternalRefundProviderPort;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class StripeRefundProviderAdapter implements ExternalRefundProviderPort {

    public StripeRefundProviderAdapter(String apiKey) {
        Stripe.apiKey = apiKey;
        log.info("StripeRefundProviderAdapter initialized");
    }

    @Override
    @CircuitBreaker(name = "refundProvider", fallbackMethod = "processRefundFallback")
    @Retry(name = "refundProvider")
    @TimeLimiter(name = "refund")
    @Bulkhead(name = "refund")
    @RateLimiter(name = "refund")
    @Async
    public CompletableFuture<RefundProviderResult> processRefund(RefundProviderRequest request) {
        log.info("Stripe processRefund: refundId={}, paymentIntentId={}, amount={}",
                request.refundId(), request.providerTransactionId(), request.amount());
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(request.providerTransactionId())
                .setAmount(request.amount())
                .setReason(mapReason(request.reason()))
                .putMetadata("refund_id", request.refundId())
                .build();
            
            Refund refund = Refund.create(params);
            
            log.info("Stripe refund success: refundId={}", refund.getId());
            
            return CompletableFuture.completedFuture(new RefundProviderResult(
                "succeeded".equals(refund.getStatus()),
                refund.getId(),
                null,
                null
            ));
        } catch (StripeException e) {
            log.error("Stripe refund failed: {}", e.getMessage());
            return CompletableFuture.completedFuture(new RefundProviderResult(
                false,
                null,
                e.getCode(),
                e.getMessage()
            ));
        }
    }

    @SuppressWarnings("unused")
    public CompletableFuture<RefundProviderResult> processRefundFallback(RefundProviderRequest request, Throwable t) {
        log.error("Stripe refund fallback: refundId={}, error={}", request.refundId(), t.getMessage());
        return CompletableFuture.completedFuture(new RefundProviderResult(
            false,
            null,
            "PROVIDER_UNAVAILABLE",
            "Refund processing temporarily unavailable. Please retry."
        ));
    }

    @Override
    @CircuitBreaker(name = "refundProvider")
    @Async
    public CompletableFuture<RefundStatusResult> getRefundStatus(String providerRefundId) {
        log.info("Stripe getRefundStatus: refundId={}", providerRefundId);
        try {
            Refund refund = Refund.retrieve(providerRefundId);
            return CompletableFuture.completedFuture(new RefundStatusResult(
                refund.getStatus(),
                refund.getCreated() != null ? java.time.Instant.ofEpochSecond(refund.getCreated()) : null
            ));
        } catch (StripeException e) {
            log.error("Stripe getRefundStatus failed: {}", e.getMessage());
            return CompletableFuture.completedFuture(new RefundStatusResult("unknown", null));
        }
    }

    @Override
    public String getProviderName() {
        return "STRIPE";
    }

    @Override
    public boolean isHealthy() {
        return true;
    }

    private RefundCreateParams.Reason mapReason(String reason) {
        if (reason == null) {
            return RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER;
        }
        return switch (reason.toLowerCase()) {
            case "duplicate" -> RefundCreateParams.Reason.DUPLICATE;
            case "fraudulent" -> RefundCreateParams.Reason.FRAUDULENT;
            default -> RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER;
        };
    }
}