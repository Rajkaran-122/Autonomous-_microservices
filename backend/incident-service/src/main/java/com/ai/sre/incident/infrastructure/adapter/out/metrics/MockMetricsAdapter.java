package com.ai.sre.incident.infrastructure.adapter.out.metrics;

import com.ai.sre.incident.application.port.out.MetricsPort;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Random;

@Component
public class MockMetricsAdapter implements MetricsPort {

    private final Random random = new Random();

    @Override
    public long getTotalRequests(String serviceName, Duration window) {
        // Base load of ~1000 requests per minute
        long minutes = window.toMinutes();
        return (1000L * minutes) + random.nextInt(500);
    }

    @Override
    public long getFailedRequests(String serviceName, Duration window) {
        long total = getTotalRequests(serviceName, window);
        // Randomly simulate an incident (10% chance)
        if (random.nextInt(100) > 90) {
            // Massive failure rate
            return (long) (total * (0.05 + random.nextDouble() * 0.1));
        }
        // Normal failure rate (0.01% - 0.1%)
        return (long) (total * (0.0001 + random.nextDouble() * 0.001));
    }
}
