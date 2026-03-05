package com.payment.gateway.application.customer.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Response DTO for customer operations.
 */
@Getter
@Builder
public class CustomerResponse {

    private final String id;
    private final String merchantId;
    private final String email;
    private final String name;
    private final String phone;
    private final String externalId;
    private final String status;
    private final Instant createdAt;
}
