package com.ai.sre.incident.application.port.out;

import com.ai.sre.common.event.IncidentEvent;

public interface EventPublisherPort {
    void publishIncidentEvent(String serviceName, IncidentEvent event);
}
