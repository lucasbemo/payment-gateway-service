package com.payment.gateway.infrastructure.docs;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import com.payment.gateway.infrastructure.merchant.adapter.in.rest.MerchantController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(
    name = "Merchant Management",
    description = """
        Merchant registration, updates, and suspension operations.
        
        ## Merchant Lifecycle
        1. **Register Merchant** - Create a new merchant account
        2. **Get Merchant** - Retrieve merchant details
        3. **Update Merchant** - Update merchant information
        4. **Suspend Merchant** - Suspend a merchant account
        """
)
public interface MerchantApi {

    @Operation(
        operationId = "registerMerchant",
        summary = "Register a new merchant",
        description = "Creates a new merchant account with the specified details."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Merchant registered successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Successful Registration",
                    value = """
                        {
                          "success": true,
                          "message": "Merchant registered successfully",
                          "data": {
                            "id": "merch_abc123",
                            "name": "Test Merchant",
                            "email": "merchant@test.com",
                            "status": "ACTIVE",
                            "createdAt": "2026-03-20T10:00:00Z"
                          },
                          "timestamp": "2026-03-20T10:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<ApiResponse<MerchantResponse>> registerMerchant(
            @RequestBody(
                description = "Merchant registration details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Register Merchant Request",
                        value = """
                            {
                              "name": "Test Merchant",
                              "email": "merchant@test.com",
                              "webhookUrl": "https://webhook.site/merchant"
                            }
                            """
                    )
                )
            )
            @Valid MerchantController.CreateMerchantRequest request
    );

    @Operation(
        operationId = "getMerchant",
        summary = "Get merchant by ID",
        description = "Retrieves detailed information about a specific merchant."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Merchant found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Merchant Details",
                    value = """
                        {
                          "success": true,
                          "message": "Success",
                          "data": {
                            "id": "merch_abc123",
                            "name": "Test Merchant",
                            "email": "merchant@test.com",
                            "status": "ACTIVE",
                            "webhookUrl": "https://webhook.site/merchant"
                          },
                          "timestamp": "2026-03-20T11:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<ApiResponse<MerchantResponse>> getMerchant(
            @Parameter(
                name = "id",
                description = "Unique merchant identifier",
                required = true,
                example = "merch_abc123"
            )
            String id
    );

    @Operation(
        operationId = "updateMerchant",
        summary = "Update merchant",
        description = "Updates merchant information. Only provided fields will be updated."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Merchant updated successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Updated Merchant",
                    value = """
                        {
                          "success": true,
                          "message": "Merchant updated successfully",
                          "data": {
                            "id": "merch_abc123",
                            "name": "Updated Merchant Name",
                            "email": "updated@test.com",
                            "status": "ACTIVE"
                          },
                          "timestamp": "2026-03-20T12:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<ApiResponse<MerchantResponse>> updateMerchant(
            @Parameter(
                name = "id",
                description = "Unique merchant identifier",
                required = true,
                example = "merch_abc123"
            )
            String id,
            
            @RequestBody(
                description = "Merchant update details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Update Request",
                        value = """
                            {
                              "name": "Updated Merchant Name",
                              "email": "updated@test.com",
                              "webhookUrl": "https://webhook.site/updated"
                            }
                            """
                    )
                )
            )
            @Valid MerchantController.UpdateMerchantRequest request
    );

    @Operation(
        operationId = "suspendMerchant",
        summary = "Suspend merchant",
        description = "Suspends a merchant account. Suspended merchants cannot process payments."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Merchant suspended successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Suspended Merchant",
                    value = """
                        {
                          "success": true,
                          "message": "Merchant suspended successfully",
                          "data": {
                            "id": "merch_abc123",
                            "status": "SUSPENDED"
                          },
                          "timestamp": "2026-03-20T12:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<ApiResponse<MerchantResponse>> suspendMerchant(
            @Parameter(
                name = "id",
                description = "Unique merchant identifier",
                required = true,
                example = "merch_abc123"
            )
            String id
    );
}