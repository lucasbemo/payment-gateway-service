package com.payment.gateway.application.merchant.service;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.application.merchant.port.in.SuspendMerchantUseCase;
import com.payment.gateway.application.merchant.port.out.MerchantCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.merchant.model.Merchant;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service for suspending merchants.
 */
@Slf4j
public class SuspendMerchantService implements SuspendMerchantUseCase {

    private final MerchantCommandPort merchantCommandPort;

    public SuspendMerchantService(MerchantCommandPort merchantCommandPort) {
        this.merchantCommandPort = merchantCommandPort;
    }

    @Override
    public MerchantResponse suspendMerchant(String merchantId) {
        log.info("Suspending merchant: {}", merchantId);

        Merchant merchant = merchantCommandPort.findById(merchantId)
                .orElseThrow(() -> new BusinessException("Merchant not found: " + merchantId));

        // Suspend the merchant
        merchant.suspend();

        Merchant suspendedMerchant = merchantCommandPort.saveMerchant(merchant);
        log.info("Merchant suspended successfully: {}", merchantId);

        return mapToResponse(suspendedMerchant);
    }

    private MerchantResponse mapToResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .status(merchant.getStatus().name())
                .webhookUrl(merchant.getWebhookUrl())
                .createdAt(merchant.getCreatedAt())
                
                .build();
    }
}
