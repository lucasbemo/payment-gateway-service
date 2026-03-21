package com.payment.gateway.infrastructure.merchant.adapter.in.rest;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.application.merchant.dto.RegisterMerchantCommand;
import com.payment.gateway.application.merchant.port.in.GetMerchantUseCase;
import com.payment.gateway.application.merchant.port.in.RegisterMerchantUseCase;
import com.payment.gateway.application.merchant.port.in.SuspendMerchantUseCase;
import com.payment.gateway.application.merchant.port.in.UpdateMerchantUseCase;
import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import com.payment.gateway.infrastructure.docs.MerchantApi;
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
    public static class CreateMerchantRequest {
        @NotBlank(message = "Name is required")
        private String name;
        @NotBlank(message = "Email is required")
        private String email;
        private String webhookUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateMerchantRequest {
        private String name;
        private String email;
        private String webhookUrl;
    }
}