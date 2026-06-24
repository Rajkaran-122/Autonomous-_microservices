package com.ai.sre.incident.application.port.in;

import com.ai.sre.common.event.LogEvent;

/**
 * Inbound Port: Interface for detecting anomalies and triggering incidents.
 */
public interface DetectAnomalyUseCase {
    
    /**
     * Analyzes an incoming log event to determine if an anomaly exists.
     * If an anomaly exists, it will create an incident and trigger analysis.
     * 
     * @param logEvent the event to analyze
     */
    void detectAnomaly(LogEvent logEvent);
}
