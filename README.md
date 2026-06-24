# Autonomous AI SRE Platform

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
![OpenAI](https://img.shields.io/badge/OpenAI-GPT--4o-412991?style=for-the-badge&logo=openai&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

**An enterprise-grade, AI-powered Site Reliability Engineering platform that autonomously monitors microservices, detects incidents, performs AI-driven root cause analysis, and executes self-healing remediation workflows.**

</div>

---

## Architecture

### 1. High-Level System Architecture

```mermaid
%%{
  init: {
    'theme': 'base',
    'themeVariables': {
      'darkMode': true,
      'background': '#000000',
      'primaryColor': '#1a1a1a',
      'primaryBorderColor': '#ffffff',
      'primaryTextColor': '#ffffff',
      'secondaryColor': '#2a2a2a',
      'secondaryBorderColor': '#dddddd',
      'secondaryTextColor': '#ffffff',
      'tertiaryColor': '#333333',
      'tertiaryBorderColor': '#bbbbbb',
      'lineColor': '#ffffff',
      'fontFamily': 'Inter, sans-serif'
    }
  }
}%%
graph TD
    classDef frontend fill:#111111,stroke:#ffffff,stroke-width:2px,color:#ffffff;
    classDef gateway fill:#222222,stroke:#ffffff,stroke-width:2px,color:#ffffff;
    classDef service fill:#333333,stroke:#ffffff,stroke-width:2px,color:#ffffff;
    classDef kafka fill:#444444,stroke:#ffffff,stroke-width:2px,color:#ffffff;
    classDef datastore fill:#555555,stroke:#ffffff,stroke-width:2px,color:#ffffff;

    Client(["Web Dashboard (Next.js)"]):::frontend
    
    Gateway{"API Gateway (Spring Boot)"}:::gateway

    subgraph Core Microservices
        LogIngest["Log Ingestion Service"]:::service
        IncidentSvc["Incident Service"]:::service
        AIEngine["AI Engine (LLM RCA)"]:::service
        HealingSvc["Healing Service"]:::service
        NotifSvc["Notification Service"]:::service
    end

    Kafka[["Apache Kafka (Message Broker)"]]:::kafka

    subgraph Datastores & Infrastructure
        Postgres[("PostgreSQL (Relational/Vectors)")]:::datastore
        Redis[("Redis (Cache/Rate Limits)")]:::datastore
        Prometheus[("Prometheus/Grafana")]:::datastore
        K8sAPI(("Kubernetes API")):::datastore
        Jaeger[("Jaeger (Tracing)")]:::datastore
    end

    Client -->|REST / WebSocket| Gateway
    Gateway -->|Auth / Routing| LogIngest
    Gateway -->|Auth / Routing| IncidentSvc
    Gateway -->|Auth / Routing| AIEngine
    Gateway -->|Auth / Routing| HealingSvc
    Gateway -->|Auth / Routing| NotifSvc

    LogIngest -->|Publish Logs| Kafka
    IncidentSvc -->|Publish Alerts| Kafka
    AIEngine -->|Publish Analysis| Kafka
    HealingSvc -->|Publish Actions| Kafka
    NotifSvc -->|Subscribe / Alert| Kafka

    Kafka <-->|Read/Write| Postgres
    Kafka <-->|Read/Write| Redis
    Kafka <-->|Read/Write| Prometheus
    Kafka <-->|Read/Write| Jaeger

    HealingSvc -->|Execute Remediation| K8sAPI
```

### 2. Autonomous Incident Resolution Sequence

```mermaid
%%{
  init: {
    'theme': 'base',
    'themeVariables': {
      'darkMode': true,
      'background': '#000000',
      'actorBkg': '#111111',
      'actorBorder': '#ffffff',
      'actorTextColor': '#ffffff',
      'signalColor': '#ffffff',
      'signalTextColor': '#ffffff',
      'sequenceNumberColor': '#000000',
      'noteBkgColor': '#333333',
      'noteBorderColor': '#ffffff',
      'noteTextColor': '#ffffff',
      'fontFamily': 'Inter, sans-serif'
    }
  }
}%%
sequenceDiagram
    autonumber
    participant App as Monitored App
    participant LogSvc as Log Ingestion
    participant Kafka as Apache Kafka
    participant IncSvc as Incident Service
    participant AI as AI Engine (GPT-4o)
    participant Heal as Healing Service
    participant K8s as Kubernetes API
    participant Slack as Notification Svc

    App->>LogSvc: Send Logs / Metrics (Error Spike)
    LogSvc->>Kafka: Publish to 'logs.raw' topic
    Kafka->>IncSvc: Consume & Analyze
    Note over IncSvc: Detects anomaly & SLO burn
    IncSvc->>Kafka: Publish 'incident.detected' event
    Kafka->>AI: Trigger RCA & Remediation Policy
    Note over AI: RAG queries against Knowledge Base
    AI-->>Kafka: Publish 'incident.rca_complete' event
    Kafka->>Heal: Consume remediation plan (e.g., Restart Pod)
    Heal->>K8s: Execute Action (Rollback / Scale / Restart)
    K8s-->>Heal: Action Success
    Heal->>Kafka: Publish 'incident.healed' event
    Kafka->>Slack: Route to Slack & PagerDuty
    Slack-->>App: (Admin sees alert and RCA in channel)
```

### 3. Data Flow Diagram (DFD)

```mermaid
%%{
  init: {
    'theme': 'base',
    'themeVariables': {
      'darkMode': true,
      'background': '#000000',
      'primaryColor': '#111111',
      'primaryBorderColor': '#ffffff',
      'primaryTextColor': '#ffffff',
      'lineColor': '#ffffff',
      'fontFamily': 'Inter, sans-serif'
    }
  }
}%%
flowchart TD
    %% External Entities
    ExtApps[Monitored Microservices]:::ext
    K8s[Kubernetes Cluster API]:::ext
    Slack[Slack / PagerDuty]:::ext

    %% Processes
    LogIngest((1. Log Ingestion & Parsing)):::proc
    AnomalyDet((2. Incident Detection & SLO Engine)):::proc
    AIEngine((3. AI Root Cause Analysis)):::proc
    HealingEngine((4. Policy & Self-Healing)):::proc
    NotificationEngine((5. Notification Routing)):::proc

    %% Data Stores
    Kafka{Apache Kafka Event Bus}:::store
    Postgres[(PostgreSQL DB)]:::store
    VectorDB[(pgvector Knowledge Base)]:::store

    classDef ext fill:#111111,stroke:#ffffff,stroke-width:2px,color:#ffffff;
    classDef proc fill:#222222,stroke:#ffffff,stroke-width:2px,shape:circle,color:#ffffff;
    classDef store fill:#333333,stroke:#ffffff,stroke-width:2px,color:#ffffff;

    %% Flows
    ExtApps -- "Raw Logs/Metrics" --> LogIngest
    LogIngest -- "Structured Logs" --> Kafka
    Kafka -- "Stream Processing" --> AnomalyDet
    AnomalyDet -- "Store Violations" --> Postgres
    AnomalyDet -- "Incident Detected Event" --> Kafka
    
    Kafka -- "Trigger Analysis" --> AIEngine
    AIEngine -- "Semantic Search" --> VectorDB
    AIEngine -- "Save RCA/Recommendations" --> Postgres
    AIEngine -- "RCA Complete Event" --> Kafka

    Kafka -- "Trigger Remediation" --> HealingEngine
    HealingEngine -- "Check Rules" --> Postgres
    HealingEngine -- "Execute Actions (e.g., Restart)" --> K8s
    HealingEngine -- "Healing Result Event" --> Kafka

    Kafka -- "Routing Events" --> NotificationEngine
    NotificationEngine -- "Alerts & Context" --> Slack
```

### 4. Database Schema (ER Diagram)

```mermaid
%%{
  init: {
    'theme': 'base',
    'themeVariables': {
      'darkMode': true,
      'background': '#000000',
      'primaryColor': '#111111',
      'primaryBorderColor': '#ffffff',
      'primaryTextColor': '#ffffff',
      'lineColor': '#ffffff',
      'fontFamily': 'Inter, sans-serif'
    }
  }
}%%
erDiagram
    SERVICES ||--o{ INCIDENTS : "experiences"
    SERVICES ||--o{ SLO_DEFINITIONS : "monitored by"
    SERVICES ||--o{ SERVICE_DEPENDENCIES : "has"
    SERVICES ||--o{ CANARY_DEPLOYMENTS : "deploys"
    SERVICES ||--o{ CHAOS_EXPERIMENTS : "targeted by"

    SLO_DEFINITIONS ||--o{ ERROR_BUDGET_SNAPSHOTS : "has"
    SLO_DEFINITIONS ||--o{ SLO_VIOLATIONS : "triggers"
    
    INCIDENTS ||--o{ INCIDENT_TIMELINE : "contains"
    INCIDENTS ||--o{ AI_ANALYSES : "analyzed by"
    INCIDENTS ||--o{ HEALING_ACTIONS : "remediated by"
    INCIDENTS ||--o{ KNOWLEDGE_BASE : "documented in"
    
    HEALING_ACTIONS ||--o{ POLICY_DECISIONS : "evaluated by"
    POLICY_RULES ||--o{ POLICY_DECISIONS : "enforces"

    SERVICES {
        uuid id PK
        string name
        string tier
        decimal slo_target
        string health_status
    }
    
    INCIDENTS {
        uuid id PK
        uuid service_id FK
        string severity
        string status
        string root_cause
        timestamp detected_at
    }

    SLO_DEFINITIONS {
        uuid id PK
        uuid service_id FK
        string slo_type
        decimal target_percentage
        string sli_query
    }

    AI_ANALYSES {
        uuid id PK
        uuid incident_id FK
        string root_cause
        int confidence_score
        jsonb recommendations
    }

    HEALING_ACTIONS {
        uuid id PK
        uuid incident_id FK
        string action_type
        string status
        boolean requires_approval
    }

    POLICY_RULES {
        uuid id PK
        string name
        string action_risk_level
        int auto_execute_threshold
    }
```

## Features

### Core Platform
| Feature | Description |
|---------|-------------|
| **Log Ingestion** | Kafka-based pipeline parsing JSON, logfmt, and plaintext logs |
| **Incident Detection** | Statistical anomaly detection with alert correlation and dedup |
| **AI Root Cause Analysis** | LLM-powered RCA with RAG knowledge base and confidence scoring |
| **Self-Healing Engine** | Pod restart, scaling, rollback, cache clear via Kubernetes API |
| **Notification Service** | Slack, email, PagerDuty with configurable routing rules |

### Advanced Features
| Feature | Description |
|---------|-------------|
| **SLO & Error Budget** | Google SRE multi-window burn rate alerting with budget forecasting |
| **Service Dependency Graph** | DAG-based blast radius analysis and critical path identification |
| **AI Guardrails & Policy Engine** | Risk matrix scoring with confidence gates and approval workflows |
| **Feature Flag Management** | Consistent hashing rollouts, kill switches, and canary flags |
| **Distributed Tracing** | OpenTelemetry + Jaeger with request journey visualization |
| **Canary Deployments** | Progressive traffic shifting with automated SLO-based rollback |
| **Event Replay System** | Replay incidents for debugging with time-travel simulation |
| **Chaos Engineering** | Pod kill, network latency, CPU stress experiments |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 (Virtual Threads, Records, Pattern Matching) |
| Framework | Spring Boot 3.3, Spring Security, Spring Data JPA |
| Messaging | Apache Kafka (KRaft mode) |
| Database | PostgreSQL 16 + pgvector |
| Cache | Redis 7 |
| AI | LangChain4j + OpenAI GPT-4o |
| Kubernetes | Fabric8 Java Client |
| Observability | Prometheus, Grafana, OpenTelemetry, Jaeger |
| Frontend | Next.js, TypeScript, Tailwind CSS |
| Infrastructure | Docker, Kubernetes, Helm Charts |
| CI/CD | GitHub Actions |

## Quick Start

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.9+
- Node.js 20+ (for frontend)

### 1. Start Infrastructure
```bash
cp .env.example .env
# Edit .env with your OPENAI_API_KEY

docker-compose -f docker-compose.infra.yml up -d
```

### 2. Build Backend
```bash
cd backend
mvn clean install -DskipTests
```

### 3. Run Services
```bash
# Run each service (separate terminals)
cd backend/api-gateway && mvn spring-boot:run
cd backend/log-ingestion-service && mvn spring-boot:run
cd backend/incident-service && mvn spring-boot:run
cd backend/ai-engine && mvn spring-boot:run
cd backend/healing-service && mvn spring-boot:run
cd backend/notification-service && mvn spring-boot:run
```

### 4. Run Frontend
```bash
cd frontend
npm install
npm run dev
```

### 5. Access
| Service | URL |
|---------|-----|
| API Gateway | http://localhost:8080 |
| Frontend Dashboard | http://localhost:3001 |
| Kafka UI | http://localhost:8090 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |
| Jaeger UI | http://localhost:16686 |

## Project Structure

```
Autonomus_ai/
├── backend/                    # Java Spring Boot microservices
│   ├── pom.xml                 # Multi-module parent POM
│   ├── common/                 # Shared library (DTOs, events, utils)
│   ├── api-gateway/            # REST API + Auth + Feature Flags
│   ├── log-ingestion-service/  # Log pipeline + Event Replay
│   ├── incident-service/       # Detection + SLO + Dependency Graph
│   ├── ai-engine/              # LLM Analysis + Policy Engine
│   ├── healing-service/        # Self-Healing + Canary + Chaos
│   └── notification-service/   # Slack, Email, PagerDuty
├── frontend/                   # Next.js Dashboard
├── k8s/                        # Kubernetes Helm Charts
├── monitoring/                 # Prometheus + Grafana configs
├── scripts/                    # Database init + utilities
└── docker-compose.infra.yml    # Infrastructure services
```

## API Documentation

Base URL: `http://localhost:8080/api/v1`

### Authentication
```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@sre-platform.ai", "password": "admin123"}'

# Use the returned JWT token
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/v1/incidents
```

## License

This project is for educational and portfolio purposes.
