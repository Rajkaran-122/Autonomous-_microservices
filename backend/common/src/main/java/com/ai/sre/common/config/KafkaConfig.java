package com.ai.sre.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Shared Kafka configuration — creates all required topics with proper
 * partition counts and replication factors.
 */
@Configuration
public class KafkaConfig {

    private static final int DEFAULT_PARTITIONS = 3;
    private static final int DEFAULT_REPLICAS = 1; // Use 3 in production

    // ==================== CORE TOPICS ====================

    @Bean
    public NewTopic rawLogEventsTopic() {
        return TopicBuilder.name(KafkaTopics.RAW_LOG_EVENTS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic parsedLogEventsTopic() {
        return TopicBuilder.name(KafkaTopics.PARSED_LOG_EVENTS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic incidentEventsTopic() {
        return TopicBuilder.name(KafkaTopics.INCIDENT_EVENTS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic aiAnalysisResultsTopic() {
        return TopicBuilder.name(KafkaTopics.AI_ANALYSIS_RESULTS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic healingCommandsTopic() {
        return TopicBuilder.name(KafkaTopics.HEALING_COMMANDS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic healingResultsTopic() {
        return TopicBuilder.name(KafkaTopics.HEALING_RESULTS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_EVENTS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic auditEventsTopic() {
        return TopicBuilder.name(KafkaTopics.AUDIT_EVENTS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    // ==================== ENHANCED FEATURE TOPICS ====================

    @Bean
    public NewTopic sloViolationsTopic() {
        return TopicBuilder.name(KafkaTopics.SLO_VIOLATIONS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic canaryMetricsTopic() {
        return TopicBuilder.name(KafkaTopics.CANARY_METRICS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic chaosEventsTopic() {
        return TopicBuilder.name(KafkaTopics.CHAOS_EVENTS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic featureFlagChangesTopic() {
        return TopicBuilder.name(KafkaTopics.FEATURE_FLAG_CHANGES)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic policyDecisionsTopic() {
        return TopicBuilder.name(KafkaTopics.POLICY_DECISIONS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic replayCommandsTopic() {
        return TopicBuilder.name(KafkaTopics.REPLAY_COMMANDS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic traceEventsTopic() {
        return TopicBuilder.name(KafkaTopics.TRACE_EVENTS)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    // ==================== DLQ TOPICS ====================

    @Bean
    public NewTopic dlqRawLogEventsTopic() {
        return TopicBuilder.name(KafkaTopics.DLQ_RAW_LOG_EVENTS)
                .partitions(1)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic dlqIncidentEventsTopic() {
        return TopicBuilder.name(KafkaTopics.DLQ_INCIDENT_EVENTS)
                .partitions(1)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    // ==================== JACKSON CONFIG ====================

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
