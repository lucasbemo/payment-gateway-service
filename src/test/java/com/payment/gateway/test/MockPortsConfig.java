package com.payment.gateway.test;

import com.payment.gateway.application.payment.port.out.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

/**
 * Test configuration providing mock implementations for ports not under test.
 */
@TestConfiguration
public class MockPortsConfig {

    @Bean
    public MerchantQueryPort mockMerchantQueryPort() {
        return new MerchantQueryPort() {
            @Override
            public Optional<com.payment.gateway.domain.merchant.model.Merchant> findById(String id) {
                return Optional.empty();
            }

            @Override
            public boolean existsById(String id) {
                return false;
            }
        };
    }

    @Bean
    public CustomerQueryPort mockCustomerQueryPort() {
        return new CustomerQueryPort() {
            @Override
            public Optional<com.payment.gateway.domain.customer.model.Customer> findCustomerById(String id) {
                return Optional.empty();
            }

            @Override
            public Optional<com.payment.gateway.domain.customer.model.Customer> findCustomerByIdAndMerchantId(String customerId, String merchantId) {
                return Optional.empty();
            }

            @Override
            public Optional<com.payment.gateway.domain.customer.model.PaymentMethod> findPaymentMethodById(String paymentMethodId) {
                return Optional.empty();
            }

            @Override
            public Optional<com.payment.gateway.domain.customer.model.PaymentMethod> findPaymentMethodByToken(String token) {
                return Optional.empty();
            }
        };
    }

    @Bean
    public ExternalPaymentProviderPort mockExternalPaymentProviderPort() {
        return new ExternalPaymentProviderPort() {
            @Override
            public PaymentProviderResult authorize(PaymentProviderRequest request) {
                return new PaymentProviderResult(true, "test-txn-id", null, null);
            }

            @Override
            public PaymentProviderResult capture(PaymentProviderRequest request) {
                return new PaymentProviderResult(true, "test-txn-id", null, null);
            }

            @Override
            public PaymentProviderResult cancel(PaymentProviderRequest request) {
                return new PaymentProviderResult(true, "test-txn-id", null, null);
            }

            @Override
            public String tokenizeCard(CardTokenizationRequest request) {
                return "tok_test";
            }
        };
    }

    @Bean
    public TokenizationServicePort mockTokenizationServicePort() {
        return new TokenizationServicePort() {
            @Override
            public String tokenize(String cardNumber, String expiryMonth, String expiryYear, String cvv) {
                return "tok_test";
            }

            @Override
            public CardData detokenize(String token) {
                return new CardData("4111111111111111", "12", "2030", "123");
            }
        };
    }

    @Bean
    public TransactionCommandPort mockTransactionCommandPort() {
        return new TransactionCommandPort() {
            @Override
            public String createTransaction(CreateTransactionCommand command) {
                return "txn-" + command.paymentId();
            }
        };
    }
}
