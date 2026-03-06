package com.payment.gateway.application.customer.service;

import com.payment.gateway.application.customer.dto.CustomerResponse;
import com.payment.gateway.application.customer.port.in.GetCustomerUseCase;
import com.payment.gateway.application.customer.port.out.CustomerCommandPort;
import com.payment.gateway.commons.exception.BusinessException;
import com.payment.gateway.domain.customer.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for getting customer information.
 */
@Slf4j
@Service
@Transactional
public class GetCustomerService implements GetCustomerUseCase {

    private final CustomerCommandPort customerCommandPort;

    public GetCustomerService(CustomerCommandPort customerCommandPort) {
        this.customerCommandPort = customerCommandPort;
    }

    @Override
    public CustomerResponse getCustomerById(String customerId, String merchantId) {
        log.info("Getting customer by id: {} for merchant: {}", customerId, merchantId);

        Customer customer = customerCommandPort.findByIdAndMerchantId(customerId, merchantId)
                .orElseThrow(() -> new BusinessException("Customer not found: " + customerId));

        return mapToResponse(customer);
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
