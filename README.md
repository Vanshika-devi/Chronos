<p align="center">

# ⚙️ Chronos — Distributed Job Scheduling Platform
**A production-grade distributed job scheduling and execution platform built with Java and Spring Boot.**

Designed to explore **backend engineering, concurrency, distributed systems, database engineering, event-driven architecture, and system design** through one evolving real-world system.

<br/>

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk\&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot\&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql\&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Cache%20%26%20Coordination-DC382D?logo=redis\&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Streaming-231F20?logo=apachekafka\&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker\&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven\&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

</p>

---

## 🚀 Overview

**Chronos** is a distributed job scheduling and execution platform designed to reliably schedule, execute, monitor, and manage background jobs at scale.

The project starts as a clean Spring Boot backend and progressively evolves into a distributed system capable of handling:

* Scheduled jobs
* Priority-based execution
* Concurrent workers
* Job retries
* Failure handling
* Idempotent execution
* Distributed coordination
* Caching
* Asynchronous processing
* Event-driven communication
* Worker health monitoring
* Horizontal scalability

The goal is not simply to build a CRUD application.

> **Chronos is a practical exploration of how production-grade backend systems are designed, implemented, scaled, and made fault tolerant.**

---

# 🎯 Why Chronos?

Many backend projects stop at:

```text
Controller
   ↓
Service
   ↓
Database
```

Chronos intentionally goes much further.

```text
                         Client
                           │
                           ▼
                    Spring Boot API
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
        PostgreSQL       Redis          Kafka
             │             │             │
             │             │             ▼
             │             │       Distributed Workers
             │             │             │
             │             └─────────────┤
             │                           ▼
             └────────────────────► Job Execution
                                         │
                              ┌───────────┴───────────┐
                              ▼                       ▼
                           Success                 Failure
                              │                       │
                              ▼                       ▼
                           Result                  Retry
                                                      │
                                                      ▼
                                               Dead Letter Queue
```

Every architectural decision is introduced to solve a real engineering problem.

---

# 🧠 Core Engineering Goals

Chronos focuses on becoming deeply proficient in:

* Java backend development
* Spring Boot
* REST API design
* PostgreSQL
* SQL optimization
* JPA/Hibernate
* Transactions
* Concurrency
* Multithreading
* Thread pools
* Distributed systems
* Redis
* Kafka
* Event-driven architecture
* Fault tolerance
* Distributed coordination
* Caching
* Idempotency
* Observability
* Docker
* Testing
* System design

---

# 🏗️ Architecture

## Current Architecture

The project begins with a simple layered backend:

```text
             Client
                │
                ▼
        ┌────────────────┐
        │ Spring Boot API│
        └───────┬────────┘
                │
                ▼
          ┌───────────┐
          │Controller │
          └─────┬─────┘
                │
                ▼
          ┌───────────┐
          │  Service  │
          └─────┬─────┘
                │
                ▼
          ┌───────────┐
          │Repository │
          └─────┬─────┘
                │
                ▼
          ┌───────────┐
          │PostgreSQL │
          └───────────┘
```

---

## Target Architecture

As the project evolves:

```text
                         ┌──────────────┐
                         │    Client    │
                         └──────┬───────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │ Load Balancer   │
                       └────────┬────────┘
                                │
                  ┌─────────────┴─────────────┐
                  ▼                           ▼
          ┌──────────────┐            ┌──────────────┐
          │ Spring Boot  │            │ Spring Boot  │
          │   Instance 1 │            │   Instance 2 │
          └──────┬───────┘            └──────┬───────┘
                 │                           │
                 └─────────────┬─────────────┘
                               │
                ┌──────────────┼──────────────┐
                ▼              ▼              ▼
          PostgreSQL         Redis          Kafka
                │              │              │
                │              │              ▼
                │              │       ┌─────────────┐
                │              └──────►│  Scheduler  │
                │                      └──────┬──────┘
                │                             │
                │                             ▼
                │                         Job Queue
                │                             │
                │                ┌────────────┼────────────┐
                │                ▼            ▼            ▼
                │             Worker 1     Worker 2     Worker 3
                │                │            │            │
                └────────────────┴────────────┴────────────┘
                                             │
                                             ▼
                                      Job Execution
                                             │
                              ┌──────────────┴──────────────┐
                              ▼                             ▼
                           Success                       Failure
                              │                             │
                              ▼                             ▼
                           Result                         Retry
                                                            │
                                                            ▼
                                                     Dead Letter Queue
```

