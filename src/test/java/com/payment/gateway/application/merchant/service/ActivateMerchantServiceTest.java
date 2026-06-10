package com.payment.gateway.application.merchant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.payment.gateway.application.merchant.dto.MerchantResponse;
import com.payment.gateway.application.merchant.port.out.MerchantCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.merchant.model.Merchant;
import com.payment.gateway.domain.merchant.model.MerchantConfiguration;
import com.payment.gateway.domain.merchant.model.MerchantStatus;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("Activate Merchant Service Tests")
@ExtendWith(MockitoExtension.class)
class ActivateMerchantServiceTest {

    @Mock
    private MerchantCommandPort merchantCommandPort;

    private ActivateMerchantService activateMerchantService;

    @BeforeEach
    void setUp() {
        activateMerchantService = new ActivateMerchantService(merchantCommandPort);
    }

    @Nested
    @DisplayName("Successful Merchant Activation")
    class SuccessfulActivationTests {

        @Test
        @DisplayName("Should activate PENDING merchant successfully")
        void shouldActivatePendingMerchantSuccessfully() {
            // Given
            String merchantId = "merchant-123";
            Merchant merchant = createMerchant(merchantId, MerchantStatus.PENDING);

            given(merchantCommandPort.findById(merchantId)).willReturn(Optional.of(merchant));
            given(merchantCommandPort.saveMerchant(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            MerchantResponse response = activateMerchantService.activateMerchant(merchantId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(merchantId);
            assertThat(response.getStatus()).isEqualTo(MerchantStatus.ACTIVE.name());

            then(merchantCommandPort).should().findById(merchantId);
            then(merchantCommandPort).should().saveMerchant(any());
        }

        @Test
        @DisplayName("Should reactivate SUSPENDED merchant successfully")
        void shouldReactivateSuspendedMerchantSuccessfully() {
            // Given
            String merchantId = "merchant-123";
            Merchant merchant = createMerchant(merchantId, MerchantStatus.SUSPENDED);

            given(merchantCommandPort.findById(merchantId)).willReturn(Optional.of(merchant));
            given(merchantCommandPort.saveMerchant(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            MerchantResponse response = activateMerchantService.activateMerchant(merchantId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(merchantId);
            assertThat(response.getStatus()).isEqualTo(MerchantStatus.ACTIVE.name());

            then(merchantCommandPort).should().findById(merchantId);
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
            assertThatThrownBy(() -> activateMerchantService.activateMerchant(merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Merchant not found");
        }

        @Test
        @DisplayName("Should throw exception when merchant is already ACTIVE")
        void shouldThrowExceptionWhenMerchantAlreadyActive() {
            // Given
            String merchantId = "merchant-123";
            Merchant merchant = createMerchant(merchantId, MerchantStatus.ACTIVE);

            given(merchantCommandPort.findById(merchantId)).willReturn(Optional.of(merchant));

            // When & Then
            assertThatThrownBy(() -> activateMerchantService.activateMerchant(merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot transition from ACTIVE to ACTIVE");

            then(merchantCommandPort).should(never()).saveMerchant(any());
        }
    }

    private Merchant createMerchant(String id, MerchantStatus status) {
        Merchant merchant = Merchant.register(
                "Test Merchant",
                "test@merchant.com",
                "test-api-key",
                "hashed_key",
                "hashed_secret",
                "https://webhook.example.com",
                MerchantConfiguration.empty());
        setId(merchant, id);
        setStatus(merchant, status);
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
