package com.payment.gateway.application.refund.service;

import com.payment.gateway.application.refund.dto.RefundResponse;
import com.payment.gateway.application.refund.port.in.GetRefundUseCase;
import com.payment.gateway.application.refund.port.out.RefundQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.refund.model.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service for getting refund information.
 */
@Slf4j
@RequiredArgsConstructor
public class GetRefundService implements GetRefundUseCase {

    private final RefundQueryPort refundQueryPort;

    @Override
    public RefundResponse getRefundById(String refundId, String merchantId) {
        log.info("Getting refund by id: {} for merchant: {}", refundId, merchantId);

        Refund refund = refundQueryPort.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found: " + refundId));

        // Validate merchant ownership
        if (!refund.getMerchantId().equals(merchantId)) {
            throw new BusinessException("Refund does not belong to merchant: " + merchantId);
        }

        return mapToResponse(refund);
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
