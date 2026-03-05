package com.payment.gateway.application.refund.port.in;

import com.payment.gateway.application.refund.dto.RefundResponse;

/**
 * Use case for processing a refund.
 */
public interface ProcessRefundUseCase {

    RefundResponse processRefund(String paymentId, String merchantId, Long amount, String refundIdempotencyKey, String reason);
}
