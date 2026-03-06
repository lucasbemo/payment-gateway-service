package com.payment.gateway.application.merchant.service;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.application.merchant.port.in.GetMerchantUseCase;
import com.payment.gateway.application.merchant.port.out.MerchantCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.merchant.model.Merchant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for getting merchant information.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GetMerchantService implements GetMerchantUseCase {

    private final MerchantCommandPort merchantCommandPort;

    @Override
    public MerchantResponse getMerchantById(String merchantId) {
        log.info("Getting merchant by id: {}", merchantId);

        Merchant merchant = merchantCommandPort.findById(merchantId)
                .orElseThrow(() -> new BusinessException("Merchant not found: " + merchantId));

        return mapToResponse(merchant);
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
