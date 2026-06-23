package com.ai.sre.common.config;

/**
 * Central registry of all Kafka topic names used across the platform.
 * All producers and consumers reference these constants to prevent topic name drift.
 */
public final class KafkaTopics {

    private KafkaTopics() {} // Utility class — no instantiation

    // ==================== CORE PIPELINE ====================
    /** Raw log events from monitored services */
    public static final String RAW_LOG_EVENTS = "raw-log-events";
    /** Parsed, enriched log events ready for analysis */
    public static final String PARSED_LOG_EVENTS = "parsed-log-events";
    /** New incidents detected by the anomaly detection engine */
    public static final String INCIDENT_EVENTS = "incident-events";
    /** AI-generated root cause analysis results */
    public static final String AI_ANALYSIS_RESULTS = "ai-analysis-results";
    /** Self-healing action requests (commands to the healing service) */
    public static final String HEALING_COMMANDS = "healing-commands";
    /** Self-healing action outcomes (results from the healing service) */
    public static final String HEALING_RESULTS = "healing-results";
    /** Notification dispatch events (alerts to Slack/email/PagerDuty) */
    public static final String NOTIFICATION_EVENTS = "notification-events";
    /** Immutable audit log events */
    public static final String AUDIT_EVENTS = "audit-events";

    // ==================== ENHANCED FEATURES ====================
    /** SLO burn rate violation alerts */
    public static final String SLO_VIOLATIONS = "slo-violations";
    /** Canary deployment metric snapshots per step */
    public static final String CANARY_METRICS = "canary-metrics";
    /** Chaos engineering experiment lifecycle events */
    public static final String CHAOS_EVENTS = "chaos-events";
    /** Feature flag change notifications (cache invalidation) */
    public static final String FEATURE_FLAG_CHANGES = "feature-flag-changes";
    /** AI guardrails policy decision audit events */
    public static final String POLICY_DECISIONS = "policy-decisions";
    /** Event replay control commands */
    public static final String REPLAY_COMMANDS = "replay-commands";
    /** Distributed trace correlation events */
    public static final String TRACE_EVENTS = "trace-events";

    // ==================== DEAD LETTER QUEUES ====================
    public static final String DLQ_PREFIX = "dlq-";
    public static final String DLQ_RAW_LOG_EVENTS = DLQ_PREFIX + RAW_LOG_EVENTS;
    public static final String DLQ_INCIDENT_EVENTS = DLQ_PREFIX + INCIDENT_EVENTS;
    public static final String DLQ_HEALING_COMMANDS = DLQ_PREFIX + HEALING_COMMANDS;
    public static final String DLQ_NOTIFICATION_EVENTS = DLQ_PREFIX + NOTIFICATION_EVENTS;

    // ==================== CONSUMER GROUPS ====================
    public static final String GROUP_LOG_INGESTION = "log-ingestion-group";
    public static final String GROUP_INCIDENT_SERVICE = "incident-service-group";
    public static final String GROUP_AI_ENGINE = "ai-engine-group";
    public static final String GROUP_HEALING_SERVICE = "healing-service-group";
    public static final String GROUP_NOTIFICATION_SERVICE = "notification-service-group";
    public static final String GROUP_EVENT_CAPTURE = "event-capture-group";
}
