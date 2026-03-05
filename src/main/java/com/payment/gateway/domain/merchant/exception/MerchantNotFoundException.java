package com.payment.gateway.domain.merchant.exception;

import com.payment.gateway.commons.exception.DomainException;

/**
 * Exception thrown when a merchant is not found.
 */
public class MerchantNotFoundException extends DomainException {

    public MerchantNotFoundException(String merchantId) {
        super("MERCHANT_NOT_FOUND", "Merchant not found with id: " + merchantId);
    }
}
