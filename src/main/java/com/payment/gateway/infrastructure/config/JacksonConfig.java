package com.payment.gateway.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.text.SimpleDateFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4 auto-configures a Jackson 3 ({@code tools.jackson}) mapper for the HTTP
 * layer and no longer exposes a Jackson 2 {@link ObjectMapper} bean. The application's
 * internal serialization (transactional-outbox payloads, Kafka event JSON, S3 settlement
 * reports) deliberately stays on Jackson 2 so payload output remains byte-identical across
 * the JDK 26 / Spring Boot 4 upgrade — in-flight events and already-persisted outbox rows
 * must (de)serialize exactly as before. This bean reproduces the Boot 3 default mapper:
 * all Jackson 2 modules on the classpath are registered (incl. JSR-310), dates are written
 * as ISO strings, and the configured date format is applied.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .build();
    }
}
