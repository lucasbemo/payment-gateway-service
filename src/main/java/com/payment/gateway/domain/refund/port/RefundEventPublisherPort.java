package com.payment.gateway.domain.refund.port;

import com.payment.gateway.domain.refund.model.Refund;

/**
 * Refund event publisher port interface.
 */
public interface RefundEventPublisherPort {
    void publishRefundCreated(Refund refund);
    void publishRefundCompleted(Refund refund);
    void publishRefundFailed(Refund refund);
}
