package com.payment.gateway.domain.merchant.exception;

import com.payment.gateway.commons.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link InvalidMerchantException}.
 */
class InvalidMerchantExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "Merchant email is invalid";

        InvalidMerchantException exception = new InvalidMerchantException(message);

        assertThat(exception.getMessage()).isEqualTo("Merchant email is invalid");
        assertThat(exception.getCode()).isEqualTo("INVALID_MERCHANT");
    }

    @Test
    void shouldBeBusinessException() {
        InvalidMerchantException exception = new InvalidMerchantException("Test message");

        assertThat(exception).isInstanceOf(BusinessException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldThrowException() {
        assertThatThrownBy(() -> {
            throw new InvalidMerchantException("Merchant API key is missing");
        })
                .isInstanceOf(InvalidMerchantException.class)
                .hasMessage("Merchant API key is missing")
                .extracting("code")
                .isEqualTo("INVALID_MERCHANT");
    }
}
