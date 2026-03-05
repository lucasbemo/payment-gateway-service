package com.payment.gateway.application.merchant.port.in;

import com.payment.gateway.application.merchant.dto.MerchantResponse;

/**
 * Use case for updating a merchant.
 */
public interface UpdateMerchantUseCase {

    MerchantResponse updateMerchant(String merchantId, String name, String email, String webhookUrl);
}
