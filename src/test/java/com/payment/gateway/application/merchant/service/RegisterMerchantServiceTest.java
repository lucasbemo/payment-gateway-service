package com.payment.gateway.application.merchant.service;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.application.merchant.dto.RegisterMerchantCommand;
import com.payment.gateway.application.merchant.port.out.MerchantCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.utils.CryptoUtils;
import com.payment.gateway.commons.utils.IdGenerator;
import com.payment.gateway.domain.merchant.model.Merchant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Register Merchant Service Tests")
@ExtendWith(MockitoExtension.class)
class RegisterMerchantServiceTest {

    @Mock
    private MerchantCommandPort merchantCommandPort;

    private RegisterMerchantService registerMerchantService;

    @BeforeEach
    void setUp() {
        registerMerchantService = new RegisterMerchantService(merchantCommandPort);
    }

    @Nested
    @DisplayName("Successful Merchant Registration")
    class SuccessfulRegistrationTests {

        @Test
        @DisplayName("Should register merchant successfully")
        void shouldRegisterMerchantSuccessfully() {
            // Given
            String name = "Test Merchant";
            String email = "test@merchant.com";
            String webhookUrl = "https://webhook.example.com";
            String merchantId = "merchant_123";

            RegisterMerchantCommand command = RegisterMerchantCommand.builder()
                    .name(name)
                    .email(email)
                    .webhookUrl(webhookUrl)
                    .build();

            given(merchantCommandPort.existsByEmail(email)).willReturn(false);
            given(merchantCommandPort.saveMerchant(any(Merchant.class))).willAnswer(invocation -> {
                Merchant merchant = invocation.getArgument(0);
                setId(merchant, merchantId);
                return merchant;
            });

            // When
            MerchantResponse response = registerMerchantService.registerMerchant(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(merchantId);
            assertThat(response.getName()).isEqualTo(name);
            assertThat(response.getEmail()).isEqualTo(email);
            assertThat(response.getApiKey()).isNotNull();
            assertThat(response.getApiSecret()).isNotNull();

            then(merchantCommandPort).should().saveMerchant(any(Merchant.class));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            String email = "test@merchant.com";
            RegisterMerchantCommand command = RegisterMerchantCommand.builder()
                    .name("Test Merchant")
                    .email(email)
                    .build();

            given(merchantCommandPort.existsByEmail(email)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> registerMerchantService.registerMerchant(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already exists");
        }
    }

    // Helper methods

    private void setId(Object obj, String id) {
        try {
            Field idField = obj.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(obj, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id field", e);
        }
    }
}
