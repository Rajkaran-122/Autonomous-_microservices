package com.ai.sre.common.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cross-cutting tracing configuration shared across all microservices.
 * Enables Micrometer Observation API with OpenTelemetry bridge for
 * automatic trace propagation through HTTP (W3C TraceContext) and Kafka headers.
 */
@Configuration
public class TracingConfig {

    /**
     * Enables @Observed annotation support for creating custom spans
     * around any Spring-managed method.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }
}
