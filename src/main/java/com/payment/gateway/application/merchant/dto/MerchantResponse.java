package com.payment.gateway.application.merchant.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Response DTO for merchant operations.
 */
@Getter
@Builder
public class MerchantResponse {

    private final String id;
    private final String name;
    private final String email;
    private final String status;
    private final String apiKey;
    private final String apiSecret;
    private final String webhookUrl;
    private final Instant createdAt;
}
