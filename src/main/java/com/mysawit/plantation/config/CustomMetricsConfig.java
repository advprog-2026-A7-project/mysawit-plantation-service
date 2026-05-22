package com.mysawit.plantation.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Pre-registers custom Micrometer counters/timers for plantation-service.
 *
 * Once this bean is loaded, the metrics are visible at /actuator/prometheus
 * (initially with value 0). Wire them into PlantationService by autowiring
 * the Counter / Timer and calling .increment() / .record() at the right spots.
 *
 * HTTP request rate, error rate, latency, JVM/CPU metrics — all come for free
 * from spring-boot-starter-actuator and the histogram tag in application.properties.
 *
 * Database query latency: see hikaricp_connections_usage_seconds in Prometheus.
 */
@Configuration
public class CustomMetricsConfig {

    @Bean
    Counter overlapValidationFailureCounter(MeterRegistry registry) {
        return Counter.builder("plantation_overlap_validation_failure_total")
                .description("Times a plantation coordinate overlap check failed")
                .register(registry);
    }

    @Bean
    Counter deletePlantationFailureCounter(MeterRegistry registry) {
        return Counter.builder("plantation_delete_failure_total")
                .description("Times a delete plantation failed (constraint, missing, FK, etc.)")
                .register(registry);
    }

    @Bean
    Counter assignmentFailureCounter(MeterRegistry registry) {
        return Counter.builder("plantation_assignment_failure_total")
                .description("Times an assignment (mandor/supir → plantation) failed")
                .register(registry);
    }

    @Bean
    Counter assignmentSuccessCounter(MeterRegistry registry) {
        return Counter.builder("plantation_assignment_success_total")
                .description("Times an assignment (mandor/supir → plantation) succeeded")
                .register(registry);
    }

    /**
     * Latency wrapping the assignment business logic. Spring's http_server_requests
     * timer already gives you per-URI latency, but this timer is endpoint-agnostic
     * if you want to time JUST the assignment service method, regardless of caller.
     */
    @Bean
    Timer assignmentLatencyTimer(MeterRegistry registry) {
        return Timer.builder("plantation_assignment_latency_seconds")
                .description("End-to-end latency of plantation assignment operations")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);
    }
}
