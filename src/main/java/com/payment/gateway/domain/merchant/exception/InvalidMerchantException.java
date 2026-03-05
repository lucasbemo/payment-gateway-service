package com.payment.gateway.domain.merchant.exception;

import com.payment.gateway.commons.exception.BusinessException;

/**
 * Exception thrown when merchant data is invalid.
 */
public class InvalidMerchantException extends BusinessException {

    public InvalidMerchantException(String message) {
        super("INVALID_MERCHANT", message);
    }
}
