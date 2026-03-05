package com.payment.gateway.application.refund.port.in;

import com.payment.gateway.application.refund.dto.RefundResponse;

/**
 * Use case for canceling a refund.
 */
public interface CancelRefundUseCase {

    RefundResponse cancelRefund(String refundId, String merchantId, String reason);
}
