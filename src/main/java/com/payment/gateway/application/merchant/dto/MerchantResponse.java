package com.payment.gateway.application.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@Schema(description = "Merchant account details")
public class MerchantResponse {

    @Schema(description = "Unique merchant identifier", example = "merch_abc123")
    private final String id;

    @Schema(description = "Merchant display name", example = "Acme Corporation")
    private final String name;

    @Schema(description = "Merchant email address", example = "contact@acme.com")
    private final String email;

    @Schema(
        description = "Merchant account status",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "SUSPENDED", "PENDING", "CLOSED"}
    )
    private final String status;

    @Schema(description = "API key for authentication (shown only on registration)", example = "pk_live_abc123...")
    private final String apiKey;

    @Schema(description = "API secret for authentication (shown only on registration)", example = "sk_live_xyz789...")
    private final String apiSecret;

    @Schema(description = "Webhook URL for event notifications", example = "https://acme.com/webhooks")
    private final String webhookUrl;

    @Schema(description = "Account creation timestamp", example = "2026-03-20T10:00:00Z")
    private final Instant createdAt;
}
