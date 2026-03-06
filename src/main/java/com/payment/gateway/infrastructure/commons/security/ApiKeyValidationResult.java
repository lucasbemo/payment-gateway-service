package com.payment.gateway.infrastructure.commons.security;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

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
