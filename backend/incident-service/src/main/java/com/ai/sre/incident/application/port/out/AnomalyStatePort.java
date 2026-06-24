package com.ai.sre.incident.application.port.out;

import java.time.Duration;

public interface AnomalyStatePort {
    Long incrementErrorCount(String serviceName, Duration window);
    void resetErrorCount(String serviceName);
    boolean isAlreadyDeduplicated(String serviceName);
    void markAsDeduplicated(String serviceName, Duration window);
}
