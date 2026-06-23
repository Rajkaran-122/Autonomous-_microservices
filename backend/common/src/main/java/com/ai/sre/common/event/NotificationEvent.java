package com.ai.sre.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event for notification dispatch.
 * Published to topic: notification-events
 * Consumed by: notification-service
 */
public record NotificationEvent(
        UUID eventId,
        UUID incidentId,
        String notificationType,      // INCIDENT_DETECTED, INCIDENT_RESOLVED, HEALING_STARTED,
                                      // HEALING_COMPLETED, SLO_VIOLATION, APPROVAL_REQUIRED, WEEKLY_DIGEST
        String severity,              // P1, P2, P3, P4
        String title,
        String message,
        String serviceName,

        // Channel routing
        String targetChannel,         // SLACK, EMAIL, PAGERDUTY, ALL
        String targetRecipient,       // Slack channel, email address, PD service

        // Action buttons (for Slack)
        Map<String, String> actions,  // {"acknowledge": "url", "view_incident": "url"}

        // Rich context
        Map<String, Object> context,  // Additional data for notification template

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public NotificationEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (timestamp == null) timestamp = Instant.now();
        if (targetChannel == null) targetChannel = "ALL";
    }
}
