package com.ai.sre.ai.service.telemetry;

import org.springframework.stereotype.Service;

@Service
public class MockTelemetryContextGatherer implements TelemetryContextGatherer {

    @Override
    public String gatherStackTraces(String serviceName) {
        // In a real system, this would query Jaeger or OpenSearch
        if ("payment-service".equals(serviceName)) {
            return """
                   Exception in thread "main" java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 30000ms.
                       at com.zaxxer.hikari.pool.HikariPool.createTimeoutException(HikariPool.java:695)
                       at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:197)
                       at com.zaxxer.hikari.HikariDataSource.getConnection(HikariDataSource.java:128)
                       at org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl.getConnection(DatasourceConnectionProviderImpl.java:122)
                   """;
        } else if ("auth-service".equals(serviceName)) {
            return """
                   java.lang.OutOfMemoryError: Java heap space
                       at java.base/java.util.Arrays.copyOf(Arrays.java:3537)
                       at java.base/java.lang.AbstractStringBuilder.ensureCapacityInternal(AbstractStringBuilder.java:228)
                   """;
        }
        return "No recent stack traces found in OpenTelemetry for " + serviceName + ".";
    }

    @Override
    public String gatherRecentCommits(String serviceName) {
        // In a real system, this would query GitHub/GitLab API
        if ("payment-service".equals(serviceName)) {
            return """
                   Recent Commits (last 2 hours):
                   - commit 9f8a7c2: "Update HikariCP max pool size from 50 to 5" (Author: dev@example.com)
                   - commit 1b2c3d4: "Add new payment processor integration" (Author: feature@example.com)
                   """;
        }
        return "No recent commits found in the last 24 hours.";
    }

    @Override
    public String gatherMetrics(String serviceName) {
        // In a real system, this would query Prometheus
        if ("auth-service".equals(serviceName)) {
            return "Prometheus Metrics: CPU Usage is 99%, Memory Usage is 98% (Spike detected 5 mins ago).";
        }
        return "Prometheus Metrics: CPU and Memory are within normal thresholds.";
    }
}
