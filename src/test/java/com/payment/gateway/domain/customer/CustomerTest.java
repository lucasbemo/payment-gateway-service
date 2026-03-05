package com.payment.gateway.domain.customer;

import com.payment.gateway.domain.customer.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Customer aggregate.
 */
class CustomerTest {

    private static final String MERCHANT_ID = "merch_123";
    private static final String EMAIL = "customer@example.com";
    private static final String NAME = "John Doe";

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.create(MERCHANT_ID, EMAIL, NAME);
    }

    @Nested
    @DisplayName("Customer Creation")
    class CustomerCreation {

        @Test
        @DisplayName("Should create customer with ACTIVE status")
        void shouldCreateCustomerWithActiveStatus() {
            assertNotNull(customer.getId());
            assertEquals(MERCHANT_ID, customer.getMerchantId());
            assertEquals(EMAIL, customer.getEmail());
            assertEquals(NAME, customer.getName());
            assertEquals(com.payment.gateway.domain.customer.model.CustomerStatus.ACTIVE, customer.getStatus());
            assertNotNull(customer.getCreatedAt());
        }

        @Test
        @DisplayName("Should create customer with empty payment methods")
        void shouldCreateCustomerWithEmptyPaymentMethods() {
            assertNotNull(customer.getPaymentMethods());
            assertTrue(customer.getPaymentMethods().isEmpty());
        }

        @Test
        @DisplayName("Should create customer with null default payment method")
        void shouldCreateCustomerWithNullDefaultPaymentMethod() {
            assertNull(customer.getDefaultPaymentMethodId());
        }
    }

    @Nested
    @DisplayName("Payment Method Management")
    class PaymentMethodManagement {

        @Test
        @DisplayName("Should add payment method to customer")
        void shouldAddPaymentMethodToCustomer() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod paymentMethod = PaymentMethod.createCard(customer.getId(), cardDetails, "token_123");

            customer.addPaymentMethod(paymentMethod);

            assertEquals(1, customer.getPaymentMethods().size());
            assertEquals(paymentMethod, customer.getPaymentMethods().get(0));
        }

        @Test
        @DisplayName("Should set default payment method when marked as default")
        void shouldSetDefaultPaymentMethodWhenMarkedAsDefault() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod paymentMethod = PaymentMethod.createCard(customer.getId(), cardDetails, "token_123");
            paymentMethod.markAsDefault();

            customer.addPaymentMethod(paymentMethod);

            assertEquals(paymentMethod.getId(), customer.getDefaultPaymentMethodId());
        }

        @Test
        @DisplayName("Should remove payment method from customer")
        void shouldRemovePaymentMethodFromCustomer() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod paymentMethod = PaymentMethod.createCard(customer.getId(), cardDetails, "token_123");
            customer.addPaymentMethod(paymentMethod);

            customer.removePaymentMethod(paymentMethod.getId());

            assertTrue(customer.getPaymentMethods().isEmpty());
            assertNull(customer.getDefaultPaymentMethodId());
        }

        @Test
        @DisplayName("Should get payment method by id")
        void shouldGetPaymentMethodById() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod paymentMethod = PaymentMethod.createCard(customer.getId(), cardDetails, "token_123");
            customer.addPaymentMethod(paymentMethod);

            PaymentMethod found = customer.getPaymentMethod(paymentMethod.getId());

            assertNotNull(found);
            assertEquals(paymentMethod.getId(), found.getId());
        }

        @Test
        @DisplayName("Should return null for non-existent payment method")
        void shouldReturnNullForNonExistentPaymentMethod() {
            PaymentMethod found = customer.getPaymentMethod("non_existent_id");

            assertNull(found);
        }

        @Test
        @DisplayName("Should set default payment method")
        void shouldSetDefaultPaymentMethod() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod paymentMethod = PaymentMethod.createCard(customer.getId(), cardDetails, "token_123");
            customer.addPaymentMethod(paymentMethod);

            customer.setDefaultPaymentMethod(paymentMethod.getId());

            assertEquals(paymentMethod.getId(), customer.getDefaultPaymentMethodId());
            assertTrue(Boolean.TRUE.equals(paymentMethod.getIsDefault()));
        }
    }

    @Nested
    @DisplayName("Customer Updates")
    class CustomerUpdates {

        @Test
        @DisplayName("Should update email")
        void shouldUpdateEmail() {
            String newEmail = "newemail@example.com";

            customer.updateEmail(newEmail);

            assertEquals(newEmail, customer.getEmail());
        }

        @Test
        @DisplayName("Should update phone")
        void shouldUpdatePhone() {
            String newPhone = "+1234567890";

            customer.updatePhone(newPhone);

            assertEquals(newPhone, customer.getPhone());
        }

        @Test
        @DisplayName("Should update name")
        void shouldUpdateName() {
            String newName = "Jane Doe";

            customer.updateName(newName);

            assertEquals(newName, customer.getName());
        }

        @Test
        @DisplayName("Should update external id")
        void shouldUpdateExternalId() {
            String externalId = "ext_12345";

            customer.updateExternalId(externalId);

            assertEquals(externalId, customer.getExternalId());
        }

        @Test
        @DisplayName("Should update updated_at on email change")
        void shouldUpdateUpdatedAtOnEmailChange() {
            var beforeUpdate = customer.getUpdatedAt();

            customer.updateEmail("new@example.com");

            assertTrue(customer.getUpdatedAt().isAfter(beforeUpdate));
        }
    }

    @Nested
    @DisplayName("Customer Status")
    class CustomerStatus {

        @Test
        @DisplayName("Should activate customer")
        void shouldActivateCustomer() {
            customer.deactivate();
            customer.activate();

            assertEquals(com.payment.gateway.domain.customer.model.CustomerStatus.ACTIVE, customer.getStatus());
        }

        @Test
        @DisplayName("Should deactivate customer")
        void shouldDeactivateCustomer() {
            customer.deactivate();

            assertEquals(com.payment.gateway.domain.customer.model.CustomerStatus.INACTIVE, customer.getStatus());
        }

        @Test
        @DisplayName("Should suspend customer")
        void shouldSuspendCustomer() {
            customer.suspend();

            assertEquals(com.payment.gateway.domain.customer.model.CustomerStatus.SUSPENDED, customer.getStatus());
        }

        @Test
        @DisplayName("Should return true for isActive when status is ACTIVE")
        void shouldBeActiveWhenStatusIsActive() {
            assertTrue(customer.isActive());
        }

        @Test
        @DisplayName("Should return false for isActive when status is INACTIVE")
        void shouldNotBeActiveWhenStatusIsInactive() {
            customer.deactivate();

            assertFalse(customer.isActive());
        }
    }

    @Nested
    @DisplayName("Customer Status Transitions")
    class StatusTransitions {

        @Test
        @DisplayName("Should allow ACTIVE to INACTIVE transition")
        void shouldAllowActiveToInactive() {
            assertTrue(com.payment.gateway.domain.customer.model.CustomerStatus.ACTIVE.canTransitionTo(com.payment.gateway.domain.customer.model.CustomerStatus.INACTIVE));
        }

        @Test
        @DisplayName("Should allow ACTIVE to SUSPENDED transition")
        void shouldAllowActiveToSuspended() {
            assertTrue(com.payment.gateway.domain.customer.model.CustomerStatus.ACTIVE.canTransitionTo(com.payment.gateway.domain.customer.model.CustomerStatus.SUSPENDED));
        }

        @Test
        @DisplayName("Should allow INACTIVE to ACTIVE transition")
        void shouldAllowInactiveToActive() {
            assertTrue(com.payment.gateway.domain.customer.model.CustomerStatus.INACTIVE.canTransitionTo(com.payment.gateway.domain.customer.model.CustomerStatus.ACTIVE));
        }

        @Test
        @DisplayName("Should not allow BLOCKED to ACTIVE transition")
        void shouldNotAllowBlockedToActive() {
            assertFalse(com.payment.gateway.domain.customer.model.CustomerStatus.BLOCKED.canTransitionTo(com.payment.gateway.domain.customer.model.CustomerStatus.ACTIVE));
        }
    }

    @Nested
    @DisplayName("Card Details Tests")
    class CardDetailsTests {

        @Test
        @DisplayName("Should create card details with valid data")
        void shouldCreateCardDetailsWithValidData() {
            com.payment.gateway.domain.customer.model.CardDetails cardDetails = buildCardDetails();

            assertNotNull(cardDetails.getId());
            assertEquals("1234", cardDetails.getCardNumberLast4());
            assertEquals("411111", cardDetails.getCardNumberBin());
            assertEquals("VISA", cardDetails.getCardBrand());
        }

        @Test
        @DisplayName("Should create masked card number")
        void shouldCreateMaskedCardNumber() {
            com.payment.gateway.domain.customer.model.CardDetails cardDetails = buildCardDetails();

            assertEquals("411111****1234", cardDetails.getMaskedCardNumber());
        }

        @Test
        @DisplayName("Should detect expired card")
        void shouldDetectExpiredCard() {
            com.payment.gateway.domain.customer.model.CardDetails expiredCard = com.payment.gateway.domain.customer.model.CardDetails.builder()
                    .cardNumberLast4("1234")
                    .cardNumberBin("411111")
                    .cardBrand("VISA")
                    .expiryMonth(1)
                    .expiryYear(20) // Past year
                    .build();

            assertTrue(expiredCard.isExpired());
        }

        @Test
        @DisplayName("Should detect non-expired card")
        void shouldDetectNonExpiredCard() {
            com.payment.gateway.domain.customer.model.CardDetails validCard = com.payment.gateway.domain.customer.model.CardDetails.builder()
                    .cardNumberLast4("1234")
                    .cardNumberBin("411111")
                    .cardBrand("VISA")
                    .expiryMonth(12)
                    .expiryYear(2030) // Future year
                    .build();

            assertFalse(validCard.isExpired());
        }

        @Test
        @DisplayName("Should mark card as default")
        void shouldMarkCardAsDefault() {
            com.payment.gateway.domain.customer.model.CardDetails cardDetails = buildCardDetails();
            assertFalse(Boolean.TRUE.equals(cardDetails.getIsDefault()));

            cardDetails.markAsDefault();

            assertTrue(Boolean.TRUE.equals(cardDetails.getIsDefault()));
        }

        @Test
        @DisplayName("Should reject invalid card number last 4")
        void shouldRejectInvalidCardNumberLast4() {
            assertThrows(IllegalArgumentException.class, () ->
                com.payment.gateway.domain.customer.model.CardDetails.create("123", "411111", "VISA", 12, 2025, "John Doe")
            );
        }

        @Test
        @DisplayName("Should reject non-digit card number last 4")
        void shouldRejectNonDigitCardNumberLast4() {
            assertThrows(IllegalArgumentException.class, () ->
                com.payment.gateway.domain.customer.model.CardDetails.create("123a", "411111", "VISA", 12, 2025, "John Doe")
            );
        }

        @Test
        @DisplayName("Should reject invalid expiry month")
        void shouldRejectInvalidExpiryMonth() {
            assertThrows(IllegalArgumentException.class, () ->
                com.payment.gateway.domain.customer.model.CardDetails.create("1234", "411111", "VISA", 13, 2025, "John Doe")
            );
        }
    }

    @Nested
    @DisplayName("Payment Method")
    class PaymentMethodTests {

        @Test
        @DisplayName("Should create card payment method")
        void shouldCreateCardPaymentMethod() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod pm = PaymentMethod.createCard("cust_123", cardDetails, "token_abc");

            assertEquals(com.payment.gateway.domain.customer.model.PaymentMethodType.CREDIT_CARD, pm.getType());
            assertEquals(com.payment.gateway.domain.customer.model.PaymentMethodStatus.PENDING_VERIFICATION, pm.getStatus());
            assertEquals(cardDetails, pm.getCardDetails());
        }

        @Test
        @DisplayName("Should create bank account payment method")
        void shouldCreateBankAccountPaymentMethod() {
            PaymentMethod pm = PaymentMethod.createBankAccount("cust_123", "1234", "5678", "token_bank");

            assertEquals(com.payment.gateway.domain.customer.model.PaymentMethodType.BANK_ACCOUNT, pm.getType());
            assertEquals("1234", pm.getBankAccountLast4());
            assertEquals("5678", pm.getBankAccountRoutingNumberLast4());
        }

        @Test
        @DisplayName("Should create digital wallet payment method")
        void shouldCreateDigitalWalletPaymentMethod() {
            PaymentMethod pm = PaymentMethod.createDigitalWallet("cust_123", "APPLE_PAY", "token_wallet");

            assertEquals(com.payment.gateway.domain.customer.model.PaymentMethodType.DIGITAL_WALLET, pm.getType());
            assertEquals("APPLE_PAY", pm.getDigitalWalletProvider());
            assertEquals(com.payment.gateway.domain.customer.model.PaymentMethodStatus.VERIFIED, pm.getStatus());
        }

        @Test
        @DisplayName("Should verify payment method")
        void shouldVerifyPaymentMethod() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod pm = PaymentMethod.createCard("cust_123", cardDetails, "token_abc");

            pm.verify();

            assertEquals(com.payment.gateway.domain.customer.model.PaymentMethodStatus.VERIFIED, pm.getStatus());
        }

        @Test
        @DisplayName("Should fail verification")
        void shouldFailVerification() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod pm = PaymentMethod.createCard("cust_123", cardDetails, "token_abc");

            pm.failVerification();

            assertEquals(com.payment.gateway.domain.customer.model.PaymentMethodStatus.FAILED_VERIFICATION, pm.getStatus());
        }

        @Test
        @DisplayName("Should activate payment method")
        void shouldActivatePaymentMethod() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod pm = PaymentMethod.createCard("cust_123", cardDetails, "token_abc");

            pm.activate();

            assertEquals(com.payment.gateway.domain.customer.model.PaymentMethodStatus.ACTIVE, pm.getStatus());
        }

        @Test
        @DisplayName("Should deactivate payment method")
        void shouldDeactivatePaymentMethod() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod pm = PaymentMethod.createCard("cust_123", cardDetails, "token_abc");
            pm.activate();

            pm.deactivate();

            assertEquals(com.payment.gateway.domain.customer.model.PaymentMethodStatus.INACTIVE, pm.getStatus());
        }

        @Test
        @DisplayName("Should revoke payment method")
        void shouldRevokePaymentMethod() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod pm = PaymentMethod.createCard("cust_123", cardDetails, "token_abc");

            pm.revoke();

            assertEquals(com.payment.gateway.domain.customer.model.PaymentMethodStatus.REVOKED, pm.getStatus());
        }

        @Test
        @DisplayName("Should increment usage count")
        void shouldIncrementUsageCount() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod pm = PaymentMethod.createCard("cust_123", cardDetails, "token_abc");

            pm.incrementUsage();

            assertEquals(1, pm.getUsageCount());
            assertNotNull(pm.getLastUsedAt());
        }

        @Test
        @DisplayName("Should return true for isCard when type is CREDIT_CARD")
        void shouldBeCardWhenTypeIsCreditCard() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod pm = PaymentMethod.createCard("cust_123", cardDetails, "token_abc");

            assertTrue(pm.isCard());
        }

        @Test
        @DisplayName("Should return true for isActive when status is ACTIVE")
        void shouldBeActiveWhenStatusIsActive() {
            CardDetails cardDetails = buildCardDetails();
            PaymentMethod pm = PaymentMethod.createCard("cust_123", cardDetails, "token_abc");
            pm.activate();

            assertTrue(pm.isActive());
        }
    }

    private CardDetails buildCardDetails() {
        return CardDetails.create("1234", "411111", "VISA", 12, 2025, "John Doe");
    }
}
