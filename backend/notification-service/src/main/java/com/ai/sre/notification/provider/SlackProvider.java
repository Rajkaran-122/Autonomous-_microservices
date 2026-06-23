package com.ai.sre.notification.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class SlackProvider {

    private static final Logger log = LoggerFactory.getLogger(SlackProvider.class);
    
    private final WebClient webClient;
    private final String webhookUrl;

    public SlackProvider(WebClient.Builder webClientBuilder,
                         @Value("${sre.notifications.slack.webhook-url:}") String webhookUrl) {
        this.webClient = webClientBuilder.build();
        this.webhookUrl = webhookUrl;
    }

    public void sendMessage(String channel, String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.info("[SLACK MOCK] To channel '{}': {}", channel, message);
            return;
        }

        Map<String, String> payload = Map.of(
                "channel", channel,
                "text", message
        );

        webClient.post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Failed to send Slack message: {}", e.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }
}
