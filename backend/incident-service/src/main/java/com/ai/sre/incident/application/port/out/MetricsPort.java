package com.ai.sre.incident.application.port.out;

import java.time.Duration;

public interface MetricsPort {
    long getTotalRequests(String serviceName, Duration window);
    long getFailedRequests(String serviceName, Duration window);
}
