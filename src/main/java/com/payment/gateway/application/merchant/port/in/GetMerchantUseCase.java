package com.payment.gateway.application.merchant.port.in;

import com.payment.gateway.application.merchant.dto.MerchantResponse;

/**
 * Use case for getting merchant information.
 */
public interface GetMerchantUseCase {

    MerchantResponse getMerchantById(String merchantId);
}
