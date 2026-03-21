package com.payment.gateway.infrastructure.merchant.adapter.in.rest;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.application.merchant.dto.RegisterMerchantCommand;
import com.payment.gateway.application.merchant.port.in.GetMerchantUseCase;
import com.payment.gateway.application.merchant.port.in.RegisterMerchantUseCase;
import com.payment.gateway.application.merchant.port.in.SuspendMerchantUseCase;
import com.payment.gateway.application.merchant.port.in.UpdateMerchantUseCase;
import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import com.payment.gateway.infrastructure.docs.MerchantApi;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController implements MerchantApi {

    private final RegisterMerchantUseCase registerMerchantUseCase;
    private final GetMerchantUseCase getMerchantUseCase;
    private final UpdateMerchantUseCase updateMerchantUseCase;
    private final SuspendMerchantUseCase suspendMerchantUseCase;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<MerchantResponse>> registerMerchant(
            @Valid @RequestBody CreateMerchantRequest request) {
        log.info("Registering merchant: {}", request.getEmail());
        var command = RegisterMerchantCommand.builder()
                .name(request.getName())
                .email(request.getEmail())
                .webhookUrl(request.getWebhookUrl())
                .build();
        var response = registerMerchantUseCase.registerMerchant(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Merchant registered successfully", response));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MerchantResponse>> getMerchant(@PathVariable String id) {
        log.info("Getting merchant: {}", id);
        var response = getMerchantUseCase.getMerchantById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MerchantResponse>> updateMerchant(
            @PathVariable String id,
            @Valid @RequestBody UpdateMerchantRequest request) {
        log.info("Updating merchant: {}", id);
        var response = updateMerchantUseCase.updateMerchant(id, request.getName(), request.getEmail(), request.getWebhookUrl());
        return ResponseEntity.ok(ApiResponse.success("Merchant updated successfully", response));
    }

    @Override
    @PostMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<MerchantResponse>> suspendMerchant(@PathVariable String id) {
        log.info("Suspending merchant: {}", id);
        var response = suspendMerchantUseCase.suspendMerchant(id);
        return ResponseEntity.ok(ApiResponse.success("Merchant suspended successfully", response));
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Request to register a new merchant")
    public static class CreateMerchantRequest {

        @Schema(description = "Merchant display name", example = "Acme Corporation", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name is required")
        private String name;

        @Schema(description = "Merchant email address", example = "contact@acme.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        private String email;

        @Schema(description = "Webhook URL for event notifications", example = "https://acme.com/webhooks")
        private String webhookUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Request to update merchant information")
    public static class UpdateMerchantRequest {

        @Schema(description = "New merchant display name", example = "Acme Corp Updated")
        private String name;

        @Schema(description = "New merchant email address", example = "new-contact@acme.com")
        private String email;

        @Schema(description = "New webhook URL", example = "https://acme.com/new-webhooks")
        private String webhookUrl;
    }
}