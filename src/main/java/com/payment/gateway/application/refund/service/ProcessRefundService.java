package com.payment.gateway.application.refund.service;

import com.payment.gateway.application.refund.dto.RefundResponse;
import com.payment.gateway.application.refund.port.in.ProcessRefundUseCase;
import com.payment.gateway.application.refund.port.out.RefundPaymentQueryPort;
import com.payment.gateway.application.refund.port.out.RefundQueryPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.payment.model.Payment;
import com.payment.gateway.domain.refund.model.Refund;
import com.payment.gateway.domain.refund.model.RefundStatus;
import com.payment.gateway.domain.refund.model.RefundType;
import com.payment.gateway.domain.transaction.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

/**
 * Application service for processing refunds.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProcessRefundService implements ProcessRefundUseCase {

    private final RefundQueryPort refundQueryPort;
    private final RefundPaymentQueryPort refundPaymentQueryPort;

    @Override
    public RefundResponse processRefund(String paymentId, String merchantId, Long amount,
                                         String refundIdempotencyKey, String reason) {
        log.info("Processing refund for payment: {} for merchant: {}", paymentId, merchantId);

        // Check for duplicate refund (idempotency)
        if (refundQueryPort.existsByIdempotencyKey(refundIdempotencyKey)) {
            log.info("Duplicate refund detected for idempotency key: {}", refundIdempotencyKey);
            Refund existingRefund = refundQueryPort.findByIdempotencyKey(refundIdempotencyKey).get();
            return mapToResponse(existingRefund);
        }

        // Get payment
        Payment payment = refundPaymentQueryPort.findPaymentById(paymentId)
                .orElseThrow(() -> new BusinessException("Payment not found: " + paymentId));

        // Validate merchant ownership
        if (!payment.getMerchantId().equals(merchantId)) {
            throw new BusinessException("Payment does not belong to merchant: " + merchantId);
        }

        // Validate refund amount doesn't exceed payment amount
        Long refundAmount = amount != null ? amount : payment.getAmount().getAmountInCents();
        if (refundAmount > payment.getAmount().getAmountInCents()) {
            throw new BusinessException("Refund amount cannot exceed payment amount");
        }

        // Get latest transaction
        Transaction transaction = refundPaymentQueryPort.findLatestTransactionByPaymentId(paymentId)
                .orElseThrow(() -> new BusinessException("No transaction found for payment: " + paymentId));

        // Determine refund type
        RefundType refundType = determineRefundType(payment, amount);

        // Create refund
        Refund refund = createRefund(payment, transaction, refundType, amount, refundIdempotencyKey, reason);

        // Save refund
        Refund savedRefund = refundQueryPort.saveRefund(refund);
        log.info("Refund created with id: {}", savedRefund.getId());

        // Process refund
        processRefundLogic(savedRefund);

        // Update refund status - first approve, then complete
        savedRefund.approve();
        savedRefund.complete();
        savedRefund = refundQueryPort.saveRefund(savedRefund);

        log.info("Refund processed successfully: {}", savedRefund.getId());
        return mapToResponse(savedRefund);
    }

    private RefundType determineRefundType(Payment payment, Long refundAmount) {
        if (refundAmount == null || refundAmount.equals(payment.getAmount().getAmountInCents())) {
            return RefundType.FULL;
        }
        return RefundType.PARTIAL;
    }

    private Refund createRefund(Payment payment, Transaction transaction, RefundType refundType,
                                 Long amount, String refundIdempotencyKey, String reason) {
        Long refundAmount = amount != null ? amount : payment.getAmount().getAmountInCents();
        Money refundMoney = Money.of(refundAmount, Currency.getInstance(payment.getCurrency()));

        return Refund.create(
                payment.getId(),
                transaction.getId(),
                payment.getMerchantId(),
                refundType,
                refundMoney,
                payment.getCurrency(),
                refundIdempotencyKey,
                reason
        );
    }

    private void processRefundLogic(Refund refund) {
        log.debug("Processing refund {} with external provider", refund.getId());
        // In a real implementation, this would call the external payment provider
        // For now, we just simulate processing - the status transition happens after approval
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
