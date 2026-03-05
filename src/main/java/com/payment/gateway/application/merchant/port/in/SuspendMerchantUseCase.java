package com.payment.gateway.application.merchant.port.in;

import com.payment.gateway.application.merchant.dto.MerchantResponse;

/**
 * Use case for suspending a merchant.
 */
public interface SuspendMerchantUseCase {

    MerchantResponse suspendMerchant(String merchantId);
}
