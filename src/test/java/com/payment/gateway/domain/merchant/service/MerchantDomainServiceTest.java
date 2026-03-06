package com.payment.gateway.domain.merchant.service;

import com.payment.gateway.domain.merchant.exception.MerchantNotFoundException;
import com.payment.gateway.domain.merchant.model.ApiCredentials;
import com.payment.gateway.domain.merchant.model.Merchant;
import com.payment.gateway.domain.merchant.model.MerchantConfiguration;
import com.payment.gateway.domain.merchant.model.MerchantStatus;
import com.payment.gateway.domain.merchant.port.MerchantRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MerchantDomainService Tests")
class MerchantDomainServiceTest {

    @Mock
    private MerchantRepositoryPort merchantRepository;

    private MerchantDomainService merchantDomainService;

    private final String MERCHANT_ID = "merch_123";
    private final String MERCHANT_NAME = "Test Merchant";
    private final String MERCHANT_EMAIL = "test@merchant.com";
    private final String API_KEY = "api_key_123";
    private final String API_SECRET = "api_secret_123";
    private final String WEBHOOK_URL = "https://example.com/webhook";

    @BeforeEach
    void setUp() {
        merchantDomainService = new MerchantDomainService(merchantRepository);
    }

    @Nested
    @DisplayName("Register Merchant")
    class RegisterMerchantTests {

