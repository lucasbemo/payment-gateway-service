package com.payment.gateway.application.merchant.service;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.application.merchant.port.in.UpdateMerchantUseCase;
import com.payment.gateway.application.merchant.port.out.MerchantCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.merchant.model.Merchant;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service for updating merchants.
 */
@Slf4j
public class UpdateMerchantService implements UpdateMerchantUseCase {

    private final MerchantCommandPort merchantCommandPort;

    public UpdateMerchantService(MerchantCommandPort merchantCommandPort) {
        this.merchantCommandPort = merchantCommandPort;
    }

    @Override
    public MerchantResponse updateMerchant(String merchantId, String name, String email, String webhookUrl) {
        log.info("Updating merchant: {}", merchantId);

        Merchant merchant = merchantCommandPort.findById(merchantId)
                .orElseThrow(() -> new BusinessException("Merchant not found: " + merchantId));

        // Update fields
        if (name != null && !name.isBlank()) {
            updateName(merchant, name);
        }

        if (email != null && !email.isBlank()) {
            updateEmail(merchant, email);
        }

        if (webhookUrl != null) {
            merchant.updateWebhookUrl(webhookUrl);
        }

        Merchant updatedMerchant = merchantCommandPort.saveMerchant(merchant);
        log.info("Merchant updated successfully: {}", merchantId);

        return mapToResponse(updatedMerchant);
    }

    private void updateName(Merchant merchant, String name) {
        if (name.isBlank()) {
            throw new BusinessException("Merchant name cannot be empty");
        }
        try {
            java.lang.reflect.Field nameField = Merchant.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(merchant, name);
        } catch (Exception e) {
            throw new BusinessException("Failed to update merchant name");
        }
    }

    private void updateEmail(Merchant merchant, String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessException("Invalid email address: " + email);
        }

        // Check for duplicate email (excluding current merchant)
        merchantCommandPort.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(merchant.getId())) {
                throw new BusinessException("Merchant with this email already exists");
            }
        });

        try {
            java.lang.reflect.Field emailField = Merchant.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(merchant, email);
        } catch (Exception e) {
            throw new BusinessException("Failed to update merchant email");
        }
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
