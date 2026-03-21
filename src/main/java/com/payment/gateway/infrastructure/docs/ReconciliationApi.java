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
    name = "Reconciliation",
    description = """
        Transaction reconciliation and settlement report generation.
        
        ## Reconciliation Process
        1. Match internal transactions with payment gateway records
        2. Identify discrepancies
        3. Generate settlement reports
        
        ## Report Formats
        - JSON (default)
        - CSV
        - PDF
        """
)
public interface ReconciliationApi {

    @Operation(
        operationId = "reconcileTransactions",
        summary = "Reconcile transactions",
        description = """
            Reconciles internal transactions with payment gateway records for a specific date.
            Returns counts of matched and unmatched transactions, plus any discrepancies found.
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Reconciliation completed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Reconciliation Result",
                    value = """
                        {
                          "success": true,
                          "message": "Reconciliation completed",
                          "data": {
                            "matchedCount": 50,
                            "unmatchedCount": 2,
                            "discrepancies": [
                              {
                                "transactionId": "txn_abc123",
                                "type": "AMOUNT_MISMATCH",
                                "internalAmount": 10000,
                                "gatewayAmount": 9500,
                                "status": "PENDING_REVIEW"
                              }
                            ]
                          },
                          "timestamp": "2026-03-20T10:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<com.payment.gateway.infrastructure.commons.rest.ApiResponse<com.payment.gateway.application.reconciliation.dto.ReconciliationResponse>> reconcileTransactions(
            @Parameter(
                name = "merchantId",
                description = "Merchant ID to reconcile",
                required = true,
                example = "merch_xyz789"
            )
            String merchantId,
            
            @Parameter(
                name = "date",
                description = "Date to reconcile (YYYY-MM-DD format)",
                required = true,
                example = "2026-03-20"
            )
            String date
    );

    @Operation(
        operationId = "generateSettlementReport",
        summary = "Generate settlement report",
        description = """
            Generates a settlement report for a date range.
            Includes total amounts, transaction counts, and breakdown by status.
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Settlement report generated",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Settlement Report",
                    value = """
                        {
                          "success": true,
                          "message": "Settlement report generated",
                          "data": {
                            "merchantId": "merch_xyz789",
                            "startDate": "2026-03-13",
                            "endDate": "2026-03-20",
                            "totalAmount": 500000,
                            "transactionCount": 50,
                            "breakdown": {
                              "captured": {
                                "count": 45,
                                "amount": 450000
                              },
                              "refunded": {
                                "count": 5,
                                "amount": 50000
                              }
                            },
                            "fees": {
                              "processing": 15000,
                              "refunds": 500
                            },
                            "netAmount": 484500
                          },
                          "timestamp": "2026-03-20T10:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<com.payment.gateway.infrastructure.commons.rest.ApiResponse<com.payment.gateway.application.reconciliation.dto.SettlementReportDTO>> generateSettlementReport(
            @Parameter(
                name = "merchantId",
                description = "Merchant ID for report",
                required = true,
                example = "merch_xyz789"
            )
            String merchantId,
            
            @Parameter(
                name = "startDate",
                description = "Report start date (YYYY-MM-DD format)",
                required = true,
                example = "2026-03-13"
            )
            String startDate,
            
            @Parameter(
                name = "endDate",
                description = "Report end date (YYYY-MM-DD format)",
                required = true,
                example = "2026-03-20"
            )
            String endDate,
            
            @Parameter(
                name = "format",
                description = "Report format (JSON, CSV, PDF)",
                example = "JSON"
            )
            String format
    );
}