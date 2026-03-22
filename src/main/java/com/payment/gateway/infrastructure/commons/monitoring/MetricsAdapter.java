package com.payment.gateway.infrastructure.commons.monitoring;

import com.payment.gateway.application.commons.port.out.MetricsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricsAdapter implements MetricsPort {

    private final CustomMetricsBinder customMetricsBinder;

    @Override
    public void recordPaymentApproved() {
        customMetricsBinder.recordPaymentApproved();
    }

    @Override
    public void recordPaymentFailed() {
        customMetricsBinder.recordPaymentFailed();
    }

    @Override
    public void recordPaymentAmount(long amountCents) {
        customMetricsBinder.recordPaymentAmount(amountCents);
    }

    @Override
    public void recordRefundApproved() {
        customMetricsBinder.recordRefundApproved();
    }

    @Override
    public void recordRefundAmount(long amountCents) {
        customMetricsBinder.recordRefundAmount(amountCents);
    }
}