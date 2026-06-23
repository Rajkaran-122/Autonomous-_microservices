package com.ai.sre.notification.service;

import com.ai.sre.common.config.KafkaTopics;
import com.ai.sre.common.event.IncidentEvent;
import com.ai.sre.common.event.NotificationEvent;
import com.ai.sre.notification.provider.PagerDutyProvider;
import com.ai.sre.notification.provider.SlackProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationRoutingService {

    private static final Logger log = LoggerFactory.getLogger(NotificationRoutingService.class);

    private final SlackProvider slackProvider;
    private final PagerDutyProvider pagerDutyProvider;

    public NotificationRoutingService(SlackProvider slackProvider, PagerDutyProvider pagerDutyProvider) {
        this.slackProvider = slackProvider;
        this.pagerDutyProvider = pagerDutyProvider;
    }

    /**
     * Listens for Incident Events and routes to appropriate channels based on severity.
     */
    @KafkaListener(
            topics = KafkaTopics.INCIDENT_EVENTS,
            groupId = KafkaTopics.GROUP_NOTIFICATION_SERVICE
    )
    public void routeIncidentNotification(IncidentEvent event) {
        log.info("Routing notification for Incident: {} (Severity: {})", event.incidentId(), event.severity());

        String message = String.format("🚨 *New Incident: %s* 🚨\n*Severity:* %s\n*Service:* %s\n*Description:* %s",
                event.title(), event.severity(), event.serviceName(), event.description());

        // Always send to #sre-alerts Slack channel
        slackProvider.sendMessage("#sre-alerts", message);

        // If P1 or P2, page the on-call engineer via PagerDuty
        if ("P1".equals(event.severity()) || "P2".equals(event.severity())) {
            pagerDutyProvider.triggerIncident(event.title(), event.serviceName(), event.severity());
        }
    }

    /**
     * Listens for generic Notification Events from other services.
     */
    @KafkaListener(
            topics = KafkaTopics.NOTIFICATION_EVENTS,
            groupId = KafkaTopics.GROUP_NOTIFICATION_SERVICE
    )
    public void handleGenericNotification(NotificationEvent event) {
        switch (event.channel().toUpperCase()) {
            case "SLACK" -> slackProvider.sendMessage(event.recipient(), event.message());
            case "PAGERDUTY" -> pagerDutyProvider.triggerIncident(event.title(), "Autonomous Platform", "info");
            default -> log.warn("Unsupported notification channel: {}", event.channel());
        }
    }
}
