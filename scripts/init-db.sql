-- ============================================================
-- Autonomous AI SRE Platform — Database Schema
-- PostgreSQL 16 + pgvector extension
-- ============================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";       -- pgvector for RAG embeddings
CREATE EXTENSION IF NOT EXISTS "pg_trgm";      -- Trigram index for full-text search

-- ============================================================
-- CORE TABLES
-- ============================================================

-- Users & Authentication
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'ENGINEER',  -- ADMIN, SRE_LEAD, ENGINEER, VIEWER
    notification_prefs JSONB DEFAULT '{}',
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Service Registry
CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) UNIQUE NOT NULL,
    namespace VARCHAR(255) DEFAULT 'default',
    service_type VARCHAR(50),                      -- api, worker, database, cache, gateway
    tier VARCHAR(10) DEFAULT 'medium',             -- critical, high, medium, low
    owner VARCHAR(255),
    slo_target DECIMAL(6,3) DEFAULT 99.900,        -- e.g., 99.950%
    health_status VARCHAR(20) DEFAULT 'unknown',   -- healthy, degraded, down, unknown
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_services_name ON services(name);
CREATE INDEX idx_services_health ON services(health_status);
CREATE INDEX idx_services_namespace ON services(namespace);

-- Incidents
CREATE TABLE incidents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    severity VARCHAR(5) NOT NULL,                  -- P1, P2, P3, P4
    status VARCHAR(20) NOT NULL DEFAULT 'DETECTED', -- DETECTED, ANALYZING, ACTIVE, REMEDIATING, RESOLVED, CLOSED
    service_id UUID REFERENCES services(id),
    detected_at TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMP,
    mttr_seconds INTEGER,
    root_cause TEXT,
    ai_analysis_id UUID,
    correlation_key VARCHAR(255),
    created_by VARCHAR(255) DEFAULT 'system',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_incidents_severity ON incidents(severity);
CREATE INDEX idx_incidents_status ON incidents(status);
CREATE INDEX idx_incidents_service ON incidents(service_id);
CREATE INDEX idx_incidents_detected ON incidents(detected_at DESC);
CREATE INDEX idx_incidents_correlation ON incidents(correlation_key);

