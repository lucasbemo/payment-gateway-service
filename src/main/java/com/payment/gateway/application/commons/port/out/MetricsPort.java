package com.payment.gateway.application.commons.port.out;

import java.time.Duration;

public interface MetricsPort {

    void recordPaymentApproved();

    void recordPaymentFailed();

    void recordPaymentAmount(long amountCents);

    void recordPaymentProcessingDuration(Duration duration);

    void recordRefundApproved();

    void recordRefundAmount(long amountCents);
}
