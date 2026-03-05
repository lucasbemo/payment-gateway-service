package com.payment.gateway.application.refund.port.in;

import com.payment.gateway.application.refund.dto.RefundResponse;

/**
 * Use case for getting refund information.
 */
public interface GetRefundUseCase {

    RefundResponse getRefundById(String refundId, String merchantId);
}
