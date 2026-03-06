package com.payment.gateway.infrastructure.commons.rest;

import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.merchant.exception.MerchantNotFoundException;
import com.payment.gateway.domain.payment.exception.PaymentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("Payment Not Found")
    class PaymentNotFoundTests {

        @Test
        @DisplayName("should return 404 with error message for PaymentNotFoundException")
        void shouldHandlePaymentNotFound() {
            PaymentNotFoundException ex = new PaymentNotFoundException("pay-123");

            ResponseEntity<ApiResponse<Void>> response = handler.handlePaymentNotFound(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).contains("pay-123");
        }
    }

    @Nested
    @DisplayName("Merchant Not Found")
    class MerchantNotFoundTests {

        @Test
        @DisplayName("should return 404 with error message for MerchantNotFoundException")
        void shouldHandleMerchantNotFound() {
            MerchantNotFoundException ex = new MerchantNotFoundException("merchant-456");

            ResponseEntity<ApiResponse<Void>> response = handler.handleMerchantNotFound(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).contains("merchant-456");
        }
    }

    @Nested
    @DisplayName("Business Exception")
    class BusinessExceptionTests {

        @Test
        @DisplayName("should return 400 with error message for BusinessException")
        void shouldHandleBusinessException() {
            BusinessException ex = new BusinessException("Insufficient funds");

            ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).isEqualTo("Insufficient funds");
        }
    }

    @Nested
    @DisplayName("Validation Exception")
    class ValidationExceptionTests {

        @Test
        @DisplayName("should return 400 with field errors for MethodArgumentNotValidException")
        void shouldHandleValidationException() throws NoSuchMethodException {
            BeanPropertyBindingResult bindingResult =
                    new BeanPropertyBindingResult(new Object(), "testObject");
            bindingResult.addError(new FieldError("testObject", "amount", "must not be null"));
            bindingResult.addError(new FieldError("testObject", "currency", "must not be blank"));

            MethodParameter methodParameter = new MethodParameter(
                    this.getClass().getDeclaredMethod("shouldHandleValidationException"), -1);
            MethodArgumentNotValidException ex =
                    new MethodArgumentNotValidException(methodParameter, bindingResult);

            ResponseEntity<ApiResponse<Map<String, String>>> response =
                    handler.handleValidationExceptions(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
            assertThat(response.getBody().getData()).containsEntry("amount", "must not be null");
            assertThat(response.getBody().getData()).containsEntry("currency", "must not be blank");
        }
    }

    @Nested
    @DisplayName("Illegal Argument Exception")
    class IllegalArgumentExceptionTests {

        @Test
        @DisplayName("should return 400 with error message for IllegalArgumentException")
        void shouldHandleIllegalArgumentException() {
            IllegalArgumentException ex = new IllegalArgumentException("Invalid parameter value");

            ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgumentException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).isEqualTo("Invalid parameter value");
        }
    }

    @Nested
    @DisplayName("Generic Exception")
    class GenericExceptionTests {

        @Test
        @DisplayName("should return 500 with generic error message for unexpected exceptions")
        void shouldHandleGenericException() {
            RuntimeException ex = new RuntimeException("Something went terribly wrong");

            ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        }

        @Test
        @DisplayName("should not expose internal error details in generic exception response")
        void shouldNotExposeInternalDetails() {
            Exception ex = new Exception("Database connection pool exhausted");

            ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex);

            assertThat(response.getBody().getMessage()).doesNotContain("Database");
            assertThat(response.getBody().getData()).isNull();
        }
    }
}
