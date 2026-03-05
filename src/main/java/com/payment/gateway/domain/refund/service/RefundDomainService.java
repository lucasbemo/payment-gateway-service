package com.payment.gateway.domain.refund.service;

import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.refund.exception.InvalidRefundAmountException;
import com.payment.gateway.domain.refund.exception.RefundNotFoundException;
import com.payment.gateway.domain.refund.exception.RefundProcessingException;
import com.payment.gateway.domain.refund.model.Refund;
import com.payment.gateway.domain.refund.model.RefundItem;
import com.payment.gateway.domain.refund.model.RefundStatus;
import com.payment.gateway.domain.refund.model.RefundType;
import com.payment.gateway.domain.refund.port.RefundEventPublisherPort;
import com.payment.gateway.domain.refund.port.RefundRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Refund domain service.
 * Contains business logic for refund operations.
 */
@Slf4j
@RequiredArgsConstructor
public class RefundDomainService {

    private final RefundRepositoryPort repository;
    private final RefundEventPublisherPort eventPublisher;

    public Refund createRefund(String paymentId, String transactionId, String merchantId,
                                RefundType type, Money amount, String currency,
                                String refundIdempotencyKey, String reason) {
        log.info("Creating {} refund for payment {} with amount {}", type, paymentId, amount);

        if (repository.existsByRefundIdempotencyKey(refundIdempotencyKey)) {
            throw new RefundProcessingException("Refund with this idempotency key already exists");
        }

        Refund refund = Refund.create(paymentId, transactionId, merchantId, type, amount, currency,
                                       refundIdempotencyKey, reason);
        refund = repository.save(refund);
        eventPublisher.publishRefundCreated(refund);

        return refund;
    }

    public Refund createRefundWithItems(String paymentId, String transactionId, String merchantId,
                                         RefundType type, Money amount, String currency,
                                         String refundIdempotencyKey, String reason,
                                         List<RefundItem> items) {
        log.info("Creating {} refund with {} items for payment {}", type, items.size(), paymentId);

        Refund refund = createRefund(paymentId, transactionId, merchantId, type, amount, currency,
                                      refundIdempotencyKey, reason);
        refund.addItems(items);
        refund = repository.save(refund);

        return refund;
    }

    public Refund approveRefund(String refundId) {
        log.info("Approving refund {}", refundId);

        Refund refund = getRefundOrThrow(refundId);

        if (!refund.isPending()) {
            throw new RefundProcessingException(
                "Can only approve pending refunds. Current status: " + refund.getStatus()
            );
        }

        refund.approve();
        refund = repository.save(refund);
        eventPublisher.publishRefundCompleted(refund);

        return refund;
    }

    public Refund rejectRefund(String refundId, String rejectionReason) {
        log.info("Rejecting refund {} with reason: {}", refundId, rejectionReason);

        Refund refund = getRefundOrThrow(refundId);
        refund.reject(rejectionReason);
        refund = repository.save(refund);
        eventPublisher.publishRefundCompleted(refund);

        return refund;
    }

    public Refund completeRefund(String refundId) {
        log.info("Completing refund {}", refundId);

        Refund refund = getRefundOrThrow(refundId);

        if (refund.getStatus() != RefundStatus.APPROVED && refund.getStatus() != RefundStatus.PROCESSING) {
            throw new RefundProcessingException(
                "Can only complete approved or processing refunds. Current status: " + refund.getStatus()
            );
        }

        refund.complete();
        refund = repository.save(refund);
        eventPublisher.publishRefundCompleted(refund);

        return refund;
    }

    public Refund failRefund(String refundId, String errorCode, String errorMessage) {
        log.error("Failing refund {} with error: {} - {}", refundId, errorCode, errorMessage);

        Refund refund = getRefundOrThrow(refundId);
        refund.fail(errorCode, errorMessage);
        refund = repository.save(refund);
        eventPublisher.publishRefundFailed(refund);

        return refund;
    }

    public Refund cancelRefund(String refundId, String cancellationReason) {
        log.info("Cancelling refund {} with reason: {}", refundId, cancellationReason);

        Refund refund = getRefundOrThrow(refundId);

        if (refund.isTerminal()) {
            throw new RefundProcessingException(
                "Cannot cancel terminal refund. Current status: " + refund.getStatus()
            );
        }

        refund.cancel(cancellationReason);
        refund = repository.save(refund);
        eventPublisher.publishRefundCompleted(refund);

        return refund;
    }

    public Refund retryRefund(String refundId) {
        log.info("Retrying refund {}", refundId);

        Refund refund = getRefundOrThrow(refundId);
        refund.retry();
        return repository.save(refund);
    }

    public Optional<Refund> getRefund(String refundId) {
        return repository.findById(refundId);
    }

    public Refund getRefundOrThrow(String refundId) {
        return repository.findById(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));
    }

    public Optional<Refund> getRefundByPaymentId(String paymentId) {
        return repository.findFirstByPaymentId(paymentId);
    }

    public List<Refund> getRefundsByPaymentId(String paymentId) {
        return repository.findAllByPaymentId(paymentId);
    }

    public List<Refund> getRefundsByMerchantId(String merchantId) {
        return repository.findByMerchantId(merchantId);
    }

    public List<Refund> getRefundsByStatus(RefundStatus status) {
        return repository.findByStatus(status);
    }

    public boolean hasRefundWithIdempotencyKey(String refundIdempotencyKey) {
        return repository.existsByRefundIdempotencyKey(refundIdempotencyKey);
    }

    public Refund updateGatewayRefundId(String refundId, String gatewayRefundId) {
        log.info("Updating gateway refund id for {}", refundId);

        Refund refund = getRefundOrThrow(refundId);
        refund.updateGatewayRefundId(gatewayRefundId);

        return repository.save(refund);
    }
}
