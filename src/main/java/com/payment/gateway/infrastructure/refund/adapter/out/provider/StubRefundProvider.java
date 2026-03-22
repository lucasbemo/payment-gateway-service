package com.payment.gateway.infrastructure.refund.adapter.out.provider;

import com.payment.gateway.application.refund.port.out.ExternalRefundProviderPort;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class StubRefundProvider implements ExternalRefundProviderPort {

    @Override
    public CompletableFuture<RefundProviderResult> processRefund(RefundProviderRequest request) {
        log.info("StubRefundProvider.processRefund: refundId={}, providerTransactionId={}, amount={}",
                request.refundId(), request.providerTransactionId(), request.amount());
        
        return CompletableFuture.completedFuture(new RefundProviderResult(
                true,
                "stub-refund-" + request.refundId(),
                null,
                null
        ));
    }

    @Override
    public CompletableFuture<RefundStatusResult> getRefundStatus(String providerRefundId) {
        log.info("StubRefundProvider.getRefundStatus: providerRefundId={}", providerRefundId);
        
        return CompletableFuture.completedFuture(new RefundStatusResult(
                "succeeded",
                Instant.now()
        ));
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