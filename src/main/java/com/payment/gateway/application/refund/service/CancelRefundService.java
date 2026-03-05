package com.payment.gateway.application.refund.service;

import com.payment.gateway.application.refund.dto.RefundResponse;
import com.payment.gateway.application.refund.port.in.CancelRefundUseCase;
import com.payment.gateway.application.refund.port.out.RefundQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.refund.model.Refund;
import com.payment.gateway.domain.refund.model.RefundStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service for canceling refunds.
 */
@Slf4j
@RequiredArgsConstructor
public class CancelRefundService implements CancelRefundUseCase {

    private final RefundQueryPort refundQueryPort;

    @Override
    public RefundResponse cancelRefund(String refundId, String merchantId, String reason) {
        log.info("Canceling refund: {} for merchant: {}", refundId, merchantId);

        Refund refund = refundQueryPort.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found: " + refundId));

        // Validate merchant ownership
        if (!refund.getMerchantId().equals(merchantId)) {
            throw new BusinessException("Refund does not belong to merchant: " + merchantId);
        }

        // Validate refund can be canceled
        if (refund.getStatus().isTerminal()) {
            throw new BusinessException("Cannot cancel refund in terminal state: " + refund.getStatus());
        }

        // Cancel refund
        refund.cancel(reason);
        Refund savedRefund = refundQueryPort.saveRefund(refund);

        log.info("Refund canceled successfully: {}", refundId);
        return mapToResponse(savedRefund);
    }

    private RefundResponse mapToResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .paymentId(refund.getPaymentId())
                .transactionId(refund.getTransactionId())
                .merchantId(refund.getMerchantId())
                .amount(refund.getAmount().getAmountInCents())
                .currency(refund.getCurrency())
                .status(refund.getStatus().name())
                .type(refund.getType().name())
                .reason(refund.getReason())
                .createdAt(refund.getCreatedAt())
                .processedAt(refund.getProcessedAt())
                .build();
    }
}
