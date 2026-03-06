package com.payment.gateway.infrastructure.customer.adapter.out.provider;

import com.payment.gateway.application.customer.port.out.TokenizationServicePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class StubCustomerTokenizationService implements TokenizationServicePort {

    @Override
    public String tokenize(String cardNumber, String expiryMonth, String expiryYear, String cvv) {
        log.info("StubCustomerTokenizationService.tokenize: cardNumber=****{}",
                cardNumber != null && cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4) : "****");
        return "tok_" + UUID.randomUUID().toString().substring(0, 16);
    }
}
