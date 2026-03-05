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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("Get Merchant Service Tests")
@ExtendWith(MockitoExtension.class)
class GetMerchantServiceTest {

    @Mock
    private MerchantCommandPort merchantCommandPort;

    private GetMerchantService getMerchantService;

    @BeforeEach
    void setUp() {
        getMerchantService = new GetMerchantService(merchantCommandPort);
    }

    @Nested
    @DisplayName("Get Merchant By ID")
    class GetMerchantByIdTests {

        @Test
        @DisplayName("Should get merchant successfully by ID")
        void shouldGetMerchantSuccessfullyById() {
            // Given
            String merchantId = "merchant-123";
            Merchant merchant = createMerchant(merchantId);

            given(merchantCommandPort.findById(merchantId)).willReturn(Optional.of(merchant));

            // When
            MerchantResponse response = getMerchantService.getMerchantById(merchantId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(merchantId);
            assertThat(response.getName()).isEqualTo("Test Merchant");
            assertThat(response.getEmail()).isEqualTo("test@merchant.com");
            assertThat(response.getStatus()).isEqualTo(MerchantStatus.ACTIVE.name());

            then(merchantCommandPort).should().findById(merchantId);
        }

        @Test
        @DisplayName("Should throw exception when merchant not found")
        void shouldThrowExceptionWhenMerchantNotFound() {
            // Given
            String merchantId = "invalid-merchant";
            given(merchantCommandPort.findById(merchantId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> getMerchantService.getMerchantById(merchantId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Merchant not found");
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
