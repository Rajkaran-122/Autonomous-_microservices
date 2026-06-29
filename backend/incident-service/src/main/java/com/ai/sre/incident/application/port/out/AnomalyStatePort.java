package com.ai.sre.incident.application.port.out;

import java.time.Duration;

public interface AnomalyStatePort {
    Long incrementErrorCount(String serviceName, Duration window);
    void resetErrorCount(String serviceName);
    boolean tryAcquireDedupLock(String serviceName, Duration window);
    
    // Z-Score Anomaly Methods
    void recordErrorMinute(String serviceName, long minuteEpoch);
    java.util.List<Long> getHistoricalErrorCounts(String serviceName, long currentMinuteEpoch, int maxBuckets);
}
