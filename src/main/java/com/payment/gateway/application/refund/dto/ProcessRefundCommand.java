package com.payment.gateway.application.refund.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Command for processing a refund.
 */
@Getter
@Builder
public class ProcessRefundCommand {

    private final String paymentId;
    private final String transactionId;
    private final String merchantId;
    private final Long amount;
    private final String refundIdempotencyKey;
    private final String reason;
    private final String type; // FULL or PARTIAL
}
