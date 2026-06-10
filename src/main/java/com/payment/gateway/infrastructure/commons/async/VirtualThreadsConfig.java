package com.payment.gateway.infrastructure.commons.async;

import java.util.concurrent.Executors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

/**
 * Configuration for Java 21 Virtual Threads.
 * Enabled via property: spring.threads.virtual.enabled=true
 */
@Configuration
@ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "true", matchIfMissing = false)
public class VirtualThreadsConfig {

    @Bean
    public AsyncTaskExecutor virtualThreadExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