---

# ✨ Features

## Job Management

* Create jobs
* Retrieve jobs
* Update jobs
* Delete jobs
* Job status tracking
* Job metadata
* Execution history

## Scheduling

* One-time jobs
* Delayed execution
* Recurring jobs
* Priority-based scheduling
* Efficient next-job selection
* Scheduler lifecycle management

## Execution

* Worker pools
* Concurrent execution
* Configurable worker count
* Job timeout handling
* Graceful shutdown
* Execution tracking

## Reliability

* Automatic retries
* Exponential backoff
* Failure tracking
* Dead-letter handling
* Idempotent execution
* Duplicate execution prevention

## Distributed Systems

* Distributed locking
* Worker coordination
* Scheduler coordination
* Leader-election concepts
* Failure detection
* Horizontal scaling
* Fault tolerance

## Performance

* Redis caching
* Database indexing
* Connection pooling
* Asynchronous processing
* Kafka-based messaging
* Concurrent job execution

---

# 🧩 Technology Stack

| Category        | Technology           |
| --------------- | -------------------- |
| Language        | Java 21              |
| Framework       | Spring Boot          |
| API             | REST                 |
| Database        | PostgreSQL           |
| ORM             | Hibernate / JPA      |
| Cache           | Redis                |
| Messaging       | Apache Kafka         |
| Build           | Maven                |
| Testing         | JUnit / Mockito      |
| Containers      | Docker               |
| Monitoring      | Spring Boot Actuator |
| Version Control | Git / GitHub         |

---

# 📁 Project Structure

```text
chronos/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/chronos/
│   │   │       │
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       ├── dto/
│   │   │       ├── scheduler/
│   │   │       ├── worker/
│   │   │       ├── config/
│   │   │       ├── exception/
│   │   │       └── security/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/
│   │
│   └── test/
│
├── docker/
│
├── docs/
│
├── pom.xml
├── docker-compose.yml
└── README.md
```

---

# 🔄 Job Lifecycle

A job moves through a controlled lifecycle:

```text
CREATED
   │
   ▼
SCHEDULED
   │
   ▼
QUEUED
   │
   ▼
RUNNING
   │
   ├──────────────► COMPLETED
   │
   ▼
FAILED
   │
   ▼
RETRYING
   │
   ├──────────────► RUNNING
   │
   ▼
DEAD_LETTER
```

This lifecycle allows Chronos to track exactly what happened to every execution.

---

# 🧠 Data Structures Used

DSA is not added artificially.

The project uses data structures where they naturally solve backend problems.

### Priority Queue / Heap

Used for efficient scheduling:

```text
Job A → 10:01
Job B → 10:05
Job C → 10:02

          ↓

Priority Queue

Job A
  ↓
Job C
  ↓
Job B
```

Operations:

```text
Peek minimum     O(1)
Insert           O(log n)
Remove minimum   O(log n)
```

### HashMap

Used for fast lookup of:

* jobs
* workers
* execution state
* cached data

Average lookup:

```text
O(1)
```

### Queue

Used for:

* pending jobs
* worker communication
* producer-consumer workflows

### Graphs

Future scheduling/dependency support can model:

```text
Job A
 ├──► Job B
 │      └──► Job D
 └──► Job C
```

This introduces:

* DFS
* BFS
* Topological Sort
* dependency resolution

---

# 🔐 Security

Planned security architecture:

```text
Client
  │
  ▼
Authentication
  │
  ▼
JWT
  │
  ▼
Spring Security
  │
  ▼
Authorization
  │
  ▼
Protected APIs
```

Features:

* User registration
* Login
* Password hashing
* JWT authentication
* Role-based authorization
* Protected endpoints
* API security

