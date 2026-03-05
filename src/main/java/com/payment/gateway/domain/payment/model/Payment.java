package com.payment.gateway.domain.payment.model;

import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.model.Money;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root representing a Payment.
 */
@Getter
public class Payment {

    private String id;
    private String merchantId;
    private String customerId;
    private String paymentMethodId;
    private Money amount;
    private String currency;
    private PaymentStatus status;
    private String idempotencyKey;
    private String description;
    private PaymentMetadata metadata;
    private List<PaymentItem> items;
    private Instant createdAt;
    private Instant updatedAt;

    private Payment() {}

    /**
     * Create a new payment.
     */
    public static Payment create(String merchantId,
                                  Money amount,
                                  String currency,
                                  PaymentMethod paymentMethod,
                                  String idempotencyKey,
                                  String description,
                                  PaymentMetadata metadata,
                                  List<PaymentItem> items,
                                  String customerId) {
        validatePaymentData(merchantId, amount, currency, paymentMethod, idempotencyKey);

        Payment payment = new Payment();
        payment.id = UUID.randomUUID().toString();
        payment.merchantId = merchantId;
        payment.customerId = customerId;
        payment.paymentMethodId = paymentMethod != null ? paymentMethod.name() : null;
        payment.amount = amount;
        payment.currency = currency;
        payment.status = PaymentStatus.PENDING;
        payment.idempotencyKey = idempotencyKey;
        payment.description = description;
        payment.metadata = metadata != null ? metadata : PaymentMetadata.empty();
        payment.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        payment.createdAt = Instant.now();
        payment.updatedAt = Instant.now();

        return payment;
    }

    private static void validatePaymentData(String merchantId, Money amount, String currency,
                                            PaymentMethod paymentMethod, String idempotencyKey) {
        if (merchantId == null || merchantId.isBlank()) {
            throw new BusinessException("Merchant ID is required");
        }
        if (amount == null || amount.isZero()) {
            throw new BusinessException("Payment amount must be greater than zero");
        }
        if (currency == null || currency.length() != 3) {
            throw new BusinessException("Invalid currency code: " + currency);
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException("Idempotency key is required");
        }
    }

    /**
     * Authorize the payment.
     */
    public void authorize() {
        validateTransition(PaymentStatus.AUTHORIZED);
        this.status = PaymentStatus.AUTHORIZED;
        this.updatedAt = Instant.now();
    }

    /**
     * Capture the payment.
     */
    public void capture() {
        validateTransition(PaymentStatus.CAPTURED);
        this.status = PaymentStatus.CAPTURED;
        this.updatedAt = Instant.now();
    }

    /**
     * Mark payment as failed.
     */
    public void fail() {
        validateTransition(PaymentStatus.FAILED);
        this.status = PaymentStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    /**
     * Cancel the payment.
     */
    public void cancel() {
        validateTransition(PaymentStatus.CANCELLED);
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * Mark payment as refunded.
     */
    public void refund() {
        validateTransition(PaymentStatus.REFUNDED);
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = Instant.now();
    }

    private void validateTransition(PaymentStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new BusinessException(
                "Cannot transition from " + this.status + " to " + newStatus);
        }
    }

    /**
     * Check if payment is in a terminal state.
     */
    public boolean isTerminal() {
        return status.isTerminal();
    }

    /**
     * Check if payment was successful.
     */
    public boolean isSuccess() {
        return status.isSuccess();
    }

    /**
     * Validate that this payment belongs to the given merchant.
     */
    public void validateOwnership(String merchantId) {
        if (!this.merchantId.equals(merchantId)) {
            throw new BusinessException("Payment does not belong to merchant: " + merchantId);
        }
    }
}
