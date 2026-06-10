package com.payment.gateway.commons.exception;

/**
 * Exception thrown when a payment is declined by the payment provider.
 *
 * <p>Unlike a generic {@link BusinessException}, a decline is an expected business
 * outcome: the FAILED payment and its PAYMENT_FAILED outbox event must be persisted,
 * so this exception is configured as {@code noRollbackFor} on the payment
 * processing transaction.
 */
public class PaymentDeclinedException extends BusinessException {

    public PaymentDeclinedException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    /**
     * Provider error code (e.g. {@code card_declined}).
     */
    public String getErrorCode() {
        return getCode();
    }

    /**
     * Human-readable decline reason from the provider.
     */
    public String getErrorMessage() {
        return getMessage();
    }
}