---

# ⚡ Concurrency

Chronos uses Java concurrency to execute independent jobs simultaneously.

Instead of:

```text
Job A → finish → Job B → finish → Job C
```

workers can execute:

```text
Worker 1 → Job A
Worker 2 → Job B
Worker 3 → Job C
```

Concepts explored:

* Threads
* ExecutorService
* Thread pools
* CompletableFuture
* Synchronization
* Locks
* Atomic variables
* Concurrent collections
* Race conditions
* Deadlocks
* Producer-consumer patterns

---

# 🧠 Distributed Coordination

When multiple scheduler instances exist:

```text
Scheduler 1 ──┐
Scheduler 2 ──┼──► Job #123
Scheduler 3 ──┘
```

they must not accidentally execute the same job multiple times.

Chronos explores:

* Distributed locks
* Leases
* Idempotency
* Atomic operations
* Scheduler coordination
* Failure recovery

---

# 📈 Scalability

The architecture is designed to evolve from:

```text
1 API
1 Scheduler
1 Worker
1 Database
```

to:

```text
             Load Balancer
                  │
       ┌──────────┼──────────┐
       ▼          ▼          ▼
     API 1      API 2      API 3
       │          │          │
       └──────────┼──────────┘
                  │
              Kafka / Redis
                  │
       ┌──────────┼──────────┐
       ▼          ▼          ▼
    Worker 1   Worker 2   Worker 3
```

This allows job execution capacity to scale horizontally.

---

# 🧪 Testing Strategy

Testing will exist at multiple levels.

### Unit Tests

Test individual components.

```text
Service
Scheduler
Retry logic
Priority logic
```

### Integration Tests

Test:

```text
API
 ↓
Service
 ↓
Repository
 ↓
Database
```

### Distributed Testing

Eventually test:

* concurrent execution
* duplicate jobs
* worker failures
* scheduler failures
* retry behaviour
* distributed coordination

---

# 📊 Observability

Production-oriented monitoring will include:

* structured logging
* application health
* job metrics
* execution latency
* failure rate
* retry count
* worker status
* queue depth
* database metrics

Spring Boot Actuator will be used for application health and metrics exposure.

---

# 🗺️ Development Roadmap

## Phase 1 — Backend Foundations

* [x] Spring Boot project setup
* [ ] REST API
* [ ] Controller layer
* [ ] Service layer
* [ ] Repository layer
* [ ] Job entity
* [ ] PostgreSQL integration
* [ ] DTOs
* [ ] Validation
* [ ] Exception handling

## Phase 2 — Database Engineering

* [ ] PostgreSQL schema
* [ ] Relationships
* [ ] Indexes
* [ ] Transactions
* [ ] Pagination
* [ ] Query optimization
* [ ] Connection pooling
* [ ] Database migrations

## Phase 3 — Security

* [ ] User management
* [ ] Spring Security
* [ ] Password hashing
* [ ] JWT authentication
* [ ] Authorization
* [ ] Role-based access

## Phase 4 — Scheduling Engine

* [ ] Job scheduling
* [ ] Priority queue
* [ ] Delayed jobs
* [ ] Recurring jobs
* [ ] Job lifecycle
* [ ] Scheduler engine

## Phase 5 — Concurrency

* [ ] Worker pool
* [ ] ExecutorService
* [ ] Concurrent execution
* [ ] Thread safety
* [ ] Race-condition handling
* [ ] Graceful shutdown

## Phase 6 — Redis

* [ ] Caching
* [ ] TTL
* [ ] Cache invalidation
* [ ] Atomic operations
* [ ] Distributed locking
* [ ] Rate limiting

## Phase 7 — Kafka

* [ ] Producers
* [ ] Consumers
* [ ] Topics
* [ ] Partitions
* [ ] Consumer groups
* [ ] Retry topics
* [ ] Dead-letter queues
* [ ] Event-driven execution

## Phase 8 — Distributed System

* [ ] Multiple scheduler instances
* [ ] Distributed coordination
* [ ] Worker health
* [ ] Failure detection
* [ ] Idempotency
* [ ] Fault tolerance
* [ ] Horizontal scaling

