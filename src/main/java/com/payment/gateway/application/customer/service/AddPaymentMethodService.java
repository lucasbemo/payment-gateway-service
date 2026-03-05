package com.payment.gateway.application.customer.service;

import com.payment.gateway.application.customer.dto.AddPaymentMethodCommand;
import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.application.customer.port.in.AddPaymentMethodUseCase;
import com.payment.gateway.application.customer.port.out.CustomerCommandPort;
import com.payment.gateway.application.customer.port.out.TokenizationServicePort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.commons.utils.CryptoUtils;
import com.payment.gateway.domain.customer.model.Customer;
import com.payment.gateway.domain.customer.model.PaymentMethod;
import com.payment.gateway.domain.customer.model.CardDetails;
import com.payment.gateway.domain.customer.model.PaymentMethodType;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service for adding payment methods to customers.
 */
@Slf4j
public class AddPaymentMethodService implements AddPaymentMethodUseCase {

    private final CustomerCommandPort customerCommandPort;
    private final TokenizationServicePort tokenizationServicePort;

    public AddPaymentMethodService(CustomerCommandPort customerCommandPort,
                                   TokenizationServicePort tokenizationServicePort) {
        this.customerCommandPort = customerCommandPort;
        this.tokenizationServicePort = tokenizationServicePort;
    }

    @Override
    public CustomerResponse addPaymentMethod(AddPaymentMethodCommand command) {
        log.info("Adding payment method to customer: {}", command.getCustomerId());

        Customer customer = customerCommandPort.findById(command.getCustomerId())
                .orElseThrow(() -> new BusinessException("Customer not found: " + command.getCustomerId()));

        // Validate merchant ownership
        if (!customer.getMerchantId().equals(command.getMerchantId())) {
            throw new BusinessException("Customer does not belong to merchant: " + command.getMerchantId());
        }

        // Tokenize card
        String token = tokenizationServicePort.tokenize(
                command.getCardNumber(),
                command.getCardExpiryMonth(),
                command.getCardExpiryYear(),
                command.getCardCvv()
        );

        // Create card details
        CardDetails cardDetails = CardDetails.create(
                command.getCardNumber().substring(command.getCardNumber().length() - 4),
                command.getCardNumber().substring(0, 6),
                cardBrandFromNumber(command.getCardNumber()),
                Integer.parseInt(command.getCardExpiryMonth()),
                Integer.parseInt(command.getCardExpiryYear()),
                command.getCardholderName()
        );

        // Create payment method
        PaymentMethod paymentMethod = PaymentMethod.createCard(
                command.getCustomerId(),
                cardDetails,
                token
        );

        if (Boolean.TRUE.equals(command.getIsDefault())) {
            paymentMethod.markAsDefault();
        }

        // Add to customer
        customer.addPaymentMethod(paymentMethod);

        // Save customer
        Customer savedCustomer = customerCommandPort.saveCustomer(customer);
        log.info("Payment method added to customer: {}", command.getCustomerId());

        return mapToResponse(savedCustomer);
    }

    private String cardBrandFromNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank()) {
            return "UNKNOWN";
        }
        if (cardNumber.startsWith("4")) return "VISA";
        if (cardNumber.startsWith("5")) return "MASTERCARD";
        if (cardNumber.startsWith("3")) return "AMEX";
        if (cardNumber.startsWith("6")) return "DISCOVER";
        return "UNKNOWN";
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .merchantId(customer.getMerchantId())
                .email(customer.getEmail())
                .name(customer.getName())
                .phone(customer.getPhone())
                .externalId(customer.getExternalId())
                .status(customer.getStatus().name())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
