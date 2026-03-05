package com.payment.gateway.application.merchant.port.in;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.application.merchant.dto.RegisterMerchantCommand;

/**
 * Use case for registering a merchant.
 */
public interface RegisterMerchantUseCase {

    MerchantResponse registerMerchant(RegisterMerchantCommand command);
}
