package com.payment.gateway.infrastructure.payment.adapter.out.provider;

import com.payment.gateway.application.payment.port.out.TokenizationServicePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of TokenizationServicePort for testing/development.
 */
@Slf4j
@Component
public class StubTokenizationService implements TokenizationServicePort {

    @Override
    public String tokenize(String cardNumber, String expiryMonth, String expiryYear, String cvv) {
        log.info("StubTokenizationService.tokenize: cardNumber=****{}",
                cardNumber.substring(cardNumber.length() - 4));
        return "tok_stub_" + System.currentTimeMillis() + "_" + cardNumber.substring(cardNumber.length() - 4);
    }

    @Override
    public CardData detokenize(String token) {
        log.info("StubTokenizationService.detokenize: token={}", token);
        // Return stub card data for testing
        return new CardData("4111111111111111", "12", "2030", "123");
    }
}
