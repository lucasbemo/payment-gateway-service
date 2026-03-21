package com.payment.gateway.application.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@Schema(description = "Customer account details")
public class CustomerResponse {

    @Schema(description = "Unique customer identifier", example = "cust_abc123")
    private final String id;

    @Schema(description = "Merchant ID that owns this customer", example = "merch_xyz789")
    private final String merchantId;

    @Schema(description = "Customer email address", example = "john@example.com")
    private final String email;

    @Schema(description = "Customer full name", example = "John Doe")
    private final String name;

    @Schema(description = "Customer phone number", example = "+1234567890")
    private final String phone;

    @Schema(description = "External ID from merchant's system", example = "EXT-12345")
    private final String externalId;

    @Schema(
        description = "Customer account status",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "SUSPENDED", "INACTIVE"}
    )
    private final String status;

    @Schema(description = "Account creation timestamp", example = "2026-03-20T10:00:00Z")
    private final Instant createdAt;
}
