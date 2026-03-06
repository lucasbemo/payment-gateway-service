package com.payment.gateway.infrastructure.customer.adapter.out.persistence;

import com.payment.gateway.domain.customer.model.Customer;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper between Customer domain model and CustomerJpaEntity.
 */
@Component
public class CustomerMapper {

    public CustomerJpaEntity toEntity(Customer customer) {
        return CustomerJpaEntity.builder()
                .id(customer.getId())
                .merchantId(customer.getMerchantId())
                .token(UUID.randomUUID().toString())
                .email(customer.getEmail())
                .name(customer.getName())
                .phone(customer.getPhone())
                .status(CustomerStatus.valueOf(customer.getStatus().name()))
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public Customer toDomain(CustomerJpaEntity entity) {
        Customer customer = Customer.create(
                entity.getMerchantId(),
                entity.getEmail(),
                entity.getName()
        );
        // Set fields using reflection
        try {
            java.lang.reflect.Field idField = Customer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(customer, entity.getId());

            java.lang.reflect.Field phoneField = Customer.class.getDeclaredField("phone");
            phoneField.setAccessible(true);
            phoneField.set(customer, entity.getPhone());

            java.lang.reflect.Field statusField = Customer.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(customer, com.payment.gateway.domain.customer.model.CustomerStatus.valueOf(entity.getStatus().name()));

        } catch (Exception e) {
            throw new RuntimeException("Failed to set customer fields", e);
        }
        return customer;
    }
}
