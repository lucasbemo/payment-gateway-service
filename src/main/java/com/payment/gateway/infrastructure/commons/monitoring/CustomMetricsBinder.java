package com.payment.gateway.infrastructure.commons.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Custom metrics binder for payment gateway specific metrics.
 * Provides counters, timers, and distribution summaries for monitoring.
 */
@Component
public class CustomMetricsBinder implements MeterBinder {

    // Payment Metrics
    private Counter paymentsProcessed;
    private Counter paymentsApproved;
    private Counter paymentsRejected;
    private Counter paymentsFailed;
    private Timer paymentProcessingTimer;
    private DistributionSummary paymentAmountSummary;

    // Refund Metrics
    private Counter refundsProcessed;
    private Counter refundsApproved;
    private Counter refundsRejected;
    private Timer refundProcessingTimer;
    private DistributionSummary refundAmountSummary;

    // Merchant Metrics
    private Counter merchantApiCalls;

    // Transaction Metrics
    private Counter transactionsCreated;
    private Counter transactionsCompleted;

    // Per-merchant tracking
    private final ConcurrentMap<String, Counter> paymentsPerMerchant = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, DistributionSummary> revenuePerMerchant = new ConcurrentHashMap<>();

    private MeterRegistry registry;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;

        // Payment counters
        paymentsProcessed = Counter.builder("payment.gateway.payments.processed.total")
                .description("Total number of payments processed")
                .tag("type", "payment")
                .register(registry);

        paymentsApproved = Counter.builder("payment.gateway.payments.approved.total")
                .description("Total number of approved payments")
                .tag("type", "payment")
                .register(registry);

        paymentsRejected = Counter.builder("payment.gateway.payments.rejected.total")
                .description("Total number of rejected payments")
                .tag("type", "payment")
                .register(registry);

        paymentsFailed = Counter.builder("payment.gateway.payments.failed.total")
                .description("Total number of failed payments")
                .tag("type", "payment")
                .register(registry);

        // Payment timer
        paymentProcessingTimer = Timer.builder("payment.gateway.payments.processing.duration")
                .description("Payment processing time")
                .tag("type", "payment")
                .register(registry);

        // Payment amount distribution
        paymentAmountSummary = DistributionSummary.builder("payment.gateway.payments.amount")
                .description("Payment amounts distribution")
                .tag("type", "payment")
                .baseUnit("cents")
                .register(registry);

        // Refund counters
        refundsProcessed = Counter.builder("payment.gateway.refunds.processed.total")
                .description("Total number of refunds processed")
                .tag("type", "refund")
                .register(registry);

        refundsApproved = Counter.builder("payment.gateway.refunds.approved.total")
                .description("Total number of approved refunds")
                .tag("type", "refund")
                .register(registry);

        refundsRejected = Counter.builder("payment.gateway.refunds.rejected.total")
                .description("Total number of rejected refunds")
                .tag("type", "refund")
                .register(registry);

        // Refund timer
        refundProcessingTimer = Timer.builder("payment.gateway.refunds.processing.duration")
                .description("Refund processing time")
                .tag("type", "refund")
                .register(registry);

        // Refund amount distribution
        refundAmountSummary = DistributionSummary.builder("payment.gateway.refunds.amount")
                .description("Refund amounts distribution")
                .tag("type", "refund")
                .baseUnit("cents")
                .register(registry);

        // Merchant metrics
        merchantApiCalls = Counter.builder("payment.gateway.merchants.api.calls.total")
                .description("Total number of merchant API calls")
                .tag("type", "merchant")
                .register(registry);

        // Transaction metrics
        transactionsCreated = Counter.builder("payment.gateway.transactions.created.total")
                .description("Total number of transactions created")
                .tag("type", "transaction")
                .register(registry);

        transactionsCompleted = Counter.builder("payment.gateway.transactions.completed.total")
                .description("Total number of completed transactions")
                .tag("type", "transaction")
                .register(registry);
    }

    // Payment recording methods
    public void recordPaymentProcessed() {
        if (paymentsProcessed != null) paymentsProcessed.increment();
    }

    public void recordPaymentApproved() {
        if (paymentsApproved != null) paymentsApproved.increment();
        if (paymentsProcessed != null) paymentsProcessed.increment();
    }

    public void recordPaymentRejected() {
        if (paymentsRejected != null) paymentsRejected.increment();
        if (paymentsProcessed != null) paymentsProcessed.increment();
    }

    public void recordPaymentFailed() {
        if (paymentsFailed != null) paymentsFailed.increment();
    }

    public void recordPaymentAmount(long amountCents) {
        if (paymentAmountSummary != null) paymentAmountSummary.record(amountCents);
    }

    public Timer getPaymentProcessingTimer() {
        return paymentProcessingTimer;
    }

    // Refund recording methods
    public void recordRefundProcessed() {
        if (refundsProcessed != null) refundsProcessed.increment();
    }

    public void recordRefundApproved() {
        if (refundsApproved != null) refundsApproved.increment();
        if (refundsProcessed != null) refundsProcessed.increment();
    }

    public void recordRefundRejected() {
        if (refundsRejected != null) refundsRejected.increment();
        if (refundsProcessed != null) refundsProcessed.increment();
    }

    public void recordRefundAmount(long amountCents) {
        if (refundAmountSummary != null) refundAmountSummary.record(amountCents);
    }

    public Timer getRefundProcessingTimer() {
        return refundProcessingTimer;
    }

    // Merchant recording methods
    public void recordMerchantApiCall() {
        if (merchantApiCalls != null) merchantApiCalls.increment();
    }

    public void recordMerchantPayment(String merchantId, long amountCents) {
        paymentsPerMerchant
                .computeIfAbsent(merchantId, k -> Counter.builder("payment.gateway.merchant.payments.total")
                        .description("Total payments per merchant")
                        .tag("merchantId", k)
                        .register(registry))
                .increment();

        revenuePerMerchant
                .computeIfAbsent(merchantId, k -> DistributionSummary.builder("payment.gateway.merchant.revenue")
                        .description("Revenue per merchant")
                        .tag("merchantId", k)
                        .baseUnit("cents")
                        .register(registry))
                .record(amountCents);
    }

    // Transaction recording methods
    public void recordTransactionCreated() {
        if (transactionsCreated != null) transactionsCreated.increment();
    }

    public void recordTransactionCompleted() {
        if (transactionsCompleted != null) transactionsCompleted.increment();
    }
}