## Phase 9 — Production Engineering

* [ ] Docker
* [ ] Docker Compose
* [ ] Unit testing
* [ ] Integration testing
* [ ] Observability
* [ ] Metrics
* [ ] CI/CD
* [ ] Load testing
* [ ] Production deployment

---

# 📚 Engineering Concepts Demonstrated

Chronos is designed as a learning and engineering project around:

```text
Java
│
├── OOP
├── Collections
├── Generics
├── Exceptions
├── Streams
├── JVM fundamentals
├── Concurrency
└── Multithreading

Spring Boot
│
├── Dependency Injection
├── IoC
├── REST
├── Spring Data
├── Spring Security
└── Actuator

Databases
│
├── SQL
├── PostgreSQL
├── Indexes
├── Transactions
├── ACID
├── Isolation
└── Query optimization

Distributed Systems
│
├── Redis
├── Kafka
├── Distributed locks
├── Idempotency
├── Fault tolerance
└── Horizontal scaling

Software Engineering
│
├── Testing
├── Docker
├── CI/CD
├── Logging
├── Monitoring
└── System Design
```

---

# 🎓 Learning Philosophy

Chronos is intentionally developed **incrementally**.

We don't introduce Kafka simply because it looks good on a resume.

We introduce it when synchronous execution creates a real architectural problem.

We don't introduce Redis just to list Redis on a resume.

We introduce caching and distributed coordination when the system actually needs them.

We don't introduce concurrency just because it is an interview keyword.

We use concurrency because a scheduler must efficiently execute independent jobs.

### The principle:

> **Understand the problem → understand the trade-off → choose the technology → implement it → test it → measure it.**

---

# 🚀 Getting Started

### Prerequisites

Install:

* Java 21+
* Maven
* PostgreSQL
* Git

Later phases additionally require:

* Redis
* Apache Kafka
* Docker

### Clone

```bash
git clone https://github.com/<your-username>/chronos-distributed-scheduler.git

cd chronos-distributed-scheduler
```

### Run

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

---

# 📡 API

Initial API:

```text
POST   /api/jobs
GET    /api/jobs
GET    /api/jobs/{id}
PUT    /api/jobs/{id}
DELETE /api/jobs/{id}
```

The API will expand as the scheduling engine evolves.

---

# 🧪 Example Job

```json
{
  "name": "Generate Monthly Report",
  "description": "Generate the monthly sales report",
  "scheduledAt": "2026-09-01T10:00:00"
}
```

Example response:

```json
{
  "id": 1,
  "name": "Generate Monthly Report",
  "description": "Generate the monthly sales report",
  "status": "SCHEDULED",
  "scheduledAt": "2026-09-01T10:00:00"
}
```

---

# 📈 Future Improvements

Potential future capabilities include:

* Job dependencies
* Cron expressions
* Priority scheduling
* Job DAGs
* Worker registration
* Worker heartbeats
* Automatic worker recovery
* Distributed rate limiting
* Multi-tenant scheduling
* Job quotas
* Execution dashboards
* WebSocket-based live execution updates
* Advanced retry policies
* Workflow orchestration
* Load-aware scheduling

---

# 💡 What This Project Demonstrates

Chronos is designed to demonstrate that the developer understands more than framework syntax.

It demonstrates understanding of:

**Backend architecture**

**Database engineering**

**Data structures**

**Concurrency**

**Distributed systems**

**Event-driven architecture**

**Reliability**

**Performance**

**Testing**

**Production engineering**

**System design**

---

# 👩‍💻 Author

**Vanshika Devi**

Computer Science & Information Technology

Interested in:

* Backend Engineering
* Distributed Systems
* AI/ML
* LLMs
* System Design

---

# ⭐ Project Status

🚧 **Actively under development**

Chronos is being built progressively from a Spring Boot REST API into a production-oriented distributed scheduling platform.

---

<p align="center">

### ⚙️ Build it. Break it. Scale it. Understand it.

**Chronos — Distributed Job Scheduling Platform**

</p>
