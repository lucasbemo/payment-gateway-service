package com.payment.gateway.infrastructure.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
    name = "Transaction Management",
    description = """
        Transaction operations including capture and void.
        
        ## Transaction Types
        - **AUTHORIZATION** - Initial authorization hold
        - **CAPTURE** - Capture of an authorization
        - **VOID** - Cancellation of an authorization
        
        ## Transaction Statuses
        - PENDING, APPROVED, DECLINED, VOIDED
        """
)
public interface TransactionApi {

    @Operation(
        operationId = "getTransaction",
        summary = "Get transaction by ID",
        description = "Retrieves detailed information about a specific transaction."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Transaction found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Transaction Details",
                    value = """
                        {
                          "success": true,
                          "message": "Success",
                          "data": {
                            "id": "txn_abc123",
                            "paymentId": "pay_xyz789",
                            "merchantId": "merch_xyz789",
                            "type": "AUTHORIZATION",
                            "amount": 10000,
                            "currency": "USD",
                            "status": "APPROVED",
                            "gatewayTransactionId": "gtw_123",
                            "createdAt": "2026-03-20T10:00:00Z"
                          },
                          "timestamp": "2026-03-20T11:00:00Z"
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Transaction not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "message": "Transaction not found: txn_invalid",
                          "data": null,
                          "timestamp": "2026-03-20T11:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<com.payment.gateway.infrastructure.commons.rest.ApiResponse<com.payment.gateway.application.transaction.dto.TransactionResponse>> getTransaction(
            @Parameter(
                name = "id",
                description = "Unique transaction identifier",
                required = true,
                example = "txn_abc123"
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
        operationId = "captureTransaction",
        summary = "Capture a transaction",
        description = """
            Captures a previously authorized transaction.
            The transaction must be in APPROVED status with type AUTHORIZATION.
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Transaction captured successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Captured Transaction",
                    value = """
                        {
                          "success": true,
                          "message": "Transaction captured successfully",
                          "data": {
                            "id": "txn_abc123",
                            "status": "APPROVED",
                            "type": "CAPTURE"
                          },
                          "timestamp": "2026-03-20T12:00:00Z"
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Transaction cannot be captured",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "message": "Transaction cannot be captured",
                          "data": null,
                          "timestamp": "2026-03-20T12:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<com.payment.gateway.infrastructure.commons.rest.ApiResponse<com.payment.gateway.application.transaction.dto.TransactionResponse>> captureTransaction(
            @Parameter(
                name = "id",
                description = "Transaction ID to capture",
                required = true,
                example = "txn_abc123"
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
        operationId = "voidTransaction",
        summary = "Void a transaction",
        description = """
            Voids a previously authorized transaction.
            The transaction must be in APPROVED status with type AUTHORIZATION.
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Transaction voided successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Voided Transaction",
                    value = """
                        {
                          "success": true,
                          "message": "Transaction voided successfully",
                          "data": {
                            "id": "txn_abc123",
                            "status": "VOIDED"
                          },
                          "timestamp": "2026-03-20T12:00:00Z"
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Transaction cannot be voided",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": false,
                          "message": "Transaction cannot be voided: already captured",
                          "data": null,
                          "timestamp": "2026-03-20T12:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<com.payment.gateway.infrastructure.commons.rest.ApiResponse<com.payment.gateway.application.transaction.dto.TransactionResponse>> voidTransaction(
            @Parameter(
                name = "id",
                description = "Transaction ID to void",
                required = true,
                example = "txn_abc123"
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
}