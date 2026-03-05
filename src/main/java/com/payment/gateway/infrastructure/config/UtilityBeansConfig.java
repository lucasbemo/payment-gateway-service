package com.payment.gateway.infrastructure.config;

import com.payment.gateway.commons.utils.IdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for utility beans.
 */
@Configuration
public class UtilityBeansConfig {

    /**
     * Provides IdGenerator as a Spring bean.
     */
    @Bean
    public IdGenerator idGenerator() {
        return new IdGenerator();
    }
}
