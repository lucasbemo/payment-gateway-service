package com.payment.gateway.application.customer.service;

import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.application.customer.port.in.RemovePaymentMethodUseCase;
import com.payment.gateway.application.customer.port.out.CustomerCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.customer.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for removing payment methods from customers.
 */
@Slf4j
@Service
@Transactional
public class RemovePaymentMethodService implements RemovePaymentMethodUseCase {

    private final CustomerCommandPort customerCommandPort;

    public RemovePaymentMethodService(CustomerCommandPort customerCommandPort) {
        this.customerCommandPort = customerCommandPort;
    }

    @Override
    public CustomerResponse removePaymentMethod(String customerId, String paymentMethodId) {
        log.info("Removing payment method from customer: {}", customerId);

        Customer customer = customerCommandPort.findById(customerId)
                .orElseThrow(() -> new BusinessException("Customer not found: " + customerId));

        // Validate payment method exists
        if (customer.getPaymentMethod(paymentMethodId) == null) {
            throw new BusinessException("Payment method not found: " + paymentMethodId);
        }

        // Remove payment method
        customer.removePaymentMethod(paymentMethodId);

        // Save customer
        Customer savedCustomer = customerCommandPort.saveCustomer(customer);
        log.info("Payment method removed from customer: {}", customerId);

        return mapToResponse(savedCustomer);
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