-- Incident Timeline Events
CREATE TABLE incident_timeline (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,               -- DETECTED, ALERT_FIRED, AI_ANALYSIS, HEALING_STARTED, etc.
    description TEXT,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_timeline_incident ON incident_timeline(incident_id, created_at);

-- AI Analysis Results
CREATE TABLE ai_analyses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    root_cause TEXT NOT NULL,
    summary TEXT,
    affected_services TEXT[],
    recommendations JSONB DEFAULT '[]',            -- [{action, confidence, description, riskLevel}]
    confidence_score INTEGER CHECK (confidence_score BETWEEN 0 AND 100),
    model_used VARCHAR(100),
    tokens_used INTEGER,
    response_time_ms INTEGER,
    feedback_rating INTEGER CHECK (feedback_rating BETWEEN 1 AND 5),
    raw_response TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_ai_analyses_incident ON ai_analyses(incident_id);
CREATE INDEX idx_ai_analyses_confidence ON ai_analyses(confidence_score);

-- Healing Actions
CREATE TABLE healing_actions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id UUID REFERENCES incidents(id),
    action_type VARCHAR(50) NOT NULL,              -- POD_RESTART, SCALE_UP, SCALE_DOWN, ROLLBACK, CACHE_CLEAR
    target_service VARCHAR(255),
    target_namespace VARCHAR(255),
    target_resource VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, EXECUTING, VERIFYING, COMPLETED, FAILED, ROLLED_BACK, REJECTED
    confidence_score INTEGER,
    requires_approval BOOLEAN DEFAULT FALSE,
    approved_by VARCHAR(255),
    before_state JSONB,
    after_state JSONB,
    dry_run BOOLEAN DEFAULT FALSE,
    execution_log TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_healing_actions_incident ON healing_actions(incident_id);
CREATE INDEX idx_healing_actions_status ON healing_actions(status);
CREATE INDEX idx_healing_actions_type ON healing_actions(action_type);

-- Audit Log (immutable, append-only)
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    actor VARCHAR(255) NOT NULL,
    actor_type VARCHAR(20) NOT NULL DEFAULT 'SYSTEM', -- HUMAN, AI, SYSTEM
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(255),
    details JSONB DEFAULT '{}',
    ip_address VARCHAR(45),
    hash VARCHAR(64),                              -- SHA-256 hash chain for tamper detection
    previous_hash VARCHAR(64),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_audit_log_actor ON audit_log(actor);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_resource ON audit_log(resource_type, resource_id);
CREATE INDEX idx_audit_log_created ON audit_log(created_at DESC);

-- Knowledge Base (for RAG vector search)
CREATE TABLE knowledge_base (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id UUID REFERENCES incidents(id),
    title VARCHAR(500),
    content TEXT NOT NULL,
    resolution TEXT,
    tags TEXT[],
    embedding VECTOR(1536),                        -- OpenAI text-embedding-3-small dimensions
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_kb_embedding ON knowledge_base USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
CREATE INDEX idx_kb_tags ON knowledge_base USING gin(tags);

-- Notification Rules
CREATE TABLE notification_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    service_pattern VARCHAR(255) DEFAULT '*',       -- regex or glob pattern
    severity_filter VARCHAR(5)[] DEFAULT '{P1,P2}',
    channel VARCHAR(20) NOT NULL,                  -- slack, email, pagerduty
    channel_config JSONB NOT NULL DEFAULT '{}',    -- {webhook_url, email, routing_key}
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Log Entries (partitioned by month for performance)
CREATE TABLE log_entries (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    service_id UUID,
    log_level VARCHAR(10),
    message TEXT,
    structured_data JSONB,
    trace_id VARCHAR(64),
    span_id VARCHAR(32),
    timestamp TIMESTAMP NOT NULL,
    ingested_at TIMESTAMP DEFAULT NOW()
) PARTITION BY RANGE (timestamp);

-- Create initial partitions (3 months)
CREATE TABLE log_entries_2026_06 PARTITION OF log_entries
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
CREATE TABLE log_entries_2026_07 PARTITION OF log_entries
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE log_entries_2026_08 PARTITION OF log_entries
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');

CREATE INDEX idx_log_entries_service ON log_entries(service_id, timestamp DESC);
CREATE INDEX idx_log_entries_level ON log_entries(log_level, timestamp DESC);
CREATE INDEX idx_log_entries_trace ON log_entries(trace_id);

-- ============================================================
-- ENHANCED FEATURE TABLES
-- ============================================================

-- SLO Definitions
CREATE TABLE slo_definitions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    slo_type VARCHAR(30) NOT NULL,                 -- AVAILABILITY, LATENCY, ERROR_RATE, THROUGHPUT
    target_percentage DECIMAL(6,3) NOT NULL,       -- 99.950
    measurement_window_days INTEGER NOT NULL DEFAULT 30,
    sli_query TEXT NOT NULL,                       -- PromQL query for SLI
    good_events_query TEXT,
    total_events_query TEXT,
    latency_threshold_ms INTEGER,
    latency_percentile DECIMAL(5,2),
    fast_burn_rate DECIMAL(5,2) DEFAULT 14.40,
    slow_burn_rate DECIMAL(5,2) DEFAULT 2.00,
    enabled BOOLEAN DEFAULT TRUE,
    owner VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(service_id, name)
);

-- Error Budget Snapshots
CREATE TABLE error_budget_snapshots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    slo_id UUID NOT NULL REFERENCES slo_definitions(id) ON DELETE CASCADE,
    snapshot_time TIMESTAMP NOT NULL,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    total_budget_minutes DECIMAL(10,2),
    consumed_budget_minutes DECIMAL(10,2),
    remaining_budget_percentage DECIMAL(6,3),
    good_events BIGINT,
    total_events BIGINT,
    current_sli_percentage DECIMAL(6,3),
    burn_rate_1h DECIMAL(8,4),
    burn_rate_6h DECIMAL(8,4),
    burn_rate_24h DECIMAL(8,4),
    burn_rate_3d DECIMAL(8,4),
    projected_budget_exhaustion TIMESTAMP,
    on_track BOOLEAN,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_budget_snapshots_slo ON error_budget_snapshots(slo_id, snapshot_time DESC);

-- SLO Violations
CREATE TABLE slo_violations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    slo_id UUID NOT NULL REFERENCES slo_definitions(id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES services(id),
    violation_type VARCHAR(30) NOT NULL,           -- FAST_BURN, SLOW_BURN, BUDGET_EXHAUSTED, SLO_BREACH
    burn_rate DECIMAL(8,4),
    error_budget_remaining DECIMAL(6,3),
    severity VARCHAR(5) NOT NULL,
    message TEXT,
    acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by VARCHAR(255),
    incident_id UUID REFERENCES incidents(id),
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_slo_violations_slo ON slo_violations(slo_id, created_at DESC);
CREATE INDEX idx_slo_violations_service ON slo_violations(service_id);

-- Service Dependencies (graph edges)
CREATE TABLE service_dependencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    target_service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    dependency_type VARCHAR(30) NOT NULL,          -- SYNC_HTTP, ASYNC_KAFKA, DATABASE, CACHE, GRPC
    criticality VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- CRITICAL, HIGH, MEDIUM, LOW
    description TEXT,
    avg_requests_per_minute DECIMAL(10,2),
    avg_latency_ms DECIMAL(8,2),
    error_rate DECIMAL(5,2),
    last_traffic_seen TIMESTAMP,
    discovered_by VARCHAR(30) DEFAULT 'MANUAL',   -- MANUAL, TRACE_ANALYSIS, K8S_DISCOVERY
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(source_service_id, target_service_id, dependency_type),
    CHECK(source_service_id != target_service_id)
);

CREATE INDEX idx_deps_source ON service_dependencies(source_service_id);
CREATE INDEX idx_deps_target ON service_dependencies(target_service_id);

-- Blast Radius Cache
CREATE TABLE blast_radius_cache (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    affected_services JSONB NOT NULL,
    total_affected_count INTEGER,
    max_depth INTEGER,
    total_impact_score DECIMAL(8,2),
    calculated_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Policy Rules (AI guardrails)
CREATE TABLE policy_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    service_pattern VARCHAR(255) DEFAULT '*',
    action_type VARCHAR(50) DEFAULT '*',
    environment VARCHAR(30) DEFAULT '*',
    action_risk_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    auto_execute_threshold INTEGER NOT NULL DEFAULT 95,
    approval_threshold INTEGER NOT NULL DEFAULT 70,
    block_threshold INTEGER NOT NULL DEFAULT 0,
    max_actions_per_hour INTEGER DEFAULT 10,
    max_blast_radius INTEGER DEFAULT 5,
    cooldown_minutes INTEGER DEFAULT 15,
    required_approvers INTEGER DEFAULT 1,
    approval_timeout_minutes INTEGER DEFAULT 30,
    escalation_after_minutes INTEGER DEFAULT 15,
    priority INTEGER DEFAULT 100,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Policy Decisions (audit trail)
CREATE TABLE policy_decisions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    healing_action_id UUID REFERENCES healing_actions(id),
    incident_id UUID REFERENCES incidents(id),
    action_type VARCHAR(50) NOT NULL,
    target_service VARCHAR(255),
    ai_confidence_score INTEGER,
    action_risk_level VARCHAR(20),
    blast_radius_count INTEGER,
    blast_radius_score DECIMAL(8,2),
    combined_risk_score DECIMAL(5,2),
    decision VARCHAR(20) NOT NULL,                 -- AUTO_EXECUTE, APPROVAL_REQUIRED, BLOCKED, MANUAL_OVERRIDE
    decision_reason TEXT,
    matched_policy_id UUID REFERENCES policy_rules(id),
    approval_status VARCHAR(20),
    approved_by VARCHAR(255),
    approval_requested_at TIMESTAMP,
    approval_received_at TIMESTAMP,
    execution_status VARCHAR(20),
    execution_result JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_policy_decisions_action ON policy_decisions(healing_action_id);
CREATE INDEX idx_policy_decisions_decision ON policy_decisions(decision, created_at DESC);

-- Feature Flags
CREATE TABLE feature_flags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    key VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    flag_type VARCHAR(20) NOT NULL DEFAULT 'BOOLEAN', -- BOOLEAN, PERCENTAGE, USER_LIST, ENVIRONMENT
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    kill_switch BOOLEAN NOT NULL DEFAULT FALSE,
    rollout_percentage INTEGER DEFAULT 0 CHECK (rollout_percentage BETWEEN 0 AND 100),
    target_environments TEXT[],
    target_user_ids TEXT[],
    target_service_ids TEXT[],
    owner VARCHAR(255),
    tags TEXT[],
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Feature Flag Audit
CREATE TABLE feature_flag_audit (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    flag_id UUID NOT NULL REFERENCES feature_flags(id) ON DELETE CASCADE,
    changed_by VARCHAR(255) NOT NULL,
    change_type VARCHAR(20) NOT NULL,
    previous_state JSONB,
    new_state JSONB,
    reason TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_flag_audit_flag ON feature_flag_audit(flag_id, created_at DESC);

-- Trace Metadata (search index — Jaeger handles trace storage)
CREATE TABLE trace_metadata (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trace_id VARCHAR(64) NOT NULL,
    root_service VARCHAR(255),
    root_operation VARCHAR(255),
    start_time TIMESTAMP NOT NULL,
    duration_ms INTEGER,
    span_count INTEGER,
    services_involved TEXT[],
    status VARCHAR(20),
    error_message TEXT,
    incident_id UUID REFERENCES incidents(id),
    user_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_trace_meta_trace ON trace_metadata(trace_id);
CREATE INDEX idx_trace_meta_time ON trace_metadata(start_time DESC);
CREATE INDEX idx_trace_meta_service ON trace_metadata(root_service);
CREATE INDEX idx_trace_meta_incident ON trace_metadata(incident_id);

-- Canary Deployments
CREATE TABLE canary_deployments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_id UUID NOT NULL REFERENCES services(id),
    deployment_name VARCHAR(255) NOT NULL,
    namespace VARCHAR(255) NOT NULL,
    baseline_version VARCHAR(100) NOT NULL,
    canary_version VARCHAR(100) NOT NULL,
    canary_image VARCHAR(500) NOT NULL,
    current_traffic_percentage INTEGER DEFAULT 5,
    target_traffic_percentage INTEGER DEFAULT 100,
    traffic_increment INTEGER DEFAULT 10,
    step_interval_minutes INTEGER DEFAULT 5,
    max_error_rate_delta DECIMAL(5,2) DEFAULT 1.0,
    max_latency_delta_ms INTEGER DEFAULT 50,
    min_success_rate DECIMAL(5,2) DEFAULT 99.0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    current_step INTEGER DEFAULT 0,
    total_steps INTEGER,
    baseline_error_rate DECIMAL(5,2),
    canary_error_rate DECIMAL(5,2),
    baseline_p99_latency_ms DECIMAL(8,2),
    canary_p99_latency_ms DECIMAL(8,2),
    rollback_reason TEXT,
    started_by VARCHAR(255),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_canary_service ON canary_deployments(service_id);
CREATE INDEX idx_canary_status ON canary_deployments(status);

-- Canary Metric Snapshots
CREATE TABLE canary_metric_snapshots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    canary_deployment_id UUID NOT NULL REFERENCES canary_deployments(id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL,
    traffic_percentage INTEGER NOT NULL,
    baseline_error_rate DECIMAL(5,2),
    baseline_p99_ms DECIMAL(8,2),
    baseline_rps DECIMAL(8,2),
    canary_error_rate DECIMAL(5,2),
    canary_p99_ms DECIMAL(8,2),
    canary_rps DECIMAL(8,2),
    error_rate_delta DECIMAL(5,2),
    latency_delta_ms DECIMAL(8,2),
    verdict VARCHAR(20),
    captured_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_canary_metrics_deployment ON canary_metric_snapshots(canary_deployment_id, step_number);

-- Event Store (for event replay)
CREATE TABLE event_store (
    id BIGSERIAL,
    event_id UUID NOT NULL DEFAULT uuid_generate_v4(),
    topic VARCHAR(255) NOT NULL,
    partition_num INTEGER,
    offset_num BIGINT,
    event_key VARCHAR(255),
    event_payload JSONB NOT NULL,
    headers JSONB,
    trace_id VARCHAR(64),
    service_id UUID,
    incident_id UUID,
    original_timestamp TIMESTAMP NOT NULL,
    captured_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (id, original_timestamp)
) PARTITION BY RANGE (original_timestamp);

-- Event store partitions (3 months)
CREATE TABLE event_store_2026_06 PARTITION OF event_store
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
CREATE TABLE event_store_2026_07 PARTITION OF event_store
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE event_store_2026_08 PARTITION OF event_store
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');

CREATE INDEX idx_event_store_incident ON event_store(incident_id);
CREATE INDEX idx_event_store_topic ON event_store(topic, original_timestamp);
CREATE INDEX idx_event_store_trace ON event_store(trace_id);

-- Replay Sessions
CREATE TABLE replay_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    source_type VARCHAR(20) NOT NULL,              -- INCIDENT, TIME_RANGE, CUSTOM
    source_incident_id UUID REFERENCES incidents(id),
    source_start_time TIMESTAMP,
    source_end_time TIMESTAMP,
    source_topics TEXT[],
    source_services TEXT[],
    replay_speed DECIMAL(5,2) DEFAULT 1.0,
    target_topic_prefix VARCHAR(50) DEFAULT 'replay-',
    simulation_mode BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    total_events INTEGER,
    replayed_events INTEGER DEFAULT 0,
    current_position BIGINT,
    result_summary JSONB,
    started_by VARCHAR(255),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Chaos Experiments
CREATE TABLE chaos_experiments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    target_service_id UUID REFERENCES services(id),
    target_namespace VARCHAR(255) NOT NULL DEFAULT 'default',
    target_label_selector VARCHAR(255),
    experiment_type VARCHAR(30) NOT NULL,          -- POD_KILL, NETWORK_LATENCY, CPU_STRESS, MEMORY_STRESS, POD_FAILURE
    config JSONB NOT NULL,
    steady_state_hypothesis JSONB NOT NULL,
    duration_seconds INTEGER NOT NULL DEFAULT 60,
    cooldown_seconds INTEGER DEFAULT 120,
    max_blast_radius INTEGER DEFAULT 1,
    abort_on_failure BOOLEAN DEFAULT TRUE,
    requires_approval BOOLEAN DEFAULT TRUE,
    cron_expression VARCHAR(100),
    created_by VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Chaos Experiment Runs
CREATE TABLE chaos_experiment_runs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    experiment_id UUID NOT NULL REFERENCES chaos_experiments(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    pre_check_passed BOOLEAN,
    pre_check_results JSONB,
    injection_started_at TIMESTAMP,
    injection_ended_at TIMESTAMP,
    post_check_passed BOOLEAN,
    post_check_results JSONB,
    self_healing_triggered BOOLEAN,
    self_healing_action_ids UUID[],
    self_healing_response_time_ms INTEGER,
    experiment_passed BOOLEAN,
    failure_reason TEXT,
    report JSONB,
    started_by VARCHAR(255),
    approved_by VARCHAR(255),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_chaos_runs_experiment ON chaos_experiment_runs(experiment_id, created_at DESC);
CREATE INDEX idx_chaos_runs_status ON chaos_experiment_runs(status);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Default admin user (password: admin123 — bcrypt hashed)
INSERT INTO users (email, password_hash, full_name, role) VALUES
    ('admin@sre-platform.ai', '$2a$12$LJ3m4ys3ZkY1FJ5k5v5UaOJZGHxQJ8qhKpF5X2N9yXmPz0gwMc6vy', 'Platform Admin', 'ADMIN'),
    ('sre-lead@sre-platform.ai', '$2a$12$LJ3m4ys3ZkY1FJ5k5v5UaOJZGHxQJ8qhKpF5X2N9yXmPz0gwMc6vy', 'SRE Team Lead', 'SRE_LEAD'),
    ('engineer@sre-platform.ai', '$2a$12$LJ3m4ys3ZkY1FJ5k5v5UaOJZGHxQJ8qhKpF5X2N9yXmPz0gwMc6vy', 'On-Call Engineer', 'ENGINEER');

-- Sample services
INSERT INTO services (name, namespace, service_type, tier, owner, slo_target, health_status) VALUES
    ('payment-service', 'production', 'api', 'critical', 'payments-team', 99.950, 'healthy'),
    ('order-service', 'production', 'api', 'critical', 'orders-team', 99.900, 'healthy'),
    ('user-service', 'production', 'api', 'high', 'identity-team', 99.900, 'healthy'),
    ('inventory-service', 'production', 'worker', 'high', 'supply-team', 99.500, 'healthy'),
    ('notification-service', 'production', 'worker', 'medium', 'platform-team', 99.000, 'healthy'),
    ('api-gateway', 'production', 'gateway', 'critical', 'platform-team', 99.990, 'healthy'),
    ('redis-cache', 'production', 'cache', 'high', 'platform-team', 99.900, 'healthy'),
    ('postgres-primary', 'production', 'database', 'critical', 'dba-team', 99.990, 'healthy');

-- Sample service dependencies
INSERT INTO service_dependencies (source_service_id, target_service_id, dependency_type, criticality) VALUES
    ((SELECT id FROM services WHERE name='api-gateway'), (SELECT id FROM services WHERE name='payment-service'), 'SYNC_HTTP', 'CRITICAL'),
    ((SELECT id FROM services WHERE name='api-gateway'), (SELECT id FROM services WHERE name='order-service'), 'SYNC_HTTP', 'CRITICAL'),
    ((SELECT id FROM services WHERE name='api-gateway'), (SELECT id FROM services WHERE name='user-service'), 'SYNC_HTTP', 'HIGH'),
    ((SELECT id FROM services WHERE name='payment-service'), (SELECT id FROM services WHERE name='postgres-primary'), 'DATABASE', 'CRITICAL'),
    ((SELECT id FROM services WHERE name='payment-service'), (SELECT id FROM services WHERE name='redis-cache'), 'CACHE', 'HIGH'),
    ((SELECT id FROM services WHERE name='order-service'), (SELECT id FROM services WHERE name='payment-service'), 'SYNC_HTTP', 'CRITICAL'),
    ((SELECT id FROM services WHERE name='order-service'), (SELECT id FROM services WHERE name='inventory-service'), 'ASYNC_KAFKA', 'HIGH'),
    ((SELECT id FROM services WHERE name='order-service'), (SELECT id FROM services WHERE name='postgres-primary'), 'DATABASE', 'CRITICAL'),
    ((SELECT id FROM services WHERE name='user-service'), (SELECT id FROM services WHERE name='postgres-primary'), 'DATABASE', 'CRITICAL'),
    ((SELECT id FROM services WHERE name='user-service'), (SELECT id FROM services WHERE name='redis-cache'), 'CACHE', 'MEDIUM'),
    ((SELECT id FROM services WHERE name='inventory-service'), (SELECT id FROM services WHERE name='postgres-primary'), 'DATABASE', 'HIGH'),
    ((SELECT id FROM services WHERE name='notification-service'), (SELECT id FROM services WHERE name='redis-cache'), 'CACHE', 'LOW');

-- Default notification rules
INSERT INTO notification_rules (name, service_pattern, severity_filter, channel, channel_config) VALUES
    ('Slack P1/P2 Alerts', '*', '{P1,P2}', 'slack', '{"webhook_url": "${SLACK_WEBHOOK_URL}", "channel": "#incidents"}'),
    ('Email P1 Alerts', '*', '{P1}', 'email', '{"recipients": ["oncall@company.com"]}'),
    ('PagerDuty P1', '*', '{P1}', 'pagerduty', '{"routing_key": "${PAGERDUTY_ROUTING_KEY}"}');

-- Default policy rules
INSERT INTO policy_rules (name, description, service_pattern, action_type, action_risk_level, auto_execute_threshold, approval_threshold) VALUES
    ('Default Low Risk', 'Auto-execute low-risk actions with high confidence', '*', 'POD_RESTART', 'LOW', 90, 70),
    ('Default Medium Risk', 'Require approval for scaling actions', '*', 'SCALE_UP', 'MEDIUM', 95, 80),
    ('Default High Risk', 'Require approval for rollbacks', '*', 'ROLLBACK', 'HIGH', 98, 85),
    ('Critical Services Block', 'Block automatic actions on critical services below 95% confidence', 'payment-.*', '*', 'CRITICAL', 98, 90);

-- Default feature flags
INSERT INTO feature_flags (key, name, description, flag_type, enabled, rollout_percentage) VALUES
    ('enable-ai-copilot', 'AI Copilot Chat', 'Enable the AI copilot chat interface', 'PERCENTAGE', true, 100),
    ('enable-self-healing', 'Self-Healing Engine', 'Enable automated self-healing workflows', 'BOOLEAN', true, 100),
    ('enable-chaos-experiments', 'Chaos Engineering', 'Enable chaos experiment execution', 'BOOLEAN', false, 0),
    ('enable-canary-deployments', 'Canary Deployments', 'Enable canary deployment workflows', 'BOOLEAN', true, 100),
    ('enable-predictive-detection', 'Predictive Incident Detection', 'Enable ML-based predictive anomaly detection', 'PERCENTAGE', false, 0);

-- Sample SLO definitions
INSERT INTO slo_definitions (service_id, name, slo_type, target_percentage, sli_query, good_events_query, total_events_query) VALUES
    ((SELECT id FROM services WHERE name='payment-service'), 'Payment API Availability', 'AVAILABILITY', 99.950,
     'sum(rate(http_requests_total{service="payment-service",status!~"5.."}[5m])) / sum(rate(http_requests_total{service="payment-service"}[5m]))',
     'sum(rate(http_requests_total{service="payment-service",status!~"5.."}[5m]))',
     'sum(rate(http_requests_total{service="payment-service"}[5m]))'),
    ((SELECT id FROM services WHERE name='payment-service'), 'Payment API Latency', 'LATENCY', 99.000,
     'histogram_quantile(0.99, sum(rate(http_request_duration_seconds_bucket{service="payment-service"}[5m])) by (le))',
     NULL, NULL),
    ((SELECT id FROM services WHERE name='order-service'), 'Order API Availability', 'AVAILABILITY', 99.900,
     'sum(rate(http_requests_total{service="order-service",status!~"5.."}[5m])) / sum(rate(http_requests_total{service="order-service"}[5m]))',
     'sum(rate(http_requests_total{service="order-service",status!~"5.."}[5m]))',
     'sum(rate(http_requests_total{service="order-service"}[5m]))');

-- ============================================================
-- TRIGGERS & FUNCTIONS
-- ============================================================

-- Auto-update updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_services_updated_at BEFORE UPDATE ON services
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_incidents_updated_at BEFORE UPDATE ON incidents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_slo_definitions_updated_at BEFORE UPDATE ON slo_definitions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_feature_flags_updated_at BEFORE UPDATE ON feature_flags
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Calculate MTTR when incident is resolved
CREATE OR REPLACE FUNCTION calculate_mttr()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'RESOLVED' AND NEW.resolved_at IS NOT NULL AND OLD.status != 'RESOLVED' THEN
        NEW.mttr_seconds = EXTRACT(EPOCH FROM (NEW.resolved_at - NEW.detected_at))::INTEGER;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER calculate_incident_mttr BEFORE UPDATE ON incidents
    FOR EACH ROW EXECUTE FUNCTION calculate_mttr();
