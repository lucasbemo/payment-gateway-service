package com.payment.gateway.application.merchant.service;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.application.merchant.port.out.MerchantCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.merchant.model.Merchant;
import com.payment.gateway.domain.merchant.model.MerchantConfiguration;
import com.payment.gateway.domain.merchant.model.MerchantStatus;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Update Merchant Service Tests")
@ExtendWith(MockitoExtension.class)
class UpdateMerchantServiceTest {

    @Mock
    private MerchantCommandPort merchantCommandPort;

    private UpdateMerchantService updateMerchantService;

    @BeforeEach
    void setUp() {
        updateMerchantService = new UpdateMerchantService(merchantCommandPort);
    }

    @Nested
    @DisplayName("Successful Merchant Update")
    class SuccessfulUpdateTests {

        @Test
        @DisplayName("Should update merchant name successfully")
        void shouldUpdateMerchantNameSuccessfully() {
            // Given
            String merchantId = "merchant-123";
            Merchant merchant = createMerchant(merchantId);
            String newName = "Updated Merchant Name";

            given(merchantCommandPort.findById(merchantId)).willReturn(Optional.of(merchant));
            given(merchantCommandPort.saveMerchant(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            MerchantResponse response = updateMerchantService.updateMerchant(merchantId, newName, null, null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(merchantId);
            then(merchantCommandPort).should().findById(merchantId);
            then(merchantCommandPort).should().saveMerchant(any());
        }

        @Test
        @DisplayName("Should update webhook URL successfully")
        void shouldUpdateWebhookUrlSuccessfully() {
            // Given
            String merchantId = "merchant-123";
            Merchant merchant = createMerchant(merchantId);
            String newWebhookUrl = "https://new-webhook.example.com";

            given(merchantCommandPort.findById(merchantId)).willReturn(Optional.of(merchant));
            given(merchantCommandPort.saveMerchant(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            MerchantResponse response = updateMerchantService.updateMerchant(merchantId, null, null, newWebhookUrl);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(merchantId);
            then(merchantCommandPort).should().saveMerchant(any());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when merchant not found")
        void shouldThrowExceptionWhenMerchantNotFound() {
            // Given
            String merchantId = "invalid-merchant";
            given(merchantCommandPort.findById(merchantId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> updateMerchantService.updateMerchant(merchantId, "New Name", null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Merchant not found");
        }

        @Test
        @DisplayName("Should throw exception when email is invalid")
        void shouldThrowExceptionWhenEmailIsInvalid() {
            // Given
            String merchantId = "merchant-123";
            Merchant merchant = createMerchant(merchantId);
            String invalidEmail = "invalid-email";

            given(merchantCommandPort.findById(merchantId)).willReturn(Optional.of(merchant));

            // When & Then
            assertThatThrownBy(() -> updateMerchantService.updateMerchant(merchantId, null, invalidEmail, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Invalid email address");
        }
    }

    private Merchant createMerchant(String id) {
        Merchant merchant = Merchant.register(
                "Test Merchant",
                "test@merchant.com",
                "hashed_key",
                "hashed_secret",
                "https://webhook.example.com",
                MerchantConfiguration.empty()
        );
        setId(merchant, id);
        setStatus(merchant, MerchantStatus.ACTIVE);
        return merchant;
    }

    private void setId(Object obj, String id) {
        try {
            Field idField = obj.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(obj, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id field", e);
        }
    }

    private void setStatus(Object obj, MerchantStatus status) {
        try {
            Field statusField = obj.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(obj, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set status field", e);
        }
    }
}
