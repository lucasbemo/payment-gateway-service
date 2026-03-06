package com.payment.gateway.infrastructure.commons.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

/**
 * Custom metrics binder for payment gateway specific metrics.
 */
@Component
public class CustomMetricsBinder implements MeterBinder {

    private Counter paymentsProcessed;
    private Counter paymentsFailed;
    private Counter refundsProcessed;
    private Timer paymentProcessingTimer;

    @Override
    public void bindTo(MeterRegistry registry) {
        paymentsProcessed = Counter.builder("payment.gateway.payments.processed")
                .description("Total number of payments processed")
                .register(registry);

        paymentsFailed = Counter.builder("payment.gateway.payments.failed")
                .description("Total number of failed payments")
                .register(registry);

        refundsProcessed = Counter.builder("payment.gateway.refunds.processed")
                .description("Total number of refunds processed")
                .register(registry);

        paymentProcessingTimer = Timer.builder("payment.gateway.payments.processing.time")
                .description("Payment processing time")
                .register(registry);
    }

    public void recordPaymentProcessed() {
        if (paymentsProcessed != null) paymentsProcessed.increment();
    }

    public void recordPaymentFailed() {
        if (paymentsFailed != null) paymentsFailed.increment();
    }

    public void recordRefundProcessed() {
        if (refundsProcessed != null) refundsProcessed.increment();
    }

    public Timer getPaymentProcessingTimer() {
        return paymentProcessingTimer;
    }
}
