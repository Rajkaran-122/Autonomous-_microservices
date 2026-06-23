package com.ai.sre.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time event streaming to the frontend dashboard.
 * Enables STOMP protocol for structured pub/sub messaging.
 *
 * Topics:
 *   /topic/incidents     — Real-time incident updates
 *   /topic/healing       — Self-healing action updates
 *   /topic/alerts        — Live alert notifications
 *   /topic/metrics       — Real-time metric updates
 *   /topic/slo           — SLO violation alerts
 *   /topic/canary        — Canary deployment progress
 *   /topic/chaos         — Chaos experiment events
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker for topic subscriptions
        config.enableSimpleBroker("/topic");
        // Prefix for messages from client → server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint — frontend connects here
        registry.addEndpoint("/ws/events")
                .setAllowedOrigins("http://localhost:3001", "http://localhost:3000")
                .withSockJS(); // Fallback for older browsers
    }
}
