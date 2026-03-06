package com.payment.gateway.infrastructure.customer.adapter.out.persistence;

import com.payment.gateway.domain.customer.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private CustomerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CustomerMapper();
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map domain Customer to CustomerJpaEntity with all fields")
        void shouldMapDomainToEntity() {
            // given
            Customer customer = Customer.create("merchant-001", "john@example.com", "John Doe");

            // when
            CustomerJpaEntity entity = mapper.toEntity(customer);

            // then
            assertThat(entity.getId()).isEqualTo(customer.getId());
            assertThat(entity.getMerchantId()).isEqualTo("merchant-001");
            assertThat(entity.getEmail()).isEqualTo("john@example.com");
            assertThat(entity.getName()).isEqualTo("John Doe");
            assertThat(entity.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should generate a UUID token for the entity")
        void shouldGenerateUuidToken() {
            Customer customer = Customer.create("merchant-002", "jane@example.com", "Jane Doe");

            CustomerJpaEntity entity = mapper.toEntity(customer);

            assertThat(entity.getToken()).isNotNull();
            assertThat(entity.getToken()).isNotBlank();
            // UUID format: 8-4-4-4-12
            assertThat(entity.getToken()).matches(
                    "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("should generate different tokens for each call")
        void shouldGenerateDifferentTokens() {
            Customer customer = Customer.create("merchant-003", "bob@example.com", "Bob");

            CustomerJpaEntity entity1 = mapper.toEntity(customer);
            CustomerJpaEntity entity2 = mapper.toEntity(customer);

            assertThat(entity1.getToken()).isNotEqualTo(entity2.getToken());
        }

        @Test
        @DisplayName("should map suspended customer status")
        void shouldMapSuspendedStatus() {
            Customer customer = Customer.create("merchant-004", "suspended@example.com", "Suspended User");
            customer.suspend();

            CustomerJpaEntity entity = mapper.toEntity(customer);

            assertThat(entity.getStatus()).isEqualTo(CustomerStatus.SUSPENDED);
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("should map CustomerJpaEntity to domain Customer with all fields")
        void shouldMapEntityToDomain() {
            // given
            Instant now = Instant.now();
            CustomerJpaEntity entity = CustomerJpaEntity.builder()
                    .id("cust-999")
                    .merchantId("merchant-abc")
                    .token("some-token-uuid")
                    .email("entity@example.com")
                    .name("Entity Customer")
                    .phone("+1234567890")
                    .status(CustomerStatus.ACTIVE)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // when
            Customer customer = mapper.toDomain(entity);

            // then
            assertThat(customer.getId()).isEqualTo("cust-999");
            assertThat(customer.getMerchantId()).isEqualTo("merchant-abc");
            assertThat(customer.getEmail()).isEqualTo("entity@example.com");
            assertThat(customer.getName()).isEqualTo("Entity Customer");
            assertThat(customer.getPhone()).isEqualTo("+1234567890");
            assertThat(customer.getStatus()).isEqualTo(com.payment.gateway.domain.customer.model.CustomerStatus.ACTIVE);
        }

        @Test
        @DisplayName("should set phone via reflection from entity")
        void shouldSetPhoneViaReflection() {
            Instant now = Instant.now();
            CustomerJpaEntity entity = CustomerJpaEntity.builder()
                    .id("cust-phone")
                    .merchantId("merchant-phone")
                    .token("token")
                    .email("phone@example.com")
                    .name("Phone Test")
                    .phone("+9876543210")
                    .status(CustomerStatus.ACTIVE)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            Customer customer = mapper.toDomain(entity);

            assertThat(customer.getPhone()).isEqualTo("+9876543210");
        }

        @Test
        @DisplayName("should handle null phone in entity")
        void shouldHandleNullPhone() {
            Instant now = Instant.now();
            CustomerJpaEntity entity = CustomerJpaEntity.builder()
                    .id("cust-nophone")
                    .merchantId("merchant-nophone")
                    .token("token")
                    .email("nophone@example.com")
                    .name("No Phone")
                    .phone(null)
                    .status(CustomerStatus.ACTIVE)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            Customer customer = mapper.toDomain(entity);

            assertThat(customer.getPhone()).isNull();
        }

        @Test
        @DisplayName("should map INACTIVE status from entity to domain")
        void shouldMapInactiveStatus() {
            Instant now = Instant.now();
            CustomerJpaEntity entity = CustomerJpaEntity.builder()
                    .id("cust-inactive")
                    .merchantId("merchant-inactive")
                    .token("token")
                    .email("inactive@example.com")
                    .name("Inactive User")
                    .status(CustomerStatus.INACTIVE)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            Customer customer = mapper.toDomain(entity);

            assertThat(customer.getStatus()).isEqualTo(com.payment.gateway.domain.customer.model.CustomerStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("round-trip")
    class RoundTrip {

        @Test
        @DisplayName("should preserve key data through toEntity then toDomain")
        void shouldPreserveDataThroughRoundTrip() {
            Customer original = Customer.create("merchant-rt", "rt@example.com", "RoundTrip User");

            CustomerJpaEntity entity = mapper.toEntity(original);
            Customer restored = mapper.toDomain(entity);

            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getMerchantId()).isEqualTo(original.getMerchantId());
            assertThat(restored.getEmail()).isEqualTo(original.getEmail());
            assertThat(restored.getName()).isEqualTo(original.getName());
        }
    }
}
