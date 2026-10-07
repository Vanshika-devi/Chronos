# Chronos Architecture

## High-Level Architecture

```mermaid
flowchart TB
    C[Clients<br/>Web / API / CLI] --> LB[Load Balancer]
    LB --> API1[Spring Boot API 1]
    LB --> API2[Spring Boot API 2]
    LB --> APIN[Spring Boot API N]

    API1 --> PG[(PostgreSQL<br/>Durable State)]
    API2 --> PG
    APIN --> PG

    API1 --> R[(Redis<br/>Cache / Locks / Coordination)]
    API2 --> R
    APIN --> R

    API1 --> S[Scheduler / Dispatch Layer]
    API2 --> S
    APIN --> S

    S --> K[(Kafka<br/>Job Events / Queue)]
    K --> W1[Worker 1]
    K --> W2[Worker 2]
    K --> WN[Worker N]

    W1 --> PG
    W2 --> PG
    WN --> PG

    W1 --> R
    W2 --> R
    WN --> R

    W1 --> E[Job Execution]
    W2 --> E
    WN --> E

    E --> OK[Completed]
    E --> FAIL[Failed]
    FAIL --> RETRY[Retry / Exponential Backoff]
    RETRY --> K
    FAIL --> DLQ[Dead Letter Queue]

    API1 --> OBS[Observability]
    S --> OBS
    W1 --> OBS
    W2 --> OBS
    WN --> OBS

    OBS --> PROM[Prometheus]
    OBS --> GRAF[Grafana]
    OBS --> OTEL[OpenTelemetry]
```

## Ownership

| Component | Primary responsibility |
|---|---|
| Spring Boot API | Job management and API orchestration |
| Scheduler | Determine when jobs become eligible |
| PostgreSQL | Durable job and execution state |
| Redis | Cache, locks, coordination, ephemeral state |
| Kafka | Asynchronous event transport |
| Workers | Execute jobs concurrently |
| DLQ | Isolate jobs that exhaust retry policy |
| Observability | Metrics, logs, traces, health |

## Important execution rule

The architecture should not assume that a job is delivered exactly once.

Instead:

```text
Delivery
   ↓
Claim / Idempotency Check
   ↓
Execute
   ↓
Persist Result
   ↓
Publish Outcome
```

This makes duplicate delivery a normal failure mode to be handled explicitly.
