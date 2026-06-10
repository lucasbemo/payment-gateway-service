package com.payment.gateway.application.merchant.service;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.application.merchant.port.in.ActivateMerchantUseCase;
import com.payment.gateway.application.merchant.port.out.MerchantCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.merchant.model.Merchant;
import com.payment.gateway.domain.merchant.model.MerchantStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for activating merchants.
 */
@Slf4j
@Service
@Transactional
public class ActivateMerchantService implements ActivateMerchantUseCase {

    private final MerchantCommandPort merchantCommandPort;

    public ActivateMerchantService(MerchantCommandPort merchantCommandPort) {
        this.merchantCommandPort = merchantCommandPort;
    }

    @Override
    public MerchantResponse activateMerchant(String merchantId) {
        log.info("Activating merchant: {}", merchantId);

        Merchant merchant = merchantCommandPort.findById(merchantId)
                .orElseThrow(() -> new BusinessException("Merchant not found: " + merchantId));

        // Activate the merchant (reactivate when coming from suspension)
        if (merchant.getStatus() == MerchantStatus.SUSPENDED) {
            merchant.reactivate();
        } else {
            merchant.activate();
        }

        Merchant activatedMerchant = merchantCommandPort.saveMerchant(merchant);
        log.info("Merchant activated successfully: {}", merchantId);

        return mapToResponse(activatedMerchant);
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
