package com.payment.gateway.domain.merchant.exception;

import com.payment.gateway.commons.exception.BusinessException;

/**
 * Exception thrown when merchant configuration is invalid.
 */
public class MerchantConfigurationException extends BusinessException {

    public MerchantConfigurationException(String message) {
        super("MERCHANT_CONFIG_ERROR", message);
    }
}
