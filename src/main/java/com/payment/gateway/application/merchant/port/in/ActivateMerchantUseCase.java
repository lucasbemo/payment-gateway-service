package com.payment.gateway.application.merchant.port.in;

import com.payment.gateway.application.merchant.dto.MerchantResponse;

/**
 * Use case for activating a merchant.
 */
public interface ActivateMerchantUseCase {

    MerchantResponse activateMerchant(String merchantId);
}
