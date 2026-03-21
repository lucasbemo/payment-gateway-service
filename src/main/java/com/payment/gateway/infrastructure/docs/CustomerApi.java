package com.payment.gateway.infrastructure.docs;

import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.infrastructure.commons.rest.ApiResponse;
import com.payment.gateway.infrastructure.customer.adapter.in.rest.CustomerController;
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
    name = "Customer Management",
    description = """
        Customer registration and payment method management.
        
        ## Customer Lifecycle
        1. **Register Customer** - Create a customer record
        2. **Add Payment Method** - Add a card or payment method
        3. **Get Customer** - Retrieve customer with payment methods
        4. **Remove Payment Method** - Remove a payment method
        
        ## Test Card Numbers
        - Visa: 4111111111111111
        - Mastercard: 5500000000000004
        - Amex: 340000000000009
        """
)
public interface CustomerApi {

    @Operation(
        operationId = "registerCustomer",
        summary = "Register a new customer",
        description = "Creates a new customer record for a merchant."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Customer registered successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Successful Registration",
                    value = """
                        {
                          "success": true,
                          "message": "Customer registered successfully",
                          "data": {
                            "id": "cust_abc123",
                            "merchantId": "merch_xyz789",
                            "email": "customer@test.com",
                            "name": "Test Customer",
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
    ResponseEntity<ApiResponse<CustomerResponse>> registerCustomer(
            @RequestBody(
                description = "Customer registration details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Register Customer Request",
                        value = """
                            {
                              "merchantId": "merch_xyz789",
                              "email": "customer@test.com",
                              "name": "Test Customer",
                              "phone": "+1234567890",
                              "externalId": "EXT-001"
                            }
                            """
                    )
                )
            )
            @Valid CustomerController.CreateCustomerRequest request
    );

    @Operation(
        operationId = "getCustomer",
        summary = "Get customer by ID",
        description = "Retrieves customer details including registered payment methods."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Customer found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Customer Details",
                    value = """
                        {
                          "success": true,
                          "message": "Success",
                          "data": {
                            "id": "cust_abc123",
                            "merchantId": "merch_xyz789",
                            "email": "customer@test.com",
                            "name": "Test Customer",
                            "status": "ACTIVE",
                            "paymentMethods": [
                              {
                                "id": "pm_card123",
                                "type": "CREDIT_CARD",
                                "last4": "1111",
                                "brand": "VISA",
                                "isDefault": true
                              }
                            ]
                          },
                          "timestamp": "2026-03-20T11:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(
            @Parameter(
                name = "id",
                description = "Unique customer identifier",
                required = true,
                example = "cust_abc123"
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
        operationId = "addPaymentMethod",
        summary = "Add a payment method to customer",
        description = """
            Adds a payment method (credit card) to a customer. The card will be tokenized.
            
            **Test Cards:**
            - Visa: 4111111111111111
            - Mastercard: 5500000000000004
            - Amex: 340000000000009
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Payment method added successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Payment Method Added",
                    value = """
                        {
                          "success": true,
                          "message": "Payment method added successfully",
                          "data": {
                            "id": "cust_abc123",
                            "paymentMethods": [
                              {
                                "id": "pm_card123",
                                "type": "CREDIT_CARD",
                                "last4": "1111",
                                "brand": "VISA",
                                "expiryMonth": "12",
                                "expiryYear": "2028",
                                "isDefault": true
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
    ResponseEntity<ApiResponse<CustomerResponse>> addPaymentMethod(
            @Parameter(
                name = "id",
                description = "Customer ID to add payment method to",
                required = true,
                example = "cust_abc123"
            )
            String id,
            
            @RequestBody(
                description = "Payment method details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Add Payment Method Request",
                        value = """
                            {
                              "merchantId": "merch_xyz789",
                              "cardNumber": "4111111111111111",
                              "cardExpiryMonth": "12",
                              "cardExpiryYear": "2028",
                              "cardCvv": "123",
                              "cardholderName": "Test Customer",
                              "isDefault": true
                            }
                            """
                    )
                )
            )
            @Valid CustomerController.AddPaymentMethodRequest request
    );

    @Operation(
        operationId = "removePaymentMethod",
        summary = "Remove a payment method from customer",
        description = "Removes a payment method from a customer's account."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Payment method removed successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Payment Method Removed",
                    value = """
                        {
                          "success": true,
                          "message": "Payment method removed successfully",
                          "data": {
                            "id": "cust_abc123",
                            "paymentMethods": []
                          },
                          "timestamp": "2026-03-20T11:00:00Z"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "api-key")
    ResponseEntity<ApiResponse<CustomerResponse>> removePaymentMethod(
            @Parameter(
                name = "id",
                description = "Customer ID",
                required = true,
                example = "cust_abc123"
            )
            String id,
            
            @Parameter(
                name = "pmId",
                description = "Payment method ID to remove",
                required = true,
                example = "pm_card123"
            )
            String pmId
    );
}