        @Test
        @DisplayName("Should register merchant successfully when data is valid")
        void shouldRegisterMerchantSuccessfully() {
            // Given
            MerchantConfiguration config = MerchantConfiguration.empty();
            Merchant merchant = Merchant.register(
                MERCHANT_NAME,
                MERCHANT_EMAIL,
                "test-api-key",
                "hashed_key",
                "hashed_secret",
                WEBHOOK_URL,
                config
            );

            given(merchantRepository.existsByEmail(MERCHANT_EMAIL)).willReturn(false);
            given(merchantRepository.save(any(Merchant.class))).willReturn(merchant);

            // When
            Merchant result = merchantDomainService.registerMerchant(
                MERCHANT_NAME,
                MERCHANT_EMAIL,
                API_KEY,
                API_SECRET,
                WEBHOOK_URL,
                config
            );

            // Then
            assertThat(result).isNotNull();
            verify(merchantRepository).existsByEmail(MERCHANT_EMAIL);
            verify(merchantRepository).save(any(Merchant.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            MerchantConfiguration config = MerchantConfiguration.empty();
            given(merchantRepository.existsByEmail(MERCHANT_EMAIL)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> merchantDomainService.registerMerchant(
                MERCHANT_NAME,
                MERCHANT_EMAIL,
                API_KEY,
                API_SECRET,
                WEBHOOK_URL,
                config
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

            verify(merchantRepository).existsByEmail(MERCHANT_EMAIL);
            verify(merchantRepository, never()).save(any(Merchant.class));
        }
    }

    @Nested
    @DisplayName("Get Merchant")
    class GetMerchantTests {

        @Test
        @DisplayName("Should return merchant when found by ID")
        void shouldReturnMerchantWhenFoundById() {
            // Given
            Merchant merchant = createActiveMerchant();
            given(merchantRepository.findById(MERCHANT_ID)).willReturn(Optional.of(merchant));

            // When
            Merchant result = merchantDomainService.getMerchant(MERCHANT_ID);

            // Then
            assertThat(result).isEqualTo(merchant);
            verify(merchantRepository).findById(MERCHANT_ID);
        }

        @Test
        @DisplayName("Should throw MerchantNotFoundException when merchant not found")
        void shouldThrowMerchantNotFoundExceptionWhenNotFound() {
            // Given
            given(merchantRepository.findById(MERCHANT_ID)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> merchantDomainService.getMerchant(MERCHANT_ID))
                .isInstanceOf(MerchantNotFoundException.class)
                .hasMessageContaining(MERCHANT_ID);

            verify(merchantRepository).findById(MERCHANT_ID);
        }
    }

    @Nested
    @DisplayName("Activate Merchant")
    class ActivateMerchantTests {

        @Test
        @DisplayName("Should activate merchant successfully")
        void shouldActivateMerchantSuccessfully() {
            // Given
            Merchant merchant = createPendingMerchant();
            given(merchantRepository.findById(MERCHANT_ID)).willReturn(Optional.of(merchant));
            given(merchantRepository.save(any(Merchant.class))).willReturn(merchant);

            // When
            Merchant result = merchantDomainService.activateMerchant(MERCHANT_ID);

            // Then
            assertThat(result).isEqualTo(merchant);
            verify(merchantRepository).findById(MERCHANT_ID);
            verify(merchantRepository).save(merchant);
        }
    }

    @Nested
    @DisplayName("Suspend Merchant")
    class SuspendMerchantTests {

        @Test
        @DisplayName("Should suspend merchant successfully")
        void shouldSuspendMerchantSuccessfully() {
            // Given
            Merchant merchant = createActiveMerchant();
            given(merchantRepository.findById(MERCHANT_ID)).willReturn(Optional.of(merchant));
            given(merchantRepository.save(any(Merchant.class))).willReturn(merchant);

            // When
            Merchant result = merchantDomainService.suspendMerchant(MERCHANT_ID);

            // Then
            assertThat(result).isEqualTo(merchant);
            verify(merchantRepository).findById(MERCHANT_ID);
            verify(merchantRepository).save(merchant);
        }
    }

    @Nested
    @DisplayName("Reactivate Merchant")
    class ReactivateMerchantTests {

        @Test
        @DisplayName("Should reactivate suspended merchant successfully")
        void shouldReactivateMerchantSuccessfully() {
            // Given
            Merchant merchant = createActiveMerchant();
            merchant.suspend(); // Now SUSPENDED
            given(merchantRepository.findById(MERCHANT_ID)).willReturn(Optional.of(merchant));
            given(merchantRepository.save(any(Merchant.class))).willReturn(merchant);

            // When
            Merchant result = merchantDomainService.reactivateMerchant(MERCHANT_ID);

            // Then
            assertThat(result).isEqualTo(merchant);
            verify(merchantRepository).findById(MERCHANT_ID);
            verify(merchantRepository).save(merchant);
        }
    }

    @Nested
    @DisplayName("Close Merchant")
    class CloseMerchantTests {

        @Test
        @DisplayName("Should close merchant successfully")
        void shouldCloseMerchantSuccessfully() {
            // Given
            Merchant merchant = createActiveMerchant();
            merchant.suspend(); // Must be SUSPENDED before closing (PENDING -> ACTIVE -> SUSPENDED -> CLOSED)
            given(merchantRepository.findById(MERCHANT_ID)).willReturn(Optional.of(merchant));
            given(merchantRepository.save(any(Merchant.class))).willReturn(merchant);

            // When
            Merchant result = merchantDomainService.closeMerchant(MERCHANT_ID);

            // Then
            assertThat(result).isEqualTo(merchant);
            verify(merchantRepository).findById(MERCHANT_ID);
            verify(merchantRepository).save(merchant);
        }
    }

    @Nested
    @DisplayName("Update Webhook URL")
    class UpdateWebhookUrlTests {

        @Test
        @DisplayName("Should update webhook URL successfully")
        void shouldUpdateWebhookUrlSuccessfully() {
            // Given
            String newWebhookUrl = "https://new-webhook.com/webhook";
            Merchant merchant = createActiveMerchant();
            given(merchantRepository.findById(MERCHANT_ID)).willReturn(Optional.of(merchant));
            given(merchantRepository.save(any(Merchant.class))).willReturn(merchant);

            // When
            Merchant result = merchantDomainService.updateWebhookUrl(MERCHANT_ID, newWebhookUrl);

            // Then
            assertThat(result).isEqualTo(merchant);
            verify(merchantRepository).findById(MERCHANT_ID);
            verify(merchantRepository).save(merchant);
        }
    }

    @Nested
    @DisplayName("Regenerate Credentials")
    class RegenerateCredentialsTests {

        @Test
        @DisplayName("Should regenerate API credentials successfully")
        void shouldRegenerateCredentialsSuccessfully() {
            // Given
            String newApiKey = "new_api_key";
            String newApiSecret = "new_api_secret";
            Merchant merchant = createActiveMerchant();
            given(merchantRepository.findById(MERCHANT_ID)).willReturn(Optional.of(merchant));
            given(merchantRepository.save(any(Merchant.class))).willReturn(merchant);

            // When
            ApiCredentials result = merchantDomainService.regenerateCredentials(MERCHANT_ID, newApiKey, newApiSecret);

            // Then
            assertThat(result).isNotNull();
            verify(merchantRepository).findById(MERCHANT_ID);
            verify(merchantRepository).save(merchant);
        }
    }

    @Nested
    @DisplayName("List Merchants")
    class ListMerchantsTests {

        @Test
        @DisplayName("Should return list of all merchants")
        void shouldReturnListOfAllMerchants() {
            // Given
            Merchant merchant1 = createActiveMerchant();
            Merchant merchant2 = createActiveMerchant();
            given(merchantRepository.findAll()).willReturn(List.of(merchant1, merchant2));

            // When
            List<Merchant> result = merchantDomainService.listAllMerchants();

            // Then
            assertThat(result).hasSize(2);
            verify(merchantRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Validate Merchant Can Process")
    class ValidateMerchantCanProcessTests {

        @Test
        @DisplayName("Should not throw when merchant can process payments")
        void shouldNotThrowWhenMerchantCanProcess() {
            // Given
            Merchant merchant = createActiveMerchant();
            given(merchantRepository.findById(MERCHANT_ID)).willReturn(Optional.of(merchant));

            // When & Then
            assertThatCode(() -> merchantDomainService.validateMerchantCanProcess(MERCHANT_ID))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw when merchant cannot process payments")
        void shouldThrowWhenMerchantCannotProcess() {
            // Given
            Merchant merchant = Merchant.register(
                MERCHANT_NAME,
                MERCHANT_EMAIL,
                "test-api-key",
                "hashed_key",
                "hashed_secret",
                WEBHOOK_URL,
                MerchantConfiguration.empty()
            );
            merchant.suspend();
            given(merchantRepository.findById(MERCHANT_ID)).willReturn(Optional.of(merchant));

            // When & Then
            assertThatThrownBy(() -> merchantDomainService.validateMerchantCanProcess(MERCHANT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot process payments");
        }
    }

    private Merchant createActiveMerchant() {
        Merchant merchant = Merchant.register(
            MERCHANT_NAME,
            MERCHANT_EMAIL,
            "test-api-key",
            "hashed_key",
            "hashed_secret",
            WEBHOOK_URL,
            MerchantConfiguration.empty()
        );
        merchant.activate(); // Activate the merchant
        return merchant;
    }

    private Merchant createPendingMerchant() {
        return Merchant.register(
            MERCHANT_NAME,
            MERCHANT_EMAIL,
            "test-api-key",
            "hashed_key",
            "hashed_secret",
            WEBHOOK_URL,
            MerchantConfiguration.empty()
        );
    }
}
