package com.payment.gateway.domain.payment.service;

import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.model.Money;
import com.payment.gateway.domain.payment.model.Payment;
import com.payment.gateway.domain.payment.model.PaymentMetadata;
import com.payment.gateway.domain.payment.model.PaymentMethod;
import com.payment.gateway.domain.payment.model.PaymentItem;
import com.payment.gateway.domain.payment.port.PaymentRepositoryPort;
import com.payment.gateway.domain.payment.exception.DuplicatePaymentException;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Domain service for Payment operations.
 */
@RequiredArgsConstructor
public class PaymentDomainService {

    private final PaymentRepositoryPort paymentRepository;

    /**
     * Process a new payment.
     */
    public Payment processPayment(String merchantId,
                                   Money amount,
                                   String currency,
                                   PaymentMethod paymentMethod,
                                   String idempotencyKey,
                                   String description,
                                   PaymentMetadata metadata,
                                   List<PaymentItem> items,
                                   String customerId) {
        // Check for duplicate payment
        if (paymentRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicatePaymentException(idempotencyKey);
        }

        // Create and save payment
        Payment payment = Payment.create(
            merchantId,
            amount,
            currency,
            paymentMethod,
            idempotencyKey,
            description,
            metadata,
            items,
            customerId
        );

        return paymentRepository.save(payment);
    }

    /**
     * Get payment by ID.
     */
    public Payment getPayment(String paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new BusinessException("Payment not found: " + paymentId));
    }

    /**
     * Get payment by idempotency key.
     */
    public Payment getPaymentByIdempotencyKey(String idempotencyKey) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey)
            .orElseThrow(() -> new BusinessException("Payment not found for idempotency key: " + idempotencyKey));
    }

    /**
     * Validate payment amount.
     */
    public void validatePaymentAmount(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new BusinessException("Payment amount must be greater than zero");
        }
    }

    /**
     * Validate currency.
     */
    public void validateCurrency(String currency) {
        if (currency == null || currency.length() != 3) {
            throw new BusinessException("Invalid currency code: " + currency);
        }
    }
}
