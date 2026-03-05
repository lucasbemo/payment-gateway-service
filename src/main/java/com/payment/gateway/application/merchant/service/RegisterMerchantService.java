package com.payment.gateway.application.merchant.service;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.application.merchant.dto.RegisterMerchantCommand;
import com.payment.gateway.application.merchant.port.in.RegisterMerchantUseCase;
import com.payment.gateway.application.merchant.port.out.MerchantCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.utils.CryptoUtils;
import com.payment.gateway.commons.utils.IdGenerator;
import com.payment.gateway.domain.merchant.model.Merchant;
import com.payment.gateway.domain.merchant.model.MerchantConfiguration;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service for registering merchants.
 */
@Slf4j
public class RegisterMerchantService implements RegisterMerchantUseCase {

    private final MerchantCommandPort merchantCommandPort;

    public RegisterMerchantService(MerchantCommandPort merchantCommandPort) {
        this.merchantCommandPort = merchantCommandPort;
    }

    @Override
    public MerchantResponse registerMerchant(RegisterMerchantCommand command) {
        log.info("Registering merchant with email: {}", command.getEmail());

        // Check for duplicate email
        if (merchantCommandPort.existsByEmail(command.getEmail())) {
            throw new BusinessException("Merchant with this email already exists: " + command.getEmail());
        }

        // Generate API credentials
        String apiKey = IdGenerator.generateShortUuid();
        String apiSecret = IdGenerator.generateShortUuid() + "_" + generateRandomString(16);

        // Hash credentials
        String apiKeyHash = CryptoUtils.hash(apiKey);
        String apiSecretHash = CryptoUtils.hash(apiSecret);

        // Create merchant
        Merchant merchant = Merchant.register(
                command.getName(),
                command.getEmail(),
                apiKeyHash,
                apiSecretHash,
                command.getWebhookUrl(),
                MerchantConfiguration.empty()
        );

        // Save merchant
        Merchant savedMerchant = merchantCommandPort.saveMerchant(merchant);
        log.info("Merchant registered with id: {}", savedMerchant.getId());

        return mapToResponse(savedMerchant, apiKey, apiSecret);
    }

    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt((int) (Math.random() * characters.length())));
        }
        return sb.toString();
    }

    private MerchantResponse mapToResponse(Merchant merchant, String apiKey, String apiSecret) {
        return MerchantResponse.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .status(merchant.getStatus().name())
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .webhookUrl(merchant.getWebhookUrl())
                .createdAt(merchant.getCreatedAt())
                .build();
    }
}
