package com.payment.gateway.infrastructure.customer.adapter.in.rest;

import com.payment.gateway.application.customer.dto.AddPaymentMethodCommand;
import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.application.customer.dto.RegisterCustomerCommand;
import com.payment.gateway.application.customer.port.in.AddPaymentMethodUseCase;
import com.payment.gateway.application.customer.port.in.GetCustomerUseCase;
import com.payment.gateway.application.customer.port.in.RegisterCustomerUseCase;
import com.payment.gateway.application.customer.port.in.RemovePaymentMethodUseCase;
import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import com.payment.gateway.infrastructure.docs.CustomerApi;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final AddPaymentMethodUseCase addPaymentMethodUseCase;
    private final RemovePaymentMethodUseCase removePaymentMethodUseCase;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> registerCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        log.info("Registering customer for merchant: {}", request.getMerchantId());
        var command = RegisterCustomerCommand.builder()
                .merchantId(request.getMerchantId())
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .externalId(request.getExternalId())
                .build();
        var response = registerCustomerUseCase.registerCustomer(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer registered successfully", response));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(
            @PathVariable String id,
            @RequestParam String merchantId) {
        log.info("Getting customer: {} for merchant: {}", id, merchantId);
        var response = getCustomerUseCase.getCustomerById(id, merchantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @PostMapping("/{id}/payment-methods")
    public ResponseEntity<ApiResponse<CustomerResponse>> addPaymentMethod(
            @PathVariable String id,
            @Valid @RequestBody AddPaymentMethodRequest request) {
        log.info("Adding payment method for customer: {}", id);
        var command = AddPaymentMethodCommand.builder()
                .customerId(id)
                .merchantId(request.getMerchantId())
                .cardNumber(request.getCardNumber())
                .cardExpiryMonth(request.getCardExpiryMonth())
                .cardExpiryYear(request.getCardExpiryYear())
                .cardCvv(request.getCardCvv())
                .cardholderName(request.getCardholderName())
                .isDefault(request.getIsDefault())
                .build();
        var response = addPaymentMethodUseCase.addPaymentMethod(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment method added successfully", response));
    }

    @Override
    @DeleteMapping("/{id}/payment-methods/{pmId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> removePaymentMethod(
            @PathVariable String id,
            @PathVariable String pmId) {
        log.info("Removing payment method: {} from customer: {}", pmId, id);
        var response = removePaymentMethodUseCase.removePaymentMethod(id, pmId);
        return ResponseEntity.ok(ApiResponse.success("Payment method removed successfully", response));
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Request to register a new customer")
    public static class CreateCustomerRequest {

        @Schema(description = "Merchant ID that will own this customer", example = "merch_abc123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Merchant ID is required")
        private String merchantId;

        @Schema(description = "Customer email address", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        private String email;

        @Schema(description = "Customer full name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name is required")
        private String name;

        @Schema(description = "Customer phone number", example = "+1234567890")
        private String phone;

        @Schema(description = "External ID from merchant's system", example = "EXT-12345")
        private String externalId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Request to add a payment method to a customer")
    public static class AddPaymentMethodRequest {

        @Schema(description = "Merchant ID", example = "merch_abc123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Merchant ID is required")
        private String merchantId;

        @Schema(description = "Card number (test: 4111111111111111)", example = "4111111111111111", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Card number is required")
        private String cardNumber;

        @Schema(description = "Card expiry month (MM)", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Expiry month is required")
        private String cardExpiryMonth;

        @Schema(description = "Card expiry year (YYYY)", example = "2028", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Expiry year is required")
        private String cardExpiryYear;

        @Schema(description = "Card CVV", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "CVV is required")
        private String cardCvv;

        @Schema(description = "Cardholder name", example = "John Doe")
        private String cardholderName;

        @Schema(description = "Set as default payment method", example = "true")
        private Boolean isDefault;
    }
}