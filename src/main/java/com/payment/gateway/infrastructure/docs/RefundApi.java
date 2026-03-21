package com.payment.gateway.infrastructure.docs;

import com.payment.gateway.application.refund.dto.RefundResponse;
import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import com.payment.gateway.infrastructure.refund.adapter.in.rest.RefundController;
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
    name = "Refund Processing",
    description = """
        Full and partial refund processing operations.
        
        ## Refund Types
        - **Full Refund** - Refund the entire payment amount
        - **Partial Refund** - Refund a portion of the payment
        
        ## Idempotency
        All refund creation requests support idempotency via the `idempotencyKey` field.
        """
)
public interface RefundApi {

    @Operation(
        operationId = "processRefund",
        summary = "Process a refund",
        description = """
            Processes a refund for a captured payment. Can be full or partial refund.
            
            **Requirements:**
            - Payment must be in CAPTURED status
            - Refund amount cannot exceed remaining capturable amount
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Refund processed successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Successful Refund",
                    value = """
                        {
                          "success": true,
                          "message": "Refund processed successfully",
                          "data": {
                            "id": "ref_abc123",
                            "paymentId": "pay_xyz789",
                            "merchantId": "merch_xyz789",
                            "amount": 5000,
                            "status": "COMPLETED",
                            "reason": "Customer request",
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
    ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @RequestBody(
                description = "Refund request details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Refund Request",
                        value = """
                            {
                              "paymentId": "pay_xyz789",
                              "merchantId": "merch_xyz789",
                              "amount": 5000,
                              "idempotencyKey": "550e8400-e29b-41d4-a716-446655440000",
                              "reason": "Customer request"
                            }
                            """
                    )
                )
            )
            @Valid RefundController.CreateRefundRequest request
    );

    @Operation(
        operationId = "getRefund",
        summary = "Get refund by ID",
        description = "Retrieves detailed information about a specific refund."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Refund found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Refund Details",
                    value = """
                        {
                          "success": true,
                          "message": "Success",
                          "data": {
                            "id": "ref_abc123",
                            "paymentId": "pay_xyz789",
                            "merchantId": "merch_xyz789",
                            "amount": 5000,
                            "status": "COMPLETED",
                            "reason": "Customer request",
                            "createdAt": "2026-03-20T10:00:00Z"
                          },
                          "timestamp": "2026-03-20T11:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<ApiResponse<RefundResponse>> getRefund(
            @Parameter(
                name = "id",
                description = "Unique refund identifier",
                required = true,
                example = "ref_abc123"
            )
            String id,
            
            @Parameter(
                name = "merchantId",
                description = "Merchant ID for authorization",
                required = true,
                example = "merch_xyz789"
            )
            String merchantId
    );

    @Operation(
        operationId = "cancelRefund",
        summary = "Cancel a pending refund",
        description = "Cancels a refund that is in PENDING status."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Refund cancelled successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Cancelled Refund",
                    value = """
                        {
                          "success": true,
                          "message": "Refund cancelled successfully",
                          "data": {
                            "id": "ref_abc123",
                            "status": "CANCELLED"
                          },
                          "timestamp": "2026-03-20T12:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<ApiResponse<RefundResponse>> cancelRefund(
            @Parameter(
                name = "id",
                description = "Refund ID to cancel",
                required = true,
                example = "ref_abc123"
            )
            String id,
            
            @Parameter(
                name = "merchantId",
                description = "Merchant ID for authorization",
                required = true,
                example = "merch_xyz789"
            )
            String merchantId,
            
            @Parameter(
                name = "reason",
                description = "Reason for cancellation",
                example = "Customer changed mind"
            )
            String reason
    );
}