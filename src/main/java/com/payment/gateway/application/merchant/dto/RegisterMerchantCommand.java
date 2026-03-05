package com.payment.gateway.application.merchant.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Command for registering a merchant.
 */
@Getter
@Builder
public class RegisterMerchantCommand {

    private final String name;
    private final String email;
    private final String webhookUrl;
}
