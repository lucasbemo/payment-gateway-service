package com.payment.gateway.infrastructure.commons.security;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * Result of API key validation.
 */
@Getter
@Builder
public class ApiKeyValidationResult {

    private final boolean valid;
    private final String merchantId;
    private final List<String> roles;
    private final String message;

    public static ApiKeyValidationResult success(String merchantId, List<String> roles) {
        return ApiKeyValidationResult.builder()
                .valid(true)
                .merchantId(merchantId)
                .roles(roles)
                .message("API key validated successfully")
                .build();
    }

    public static ApiKeyValidationResult failure(String message) {
        return ApiKeyValidationResult.builder()
                .valid(false)
                .merchantId(null)
                .roles(null)
                .message(message)
                .build();
    }
}
