package com.payment.gateway.application.customer.service;

import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.application.customer.dto.RegisterCustomerCommand;
import com.payment.gateway.application.customer.port.in.RegisterCustomerUseCase;
import com.payment.gateway.application.customer.port.out.CustomerCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.customer.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for registering customers.
 */
@Slf4j
@Service
@Transactional
public class RegisterCustomerService implements RegisterCustomerUseCase {

    private final CustomerCommandPort customerCommandPort;

    public RegisterCustomerService(CustomerCommandPort customerCommandPort) {
        this.customerCommandPort = customerCommandPort;
    }

    @Override
    public CustomerResponse registerCustomer(RegisterCustomerCommand command) {
        log.info("Registering customer with email: {} for merchant: {}", command.getEmail(), command.getMerchantId());

        // Check for duplicate customer
        if (customerCommandPort.existsByEmailAndMerchantId(command.getEmail(), command.getMerchantId())) {
            throw new BusinessException("Customer with this email already exists for merchant: " + command.getEmail());
        }

        // Create customer
        Customer customer = Customer.create(
                command.getMerchantId(),
                command.getEmail(),
                command.getName()
        );

        // Set optional fields
        if (command.getPhone() != null && !command.getPhone().isBlank()) {
            customer.updatePhone(command.getPhone());
        }
        if (command.getExternalId() != null && !command.getExternalId().isBlank()) {
            customer.updateExternalId(command.getExternalId());
        }

        // Save customer
        Customer savedCustomer = customerCommandPort.saveCustomer(customer);
        log.info("Customer registered with id: {}", savedCustomer.getId());

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
