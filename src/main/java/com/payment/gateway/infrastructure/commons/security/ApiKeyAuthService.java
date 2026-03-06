package com.payment.gateway.infrastructure.commons.security;

import com.payment.gateway.application.payment.port.out.MerchantQueryPort;
import com.payment.gateway.domain.merchant.model.Merchant;
import com.payment.gateway.domain.merchant.model.MerchantStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for validating API keys against merchant credentials.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyAuthService {

    private final MerchantQueryPort merchantQueryPort;
    private final PasswordEncoder passwordEncoder;

    /**
     * Validates the API key and secret for a merchant.
     *
     * @param apiKey the API key (public identifier)
     * @param apiSecret the API secret (private credential)
     * @return validation result with merchant details if valid
     */
    public ApiKeyValidationResult validateApiKey(String apiKey, String apiSecret) {
        if (apiKey == null || apiKey.isBlank()) {
            return ApiKeyValidationResult.failure("API key is required");
        }

        if (apiSecret == null || apiSecret.isBlank()) {
            return ApiKeyValidationResult.failure("API secret is required");
        }

        try {
            Merchant merchant = merchantQueryPort.findByApiKey(apiKey)
                    .orElse(null);

            if (merchant == null) {
                log.warn("Merchant not found with API key: {}", maskApiKey(apiKey));
                return ApiKeyValidationResult.failure("Invalid API key");
            }

            // Validate API secret
            if (!passwordEncoder.matches(apiSecret, merchant.getApiSecretHash())) {
                log.warn("Invalid API secret for merchant: {}", merchant.getId());
                return ApiKeyValidationResult.failure("Invalid API secret");
            }

            // Check merchant status
            if (merchant.getStatus() == MerchantStatus.SUSPENDED) {
                log.warn("Merchant is suspended: {}", merchant.getId());
                return ApiKeyValidationResult.failure("Merchant account is suspended");
            }

            if (merchant.getStatus() == MerchantStatus.PENDING) {
                log.warn("Merchant account is pending activation: {}", merchant.getId());
                return ApiKeyValidationResult.failure("Merchant account is not yet activated");
            }

            // Determine roles based on merchant status
            List<String> roles = determineRoles(merchant);

            log.info("Successfully authenticated merchant: {} with roles: {}",
                    merchant.getId(), roles);

            return ApiKeyValidationResult.success(merchant.getId(), roles);

        } catch (Exception e) {
            log.error("Error validating API key: {}", e.getMessage(), e);
            return ApiKeyValidationResult.failure("Error validating credentials");
        }
    }

    private List<String> determineRoles(Merchant merchant) {
        // All merchants get MERCHANT role by default
        List<String> roles = new java.util.ArrayList<>();
        roles.add(RbacConfig.ROLE_MERCHANT);

        // Add DEVELOPER role if merchant has developer access
        if (merchant.getConfiguration() != null &&
            merchant.getConfiguration().hasDeveloperAccess()) {
            roles.add(RbacConfig.ROLE_DEVELOPER);
        }

        return roles;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 4) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****";
    }
}
