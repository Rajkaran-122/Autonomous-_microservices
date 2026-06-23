package com.ai.sre.notification.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class PagerDutyProvider {

    private static final Logger log = LoggerFactory.getLogger(PagerDutyProvider.class);
    
    private static final String PAGERDUTY_EVENTS_API = "https://events.pagerduty.com/v2/enqueue";
    
    private final WebClient webClient;
    private final String routingKey;

    public PagerDutyProvider(WebClient.Builder webClientBuilder,
                             @Value("${sre.notifications.pagerduty.routing-key:}") String routingKey) {
        this.webClient = webClientBuilder.build();
        this.routingKey = routingKey;
    }

    public void triggerIncident(String summary, String source, String severity) {
        if (routingKey == null || routingKey.isBlank()) {
            log.info("[PAGERDUTY MOCK] Triggering incident: [{}] {} from {}", severity, summary, source);
            return;
        }

        Map<String, Object> payload = Map.of(
                "routing_key", routingKey,
                "event_action", "trigger",
                "payload", Map.of(
                        "summary", summary,
                        "source", source,
                        "severity", mapSeverity(severity)
                )
        );

        webClient.post()
                .uri(PAGERDUTY_EVENTS_API)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Failed to trigger PagerDuty incident: {}", e.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }
    
    private String mapSeverity(String input) {
        return switch (input) {
            case "P1" -> "critical";
            case "P2" -> "error";
            case "P3" -> "warning";
            default -> "info";
        };
    }
}
